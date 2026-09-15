# Lornshill Timetable — Android Companion App

A modern, school timetable companion application built with **Kotlin** and **Jetpack Compose (Material 3)**, designed to work alongside **Lornshill.com**.

The app is distributed as a standalone APK (without Google Play Services or Play Store dependencies) and includes a remote configuration system for seamless over-the-air APK updates, school announcements, and automated GitHub CI/CD releases.

---

## Key Features

### 1. Standalone APK Update System (Zero Google Play Required)
- **Direct Remote Configuration**: The app checks a hosted JSON file (`app-config.json`) for newer releases.
- **Weekly Cooldown Guard (7 Days)**: If a user dismisses an update dialog for version X, they won't be nagged again for 7 days.
- **Immediate Version Bypass**: If a newer version Y is released (higher `latestVersionCode`), the cooldown is immediately bypassed so critical updates are never missed.
- **Standard Browser Download**: Tapping **"Update Now"** triggers an Android `ACTION_VIEW` intent directing the user to download the APK directly in their browser or download manager.
- **What's New Display**: Neatly formats release notes in a scrollable, stylized Material 3 dialog with Lornshill typography and colors.

### 2. Remote In-App Notifications & School Notices
- **Unlimited Text Length**: Auto-wrapping, scrollable container constrained within safe screen bounds — notices of any length render cleanly without truncation or clipping.
- **Optional Action Buttons**: Configurable external links (e.g. school project pages, forms, newsletters) with customizable button labels.
- **Optional Expiry Dates**: Supports ISO-8601 timestamps (e.g. `2027-01-01T00:00:00Z` or `2027-01-01`). Expired notices are automatically hidden.
- **Persistent Dismissal**: Notices dismissed with the "Close" or action button are saved locally in `SharedPreferences` and never shown again.

### 3. Developer Panel QA Tools (`dev1`)
- Protected behind passcode **`dev1`** in the Settings tab.
- **Live Diagnostics**: Displays installed version code/name, remote fetched version, last fetch status/timestamp, 7-day cooldown countdown timer, and dismissed notices count.
- **Interactive QA Controls**:
  - 🔄 **"Check Now (Bypass Cooldown)"**: Forces an immediate remote fetch.
  - 🚀 **"Preview Update Dialog"**: Simulates how the update alert appears to users.
  - 📢 **"Preview In-App Notice"**: Simulates custom notification banners with action links.
  - ⏳ **"Clear Cooldown (Reset 7-Day Timer)"**: Resets cooldown timer for instant re-testing.
  - 🗑️ **"Clear Dismissed Notices"**: Clears dismissed notification history.
  - 🔗 **"Custom Config URL Input"**: Switch between GitHub Raw, staging URLs, or mock endpoints on the fly.

### 4. Standardised 31-Period Bell Schedule
- Exactly 31 fixed slots across the week (7 on Monday, 6 on Tue–Fri).
- Users can edit period details (Subject, Teacher, Room, or "Set Free Period"), but time slots and period counts remain locked to school bell times.
- **Monday**: 7 periods (09:00–15:45) with Interval (10:45–11:00) and Lunch (12:40–13:15).
- **Tuesday & Thursday**: 6 periods (09:00–14:55).
- **Wednesday**: 6 periods (08:55–14:40) with shifted morning times, Interval (10:35–10:50), and 30-min Lunch (12:30–13:00).
- **Friday**: 6 periods ending at 15:00 (Period 6: 14:05–15:00).

### 5. Google Sheets Faculty & Teacher Directory
- Live sync from Lornshill Academy Google Sheet.
- Built-in 53-teacher roster across 7 faculties (Creative Arts, Design & Technology, Languages, Maths, P.E, Science, Social Subjects).
- 1-Tap "Sync Now from Google Sheet" and auto-sync on startup.

---

## Ready-to-Install Built APKs

The app has been compiled and is ready for immediate testing or distribution:

| APK File | Location | Description |
| :--- | :--- | :--- |
| **`LornshillTimetable.apk`** | Project Root (`./LornshillTimetable.apk`) | Standalone Release APK with Proguard optimizations & debug signing |
| **`LornshillTimetable-release.apk`** | Project Root & `app/build/outputs/apk/release/` | Identical release package |
| **`LornshillTimetable-debug.apk`** | Project Root & `app/build/outputs/apk/debug/` | Debug APK with logging and debug symbols |

