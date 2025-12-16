# WalkingViewModel 리팩토링 및 Validator 분리 계획

## 📋 목표

1. **WalkingViewModel 비대화 방지**: 검증 로직을 별도 클래스로 분리
2. **책임 분리**: ViewModel은 UI 상태 관리, Validator는 검증 로직 담당
3. **코드 가독성 향상**: 각 클래스의 역할 명확화
4. **유지보수성 향상**: 검증 로직 변경 시 Validator만 수정

---

## 🎯 핵심 원칙

### ✅ 실시간 검증 수행
- **산책 중 지속적으로 검증 수행** (걸음수 업데이트마다)
- 실시간으로 플래그 수집 및 누적
- 세션 종료 시 누적된 플래그로 최종 판단

### ✅ 걸음수 실시간 차단 없음
- 실시간으로 걸음수를 차단하지 않음
- 검증은 하지만 걸음수는 그대로 카운팅
- 사용자 경험을 방해하지 않음

### ✅ 검증 결과에 따른 처리
- **ACCEPT**: 정상 저장
- **ACCEPT_FLAGGED**: 저장하지만 플래그 표시
- **REJECT**: 세션 거부 (저장 안 함)

---

## 📐 아키텍처 설계

### 1. 클래스 구조

```
domain/validator/
└── WalkingSessionValidator.kt  (검증 로직)

data/model/
└── ValidationResult.kt  (검증 결과 모델)

presentation/viewmodel/
└── WalkingViewModel.kt  (UI 상태 관리만)
```

### 2. 책임 분리

| 클래스 | 책임 |
|--------|------|
| **WalkingViewModel** | UI 상태 관리, 사용자 액션 처리, 세션 생성/저장, **실시간 검증 트리거** |
| **WalkingSessionValidator** | **실시간 검증 수행**, 어뷰징 패턴 감지, 플래그 수집, 최종 검증 결과 반환 |
| **ValidationResult** | 검증 결과 데이터 모델 |
| **ValidationState** | 실시간 검증 상태 (누적된 플래그들) |

---

## 🔧 구현 계획

### Phase 1: ValidationResult 모델 생성

**파일**: `app/src/main/java/team/swyp/sdu/data/model/ValidationResult.kt`

```kotlin
package team.swyp.sdu.data.model

/**
 * 세션 검증 결과
 */
data class ValidationResult(
    val isValid: Boolean,
    val flags: List<SuspicionFlag>,
    val action: ValidationAction,
    val message: String? = null
)

/**
 * 의심 플래그 (심각도별)
 */
enum class SuspicionFlag(val severity: Severity, val description: String) {
    // Critical (즉시 거부)
    IMPOSSIBLE_STRIDE(...),
    IMPOSSIBLE_SPEED(...),
    VEHICLE_DETECTED(...),
    EXCESSIVE_STEPS(...),
    SHAKING_PATTERN(...),
    STATIONARY_WALKING(...),
    
    // Warning (저장하지만 플래그)
    HIGH_SPEED_RUNNING(...),
    SHORT_DURATION_HIGH_STEPS(...),
    
    // Info (정보성)
    INDOOR_SUSPECTED(...),
    LONG_DURATION(...),
    SLOW_WALKING(...)
}

enum class Severity { CRITICAL, WARNING, INFO }
enum class ValidationAction { ACCEPT, ACCEPT_FLAGGED, REJECT }
```

**작업 내용**:
- ✅ ValidationResult 데이터 클래스 생성
- ✅ SuspicionFlag enum 정의
- ✅ Severity, ValidationAction enum 정의

---

### Phase 2: ValidationState 추가 (실시간 검증 상태 관리)

**파일**: `app/src/main/java/team/swyp/sdu/data/model/ValidationResult.kt`에 추가

