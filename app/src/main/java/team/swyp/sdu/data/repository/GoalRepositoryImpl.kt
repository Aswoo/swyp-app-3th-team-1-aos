package team.swyp.sdu.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import team.swyp.sdu.core.Result
import team.swyp.sdu.data.local.dao.GoalDao
import team.swyp.sdu.data.local.mapper.GoalMapper
import team.swyp.sdu.data.remote.goal.GoalRemoteDataSource
import team.swyp.sdu.domain.model.Goal
import team.swyp.sdu.domain.repository.GoalRepository
import timber.log.Timber

/**
 * 목표 정보 Repository 구현체
 *
 * - 메모리(StateFlow) + Room + Remote 병합
 */
@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val remoteDataSource: GoalRemoteDataSource,
    private val userRepository: team.swyp.sdu.domain.repository.UserRepository,
) : GoalRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val goalState = MutableStateFlow<Goal?>(null)
    override val goalFlow: StateFlow<Goal?> = goalState.asStateFlow()

    init {
        // 사용자 ID가 변경될 때마다 해당 사용자의 Goal을 observe
        userRepository.userFlow
            .onEach { user ->
                user?.let {
                    goalDao.observeGoal(it.userId)
                        .onEach { entity ->
                            goalState.value = entity?.let { GoalMapper.toDomain(it) }
                        }
                        .launchIn(scope)
                } ?: run {
                    goalState.value = null
                }
            }
            .launchIn(scope)
    }

    override suspend fun getGoal(): Result<Goal> =
        withContext(Dispatchers.IO) {
            try {
                // 현재 사용자 정보 가져오기
                val currentUser = userRepository.userFlow.value
                if (currentUser == null) {
                    return@withContext Result.Error(Exception("사용자 정보가 없습니다"))
                }

                val entity = goalDao.getGoalByUserId(currentUser.userId)
                if (entity != null) {
                    val goal = GoalMapper.toDomain(entity)
                    goalState.value = goal
                    Result.Success(goal)
                } else {
                    // 로컬에 없으면 서버에서 가져오기
                    refreshGoal()
                }
            } catch (e: Exception) {
                Timber.e(e, "목표 조회 실패")
                Result.Error(e, e.message)
            }
        }

    override suspend fun updateGoal(userId: Long, goal: Goal): Result<Goal> =
        withContext(Dispatchers.IO) {
            try {
                // 서버에 업데이트
                val updatedGoal = remoteDataSource.updateGoal(goal)
                
                // 로컬에 저장
                goalDao.upsert(GoalMapper.toEntity(updatedGoal, userId))
                goalState.value = updatedGoal
                
                Result.Success(updatedGoal)
            } catch (e: Exception) {
                Timber.e(e, "목표 업데이트 실패")
                Result.Error(e, e.message)
            }
        }

    override suspend fun refreshGoal(): Result<Goal> =
        withContext(Dispatchers.IO) {
            try {
                // 현재 사용자 정보 가져오기
                val currentUser = userRepository.userFlow.value
                if (currentUser == null) {
                    return@withContext Result.Error(Exception("사용자 정보가 없습니다"))
                }

                val goal = remoteDataSource.fetchGoal()
                goalDao.upsert(GoalMapper.toEntity(goal, currentUser.userId))
                goalState.value = goal
                Result.Success(goal)
            } catch (e: Exception) {
                Timber.e(e, "목표 갱신 실패")
                Result.Error(e, e.message)
            }
        }

    override suspend fun clearGoal(userId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                goalDao.deleteByUserId(userId)
                goalState.value = null
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
}

