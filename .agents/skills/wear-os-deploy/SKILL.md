---
name: wear-os-deploy
description: >-
  Builds, deploys, and launches the Lornshill Wear OS app directly to the user's paired smartwatch
  via ADB over Wi-Fi. Use whenever the user asks to build, deploy, install, test, or download changes
  to their watch.
---

# Wear OS Smartwatch Deployment Skill

Use this skill to deploy the Wear OS app directly to the user's smartwatch over Wi-Fi.

## Watch Configuration
* **Device IP**: `192.168.1.114`
* **Pairing Status**: Already paired with PC (`adb-RFAX10XH5WM-EMSf6C`). No re-pairing needed unless watch Wi-Fi debugging is reset.
* **ADB Path**: `C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe`
* **App Package**: `com.example.simplebutton`
* **Main Activity**: `com.example.simplebutton.wear.WatchMainActivity`

## Deployment Workflow

### Step 1: Build the Wear OS APK
Run Gradle to assemble the Wear OS release APK:
```powershell
.\gradlew.bat :wear:assembleRelease
```
Copy to the project root:
```powershell
Copy-Item "wear\build\outputs\apk\release\wear-release.apk" -Destination "LornshillWearOS.apk" -Force
```

### Step 2: Connect to the Watch
Modern Wear OS (One UI 6 / Wear OS 5/6) dynamically assigns a connection port whenever wireless debugging is active.
1. Check if device is already connected:
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
   ```
2. If disconnected, ask the user for the current 5-digit port shown on the watch under **Settings > Developer options > Wireless debugging > IP address & Port**, or connect to the last known port:
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" connect 192.168.1.114:<PORT>
   ```

### Step 3: Streamed Install & Auto-Launch
Install the APK and immediately launch the updated UI on the watch screen:
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" connect 192.168.1.114:<PORT>; & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s 192.168.1.114:<PORT> install -r "LornshillWearOS.apk"
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s 192.168.1.114:<PORT> shell am start -n com.example.simplebutton/.wear.WatchMainActivity
```