```kotlin
/**
 * 실시간 검증 상태 (산책 중 누적되는 플래그들)
 */
data class ValidationState(
    val accumulatedFlags: MutableSet<SuspicionFlag> = mutableSetOf(),
    val lastValidationTime: Long = System.currentTimeMillis()
) {
    /**
     * 플래그 추가
     */
    fun addFlag(flag: SuspicionFlag) {
        accumulatedFlags.add(flag)
    }
    
    /**
     * 최종 검증 결과 생성
     */
    fun toValidationResult(): ValidationResult {
        val flags = accumulatedFlags.toList()
        val action = determineAction(flags)
        val message = generateMessage(flags, action)
        
        return ValidationResult(
            isValid = action != ValidationAction.REJECT,
            flags = flags,
            action = action,
            message = message
        )
    }
    
    private fun determineAction(flags: List<SuspicionFlag>): ValidationAction {
        return when {
            flags.any { it.severity == Severity.CRITICAL } -> ValidationAction.REJECT
            flags.any { it.severity == Severity.WARNING } -> ValidationAction.ACCEPT_FLAGGED
            else -> ValidationAction.ACCEPT
        }
    }
    
    private fun generateMessage(flags: List<SuspicionFlag>, action: ValidationAction): String? {
        if (flags.isEmpty()) return null
        return when (action) {
            ValidationAction.REJECT -> {
                val criticalFlag = flags.first { it.severity == Severity.CRITICAL }
                "기록을 저장할 수 없습니다: ${criticalFlag.description}"
            }
            ValidationAction.ACCEPT_FLAGGED -> {
                "기록이 저장되었지만 일부 의심스러운 활동이 감지되었습니다"
            }
            else -> null
        }
    }
}
```

### Phase 3: WalkingSessionValidator 클래스 생성

**파일**: `app/src/main/java/team/swyp/sdu/domain/validator/WalkingSessionValidator.kt`

#### 3-1. 클래스 구조

```kotlin
package team.swyp.sdu.domain.validator

import team.swyp.sdu.data.model.*
import team.swyp.sdu.domain.service.ActivityType
import timber.log.Timber

class WalkingSessionValidator {
    
    companion object {
        // 임계값 상수들
        private const val MIN_POSSIBLE_STRIDE = 0.2f   // 20cm
        private const val MAX_POSSIBLE_STRIDE = 2.0f   // 2m
        private const val MAX_POSSIBLE_SPEED = 20f     // 20km/h
        private const val MAX_POSSIBLE_STEPS = 100000  // 10만보
        private const val STATIONARY_STRIDE_THRESHOLD = 0.3f   // 30cm
        private const val SHAKING_STRIDE_THRESHOLD = 0.25f     // 25cm
        private const val SHAKING_SPEED_THRESHOLD = 0.8f       // 0.8km/h
        private const val MIN_LOCATION_VARIANCE = 0.0001f
        // ... 기타 임계값들
    }
    
    /**
     * 실시간 검증 수행 (걸음수 업데이트마다 호출)
     * 
     * @param currentSession 현재 진행 중인 세션
     * @param validationState 누적된 검증 상태
     * @return 새로 감지된 플래그들
     */
    fun validateRealtime(
        currentSession: WalkingSession,
        validationState: ValidationState
    ): List<SuspicionFlag> {
        val newFlags = mutableListOf<SuspicionFlag>()
        
        // 1. 물리적 불가능성 검증 (Critical)
        validatePhysicalImpossibility(currentSession, newFlags, validationState)
        
        // 2. 복합 패턴 검증 (Warning)
        validateComplexPatterns(currentSession, newFlags, validationState)
        
        // 3. 정보성 플래그 (Info) - 실시간에서는 제외하거나 최소화
        
        // 새로 감지된 플래그를 상태에 추가
        newFlags.forEach { validationState.addFlag(it) }
        
        if (newFlags.isNotEmpty()) {
            Timber.d("실시간 검증: 새 플래그 감지 ${newFlags.map { it.name }}")
        }
        
        return newFlags
    }
    
    /**
     * 최종 검증 (세션 종료 시)
     */
    fun validateFinal(session: WalkingSession, validationState: ValidationState): ValidationResult {
        // 실시간 검증에서 누락될 수 있는 최종 검증들 수행
        validateInformationalFlags(session, validationState)
        
        return validationState.toValidationResult()
    }
    
    // 검증 메서드들...
}
```

#### 3-2. 검증 로직 세부 구현

**실시간 검증 메서드 시그니처**:
```kotlin
private fun validatePhysicalImpossibility(
    session: WalkingSession,
    newFlags: MutableList<SuspicionFlag>,
    validationState: ValidationState
) {
    // 이미 감지된 플래그는 제외하고 새로 감지된 것만 추가
    // 예: validationState.accumulatedFlags.contains() 체크
}

private fun validateComplexPatterns(
    session: WalkingSession,
    newFlags: MutableList<SuspicionFlag>,
    validationState: ValidationState
) {
    // 복합 패턴 검증 (중복 방지)
}
```

**물리적 불가능성 검증** (Critical):
- 보폭: 20cm 미만 또는 2m 초과
- 속도: 20km/h 초과
- 걸음수: 10만보 초과
- 차량 이동: ActivityType.IN_VEHICLE 감지