---

## Remote Configuration Format (`app-config.json`)

Host this JSON file at a public URL (e.g. GitHub Raw or a web host):

```json
{
  "latestVersionCode": 2,
  "latestVersionName": "1.2.0",
  "apkUrl": "https://github.com/<your-username>/<your-repo>/releases/download/v1.2.0/LornshillTimetable.apk",
  "whatsNew": "Added APK update system and remote in-app notices.\nImproved timetable editor, teacher lookup, and performance.\nFixed several minor period display bugs.",
  "notifications": [
    {
      "id": "notice-001",
      "enabled": true,
      "title": "Welcome to Lornshill Timetable",
      "message": "Welcome to Lornshill Timetable!\n\nYour app will now automatically check for updates and important school announcements. Please verify your period times and teacher assignments in the Timetable tab.",
      "buttonText": "Got It",
      "actionUrl": "https://github.com/<your-username>/<your-repo>",
      "actionText": "Visit Project Page",
      "expiresAt": "2027-01-01T00:00:00Z"
    }
  ]
}
```

### JSON Schema Breakdown:
- `latestVersionCode` *(integer, required)*: Incremented with every build. Must be greater than the installed app's `versionCode` to prompt an update.
- `latestVersionName` *(string, optional)*: Semantic version string shown to users (e.g. `"1.2.0"`).
- `apkUrl` *(string, required)*: Direct link to download the APK.
- `whatsNew` *(string, optional)*: Release notes displayed in the update dialog. Supports `\n` line breaks.
- `notifications` *(array, optional)*:
  - `id` *(string, required)*: Unique identifier. Once dismissed by the user, it will not be shown again.
  - `enabled` *(boolean, required)*: Toggle notice on/off remotely.
  - `title` *(string, required)*: Notice headline.
  - `message` *(string, required)*: Message content. Unlimited length supported.
  - `buttonText` *(string, optional)*: Dismiss button text (defaults to `"Close"`).
  - `actionUrl` *(string, optional)*: Web link opened when the user taps the action button.
  - `actionText` *(string, optional)*: Label for the optional action button.
  - `expiresAt` *(string, optional)*: ISO-8601 date string. After this time passes, the notice automatically expires.

---

## Fully Automated GitHub Setup (CI/CD)

The repository includes a ready-to-use GitHub Actions workflow (`.github/workflows/build-and-release.yml`).

### Step 1: Push Local Project to Your GitHub Repository

In your terminal or PowerShell:

```powershell
# 1. Add your GitHub repository as origin
git remote add origin https://github.com/<your-username>/<your-repo>.git

# 2. Push the main branch
git push -u origin main
```

### Step 2: Use GitHub Raw as Your Live Config URL

Once pushed, your `app-config.json` is instantly available online at:
```
https://raw.githubusercontent.com/<your-username>/<your-repo>/main/app-config.json
```

You can set this URL in `DEFAULT_CONFIG_URL` inside `RemoteConfigManager.kt` or paste it directly in the Dev Panel (`dev1`).

### Step 3: Publish a New Release with GitHub Actions (Zero-Manual-Build)

Whenever you want to release a new version of the app:

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Commit the changes and create a Git version tag:
   ```powershell
   git commit -am "Release v1.2.0"
   git tag v1.2.0
   git push origin main --tags
   ```
3. **GitHub Actions automatically:**
   - Runs all 14 unit tests.
   - Compiles the release APK (`LornshillTimetable.apk`).
   - Creates a new GitHub Release under your repo with the APK file attached!
4. Update `app-config.json` with the new version code and the GitHub Release download URL, then push:
   ```powershell
   git commit -am "Update app-config.json for v1.2.0"
   git push origin main
   ```
5. Every student/user who opens the app will now automatically receive the update prompt!

---

## Local Verification & Testing

### Running Unit Tests
All 14 unit tests cover JSON parsing, version comparison, weekly cooldown suppression/bypass, and date expiry:
```powershell
.\gradlew.bat test
```

### Building APKs Manually
```powershell
# Build release APK
.\gradlew.bat assembleRelease

# Build debug APK
.\gradlew.bat assembleDebug
```

### Live Test Configuration
The app currently points by default to an active live test endpoint (`https://paste.rs/byMiC`) configured with version 2 and notice `notice-001`. You can test the live fetch right away in the app or the Dev Panel (`dev1`).


