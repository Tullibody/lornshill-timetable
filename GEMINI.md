# Lornshill Timetable Project Rules

## Wear OS Development & Deployment
* When the user modifies Wear OS UI code in `:wear` and asks to test, update, download, or deploy it to their watch:
  1. Act as the automated watch downloader and deployment engine.
  2. Compile the `:wear` module (`.\gradlew.bat :wear:assembleRelease`).
  3. Use the local ADB tool at `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.
  4. Deploy wirelessly to the paired watch at `192.168.1.114`.
  5. Launch `com.example.simplebutton/.wear.WatchMainActivity` directly on the watch face.