**복합 패턴 검증** (Warning):
- 제자리 걸음: 보폭 < 30cm + GPS 이동 거의 없음 + 걸음수 많음
- 흔들기 패턴: 보폭 < 25cm + 속도 < 0.8km/h + GPS 이동 없음
- 빠른 조깅: 속도 10~20km/h
- 짧은 시간 과도한 걸음수: 시간당 1.5만보 초과

**정보성 플래그** (Info) - 최종 검증에서만:
- 실내 활동 추정: GPS 정확도 낮음
- 장시간 활동: 3시간 초과
- 천천히 걷기: 정상 (느린 걷기)

**작업 내용**:
- ✅ WalkingSessionValidator 클래스 생성
- ✅ validateRealtime() 메서드 구현
- ✅ validateFinal() 메서드 구현
- ✅ validatePhysicalImpossibility() 구현 (중복 방지 로직 포함)
- ✅ validateComplexPatterns() 구현 (중복 방지 로직 포함)
- ✅ validateInformationalFlags() 구현
- ✅ GPS 변화량 계산 헬퍼 함수 구현

---

### Phase 4: WalkingSession 모델 확장

**파일**: `app/src/main/java/team/swyp/sdu/data/model/WalkingSession.kt`

```kotlin
data class WalkingSession(
    // 기존 필드들...
    val suspicionFlags: List<String> = emptyList(),  // 의심 플래그 리스트
    val validationAction: String? = null,             // ACCEPT, ACCEPT_FLAGGED, REJECT
    val isValidated: Boolean = false                  // 검증 완료 여부
)
```

**작업 내용**:
- ✅ suspicionFlags 필드 추가
- ✅ validationAction 필드 추가
- ✅ isValidated 필드 추가

---

### Phase 5: WalkingViewModel 통합

**파일**: `app/src/main/java/team/swyp/sdu/presentation/viewmodel/WalkingViewModel.kt`

#### 5-1. Validator 및 ValidationState 추가

```kotlin
@HiltViewModel
class WalkingViewModel @Inject constructor(...) {
    
    // 새로 추가
    private val sessionValidator = WalkingSessionValidator()
    private var validationState: ValidationState? = null  // 실시간 검증 상태
    
    // 기존 코드...
}
```

#### 5-2. startWalking()에서 ValidationState 초기화

```kotlin
fun startWalking() {
    // ... 기존 시작 로직 ...
    
    // ValidationState 초기화
    validationState = ValidationState()
    
    // ... 나머지 코드 ...
}
```

#### 5-3. 실시간 검증 (걸음수 업데이트마다)

```kotlin
stepCountJob = stepCounterManager
    .getStepCountUpdates()
    .onEach { realStepCount ->
        val state = _uiState.value
        if (state is WalkingUiState.Walking) {
            // ... 기존 걸음수 처리 로직 ...
            
            // 현재 세션 업데이트
            updateCurrentSession(stepCount = realStepCount)
            
            // ========== 실시간 검증 수행 ==========
            currentSession?.let { session ->
                validationState?.let { vState ->
                    val newFlags = sessionValidator.validateRealtime(session, vState)
                    
                    // Critical 플래그가 감지되면 즉시 로깅 (선택적: UI 알림 가능)
                    if (newFlags.any { it.severity == Severity.CRITICAL }) {
                        Timber.w("Critical 플래그 실시간 감지: ${newFlags.filter { it.severity == Severity.CRITICAL }}")
                    }
                }
            }
            // ======================================
        }
    }
    .launchIn(viewModelScope)
```

#### 5-4. stopWalking()에서 최종 검증

