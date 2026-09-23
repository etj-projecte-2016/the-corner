package com.example.thecorner

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.training.TrainingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HistoryNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test fun completedTrainingAppearsInHistoryAndDetailsWithWorkingBackAndRotation() = runBlocking<Unit> {
        val context = instrumentation.targetContext
        val repository = (context.applicationContext as TheCornerApplication).appContainer.workoutRepository
        val previousIds = repository.getAllWorkouts().first().map { it.id }.toSet()
        var createdId: Long? = null
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            onView(withId(R.id.viewAllSessions)).perform(scrollTo(), click())
            if (previousIds.isEmpty()) {
                awaitView(scenario, R.id.message) { it.visibility == View.VISIBLE }
                onView(withId(R.id.messageTitle)).check(matches(withText(R.string.history_empty_title)))
                screenshot("history-empty")
                onView(withId(R.id.filterBag)).perform(click())
                onView(withId(R.id.messageTitle)).check(matches(withText(R.string.history_filter_empty_title)))
                onView(withId(R.id.filters)).check(matches(isDisplayed()))
            }
            onView(withId(R.id.back)).perform(click())
            onView(withId(R.id.trainingFragment)).perform(click())
            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val training = host.childFragmentManager.primaryNavigationFragment!!
                ViewModelProvider(training)[TrainingViewModel::class.java]
                    .setConfig(WorkoutConfig(30, 0, 1, WorkoutType.PAD_WORK))
            }
            onView(withId(R.id.btnStartWorkout)).perform(scrollTo(), click())
            val completed = withTimeout(45_000) {
                repository.getAllWorkouts().first { sessions -> sessions.any { it.id !in previousIds } }
                    .first { it.id !in previousIds }
            }
            createdId = completed.id
            assertEquals(WorkoutType.PAD_WORK, completed.workoutType)
            assertEquals(30, completed.duration)
            assertEquals(1, completed.totalRounds)
            awaitView(scenario, R.id.btnEndWorkout) { it.isEnabled }
            onView(withId(R.id.btnEndWorkout)).perform(click())
            onView(withId(R.id.homeFragment)).perform(click())
            onView(withId(R.id.cardLastSession)).perform(scrollTo(), click())
            awaitView(scenario, R.id.content) { it.visibility == View.VISIBLE }
            onView(withId(R.id.workoutType)).check(matches(withText("PAD WORK")))
            onView(withId(R.id.duration)).check(matches(withText(context.getString(R.string.history_minutes_seconds, 0, 30))))
            screenshot("session-details")
            scenario.onActivity { activity ->
                val nav = (activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
                assertEquals(completed.id, nav.currentBackStackEntry!!.arguments!!.getLong("sessionId"))
            }
            onView(withId(R.id.back)).perform(click())
            onView(withId(R.id.viewAllSessions)).perform(scrollTo(), click())
            awaitView(scenario, R.id.sessions) { it.visibility == View.VISIBLE }
            screenshot("training-history")
            onView(withId(R.id.filterPads)).perform(click())
            scenario.recreate()
            onView(withId(R.id.filterPads)).check(matches(isChecked()))
            awaitView(scenario, R.id.sessions) { it.visibility == View.VISIBLE }
            // The whole card opens details, including its image area.
            scenario.onActivity { activity ->
                val list = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.sessions)
                list.findViewHolderForAdapterPosition(1)!!.itemView.performClick()
            }
            awaitView(scenario, R.id.content) { it.visibility == View.VISIBLE }
            onView(withId(R.id.workoutType)).check(matches(withText("PAD WORK")))
            pressBack()
            onView(withId(R.id.filterPads)).check(matches(isChecked()))
            onView(withId(R.id.back)).perform(click())
            scenario.onActivity { activity ->
                val nav = (activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
                assertEquals(R.id.homeFragment, nav.currentDestination!!.id)
            }
        } finally {
            scenario.close()
            // Remove only the real session created by this test, preserving existing user history.
            createdId?.let { id ->
                context.openOrCreateDatabase("the_corner_database", 0, null).use { database ->
                    database.delete("workouts", "id = ?", arrayOf(id.toString()))
                }
            }
        }
    }

    private fun awaitView(scenario: ActivityScenario<MainActivity>, id: Int, predicate: (View) -> Boolean) {
        val deadline = android.os.SystemClock.uptimeMillis() + 5_000
        do {
            var ready = false
            scenario.onActivity { activity -> ready = activity.findViewById<View>(id)?.let(predicate) == true }
            if (ready) return
            android.os.SystemClock.sleep(50)
        } while (android.os.SystemClock.uptimeMillis() < deadline)
        fail("View $id did not reach the expected state")
    }

    private fun screenshot(name: String) {
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
