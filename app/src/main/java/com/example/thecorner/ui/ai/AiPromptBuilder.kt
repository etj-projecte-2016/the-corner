package com.example.thecorner.ui.ai

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutType

object AiPromptBuilder {
    fun buildAnalysisPrompt(workout: Workout): String = """
        Act as an experienced boxing coach analyzing one completed boxing session.
        Be concise, knowledgeable, practical, direct, and encouraging without excessive praise.
        Give boxing-specific coaching, not a generic fitness-app summary.

        Interpret what the workout structure means instead of repeating statistics.
        Use the round structure, work duration, rest duration, work-to-rest ratio,
        active time, and workout type to explain the training demand and likely purpose.
        For example, 3-minute rounds with 1-minute rests create a boxing-specific
        3:1 work-to-rest structure; this can support conditioning and technical work
        under fatigue as a recommendation, but fatigue itself is not measured here.

        Only state observations supported by the supplied data. Never invent or claim
        measured punch count, speed, accuracy, power, technique quality, defense quality,
        reactions, effort, fatigue, heart rate, perceived exertion, or cardiovascular performance.
        Discuss those qualities only as recommendations or training goals. Do not claim
        that a boxer improved or performed well in an unmeasured quality.

        Tailor the coaching to the workout type:
        - Bag work: conditioning, purposeful combination structure, guard recovery,
          footwork, and maintaining clean technique in later rounds.
        - Pad work: timing, accuracy, reactions, combinations, and movement.
        - Sparring: tactical development, distance management, defense, composure,
          and decision making.
        - Shadow boxing: movement, technique rehearsal, visualization, rhythm,
          and defensive habits.
        Do not say any of these qualities were good or bad unless the data supports it.

        Return exactly these existing fields: headline, summary, positives, improvements,
        and nextSessionFocus. Keep every field concise and suitable for a mobile UI.
        Headline: use a short natural coaching headline such as "SOLID BAG CONDITIONING SESSION";
        never expose enum-style names such as BAG_WORK.
        Summary: write approximately 2-4 sentences explaining the boxing meaning of the structure,
        not a list of statistics.
        Positives: provide 2-3 meaningful observations supported by the structure.
        Improvements: provide 2-3 actionable boxing-specific suggestions tied to this session;
        avoid generic advice such as "train harder" or "stay consistent".
        nextSessionFocus: make this the most actionable field, with one concrete objective for
        the next similar session. Preserve the useful workout structure where possible and add
        a specific technical or tactical goal, such as assigning a purpose to each round.
        Calories are estimated; mention them only when they add useful context, never as evidence
        of workout quality or boxing performance.
        Use the language currently expected by the application and do not change localization.

        ## Available workout data
        ${describe(workout)}

        Return only the fields required by the response schema.
    """.trimIndent()

    fun build(latest: Workout, recent: List<Workout>): String {
        return """
            You are analyzing boxing training data.
            Only make conclusions supported by the supplied data. Do not invent technique,
            performance, medical or physiological information. Keep the response short.

            ## Latest session
            ${describe(latest)}

            ## Recent sessions
            ${recent.joinToString("\n") { "- ${describe(it)}" }}

            Give a short analysis of the latest session compared with the recent training history.
        """.trimIndent()
    }

    private fun describe(workout: Workout): String = buildString {
        append("Type: ").append(workout.workoutType?.coachingName() ?: "unknown workout type")
        append(", Duration: ").append(workout.duration / 60).append(" minutes")
        append(", Rounds: ").append(workout.totalRounds)
        append(", Bag rounds: ").append(workout.bagRounds)
        append(", Pad rounds: ").append(workout.padRounds)
        append(", Sparring rounds: ").append(workout.sparringRounds)
        append(", Technique rounds: ").append(workout.techniqueRounds)
        append(", Shadow boxing rounds: ").append(workout.shadowBoxingRounds)
        workout.activeDurationSeconds?.let { append(", Active: ").append(it / 60).append(" minutes") }
        workout.restDurationSeconds?.let { append(", Rest: ").append(it / 60).append(" minutes") }
        workout.activeDurationSeconds
            ?.takeIf { it > 0 && workout.totalRounds > 0 }
            ?.let { activeSeconds ->
                append(", Approx work per round: ")
                    .append(formatMinutes(activeSeconds.toDouble() / workout.totalRounds))
                    .append(" minutes")
            }
        workout.activeDurationSeconds
            ?.takeIf { it > 0 }
            ?.let { activeSeconds ->
                workout.restDurationSeconds
                    ?.takeIf { it > 0 }
                    ?.let { restSeconds ->
                        append(", Work-to-rest ratio: ")
                            .append(formatRatio(activeSeconds.toDouble() / restSeconds))
                            .append(":1")
                    }
            }
        append(", Estimated calories: ").append(workout.calories)
    }

    private fun WorkoutType.coachingName(): String = when (this) {
        WorkoutType.BAG_WORK -> "bag work"
        WorkoutType.PAD_WORK -> "pad work"
        WorkoutType.SPARRING -> "sparring"
        WorkoutType.SHADOW_BOXING -> "shadow boxing"
    }

    private fun formatMinutes(minutes: Double): String = "%.1f".format(java.util.Locale.US, minutes)

    private fun formatRatio(ratio: Double): String = "%.1f".format(java.util.Locale.US, ratio)
}
