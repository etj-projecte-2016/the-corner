package com.example.thecorner

import com.example.thecorner.ui.ai.AIConfig
import com.example.thecorner.ui.ai.AIError
import com.example.thecorner.ui.ai.AIErrorMapper
import com.example.thecorner.ui.ai.AIRetryPolicy
import com.example.thecorner.ui.ai.AIResult
import com.example.thecorner.ui.ai.AIService
import com.example.thecorner.ui.ai.AiLogger
import com.example.thecorner.ui.ai.AiUiState
import com.example.thecorner.ui.ai.AiViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val result = AIRetryPolicy.execute(
            config = AIConfig(maxRetries = 2, initialBackoff = 0),
            operation = {
                attempts++
                if (attempts < 3) AIResult.Failure(AIError.ServiceUnavailable)
                else AIResult.Success("ok")
            },
        )

        assertEquals(AIResult.Success("ok"), result)
        assertEquals(3, attempts)

        attempts = 0
        val permanent = AIRetryPolicy.execute(
            config = AIConfig(maxRetries = 2, initialBackoff = 0),
            operation = {
                attempts++
                AIResult.Failure(AIError.Authentication)
            },
        )

        assertEquals(AIResult.Failure(AIError.Authentication), permanent)
        assertEquals(1, attempts)
    }

    @Test
    fun retryPolicyDoesNotConvertCancellationIntoFailureOrRetry() {
        var attempts = 0

        try {
            runBlocking {
                AIRetryPolicy.execute<String>(
                    config = AIConfig(maxRetries = 2, initialBackoff = 0),
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
    fun viewModelPublishesSuccessAndKeepsConnectivityResponse() {
        val viewModel = AiViewModel(
            service = FakeAIService { AIResult.Success("THE CORNER AI CONNECTED") },
            requestScope = CoroutineScope(Dispatchers.Unconfined),
            logger = NoOpLogger,
        )

        viewModel.runConnectivityTest()

        assertEquals(AiUiState.Success("THE CORNER AI CONNECTED"), viewModel.uiState.value)
    }

    @Test
    fun viewModelPublishesFailureAsDomainError() {
        val viewModel = AiViewModel(
            service = FakeAIService { AIResult.Failure(AIError.RateLimited) },
            requestScope = CoroutineScope(Dispatchers.Unconfined),
            logger = NoOpLogger,
        )

        viewModel.runConnectivityTest()

        assertEquals(AiUiState.Error(AIError.RateLimited), viewModel.uiState.value)
    }

    @Test
    fun viewModelIgnoresDuplicateRequestWhileLoading() {
        val response = CompletableDeferred<AIResult<String>>()
        var calls = 0
        val viewModel = AiViewModel(
            service = FakeAIService {
                calls++
                response.await()
            },
            requestScope = CoroutineScope(Dispatchers.Unconfined),
            logger = NoOpLogger,
        )

        viewModel.runConnectivityTest()
        viewModel.runConnectivityTest()

        assertEquals(AiUiState.Loading, viewModel.uiState.value)
        assertEquals(1, calls)

        response.complete(AIResult.Success("THE CORNER AI CONNECTED"))
        assertTrue(viewModel.uiState.value is AiUiState.Success)
    }

    private class FakeAIService(
        private val response: suspend () -> AIResult<String>,
    ) : AIService {
        override suspend fun runConnectivityTest(): AIResult<String> = response()
    }

    private object NoOpLogger : AiLogger {
        override fun debug(message: String) = Unit
        override fun warning(message: String) = Unit
    }
}
