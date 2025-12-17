package team.swyp.sdu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import team.swyp.sdu.data.local.dao.GoalDao
import team.swyp.sdu.data.remote.goal.GoalRemoteDataSource
import team.swyp.sdu.data.repository.GoalRepositoryImpl
import team.swyp.sdu.domain.repository.GoalRepository
import team.swyp.sdu.domain.repository.UserRepository

/**
 * 목표 관련 의존성 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object GoalModule {
    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        goalRemoteDataSource: GoalRemoteDataSource,
        userRepository: UserRepository,
    ): GoalRepository = GoalRepositoryImpl(goalDao, goalRemoteDataSource, userRepository)
}

