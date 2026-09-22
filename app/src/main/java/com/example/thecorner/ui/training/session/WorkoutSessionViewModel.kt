package com.example.thecorner.ui.training.session

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.ceil

class WorkoutSessionViewModel : ViewModel() {

    enum class Phase {
        PREPARING,
        FIGHT,
        ROUND,
        REST,
        FINISHED
    }

    data class SessionState(
        val phase: Phase = Phase.PREPARING,
        val currentRound: Int = 1,
        val totalRounds: Int = 10,
        val remainingSeconds: Int = 3,
        val roundDurationSeconds: Int = 180,
        val restDurationSeconds: Int = 60,
        val isPaused: Boolean = false
    )

    private val _state = MutableLiveData(SessionState())
    val state: LiveData<SessionState> = _state

    private var timer: CountDownTimer? = null

    private var remainingMillis = 0L

    private var configured = false


    fun configure(
        roundDurationSeconds: Int,
        restDurationSeconds: Int,
        totalRounds: Int
    ) {

        if (configured) return

        configured = true

        _state.value = SessionState(
            phase = Phase.PREPARING,
            currentRound = 1,
            totalRounds = totalRounds,
            remainingSeconds = 3,
            roundDurationSeconds = roundDurationSeconds,
            restDurationSeconds = restDurationSeconds
        )

        startPreparing()
    }


    // ============================================================
    // PREPARING
    // ============================================================

    private fun startPreparing() {

        updateState(
            phase = Phase.PREPARING,
            remainingSeconds = 3,
            isPaused = false
        )

        startTimer(
            durationMillis = 3000L
        ) {
            startFight()
        }
    }


    // ============================================================
    // FIGHT
    // ============================================================

    private fun startFight() {

        timer?.cancel()

        updateState(
            phase = Phase.FIGHT,
            remainingSeconds = 0,
            isPaused = false
        )

        startTimer(
            durationMillis = 700L
        ) {
            startRound()
        }
    }


    // ============================================================
    // ROUND
    // ============================================================

    private fun startRound() {

        val current = _state.value ?: return

        updateState(
            phase = Phase.ROUND,
            remainingSeconds = current.roundDurationSeconds,
            isPaused = false
        )

        startTimer(
            durationMillis =
                current.roundDurationSeconds * 1000L
        ) {

            if (current.currentRound >= current.totalRounds) {

                finishWorkout()

            } else {

                if (current.restDurationSeconds > 0) {
                    startRest()
                } else {
                    startNextRound()
                }
            }
        }
    }


    // ============================================================
    // REST
    // ============================================================

    private fun startRest() {

        val current = _state.value ?: return

        updateState(
            phase = Phase.REST,
            remainingSeconds = current.restDurationSeconds,
            isPaused = false
        )

        startTimer(
            durationMillis =
                current.restDurationSeconds * 1000L
        ) {
            startNextRound()
        }
    }


    private fun startNextRound() {

        val current = _state.value ?: return

        _state.value = current.copy(
            currentRound = current.currentRound + 1
        )

        startRound()
    }


    // ============================================================
    // TIMER
    // ============================================================

    private fun startTimer(
        durationMillis: Long,
        onFinish: () -> Unit
    ) {

        timer?.cancel()

        remainingMillis = durationMillis

        timer = object : CountDownTimer(
            durationMillis,
            100L
        ) {

            override fun onTick(millisUntilFinished: Long) {

                remainingMillis = millisUntilFinished

                val seconds = ceil(
                    millisUntilFinished / 1000.0
                ).toInt()

                val current = _state.value ?: return

                _state.value = current.copy(
                    remainingSeconds = seconds
                )
            }


            override fun onFinish() {

                remainingMillis = 0

                onFinish()
            }
        }.start()
    }


    // ============================================================
    // PAUSE
    // ============================================================

    fun togglePause() {

        val current = _state.value ?: return

        if (
            current.phase != Phase.ROUND &&
            current.phase != Phase.REST
        ) {
            return
        }

        if (current.isPaused) {

            resume()

        } else {

            pause()
        }
    }


    private fun pause() {

        timer?.cancel()

        val current = _state.value ?: return

        _state.value = current.copy(
            isPaused = true
        )
    }


    private fun resume() {

        val current = _state.value ?: return

        _state.value = current.copy(
            isPaused = false
        )

        when (current.phase) {

            Phase.ROUND -> {

                startTimer(
                    durationMillis = remainingMillis
                ) {

                    val latest = _state.value ?: return@startTimer

                    if (
                        latest.currentRound >=
                        latest.totalRounds
                    ) {
                        finishWorkout()
                    } else {

                        if (latest.restDurationSeconds > 0) {
                            startRest()
                        } else {
                            startNextRound()
                        }
                    }
                }
            }


            Phase.REST -> {

                startTimer(
                    durationMillis = remainingMillis
                ) {
                    startNextRound()
                }
            }


            else -> Unit
        }
    }


    // ============================================================
    // FINISH
    // ============================================================

    fun finishWorkout() {

        timer?.cancel()

        updateState(
            phase = Phase.FINISHED,
            remainingSeconds = 0,
            isPaused = false
        )
    }


    // ============================================================
    // UPDATE
    // ============================================================

    private fun updateState(
        phase: Phase,
        remainingSeconds: Int,
        isPaused: Boolean
    ) {

        val current = _state.value ?: return

        _state.value = current.copy(
            phase = phase,
            remainingSeconds = remainingSeconds,
            isPaused = isPaused
        )
    }


    override fun onCleared() {

        timer?.cancel()

        super.onCleared()
    }
}