```kotlin
fun stopWalking() {
    val session = currentSession ?: return
    val vState = validationState ?: ValidationState()
    
    // ... 기존 세션 완성 로직 ...
    
    val completedSession = session.copy(
        endTime = endTime,
        locations = locationPointsFromService,
        totalDistance = calculateHybridDistance(...),
        activityStats = finalActivityStats,
        primaryActivity = primaryActivity,
    )
    
    // ========== 최종 검증 수행 ==========
    val validation = sessionValidator.validateFinal(completedSession, vState)
    
    when (validation.action) {
        ValidationAction.ACCEPT -> {
            // 정상 저장
            val validatedSession = completedSession.copy(
                isValidated = true,
                validationAction = "ACCEPT"
            )
            saveSession(validatedSession)
            _uiState.value = WalkingUiState.Completed(validatedSession)
        }
        
        ValidationAction.ACCEPT_FLAGGED -> {
            // 의심 플래그와 함께 저장
            val flaggedSession = completedSession.copy(
                suspicionFlags = validation.flags.map { it.name },
                validationAction = "ACCEPT_FLAGGED",
                isValidated = true
            )
            saveSession(flaggedSession)
            _uiState.value = WalkingUiState.Completed(flaggedSession)
        }
        
        ValidationAction.REJECT -> {
            // 거부 - 저장하지 않음
            currentSession = null
            locationPoints.clear()
            validationState = null
            _uiState.value = WalkingUiState.Error(
                validation.message ?: "비정상적인 활동이 감지되었습니다"
            )
        }
    }
    
    // ValidationState 초기화
    validationState = null
}
```

**작업 내용**:
- ✅ sessionValidator 인스턴스 추가
- ✅ stopWalking()에 검증 로직 통합
- ✅ 검증 결과에 따른 분기 처리
- ✅ 검증 관련 import 추가

---

## 📊 검증 플로우

### 실시간 검증 (산책 중)

```
산책 시작
    ↓
ValidationState 초기화
    ↓
걸음수 업데이트마다 반복
    ↓
┌─────────────────────────────────┐
│ 실시간 검증 수행                │
│ validateRealtime()              │
│                                 │
│ 1. 물리적 불가능성 검증         │
│    - 보폭, 속도, 걸음수, 차량   │
│                                 │
│ 2. 복합 패턴 검증               │
│    - 제자리 걸음, 흔들기 패턴   │
│    - 빠른 조깅, 과도한 걸음수   │
└─────────────────────────────────┘
    ↓
감지된 플래그를 ValidationState에 누적
    ↓
(걸음수는 그대로 카운팅 - 차단 안 함)
    ↓
다음 걸음수 업데이트까지 대기
```

### 최종 검증 (세션 종료 시)

```
사용자가 산책 종료
    ↓
stopWalking() 호출
    ↓
세션 데이터 완성 (completedSession)
    ↓
┌─────────────────────────────────┐
│ 최종 검증 수행                  │
│ validateFinal()                 │
│                                 │
│ 1. 정보성 플래그 추가           │
│    - 실내 활동, 장시간, 느린 걷기│
│                                 │
│ 2. 누적된 플래그로 최종 판단    │
│    - ValidationState → ValidationResult│
└─────────────────────────────────┘
    ↓
ValidationResult 생성
    ↓
액션에 따른 처리
    ├─ ACCEPT → 정상 저장
    ├─ ACCEPT_FLAGGED → 플래그와 함께 저장
    └─ REJECT → 거부 (저장 안 함)
```

---

## 🎯 검증 규칙 상세

### Critical (즉시 거부)

| 플래그 | 조건 | 처리 |
|--------|------|------|
| IMPOSSIBLE_STRIDE | 보폭 < 20cm 또는 > 2m | REJECT |
| IMPOSSIBLE_SPEED | 속도 > 20km/h | REJECT |
| VEHICLE_DETECTED | ActivityType.IN_VEHICLE | REJECT |
| EXCESSIVE_STEPS | 걸음수 > 10만보 | REJECT |

### Warning (저장하지만 플래그)

| 플래그 | 조건 | 처리 |
|--------|------|------|
| STATIONARY_WALKING | 보폭 < 30cm + GPS 변화 미미 + 걸음수 많음 | ACCEPT_FLAGGED |
| SHAKING_PATTERN | 보폭 < 25cm + 속도 < 0.8km/h + GPS 변화 없음 | ACCEPT_FLAGGED |
| HIGH_SPEED_RUNNING | 속도 10~20km/h | ACCEPT_FLAGGED |
| SHORT_DURATION_HIGH_STEPS | 시간당 1.5만보 초과 | ACCEPT_FLAGGED |

### Info (정보성)

| 플래그 | 조건 | 처리 |
|--------|------|------|
| INDOOR_SUSPECTED | GPS 정확도 낮음 (70% 이상) | ACCEPT |
| LONG_DURATION | 3시간 초과 | ACCEPT |
| SLOW_WALKING | 속도 0.5~2km/h + GPS 이동 있음 | ACCEPT |

---

## 🔍 구현 세부사항

### GPS 변화량 계산

