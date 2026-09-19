@echo off
setlocal enabledelayedexpansion
title Lornshill Watch App Installer
color 0B

echo ================================================================
echo         LORNSHILL TIMETABLE - WATCH APP INSTALLER
echo ================================================================
echo.

:: 1. Locate ADB automatically
set "ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not exist "%ADB_PATH%" (
    where adb >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        set "ADB_PATH=adb"
    ) else (
        color 0C
        echo [ERROR] ADB tool not found at:
        echo   %LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
        echo.
        echo Please ensure Android Studio or SDK Platform-Tools is installed.
        pause
        exit /b 1
    )
)

echo [OK] Using ADB: "%ADB_PATH%"
echo.

:: 2. Verify APK exists
set "APK_FILE=%~dp0LornshillWearOS.apk"
if not exist "%APK_FILE%" (
    set "APK_FILE=%~dp0wear\build\outputs\apk\release\wear-release.apk"
)

if not exist "%APK_FILE%" (
    color 0C
    echo [ERROR] Could not find LornshillWearOS.apk in this directory!
    echo   Checked: %~dp0LornshillWearOS.apk
    pause
    exit /b 1
)

echo [OK] Found Watch APK: "%APK_FILE%"
echo.

:: 3. Check if already connected
echo Checking for currently connected devices...
"%ADB_PATH%" devices
echo.

echo ----------------------------------------------------------------
echo  STEP 1: PREPARE YOUR WATCH
echo ----------------------------------------------------------------
echo  1. On your watch, go to Settings ^> Developer options.
echo  2. Ensure "Wireless debugging" is toggled ON.
echo  3. Make sure your watch and this PC are on the same Wi-Fi.
echo ----------------------------------------------------------------
echo.

set /p need_pair="Is this your FIRST time pairing this watch with this PC? (Y/N, default Y): "
if /i "%need_pair%"=="N" goto :do_connect

:do_pair
echo.
echo ================================================================
echo  PAIRING SETUP (One UI 6 / Wear OS)
echo ================================================================
echo  1. On your watch, tap "Pair new device" (or "Pair with pairing code").
echo  2. KEEP THAT SCREEN OPEN on your watch while typing below!
echo ================================================================
echo.

set /p watch_ip="Enter your Watch IP address (e.g. 192.168.1.50): "
set /p pair_port="Enter the PAIRING PORT shown on watch (e.g. 42105): "
set /p pair_code="Enter the 6-digit PAIRING CODE (e.g. 123456): "

echo.
echo Pairing with %watch_ip%:%pair_port% using code %pair_code%...
"%ADB_PATH%" pair %watch_ip%:%pair_port% %pair_code%

if %ERRORLEVEL% neq 0 (
    color 0C
    echo.
    echo [PAIRING FAILED] Please check:
    echo   - Watch and PC are on the exact same Wi-Fi network.
    echo   - The pairing code and port were typed correctly before watch screen went black.
    echo.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Watch successfully paired!
echo.
echo Now on your watch, press BACK once to return to the main
echo "Wireless debugging" screen to see your main connection port.
echo.

:do_connect
if not defined watch_ip (
    set /p watch_ip="Enter your Watch IP address (e.g. 192.168.1.50): "
)

set /p conn_port="Enter the MAIN CONNECTION PORT (shown on Wireless debugging screen): "

echo.
echo Connecting to %watch_ip%:%conn_port%...
"%ADB_PATH%" connect %watch_ip%:%conn_port%

echo.
echo ----------------------------------------------------------------
echo  INSTALLING LORNSHILL WEAR OS APP TO YOUR WATCH...
echo ----------------------------------------------------------------
echo  Please wait a few seconds...
"%ADB_PATH%" install -r "%APK_FILE%"

if %ERRORLEVEL% equ 0 (
    color 0A
    echo.
    echo ================================================================
    echo  [SUCCESS] LORNSHILL TIMETABLE INSTALLED ON YOUR WATCH!
    echo ================================================================
    echo  You can now open the app on your watch, sync from your phone,
    echo  and add the glanceable tile to your watch face!
    echo ================================================================
) else (
    color 0C
    echo.
    echo [INSTALLATION FAILED]
    echo If your watch showed an authorization prompt, tap "Allow" on the watch screen and run this script again.
)

echo.
pause
