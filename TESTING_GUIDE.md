# Complete Guide: Testing Your Android App on PC (Frictionless & Fast)

When developing for Android, the traditional workflow of **"edit code -> wait 45s for Gradle to compile -> package APK -> reinstall on emulator"** is notoriously slow and tedious. 

Here is how you can test this app on PC **without heavy setup** and **without having to manually rebuild/reinstall every time you make a change**.

---

## 🚀 Method 1: Instant PC Browser Preview (Zero Setup)

We included a lightweight interactive preview right in this repository that renders the exact Material 3 layout and button logic on your PC.

### How to run:
- Simply double-click [`preview/index.html`](file:///c:/Users/User/Documents/antigravity/intelligent-borg/preview/index.html) in Windows File Explorer, or run in your terminal:
  ```powershell
  npm run preview
  ```
- **Why this is great:**
  - **Zero installation:** No Android Studio, no JDK, no 15 GB emulator download.
  - **Instant testing:** Test button clicks, counter increments, animations, and responsiveness immediately on your PC.
  - Press `F12` in Chrome/Edge and toggle **Device Emulation** (mobile icon) to test various phone screen sizes.

---

## ⚡ Method 2: Android Studio "Interactive Preview" (No Emulator Required)

If you have Android Studio installed, you do **not** need to boot an Android emulator to test button clicks! Jetpack Compose has a built-in interactive simulator inside the code editor.

### Steps:
1. Open this folder (`intelligent-borg`) in Android Studio (`File > Open...`).
2. Open [`MainActivity.kt`](file:///c:/Users/User/Documents/antigravity/intelligent-borg/app/src/main/java/com/example/simplebutton/MainActivity.kt).
3. In the top-right corner of the editor, click **Split** or **Design** view.
4. Locate the `@Preview` window showing `SimpleButtonPreview`.
5. Click the **Interactive Mode** icon (a hand with a pointing finger or play icon) above the preview window.
6. **Result:** The preview becomes live! You can click the button, test the counter, and see animations directly inside the IDE without running an emulator or phone.

---

## 🔥 Method 3: Jetpack Compose "Live Edit" (Hot-Swapping Without Rebuilding)

If you are running the app on an Android emulator or a physical phone, you do **not** need to manually rebuild or reinstall the APK every time you edit code. Android Studio has **Live Edit**.

### How to use Live Edit:
1. In Android Studio, go to **Settings > Build, Execution, Deployment > Live Edit**.
2. Make sure **Enable Live Edit** is checked (configured to "Push edits automatically" or on save).
3. Run the app once on your emulator or connected phone (`Shift + F10`).
4. Now, change any code in `MainActivity.kt` (such as button text, colors, spacing, or logic) and press `Ctrl + S`.
5. **Result:** The changes appear on the screen **instantly (<1 second)** without Gradle recompiling or reinstalling the APK!

---

## 📱 Method 4: Physical Android Device + `scrcpy` (Fastest, Lightest PC Testing)

Android emulators consume 4–8 GB of RAM and can make your PC fans spin up. If you have an Android phone, you can mirror and control it directly on your PC monitor at 60 FPS with zero lag.

### Steps:
1. Enable **Developer Options** on your Android phone (tap *Build Number* in Settings 7 times).
2. Turn on **USB Debugging**.
3. Plug your phone into your PC via USB cable.
4. Install `scrcpy` on Windows (using winget):
   ```powershell
   winget install Genymobile.scrcpy
   ```
5. Run `scrcpy` in your terminal.
6. **Result:** A low-latency window opens on your PC showing your phone's screen. You can interact with your app using your PC mouse and keyboard. Paired with **Live Edit** from Method 3, this is the preferred setup of professional Android engineers.

---

## 📊 How to Test Google Sheets Faculty & Teacher Sync

You can test the spreadsheet integration directly in both the **PC Browser Preview** and the **Native Android App**:

### Quick Test Flow:
1. **Open Settings** tab in the app or preview (`preview/index.html`).
2. Locate the **"FACULTY & TEACHER DIRECTORY"** section.
3. Click **"Sync with Google Sheets"**:
   - **Via URL**: Click **"⚡ Fill Demo Secondary School Sheet"** to load a pre-configured Scottish secondary school roster, then tap **Fetch & Sync**.
   - **Via Custom Sheet**: Paste any public Google Sheet link (make sure General access is set to *"Anyone with the link can view"*).
   - **Via Direct CSV**: Switch to the **Direct CSV / Paste** tab and paste raw spreadsheet data or click **⚡ Load Scottish Preset CSV**.
4. **Check Directory**: Click **"Browse Faculty Directory"** to search across teachers, subjects, and departments.
5. **Verify Lesson Editor Autocomplete**: Go to **Timetable** → click **+ Add Lesson** (or edit an existing period). When selecting a subject, observe that the department's teachers and default rooms are automatically suggested from your synced sheet!
6. **Revert Anytime**: Click **"Reset to Built-in Roster"** in Settings to instantly restore the default Scottish secondary school database.

---

## 🌐 Method 5: For Future Projects: React Native / Expo (The Ultimate Hot-Reload Setup)

If in the future you want a workflow where:
- You save a file and it updates in 50 milliseconds in your PC browser.
- You test on your phone by scanning a QR code with the free **Expo Go** app (no USB cables, no manual APK transfer).
- You never install Android Studio or Gradle.

You can create an Expo app with:
```powershell
npx create-expo-app MyButtonApp
cd MyButtonApp
npx expo start
```
- Press `w` to open in your PC browser immediately.
- Scan the terminal QR code with your phone camera to test live on your phone.
- When ready for production, Expo builds the standalone `.apk` for you in the cloud (`npx eas build -p android`).

---

## Summary Recommendation

| Your Scenario | Recommended Tool |
| :--- | :--- |
| **Want to test right now on PC with 0 setup** | Open [`preview/index.html`](file:///c:/Users/User/Documents/antigravity/intelligent-borg/preview/index.html) |
| **Working in Android Studio without lag** | Use **Interactive Preview** in `MainActivity.kt` |
| **Testing on emulator/device with live code updates** | Enable **Live Edit** in Android Studio settings |
| **Testing on a real phone from PC screen** | Use `scrcpy` via USB |
