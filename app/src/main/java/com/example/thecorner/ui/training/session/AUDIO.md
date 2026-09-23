# Workout timer audio

Add these optional local resources under `app/src/main/res/raw/`:

- `boxing_countdown_beep.mp3`: the complete countdown audio, played once.
- `boxing_round_start.mp3`: a single clip containing three boxing-ring bell strikes,
  with approximately 250–350 ms between strikes and a short natural decay.

WAV or OGG files with the same resource names also work. Supply only one format
per name. Use short SoundPool-compatible clips; no network loading is used.

The countdown clip plays once when preparation begins, without synchronizing
individual sounds to the displayed numbers.
The bell plays when each work round naturally ends, including the last round
and rounds resumed after a pause. It does not play at round start, rest end,
or manual workout cancellation. The resource retains its `boxing_round_start` name.
No sound is emitted from UI rendering or replayed when the screen becomes visible.

The ViewModel owns one SoundPool using application context. Initial countdown
waits for loading (at most 1.5 seconds) and a visible screen. Missing resources
are skipped, and the timer still works. Leaving the screen stops playback;
clearing the session ViewModel releases the pool. Configuration changes reuse
the same player and never restart the countdown or replay past cues.
