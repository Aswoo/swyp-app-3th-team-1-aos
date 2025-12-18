package team.swyp.sdu.presentation.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import team.swyp.sdu.data.model.ActivityStats
import team.swyp.sdu.data.model.Emotion
import team.swyp.sdu.data.model.EmotionType
import team.swyp.sdu.data.model.LocationPoint
import team.swyp.sdu.data.model.WalkingSession
import team.swyp.sdu.data.repository.WalkingSessionRepository
import team.swyp.sdu.domain.calculator.DistanceCalculator
import team.swyp.sdu.domain.estimator.StepEstimator
import team.swyp.sdu.domain.model.StepValidationInput
import team.swyp.sdu.domain.movement.MovementStateStabilizer
import team.swyp.sdu.domain.service.AccelerometerManager
import team.swyp.sdu.domain.service.ActivityRecognitionManager
import team.swyp.sdu.domain.service.ActivityState
import team.swyp.sdu.domain.service.ActivityType
import team.swyp.sdu.domain.service.LocationTrackingService
import team.swyp.sdu.domain.service.MovementState
import team.swyp.sdu.domain.service.StepCounterManager
import team.swyp.sdu.domain.validator.StepCountValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import team.swyp.sdu.domain.model.StepValidationResult
import timber.log.Timber
import javax.inject.Inject

/**
 * 산책 상태를 관리하는 ViewModel
 */
