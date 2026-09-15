# Lornshill Timetable — Android Companion App

A modern, school timetable companion application built with **Kotlin** and **Jetpack Compose (Material 3)**, designed to work alongside **Lornshill.com**.

## Features

- **Standardised 31-Period Bell Schedule**:
  - Exactly 31 fixed slots across the week (7 on Monday, 6 on Tue–Fri).
  - Users can edit period details (Subject, Teacher, Room, or "Set Free Period"), but time slots and period counts are locked to school bell times.
  - **Monday**: 7 periods (09:00–15:45) with Interval (10:45–11:00) and Lunch (12:40–13:15).
  - **Tuesday & Thursday**: 6 periods (09:00–14:55).
  - **Wednesday**: 6 periods (08:55–14:40) with shifted morning times, Interval (10:35–10:50), and 30-min Lunch (12:30–13:00).
  - **Friday**: 6 periods ending at 15:00 (Period 6: 14:05–15:00).
- **Google Sheets Faculty & Teacher Directory**:
  - Live sync from Lornshill Academy Google Sheet: `https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/edit?usp=sharing`.
  - Built-in 53-teacher roster across 7 faculties (Creative Arts, Design & Technology, Languages, Maths, P.E, Science, Social Subjects).
  - **1-Tap "Sync Now from Google Sheet"** button in Settings with live progress indicator.
  - **Auto-sync on Launch** switch that silently fetches faculty updates on app startup.
  - Searchable faculty directory browser.
- **Intuitive Home Screen**:
  - **Live Next Period / NOW Component**: Displays current active lesson or countdown to next period (*"Starts in 18 minutes"*, *"Starts in 5 minutes"*, *"Starting now"*).
  - **Today's Schedule**: Full day view with Interval and Lunch divider rows and clear distinction between assigned classes and Free / Study Periods.
  - **Morning Daily Brief Card**: Active between 07:30 and 08:55 AM (or previewed on demand) reporting P.E. kit requirements, core Maths & English, and doubles.
- **Lunch & Dining Hub**:
  - Official school bell timetable display for Intervals and Lunches.
  - Coming soon preview for school meal menus.
- **Settings & Developer Panel**:
  - Student Profile configuration (Name, S1–S6 year group, Devon/Forebraes/Grange house).
  - Passcode-protected Developer Panel (`dev1`) with time jumping simulation (07:45, 08:42, 09:25, Interval, Lunch, etc.) and push notification testing.

## Project Structure

```
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/simplebutton/
│   │   │   ├── MainActivity.kt               <-- Navigation, notification handler & edge-to-edge
│   │   │   ├── model/
│   │   │   │   ├── TimetablePeriod.kt        <-- Period data model & countdown status
│   │   │   │   ├── TimetableRepository.kt    <-- State, Scottish presets & time simulation
│   │   │   │   ├── FacultyDatabase.kt        <-- Dynamic faculty roster & persistent state
│   │   │   │   └── GoogleSheetsFacultyReader.kt <-- Google Sheets CSV parser & network sync
│   │   │   ├── ui/
│   │   │   │   ├── components/
│   │   │   │   │   ├── NextPeriodCard.kt     <-- Next period / NOW card with alive countdown
│   │   │   │   │   ├── PeriodRowItem.kt      <-- Lesson row item with active badges
│   │   │   │   │   ├── DaySelector.kt        <-- Mon-Fri switcher
│   │   │   │   │   └── EmptyTimetableState.kt<-- Polished empty state
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt         <-- Main dashboard
│   │   │   │   │   ├── TimetableScreen.kt    <-- Weekly schedule
│   │   │   │   │   ├── EditPeriodDialog.kt   <-- Period editor dialog
│   │   │   │   │   ├── LunchScreen.kt        <-- Coming soon lunch menus
│   │   │   │   │   ├── SettingsScreen.kt     <-- Settings & dev code prompt
│   │   │   │   │   ├── DevPanelScreen.kt     <-- QA testing & time simulator
│   │   │   │   │   └── NotificationDialog.kt <-- In-app dialog for notification tap
│   │   │   │   └── theme/                    <-- Lornshill navy & blue design palette
│   │   └── res/values/                       <-- strings.xml, themes.xml
│   └── build.gradle.kts
├── preview/
│   └── index.html                            <-- Instant interactive PC browser preview
├── LornshillTimetable.apk                    <-- Built standalone debug APK
├── TESTING_GUIDE.md
└── package.json
```

## How to Test on PC (Instant, Zero Setup)

1. Double-click `preview/index.html` or run:
   ```powershell
   npm run preview
   ```
2. You can also build or run the native Android app using:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
   The generated APK is located at `app/build/outputs/apk/debug/app-debug.apk` and copied to `LornshillTimetable.apk`.