```kotlin
private fun calculateLocationVariance(locations: List<LocationPoint>): Float {
    if (locations.size < 2) return 0f
    
    val latitudes = locations.map { it.latitude }
    val longitudes = locations.map { it.longitude }
    
    val latMean = latitudes.average()
    val lonMean = longitudes.average()
    
    val latVariance = latitudes.map { (it - latMean).pow(2) }.average()
    val lonVariance = longitudes.map { (it - lonMean).pow(2) }.average()
    
    return (latVariance + lonVariance).toFloat()
}
```

### 보폭 계산

```kotlin
private fun calculateStride(session: WalkingSession): Float {
    return if (session.stepCount > 0 && session.totalDistance > 0f) {
        session.totalDistance / session.stepCount
    } else {
        0f
    }
}
```

### 속도 계산

```kotlin
private fun calculateSpeedKmh(session: WalkingSession): Float {
    val durationHours = session.getDurationHours()
    return if (durationHours > 0f && session.totalDistance > 0f) {
        session.totalDistance / 1000f / durationHours
    } else {
        0f
    }
}
```

---

## ✅ 체크리스트

### Phase 1: ValidationResult 모델
- [ ] ValidationResult.kt 파일 생성
- [ ] SuspicionFlag enum 정의
- [ ] Severity enum 정의
- [ ] ValidationAction enum 정의

### Phase 2: ValidationState 추가
- [ ] ValidationState 데이터 클래스 생성
- [ ] addFlag() 메서드 구현
- [ ] toValidationResult() 메서드 구현
- [ ] determineAction() 헬퍼 메서드 구현
- [ ] generateMessage() 헬퍼 메서드 구현

### Phase 3: WalkingSessionValidator
- [ ] WalkingSessionValidator.kt 파일 생성
- [ ] validateRealtime() 메서드 구현 (실시간 검증)
- [ ] validateFinal() 메서드 구현 (최종 검증)
- [ ] validatePhysicalImpossibility() 구현
- [ ] validateComplexPatterns() 구현
- [ ] validateInformationalFlags() 구현
- [ ] 헬퍼 함수들 구현 (GPS 변화량, 보폭, 속도 계산)

### Phase 4: WalkingSession 모델
- [ ] suspicionFlags 필드 추가
- [ ] validationAction 필드 추가
- [ ] isValidated 필드 추가

### Phase 5: WalkingViewModel 통합
- [ ] sessionValidator 인스턴스 추가
- [ ] validationState 변수 추가
- [ ] startWalking()에서 ValidationState 초기화
- [ ] 걸음수 업데이트마다 실시간 검증 수행
- [ ] stopWalking()에서 최종 검증 수행
- [ ] 검증 결과에 따른 분기 처리
- [ ] import 문 추가

### Phase 6: 테스트
- [ ] 정상 케이스 테스트
- [ ] 제자리 걸음 케이스 테스트
- [ ] 흔들기 패턴 케이스 테스트
- [ ] 차량 이동 케이스 테스트
- [ ] 빠른 조깅 케이스 테스트

---

## 📝 주의사항

1. **실시간 검증 수행**: 걸음수 업데이트마다 검증 수행하여 플래그 누적
2. **걸음수 차단 없음**: 실시간으로 걸음수를 차단하지 않음 (검증만 수행)
3. **플래그 누적**: 실시간으로 감지된 플래그들을 ValidationState에 누적
4. **최종 판단**: 세션 종료 시 누적된 플래그로 최종 판단
5. **정상 사용자 보호**: 느린 걷기, 어린이, 노인 고려
6. **복합 조건 판단**: 단일 지표가 아닌 여러 지표 조합으로 판단
7. **사용자 경험**: REJECT 시 명확한 에러 메시지 제공
8. **성능 고려**: 실시간 검증이 너무 자주 호출되지 않도록 최적화 (예: 1초마다 또는 걸음수 변화량 기준)

---

## 🚀 예상 효과

1. **코드 가독성 향상**: ViewModel 코드 길이 감소, 검증 로직 분리
2. **유지보수성 향상**: 검증 로직 변경 시 Validator만 수정
3. **테스트 용이성**: Validator 단위 테스트 가능
4. **재사용성**: 다른 곳에서도 Validator 사용 가능
5. **실시간 모니터링**: 산책 중 어뷰징 패턴 실시간 감지
6. **정확한 판단**: 누적된 플래그로 최종 판단하여 오탐 감소
7. **사용자 경험**: 걸음수 차단 없이 검증만 수행하여 UX 유지

---

**이 계획에 따라 단계별로 구현하면 깔끔하게 리팩토링할 수 있습니다!** 🎯