@HiltViewModel
class WalkingViewModel
@Inject
constructor(
    application: Application,
    private val stepCounterManager: StepCounterManager,
    private val activityRecognitionManager: ActivityRecognitionManager,
    private val accelerometerManager: AccelerometerManager,
    private val walkingSessionRepository: WalkingSessionRepository,
    private val stepCountValidator: StepCountValidator,
    private val movementStateStabilizer: MovementStateStabilizer,
    private val stepEstimator: StepEstimator,
    private val distanceCalculator: DistanceCalculator,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<WalkingUiState>(
        WalkingUiState.EmotionSelection(selectedEmotions = emptySet()),
    )
    val uiState: StateFlow<WalkingUiState> = _uiState.asStateFlow()

    // 선택된 감정 리스트
    private val selectedEmotions = mutableSetOf<EmotionType>()

    // 감정 기록 상태
    private val _emotionValue = MutableStateFlow<Float>(0.5f) // 슬라이더 값 (0.0 ~ 1.0)
    val emotionValue: StateFlow<Float> = _emotionValue.asStateFlow()

    private val _emotionPhotoUri = MutableStateFlow<android.net.Uri?>(null)
    val emotionPhotoUri: StateFlow<android.net.Uri?> = _emotionPhotoUri.asStateFlow()

    private val _emotionText = MutableStateFlow<String>("")
    val emotionText: StateFlow<String> = _emotionText.asStateFlow()

    init {
        // 초기 상태를 감정 선택 상태로 설정
        _uiState.value = WalkingUiState.EmotionSelection(selectedEmotions = emptySet())
    }

    // Location 리스트를 StateFlow로 노출 (Shared ViewModel을 위한)
    private val _locations = MutableStateFlow<List<LocationPoint>>(emptyList())
    val locations: StateFlow<List<LocationPoint>> = _locations.asStateFlow()

    private var currentSession: WalkingSession? = null
    private val locationPoints = mutableListOf<LocationPoint>()
    private var stepCountJob: Job? = null
    private var locationJob: Job? = null
    private var durationUpdateJob: Job? = null
    private var activityJob: Job? = null
    private var accelerometerJob: Job? = null
    private var locationReceiver: BroadcastReceiver? = null

    // 활동 상태 추적 관련
    private val activityStatsList = mutableListOf<ActivityStats>()
    private var lastActivityState: ActivityState? = null
    private var lastActivityChangeTime: Long = 0L
    private var lastLocationForActivity: LocationPoint? = null

    // 가속도계 기반 즉각 피드백
    private var currentMovementState: MovementState? = null
    private var lastAcceleration: Float = 0f // 마지막 가속도

    // 걸음 수 추적
    private var lastStepCount: Int = 0
    private var lastRawStepCount: Int = 0 // 센서에서 받은 누적 걸음 수 (보정 전)
    private var startTimeMillis: Long = 0L // 산책 시작/재개 시점
    private var elapsedBeforePause: Long = 0L // 일시정지 전까지 누적 시간
    private var stepOffset: Int = 0 // 일시정지 구간에서 제외할 누적 걸음 수
    private var pausedStepBase: Int = 0 // 일시정지 시점의 실제 걸음 수

    /**
     * 감정 선택/해제
     */
    fun toggleEmotion(emotionType: EmotionType) {
        if (selectedEmotions.contains(emotionType)) {
            selectedEmotions.remove(emotionType)
        } else {
            selectedEmotions.add(emotionType)
        }
        // UI 상태 업데이트
        val currentState = _uiState.value
        if (currentState is WalkingUiState.EmotionSelection || currentState is WalkingUiState.Initial) {
            _uiState.value = WalkingUiState.EmotionSelection(selectedEmotions = selectedEmotions.toSet())
        }
    }

    /**
     * 선택된 감정 리스트 반환
     */
    fun getSelectedEmotions(): Set<EmotionType> = selectedEmotions.toSet()

    /**
     * 감정 선택 슬라이더 값 업데이트
     */
    fun setEmotionValue(value: Float) {
        _emotionValue.value = value.coerceIn(0f, 1f)
    }

    /**
     * 감정 기록 사진 URI 설정
     */
    fun setEmotionPhotoUri(uri: android.net.Uri?) {
        _emotionPhotoUri.value = uri
    }

    /**
     * 감정 기록 텍스트 설정
     */
    fun setEmotionText(text: String) {
        _emotionText.value = text
    }

    /**
     * 산책 시작
     */
    fun startWalking() {
        if (!stepCounterManager.isStepCounterAvailable()) {
            _uiState.value = WalkingUiState.Error("걸음 수 센서를 사용할 수 없습니다")
            return
        }

        startTimeMillis = System.currentTimeMillis()
        elapsedBeforePause = 0L
        stepOffset = 0
        pausedStepBase = 0
        lastRawStepCount = 0
        // 선택된 감정들을 Emotion 리스트로 변환
        val emotions = selectedEmotions.map { emotionType ->
            Emotion(
                type = emotionType,
                timestamp = startTimeMillis,
            )
        }
        currentSession = WalkingSession(
            startTime = startTimeMillis,
            emotions = emotions,
        )
        locationPoints.clear()
        _locations.value = emptyList()

        stepCounterManager.startTracking()
        startLocationTracking()
        startActivityTracking()
        startAccelerometerTracking()

        // Domain 클래스 초기화
        lastStepCount = 0
        lastRawStepCount = 0
        lastAcceleration = 0f
        currentMovementState = null
        movementStateStabilizer.reset()
        stepEstimator.reset()
        distanceCalculator.reset()

        _uiState.value =
            WalkingUiState.Walking(
                stepCount = 0,
                duration = 0L,
                distance = 0f,
                currentActivity = null,
                currentMovementState = null,
                currentSpeed = 0f,
                debugInfo = null,
            )

        // 걸음 수 업데이트 수신 - 하이브리드 거리 계산 사용
        stepCountJob =
            stepCounterManager
                .getStepCountUpdates()
                .onEach { realStepCount ->
                    lastRawStepCount = realStepCount
                    val state = _uiState.value
                    if (state is WalkingUiState.Walking) {
                        if (state.isPaused) return@onEach
                        val effectiveStepCount = (realStepCount - stepOffset).coerceAtLeast(0)

                        // 초기 걸음 수 설정
                        val previousStepCount = lastStepCount
                        if (distanceCalculator.getAverageStepLength() == null && effectiveStepCount > 0) {
                            distanceCalculator.initialize(effectiveStepCount)
                        }

                        // StepEstimator에 실제 걸음 수 업데이트 알림
                        stepEstimator.onRealStepUpdated(effectiveStepCount, System.currentTimeMillis())

                        // 걸음 수 검증
                        val stepDelta = effectiveStepCount - previousStepCount
                        lastStepCount = effectiveStepCount
                        val gpsDistance = distanceCalculator.calculateGpsDistance(locationPoints)
                        val gpsSpeed = distanceCalculator.calculateSpeed(locationPoints)
                        val validationInput = StepValidationInput(
                            stepDelta = stepDelta,
                            activityType = state.currentActivity,
                            movementState = state.currentMovementState,
                            gpsDistance = gpsDistance,
                            gpsSpeed = gpsSpeed,
                            acceleration = lastAcceleration,
                            locations = locationPoints,
                        )
                        val validationResult = stepCountValidator.validate(validationInput)

                        // 검증 통과 시에만 걸음 수 업데이트
                        val validatedStepCount = when (validationResult) {
                            is StepValidationResult.Accepted -> {
                                effectiveStepCount
                            }
                            is StepValidationResult.Rejected.StationaryWalking -> {
                                // 제자리 걷기 감지 시 걸음수 증가 차단!
                                Timber.w("제자리 걷기 감지: 걸음수 증가 차단")
                                lastStepCount  // 이전 값 유지
                            }
                            else -> {
                                Timber.w("걸음 수 검증 실패: $validationResult")
                                lastStepCount
                            }
                        }

                        // 보간된 걸음 수 계산
                        val displayStepCount = if (currentMovementState != null && lastAcceleration > 0f) {
                            stepEstimator.estimate(
                                currentMovementState!!,
                                lastAcceleration,
                                System.currentTimeMillis(),
                            )
                        } else {
                            validatedStepCount
                        }

                        // 거리 계산
                        val totalDistance = distanceCalculator.calculateTotalDistance(locationPoints, validatedStepCount)

                        // 디버그 정보 생성
                        val debugInfo =
                            WalkingUiState.DebugInfo(
                                acceleration = lastAcceleration,
                                stepsPerSecond = stepEstimator.getEstimatedStepsPerSecond(),
                                averageStepLength = distanceCalculator.getAverageStepLength(),
                                realStepCount = validatedStepCount,
                                interpolatedStepCount = displayStepCount,
                                gpsDistance = totalDistance, // 하이브리드 거리
                                stepBasedDistance = totalDistance, // 하이브리드 거리
                                locationPointCount = locationPoints.size,
                                lastLocation = locationPoints.lastOrNull(),
                            )

                        _uiState.value =
                            state.copy(
                                stepCount = displayStepCount,
                                distance = totalDistance,
                                currentSpeed = gpsSpeed,
                                debugInfo = debugInfo,
                            )
                        updateCurrentSession(stepCount = validatedStepCount)

                        // 포그라운드 알림 업데이트
                        updateForegroundNotification(displayStepCount, totalDistance, state.duration)

                        Timber.d("걸음 수 업데이트: 실제=$realStepCount, 검증=$validatedStepCount, 보간=$displayStepCount")
                    }
                }.catch { e ->
                    Timber.e(e, "걸음 수 업데이트 오류")
                }.launchIn(viewModelScope)

        // 시간 업데이트 (1초마다) - 하이브리드 거리 계산 사용 + 보간된 걸음 수 업데이트
        durationUpdateJob =
            viewModelScope.launch {
                while (true) {
                    delay(1000)
                    val state = _uiState.value
                    if (state is WalkingUiState.Walking) {
                        if (state.isPaused) continue
                        val currentDuration = elapsedBeforePause + (System.currentTimeMillis() - startTimeMillis)

                        // 보간된 걸음 수 계산
                        val displayStepCount = if (currentMovementState != null && lastAcceleration > 0f) {
                            stepEstimator.estimate(
                                currentMovementState!!,
                                lastAcceleration,
                                System.currentTimeMillis(),
                            )
                        } else {
                            lastStepCount
                        }

                        // 거리 및 속도 계산
                        val totalDistance = distanceCalculator.calculateTotalDistance(locationPoints, lastStepCount)
                        val gpsSpeed = distanceCalculator.calculateSpeed(locationPoints)

                        // 디버그 정보 생성
                        val debugInfo =
                            WalkingUiState.DebugInfo(
                                acceleration = lastAcceleration,
                                stepsPerSecond = stepEstimator.getEstimatedStepsPerSecond(),
                                averageStepLength = distanceCalculator.getAverageStepLength(),
                                realStepCount = lastStepCount,
                                interpolatedStepCount = displayStepCount,
                                gpsDistance = totalDistance,
                                stepBasedDistance = totalDistance,
                                locationPointCount = locationPoints.size,
                                lastLocation = locationPoints.lastOrNull(),
                            )

                        _uiState.value =
                            state.copy(
                                stepCount = displayStepCount,
                                duration = currentDuration,
                                distance = totalDistance,
                                currentSpeed = gpsSpeed,
                                debugInfo = debugInfo,
                            )

                        // 포그라운드 알림 업데이트 (1초마다)
                        updateForegroundNotification(displayStepCount, totalDistance, currentDuration)
                    } else {
                        break
                    }
                }
            }

        Timber.d("산책 시작")
    }

    /**
     * 산책 종료
     */
    fun stopWalking() {
        val session = currentSession ?: return

        stepCounterManager.stopTracking()
        stopLocationTracking()
        accelerometerManager.stopTracking()

        // Job 취소
        stepCountJob?.cancel()
        locationJob?.cancel()
        durationUpdateJob?.cancel()
        activityJob?.cancel()
        accelerometerJob?.cancel()

        val endTime = System.currentTimeMillis()

        // 마지막 활동 상태 시간 기록
        updateActivityStatsForCurrentState(endTime)

        // BroadcastReceiver에서 수신한 위치 데이터 사용
        val locationPointsFromService = locationPoints.toList()

        // 활동 상태별 통계 계산
        val finalActivityStats = calculateFinalActivityStats(locationPointsFromService)
        val primaryActivity = findPrimaryActivity(finalActivityStats)

        val completedSession =
            session.copy(
                endTime = endTime,
                locations = locationPointsFromService,
                totalDistance = distanceCalculator.calculateTotalDistance(locationPointsFromService, session.stepCount),
                activityStats = finalActivityStats,
                primaryActivity = primaryActivity,
            )

        // Location 리스트를 StateFlow에 저장 (Shared ViewModel을 위한)
        _locations.value = locationPointsFromService

        // 활동 상태 추적 중지
        activityRecognitionManager.stopTracking()

        // 세션 저장 (로컬 저장 + 서버 동기화 시도)
        viewModelScope.launch {
            try {
                walkingSessionRepository.saveSession(completedSession)
                Timber.d("산책 세션 저장 완료: ${completedSession.stepCount}걸음, ${completedSession.getFormattedDistance()}")
            } catch (e: Exception) {
                Timber.e(e, "산책 세션 저장 실패")
                // 저장 실패해도 UI는 정상적으로 완료 상태로 전환
            }
        }

        currentSession = null
        locationPoints.clear()

        _uiState.value = WalkingUiState.Completed(completedSession)
        Timber.d("산책 종료: ${completedSession.stepCount}걸음, ${completedSession.getFormattedDistance()}")
    }

    /**
     * 일시정지
     */
    fun pauseWalking() {
        val state = _uiState.value
        if (state is WalkingUiState.Walking && !state.isPaused) {
            elapsedBeforePause = state.duration
            pausedStepBase = lastRawStepCount
            _uiState.value = state.copy(isPaused = true)
            Timber.d("산책 일시정지: $elapsedBeforePause ms 누적")
        }
    }

    /**
     * 일시정지 해제 (재개)
     */
    fun resumeWalking() {
        val state = _uiState.value
        if (state is WalkingUiState.Walking && state.isPaused) {
            val pausedDelta = (lastRawStepCount - pausedStepBase).coerceAtLeast(0)
            stepOffset += pausedDelta
            pausedStepBase = lastRawStepCount
            startTimeMillis = System.currentTimeMillis()
            _uiState.value = state.copy(isPaused = false, duration = elapsedBeforePause)
            Timber.d("산책 재개: 기준 시각 업데이트, 누적 $elapsedBeforePause ms")
        }
    }

    /**
     * 위치 추적 시작
     */
    private fun startLocationTracking() {
        val intent =
            Intent(
                getApplication<Application>(),
                LocationTrackingService::class.java,
            ).apply {
                action = LocationTrackingService.ACTION_START_TRACKING
            }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }

        // 위치 데이터를 BroadcastReceiver로 수신
        registerLocationReceiver()

        Timber.d("위치 추적 서비스 시작")
    }

    /**
     * 위치 추적 중지
     */
    private fun stopLocationTracking() {
        val intent =
            android.content
                .Intent(
                    getApplication<Application>(),
                    LocationTrackingService::class.java,
                ).apply {
                    action = LocationTrackingService.ACTION_STOP_TRACKING
                }
        // 서비스를 중지할 때는 startService()를 사용합니다.
        // startForegroundService()를 사용하면 서비스가 startForeground()를 호출해야 하는데,
        // ACTION_STOP_TRACKING은 서비스를 중지하는 액션이므로 startForeground()를 호출하지 않아 오류가 발생합니다.
        // startService()는 일반 서비스로 시작하므로 startForeground()를 호출할 필요가 없습니다.
        getApplication<Application>().startService(intent)
    }

    /**
     * 포그라운드 알림 업데이트
     */
    private fun updateForegroundNotification(
        stepCount: Int,
        distance: Float,
        duration: Long,
    ) {
        val intent =
            android.content
                .Intent(
                    getApplication<Application>(),
                    LocationTrackingService::class.java,
                ).apply {
                    action = LocationTrackingService.ACTION_UPDATE_NOTIFICATION
                    putExtra(LocationTrackingService.EXTRA_STEP_COUNT, stepCount)
                    putExtra(LocationTrackingService.EXTRA_DISTANCE, distance)
                    putExtra(LocationTrackingService.EXTRA_DURATION, duration)
                }
        getApplication<Application>().startForegroundService(intent)
    }

    /**
     * 가속도계 기반 즉각 피드백 시작
     */
    private fun startAccelerometerTracking() {
        if (!accelerometerManager.isAccelerometerAvailable()) {
            Timber.w("가속도계를 사용할 수 없습니다")
            return
        }

        accelerometerManager.startTracking()

        // 가속도계 움직임 감지 업데이트 수신 (즉각 피드백 + 실시간 걸음 수 보간)
        accelerometerJob =
            accelerometerManager
                .getMovementUpdates()
                .onEach { detection ->
                    val state = _uiState.value
                    if (state is WalkingUiState.Walking && !state.isPaused) {
                        val currentTime = System.currentTimeMillis()
                        currentMovementState = detection.state
                        lastAcceleration = detection.acceleration

                        // MovementStateStabilizer를 사용하여 상태 스무딩
                        val stableState = movementStateStabilizer.update(detection.state, currentTime)

                        // 보간된 걸음 수 계산
                        val displayStepCount = stepEstimator.estimate(
                            stableState,
                            detection.acceleration,
                            currentTime,
                        )

                        // 거리 및 속도 계산
                        val totalDistance = distanceCalculator.calculateTotalDistance(locationPoints, lastStepCount)
                        val estimatedSpeed = distanceCalculator.calculateSpeed(locationPoints)

                        // 디버그 정보 생성
                        val debugInfo =
                            WalkingUiState.DebugInfo(
                                acceleration = detection.acceleration,
                                stepsPerSecond = stepEstimator.getEstimatedStepsPerSecond(),
                                averageStepLength = distanceCalculator.getAverageStepLength(),
                                realStepCount = lastStepCount,
                                interpolatedStepCount = displayStepCount,
                                gpsDistance = totalDistance,
                                stepBasedDistance = totalDistance,
                                locationPointCount = locationPoints.size,
                                lastLocation = locationPoints.lastOrNull(),
                            )

                        _uiState.value =
                            state.copy(
                                stepCount = displayStepCount,
                                distance = totalDistance,
                                currentMovementState = stableState,
                                currentSpeed = estimatedSpeed,
                                debugInfo = debugInfo,
                            )

//                    Timber.d("가속도계 움직임 감지: ${detection.state}, 가속도: ${detection.acceleration}m/s², 예상 걸음/초: $estimatedStepsPerSecond, 보간 걸음 수: $displayStepCount")
                    }
                }.catch { e ->
                    Timber.e(e, "가속도계 업데이트 오류")
                }.launchIn(viewModelScope)
    }

    /**
     * 활동 상태 추적 시작
     */
    private fun startActivityTracking() {
        if (!activityRecognitionManager.isActivityRecognitionAvailable()) {
            Timber.w("Activity Recognition을 사용할 수 없습니다")
            return
        }

        activityStatsList.clear()
        lastActivityState = null
        lastActivityChangeTime = System.currentTimeMillis()
        lastLocationForActivity = null

        activityRecognitionManager.startTracking()

        // 활동 상태 업데이트 수신
        activityJob =
            activityRecognitionManager
                .getActivityUpdates()
                .onEach { activityState ->
                    handleActivityStateChange(activityState)
                }.catch { e ->
                    Timber.e(e, "활동 상태 업데이트 오류")
                }.launchIn(viewModelScope)
    }

    /**
     * 활동 상태 변경 처리
     */
    private fun handleActivityStateChange(newState: ActivityState) {
        val currentUiState = _uiState.value
        if (currentUiState is WalkingUiState.Walking && currentUiState.isPaused) {
            return
        }
        val currentTime = System.currentTimeMillis()

        // 이전 활동 상태의 시간 기록
        if (lastActivityState != null && lastActivityChangeTime > 0) {
            val duration = currentTime - lastActivityChangeTime
            updateActivityStats(lastActivityState!!.type, duration, 0f)
        }

        // 현재 활동 상태 업데이트
        lastActivityState = newState
        lastActivityChangeTime = currentTime

        // LocationTrackingService에 활동 상태 변경 즉시 전송 (지연 해결)
        val intent =
            Intent(
                LocationTrackingService.ACTION_ACTIVITY_UPDATE,
            ).apply {
                setPackage(getApplication<Application>().packageName)
                putExtra(LocationTrackingService.EXTRA_ACTIVITY_TYPE, newState.type.ordinal)
            }
        getApplication<Application>().sendBroadcast(intent)
        Timber.d("활동 상태 변경 Broadcast 전송: ${newState.type.name}")

        // UI 업데이트
        val state = _uiState.value
        if (state is WalkingUiState.Walking) {
            val totalDistance = distanceCalculator.calculateTotalDistance(locationPoints, state.stepCount)

            // 디버그 정보 생성 (기존 정보 유지)
            val debugInfo =
                state.debugInfo?.copy(
                    gpsDistance = totalDistance,
                    stepBasedDistance = totalDistance,
                    locationPointCount = locationPoints.size,
                    lastLocation = locationPoints.lastOrNull(),
                ) ?: WalkingUiState.DebugInfo(
                    acceleration = lastAcceleration,
                    stepsPerSecond = stepEstimator.getEstimatedStepsPerSecond(),
                    averageStepLength = distanceCalculator.getAverageStepLength(),
                    realStepCount = state.stepCount,
                    interpolatedStepCount = state.stepCount,
                    gpsDistance = totalDistance,
                    stepBasedDistance = totalDistance,
                    locationPointCount = locationPoints.size,
                    lastLocation = locationPoints.lastOrNull(),
                )

            _uiState.value =
                state.copy(
                    currentActivity = newState.type,
                    distance = totalDistance,
                    debugInfo = debugInfo,
                )
        }

        Timber.d("활동 상태 변경: ${newState.type.name}, 신뢰도: ${newState.confidence}%")
    }

    /**
     * 활동 상태별 통계 업데이트
     */
    private fun updateActivityStats(
        type: ActivityType,
        duration: Long,
        distance: Float,
    ) {
        val existingIndex = activityStatsList.indexOfFirst { it.type == type }
        if (existingIndex >= 0) {
            val currentStats = activityStatsList[existingIndex]
            activityStatsList[existingIndex] =
                ActivityStats(
                    type = type,
                    duration = currentStats.duration + duration,
                    distance = currentStats.distance + distance,
                )
        } else {
            activityStatsList.add(ActivityStats(type, duration, distance))
        }
    }

    /**
     * 현재 활동 상태의 시간 기록 (산책 종료 시 호출)
     */
    private fun updateActivityStatsForCurrentState(endTime: Long) {
        lastActivityState?.let { state ->
            val duration = endTime - lastActivityChangeTime
            updateActivityStats(state.type, duration, 0f)
        }
    }

    /**
     * 위치 데이터와 활동 상태를 매칭하여 활동 상태별 거리 계산
     */
    private fun calculateFinalActivityStats(locations: List<LocationPoint>): List<ActivityStats> {
        val finalStats = activityStatsList.toMutableList()

        // 위치 데이터를 시간순으로 정렬
        val sortedLocations = locations.sortedBy { it.timestamp }

        if (sortedLocations.isEmpty() || lastActivityState == null) {
            return finalStats
        }

        // 각 위치 포인트에 대해 활동 상태 매칭
        // 간단한 구현: 마지막 활동 상태를 사용
        // 실제로는 시간 기반으로 활동 상태를 매칭해야 함
        val lastActivity = lastActivityState!!.type
        val totalDistance = distanceCalculator.calculateTotalDistance(sortedLocations, lastStepCount)

        // 전체 거리를 현재 활동 상태에 할당 (간단한 구현)
        // 실제로는 시간 기반으로 더 정확하게 계산해야 함
        val existingIndex = finalStats.indexOfFirst { it.type == lastActivity }
        if (existingIndex >= 0) {
            val currentStats = finalStats[existingIndex]
            finalStats[existingIndex] = currentStats.copy(distance = currentStats.distance + totalDistance)
        } else {
            finalStats.add(ActivityStats(lastActivity, 0L, totalDistance))
        }

        return finalStats
    }

    /**
     * 주요 활동 상태 찾기 (가장 오래 한 활동)
     */
    private fun findPrimaryActivity(stats: List<ActivityStats>): ActivityType? {
        if (stats.isEmpty()) return null

        return stats.maxByOrNull { it.duration }?.type
    }

    /**
     * 현재 세션 업데이트
     */
    private fun updateCurrentSession(stepCount: Int) {
        currentSession =
            currentSession?.copy(
                stepCount = stepCount,
                locations = locationPoints.toList(),
                totalDistance = distanceCalculator.calculateTotalDistance(locationPoints, stepCount),
            )
    }


    /**
     * 상태 초기화 (리셋)
     */
    fun reset() {
        Timber.d("WalkingViewModel 상태 초기화")

        // 모든 Job 취소
        stepCountJob?.cancel()
        locationJob?.cancel()
        durationUpdateJob?.cancel()
        activityJob?.cancel()
        accelerometerJob?.cancel()

        // 추적 중지
        stepCounterManager.stopTracking()
        stopLocationTracking()
        accelerometerManager.stopTracking()
        activityRecognitionManager.stopTracking()

        // 상태 초기화
        currentSession = null
        locationPoints.clear()
        _locations.value = emptyList()
        activityStatsList.clear()
        lastActivityState = null
        lastActivityChangeTime = 0L
        lastLocationForActivity = null
        currentMovementState = null
        lastStepCount = 0
        lastRawStepCount = 0
        lastAcceleration = 0f
        movementStateStabilizer.reset()
        stepEstimator.reset()
        distanceCalculator.reset()
        selectedEmotions.clear()

        // 감정 기록 상태 초기화
        _emotionValue.value = 0.5f
        _emotionPhotoUri.value = null
        _emotionText.value = ""

        // UI 상태를 감정 선택 상태로 초기화
        _uiState.value = WalkingUiState.EmotionSelection(selectedEmotions = emptySet())

        Timber.d("WalkingViewModel 상태 초기화 완료")
    }

    /**
     * 위치 데이터 BroadcastReceiver 등록
     */
    private fun registerLocationReceiver() {
        locationReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    if (intent?.action == LocationTrackingService.ACTION_LOCATION_DATA) {
                        val locationsJson = intent.getStringExtra(LocationTrackingService.EXTRA_LOCATIONS) ?: return
                        try {
                            val currentStateSnapshot = _uiState.value
                            if (currentStateSnapshot is WalkingUiState.Walking && currentStateSnapshot.isPaused) {
                                return
                            }

                            val newLocations = Json.decodeFromString<List<LocationPoint>>(locationsJson)

                            // 새로운 위치 포인트만 추가 (중복 제거)
                            newLocations.forEach { newPoint ->
                                // 이미 존재하는 위치인지 확인 (타임스탬프와 좌표로 판단)
                                val exists =
                                    locationPoints.any { existing ->
                                        existing.timestamp == newPoint.timestamp ||
                                                (
                                                        kotlin.math.abs(existing.latitude - newPoint.latitude) < 0.000001 &&
                                                                kotlin.math.abs(existing.longitude - newPoint.longitude) <
                                                                0.000001
                                                        )
                                    }

                                if (!exists) {
                                    locationPoints.add(newPoint)
                                    _locations.value = locationPoints.toList()
                                }
                            }

                            // 거리 계산 및 UI 업데이트
                            val currentState = _uiState.value
                            if (currentState is WalkingUiState.Walking && !currentState.isPaused) {
                                val totalDistance = distanceCalculator.calculateTotalDistance(locationPoints, currentState.stepCount)
                                _uiState.value = currentState.copy(distance = totalDistance)
                                updateCurrentSession(stepCount = currentState.stepCount)
                            }

                            Timber.d("위치 데이터 수신: ${newLocations.size}개 포인트, 총 ${locationPoints.size}개 포인트")
                        } catch (e: Exception) {
                            Timber.e(e, "위치 데이터 파싱 실패")
                        }
                    }
                }
            }

        val filter = IntentFilter(LocationTrackingService.ACTION_LOCATION_DATA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getApplication<Application>().registerReceiver(
                locationReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED,
            )
        } else {
            getApplication<Application>().registerReceiver(locationReceiver, filter)
        }
    }

    /**
     * 위치 데이터 BroadcastReceiver 해제
     */
    private fun unregisterLocationReceiver() {
        locationReceiver?.let {
            try {
                getApplication<Application>().unregisterReceiver(it)
            } catch (e: Exception) {
                Timber.e(e, "BroadcastReceiver 해제 실패")
            }
        }
        locationReceiver = null
    }

    override fun onCleared() {
        super.onCleared()
        unregisterLocationReceiver()
        stepCountJob?.cancel()
        locationJob?.cancel()
        durationUpdateJob?.cancel()
        activityJob?.cancel()
        activityRecognitionManager.stopTracking()
    }
}

