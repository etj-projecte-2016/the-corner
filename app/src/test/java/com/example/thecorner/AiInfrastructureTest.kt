package com.example.thecorner

import com.example.thecorner.data.local.WorkoutDao
import com.example.thecorner.data.local.WorkoutEntity
import com.example.thecorner.data.local.toEntity
import com.example.thecorner.data.repository.WorkoutRepository
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutAnalysis
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.ai.AIConfig
import com.example.thecorner.ui.ai.AIError
import com.example.thecorner.ui.ai.AIErrorMapper
import com.example.thecorner.ui.ai.AIRetryPolicy
import com.example.thecorner.ui.ai.AIResult
import com.example.thecorner.ui.ai.AIService
import com.example.thecorner.ui.ai.AiLogger
import com.example.thecorner.ui.ai.AiUiState
import com.example.thecorner.ui.ai.AiViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeoutException

class AiInfrastructureTest {
    @Test
    fun errorMapperCategorizesTransportErrors() {
        assertEquals(AIError.Timeout, AIErrorMapper.map(TimeoutException()))
        assertEquals(AIError.Network, AIErrorMapper.map(IOException()))
        assertEquals(AIError.Unknown, AIErrorMapper.map(IllegalStateException()))
    }

    @Test
    fun retryPolicyRetriesOnlyTransientErrors() = runBlocking {
        var attempts = 0
        val result = AIRetryPolicy.execute<String>(
            config = AIConfig(maxRetries = 2),
            operation = {
                attempts++
                if (attempts < 3) AIResult.Failure(AIError.ServiceUnavailable)
                else AIResult.Success("ok")
            },
        )

        assertEquals(AIResult.Success("ok"), result)
        assertEquals(3, attempts)
        assertTrue(AIRetryPolicy.isRetryable(AIError.Timeout))
        assertTrue(!AIRetryPolicy.isRetryable(AIError.RateLimited))
        assertTrue(!AIRetryPolicy.isRetryable(AIError.NotConfigured))

        attempts = 0
        val rateLimited = AIRetryPolicy.execute<String>(
            config = AIConfig(maxRetries = 2),
            operation = {
                attempts++
                AIResult.Failure(AIError.RateLimited)
            },
        )

        assertEquals(AIResult.Failure(AIError.RateLimited), rateLimited)
        assertEquals(1, attempts)

        attempts = 0
        val permanent = AIRetryPolicy.execute<String>(
            config = AIConfig(maxRetries = 2),
            operation = {
                attempts++
                AIResult.Failure(AIError.Authentication)
            },
        )

        assertEquals(AIResult.Failure(AIError.Authentication), permanent)
        assertEquals(1, attempts)
    }

    @Test
    fun retryPolicyUsesTwoSecondThenFiveSecondDelays() = runBlocking {
        var attempts = 0
        val delays = mutableListOf<Long>()

        val result = AIRetryPolicy.execute<String>(
            config = AIConfig(maxRetries = 2),
            operation = {
                attempts++
                if (attempts < 3) AIResult.Failure(AIError.ServiceUnavailable)
                else AIResult.Success("ok")
            },
            wait = { delayMs -> delays += delayMs },
        )

        assertEquals(AIResult.Success("ok"), result)
        assertEquals(3, attempts)
        assertEquals(listOf(2_000L, 5_000L), delays)
    }

    @Test
    fun retryPolicyDoesNotConvertCancellationIntoFailureOrRetry() {
        var attempts = 0

        try {
            runBlocking {
                AIRetryPolicy.execute<String>(
                    config = AIConfig(maxRetries = 2),
                    operation = {
                        attempts++
                        throw CancellationException("test cancellation")
                    },
                )
            }
        } catch (error: CancellationException) {
            assertEquals("test cancellation", error.message)
        }

        assertEquals(1, attempts)
    }

    @Test
    fun retryPolicyPreservesCancellationDuringRetryDelay() {
        var attempts = 0

        try {
            runBlocking {
                AIRetryPolicy.execute<String>(
                    config = AIConfig(maxRetries = 2),
                    operation = {
                        attempts++
                        AIResult.Failure(AIError.ServiceUnavailable)
                    },
                    wait = {
                        throw CancellationException("cancelled during retry delay")
                    },
                )
            }
        } catch (error: CancellationException) {
            assertEquals("cancelled during retry delay", error.message)
        }

        assertEquals(1, attempts)
    }

    @Test
    fun viewModelPublishesAnalysisSuccess() {
        val expected = analysis()
        val viewModel = viewModel(
            service = FakeAIService(
                analysisResponse = { AIResult.Success(expected) },
            ),
        )

        viewModel.analyzeLastSession()

        assertEquals(AiUiState.AnalysisSuccess(expected, workout()), viewModel.uiState.value)
    }

    @Test
    fun viewModelPublishesAiFailure() {
        val viewModel = viewModel(
            service = FakeAIService(
                analysisResponse = { AIResult.Failure(AIError.RateLimited) },
            ),
        )

        viewModel.analyzeLastSession()

        assertEquals(AiUiState.Error(AIError.RateLimited, workout()), viewModel.uiState.value)
    }

