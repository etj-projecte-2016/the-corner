package com.example.thecorner.ui.ai

import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.ContentBlockedException
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.InvalidLocationException
import com.google.firebase.ai.type.PermissionMissingException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceConnectionHandshakeFailedException
import com.google.firebase.ai.type.ServiceDisabledException
import com.google.firebase.ai.type.UnsupportedUserLocationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

object AIErrorMapper {
    fun map(throwable: Throwable): AIError = when (throwable) {
        is RequestTimeoutException,
        is SocketTimeoutException,
        is TimeoutException -> AIError.Timeout

        is QuotaExceededException -> AIError.RateLimited
        is ServerException -> AIError.ServiceUnavailable

        is ServiceConnectionHandshakeFailedException,
        is IOException -> AIError.Network

        is APINotConfiguredException,
        is InvalidAPIKeyException,
        is InvalidLocationException,
        is PermissionMissingException,
        is ServiceDisabledException,
        is UnsupportedUserLocationException -> AIError.Authentication

        is ContentBlockedException,
        is PromptBlockedException -> AIError.Safety

        is FirebaseAIException -> when {
            throwable.message?.contains("high demand", ignoreCase = true) == true ->
                AIError.ServiceUnavailable
            throwable.message?.contains("429", ignoreCase = true) == true -> AIError.RateLimited
            throwable.message?.contains("503", ignoreCase = true) == true ->
                AIError.ServiceUnavailable
            else -> AIError.Unknown
        }
        else -> AIError.Unknown
    }
}
