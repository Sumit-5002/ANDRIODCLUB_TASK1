# NotifyMe — Simple Notification Reminder App

> Android Club Technical Department — TASK 1 (Minor Task)

> Build a basic reminder app where users can set task titles and time/date; trigger a local notification at the scheduled time to remind the user.

## Requirement Checklist (covered)

- ✅ Task creation: title + optional description
- ✅ Date & time picker for reminder
- ✅ Local notifications at scheduled time (tap opens app)
- ✅ Task list in RecyclerView (edit/delete)
- ✅ Validation for empty title and past time; user feedback
- ✅ Persistence with Room (save, edit, delete; clear all)
- ✅ Light/Dark mode (system-based)
- ✅ Repeat reminders (daily or selected weekdays)
- ✅ Notification Channel (Android 8+)
- ✅ Tech stack: Kotlin, Room, RecyclerView, ViewModel+LiveData, AlarmManager, Material 3

---

NotifyMe is a lightweight, modern Android reminder app. Create tasks with title/description, pick a date/time, and receive local notifications on time — even with repeats (Daily or selected weekdays). Built with Kotlin, Room, ViewModel, LiveData, Material 3, and AlarmManager.

## Features

- Task list with create/edit/delete and multi-select deletion
- Date and time pickers with validation (future time, required title)
- Local notifications with channel, sound, vibration, tap-to-open
- Exact-time alarms (AlarmClock / exact idle), resilient to Doze
- Repeat reminders:
  - Daily (all days) or
  - Weekly by selected weekdays (M, T, W, Th, F, S, Su)
- Clear all tasks
- Light/Dark (system) theme
- Room persistence with ViewModel + LiveData

## Screens

- Main: RecyclerView of reminders with FAB to add
- Add/Edit: title, description, date, time, weekday chips (2 rows), Daily chip

## Tech Stack

- Language: Kotlin
- UI: Material 3, RecyclerView, ViewBinding, ConstraintLayout
- State: ViewModel + LiveData
- Storage: Room (Entity/DAO/DB)
- Scheduling: AlarmManager (AlarmClock, setExactAndAllowWhileIdle)
- Notifications: NotificationCompat + NotificationChannel (Android 8+)

## Project Structure

- `app/src/main/java/com/task_one/notifyme`
  - `MainActivity.kt` — list UI, permission prompts, actions
  - `NotifyMeApp.kt` — Application and notification channel
  - `data/` — `Task`, `TaskDao`, `NotifyMeDatabase`, `TaskRepository`
  - `notifications/` — `AlarmScheduler`, `ReminderReceiver`
  - `ui/` — `AddEditTaskDialog`, `TasksAdapter`, `TasksViewModel`
- `app/src/main/res/` — layouts, drawables, strings, themes, menu

## Requirements

- Android Studio Ladybug+ (AGP 8.11.x) and JDK 11
- Min SDK 30, Target SDK 36

## Build & Run

1) Sync and build
- Android Studio: Sync Gradle, then Run on device/emulator
- CLI (Windows):
```
./gradlew.bat assembleDebug
```

2) First run permissions
- Android 13+ (Tiramisu): Allow Notifications when prompted
- Android 12+ (S+): Allow Exact Alarms when prompted (Settings > Apps > Special app access > Alarms & reminders)
- Optional: Allow “Ignore battery optimizations” to avoid OEM throttling

3) Create a reminder
- Tap FAB, enter Title, optionally Description
- Choose Date and Time (future) and pick repeats:
  - Select Daily to enable all days, or
  - Pick specific weekdays across two rows (Mon–Thu, Fri–Sun)
- Save → a notification will fire at the scheduled time

## Notifications Reliability Tips

If notifications don’t appear on time:
- Ensure Notifications are enabled for the app
- Allow Exact Alarms (Android 12+)
- Disable battery optimization for this app (OEMs may delay alarms)
- Test with a reminder 1–2 minutes in the future while screen is off

## Customization

- App Icon: `res/drawable/appstore.png` is used as adaptive foreground
- Colors/Theme: Update `res/values/themes.xml` and `res/values/colors.xml`
- Strings: Localization-ready under `res/values/strings.xml`

## License

This project is provided as-is for educational purposes.