    @Test
    fun viewModelPublishesNotConfiguredStateWithoutLoadingWorkout() {
        val viewModel = viewModel(
            service = FakeAIService(isConfigured = false),
        )

        viewModel.onScreenVisible()
        assertEquals(AiUiState.NotConfigured, viewModel.uiState.value)

        viewModel.analyzeLastSession()
        assertEquals(AiUiState.NotConfigured, viewModel.uiState.value)
    }

    @Test
    fun viewModelPublishesNoWorkoutWithoutCallingAi() {
        var aiCalls = 0
        val viewModel = viewModel(
            latestWorkout = null,
            service = FakeAIService(
                analysisResponse = {
                    aiCalls++
                    AIResult.Success(analysis())
                },
            ),
        )

        viewModel.analyzeLastSession()

        assertEquals(AiUiState.NoWorkout, viewModel.uiState.value)
        assertEquals(0, aiCalls)
    }

    @Test
    fun viewModelPreventsDuplicateAnalysisRequests() {
        val response = CompletableDeferred<AIResult<WorkoutAnalysis>>()
        var calls = 0
        val viewModel = viewModel(
            service = FakeAIService(
                analysisResponse = {
                    calls++
                    response.await()
                },
            ),
        )

        viewModel.analyzeLastSession()
        viewModel.analyzeLastSession()

        assertEquals(AiUiState.Loading(workout()), viewModel.uiState.value)
        assertEquals(1, calls)

        response.complete(AIResult.Success(analysis()))
        assertTrue(viewModel.uiState.value is AiUiState.AnalysisSuccess)
    }

    @Test
    fun screenReentryReusesCachedSuccessForSameWorkoutWithoutCallingAi() {
        var calls = 0
        val viewModel = viewModel(
            latestWorkout = workout(id = 41L),
            service = FakeAIService(
                analysisResponse = {
                    calls++
                    AIResult.Success(analysis())
                },
            ),
        )

        viewModel.analyzeLastSession()
        viewModel.onScreenVisible()

        assertEquals(1, calls)
        assertEquals(
            AiUiState.AnalysisSuccess(analysis(), workout(id = 41L)),
            viewModel.uiState.value,
        )
    }

    @Test
    fun screenReentryInvalidatesCachedSuccessForNewLatestWorkoutWithoutCallingAi() {
        var calls = 0
        val dao = FakeWorkoutDao(workout(id = 41L).toEntity())
        val viewModel = AiViewModel(
            service = FakeAIService(
                analysisResponse = {
                    calls++
                    AIResult.Success(analysis())
                },
            ),
            workoutRepository = WorkoutRepository(dao),
            requestScope = CoroutineScope(Dispatchers.Unconfined),
            logger = NoOpLogger,
        )

        viewModel.analyzeLastSession()
        dao.replaceLatest(workout(id = 42L).toEntity())
        viewModel.onScreenVisible()

        assertEquals(1, calls)
        assertEquals(AiUiState.Ready(workout(id = 42L)), viewModel.uiState.value)
    }

    @Test
    fun screenVisibilityLoadsReadyStateWithoutCallingAi() {
        var calls = 0
        val viewModel = viewModel(
            latestWorkout = workout(id = 51L),
            service = FakeAIService(
                analysisResponse = {
                    calls++
                    AIResult.Success(analysis())
                },
            ),
        )

        viewModel.onScreenVisible()

        assertEquals(AiUiState.Ready(workout(id = 51L)), viewModel.uiState.value)
        assertEquals(0, calls)
    }

    private fun viewModel(
        latestWorkout: Workout? = workout(),
        service: FakeAIService,
    ): AiViewModel = AiViewModel(
        service = service,
        workoutRepository = WorkoutRepository(FakeWorkoutDao(latestWorkout?.toEntity())),
        requestScope = CoroutineScope(Dispatchers.Unconfined),
        logger = NoOpLogger,
    )

    private class FakeAIService(
        override val isConfigured: Boolean = true,
        private val analysisResponse: suspend (String) -> AIResult<WorkoutAnalysis> = {
            AIResult.Failure(AIError.Unknown)
        },
    ) : AIService {
        override suspend fun analyzeLastSession(prompt: String): AIResult<WorkoutAnalysis> =
            analysisResponse(prompt)
    }

    private class FakeWorkoutDao(
        private var latestWorkout: WorkoutEntity?,
    ) : WorkoutDao {
        fun replaceLatest(workout: WorkoutEntity) {
            latestWorkout = workout
        }

        override suspend fun insertWorkout(workout: WorkoutEntity) = Unit
        override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override fun getLastWorkout(): Flow<WorkoutEntity?> = flowOf(latestWorkout)
        override fun getWorkoutById(id: Long): Flow<WorkoutEntity?> = flowOf(null)
        override suspend fun deleteWorkoutsById(ids: List<Long>) = Unit
    }

    private object NoOpLogger : AiLogger {
        override fun debug(message: String) = Unit
        override fun warning(message: String) = Unit
    }

    private fun workout(id: Long = 0L): Workout = Workout.completed(
        WorkoutConfig(180, 60, 8, WorkoutType.BAG_WORK),
        100L,
    ).copy(id = id)

    private fun analysis() = WorkoutAnalysis(
        headline = "Structured session review",
        summary = "The session included eight bag rounds.",
        positives = listOf("Completed the planned rounds."),
        improvements = listOf("Consider varying round structure."),
        nextSessionFocus = "Keep a consistent round plan.",
    )
}