/**
 * Walking UI State
 */
sealed interface WalkingUiState {
    /**
     * 초기 상태 (감정 선택 전)
     */
    data object Initial : WalkingUiState

    /**
     * 감정 선택 상태
     */
    data class EmotionSelection(
        val selectedEmotions: Set<EmotionType>,
    ) : WalkingUiState

    /**
     * 산책 중
     */
    data class Walking(
        val stepCount: Int,
        val duration: Long,
        val distance: Float = 0f,
        val currentActivity: ActivityType? = null,
        val currentMovementState: team.swyp.sdu.domain.service.MovementState? = null, // 걷는 중/뛰는 중 상태
        val currentSpeed: Float = 0f, // 가속도계로 측정한 현재 속도 (m/s)
        // 검증용 디버그 정보
        val debugInfo: DebugInfo? = null,
        val isPaused: Boolean = false,
    ) : WalkingUiState

    /**
     * 검증용 디버그 정보
     */
    data class DebugInfo(
        val acceleration: Float, // 가속도 (m/s²)
        val stepsPerSecond: Float, // 걸음 수/초
        val averageStepLength: Float?, // 평균 보폭 (m)
        val realStepCount: Int, // 실제 걸음 수
        val interpolatedStepCount: Int, // 보간된 걸음 수
        val gpsDistance: Float, // GPS 거리 (m)
        val stepBasedDistance: Float, // Step Counter 거리 (m)
        val locationPointCount: Int, // 위치 포인트 개수
        val lastLocation: LocationPoint?, // 마지막 위치
    )

    /**
     * 산책 완료
     */
    data class Completed(
        val session: WalkingSession,
    ) : WalkingUiState

    /**
     * 오류 상태
     */
    data class Error(
        val message: String,
    ) : WalkingUiState
}