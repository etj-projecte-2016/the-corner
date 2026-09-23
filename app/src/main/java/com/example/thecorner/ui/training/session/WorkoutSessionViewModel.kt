package com.example.thecorner.ui.training.session

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.model.Workout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class WorkoutSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        (application as TheCornerApplication).appContainer.workoutRepository

    enum class SaveStatus { NOT_REQUIRED, SAVING, SAVED, FAILED }

    private var completedWorkout: Workout? = null

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
        val isPaused: Boolean = false,
        val saveStatus: SaveStatus = SaveStatus.NOT_REQUIRED
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

                completeWorkout()

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
                        completeWorkout()
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

        if (_state.value?.phase == Phase.FINISHED) return

        timer?.cancel()

        updateState(
            phase = Phase.FINISHED,
            remainingSeconds = 0,
            isPaused = false
        )
    }

    private fun completeWorkout() {
        val current = _state.value ?: return
        if (current.phase == Phase.FINISHED) return

        timer?.cancel()
        completedWorkout = Workout(
            id = 0,
            date = System.currentTimeMillis(),
            duration = current.totalRounds * current.roundDurationSeconds +
                (current.totalRounds - 1).coerceAtLeast(0) * current.restDurationSeconds,
            calories = 0, // No calorie estimate is available yet.
            totalRounds = current.totalRounds,
            bagRounds = 0, // Session configuration does not classify rounds.
            sparringRounds = 0,
            techniqueRounds = 0
        )
        _state.value = current.copy(
            phase = Phase.FINISHED,
            remainingSeconds = 0,
            isPaused = false,
            saveStatus = SaveStatus.SAVING
        )
        saveCompletedWorkout()
    }

    fun retrySave() {
        val current = _state.value ?: return
        if (current.saveStatus != SaveStatus.FAILED) return
        _state.value = current.copy(saveStatus = SaveStatus.SAVING)
        saveCompletedWorkout()
    }

    private fun saveCompletedWorkout() {
        val workout = completedWorkout ?: return
        // Enter the protected insert immediately, before navigation can clear this ViewModel.
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                // Let this short local write finish even if the user leaves the session.
                withContext(NonCancellable) {
                    repository.insertWorkout(workout)
                }
                _state.value = _state.value?.copy(saveStatus = SaveStatus.SAVED)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value?.copy(saveStatus = SaveStatus.FAILED)
            }
        }
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
