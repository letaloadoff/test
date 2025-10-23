### AlarmSetter Android App

A minimal Android app to set an alarm using the system Clock via `AlarmClock.ACTION_SET_ALARM`.

#### Build and Run
- Open this folder in Android Studio.
- Ensure Android Gradle Plugin 8.4+ and Kotlin 1.9.24 are installed.
- Let Gradle sync.
- Connect a device or start an emulator.
- Run the `app` configuration.

#### Usage
- Pick a time.
- Optionally enter a label, toggle vibrate, and choose to skip the Clock UI.
- Tap Set Alarm.

Notes:
- `EXTRA_SKIP_UI` may be ignored by some OEM Clock apps.
- No runtime permission prompts are needed.