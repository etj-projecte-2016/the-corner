package com.example.thecorner.ui.ai

import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

object AIErrorMapper {
    fun map(throwable: Throwable): AIError = when (throwable) {
        is SocketTimeoutException,
        is TimeoutException -> AIError.Timeout

        is IOException -> AIError.Network
        else -> AIError.Unknown
    }
}
