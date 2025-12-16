✅ Cursor AI 리팩토링 마스터 프롬프트

목표
WalkingViewModel에서 센서 판단/보정/계산 로직을 분리하고
ViewModel은 UI State 관리만 담당하도록 리팩토링한다.

📌 1️⃣ 리팩토링 최종 아키텍처 (절대 변경 금지)
presentation
 └─ WalkingViewModel

domain
 ├─ validator
 │    └─ StepCountValidator
 │    └─ DefaultStepCountValidator
 │
 ├─ movement
 │    └─ MovementStateStabilizer
 │
 ├─ estimator
 │    └─ StepEstimator
 │
 ├─ calculator
 │    └─ DistanceCalculator
 │
 └─ model
      └─ StepValidationInput
      └─ StepValidationResult


⚠️ ViewModel 내부에 센서 해석 로직, 판단 if문, 계산식 절대 남기지 말 것

📌 2️⃣ StepCountValidator 책임
역할

걷기 / 러닝만 걸음수 인정

제자리 걷기, 폰 흔들기, 차량 이동 차단

인터페이스
interface StepCountValidator {
    fun validate(input: StepValidationInput): StepValidationResult
}

판별 규칙 (하드코딩 OK)

ActivityType ≠ WALKING/RUNNING → reject

MovementState ≠ WALKING/RUNNING → reject

GPS 이동 < 1.5m + acceleration > 2.5 → PHONE_SHAKE

GPS speed > 3.5m/s + stepDelta == 0 → VEHICLE

통과 시 stepDelta 반환

📌 3️⃣ MovementStateStabilizer (스무딩 전담)
책임

MovementState 변경 시 n초 이상 유지되어야만 상태 확정

ViewModel에서 pending / stable 상태 제거

class MovementStateStabilizer(
    private val stableDurationMs: Long = 3000L
) {
    fun update(
        detectedState: MovementState,
        timestamp: Long
    ): MovementState
}


✔ 내부에서 pending / lastChangeTime 관리
✔ ViewModel에는 확정된 상태만 반환

📌 4️⃣ StepEstimator (보간 전담)
책임

실제 step sensor 업데이트 전까지 가속도 기반 추정

실제 값이 오면 점진적 수렴

class StepEstimator {

    fun onRealStepUpdated(realStepCount: Int, timestamp: Long)

    fun estimate(
        movementState: MovementState,
        acceleration: Float,
        timestamp: Long
    ): Int
}


규칙:

WALKING: 1.5 ~ 2.5 step/s

RUNNING: 2.5 ~ 4.0 step/s

최대 보간 선행: +10 steps

📌 5️⃣ DistanceCalculator (거리 계산 전담)
책임

GPS / Step Counter 하이브리드 거리 계산

평균 보폭 관리

class DistanceCalculator {

    fun calculateTotalDistance(
        locations: List<LocationPoint>,
        stepCount: Int
    ): Float

    fun calculateSpeed(
        locations: List<LocationPoint>
    ): Float
}


✔ ViewModel에서 Haversine, accuracy 판단 로직 완전 제거

📌 6️⃣ ViewModel 리팩토링 지침 (가장 중요)
ViewModel에는 아래만 남길 것

StateFlow 관리

UseCase/Service 호출

UI 상태 업데이트

lifecycle 관리

❌ 제거 대상

if (gpsDistance < …)

acceleration threshold 계산

step 보간 수식

평균 보폭 계산

movement pending/stable 상태 변수

📌 7️⃣ 의존성 주입

위 모든 클래스는 @Inject constructor() 사용

Hilt Module 생성

ViewModel 생성자에서 주입

📌 8️⃣ 리팩토링 완료 조건 (검증 체크리스트)

 WalkingViewModel 파일 길이 40% 이상 감소

 센서 기반 if/else 로직 0개

 StepCountValidator 단위 테스트 가능

 MovementStateStabilizer는 독립 테스트 가능

 기능 동작 동일 (걸음/러닝만 카운트)

📌 9️⃣ 추가 요구사항

기존 public API / UI State 변경 금지

동작 결과는 현재와 동일

로직 이동만 수행

TODO 남기지 말 것