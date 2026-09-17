@echo off
title Push Lornshill Timetable to GitHub
echo ===================================================
echo   Pushing Lornshill Timetable to GitHub...
echo   Repository: https://github.com/Tullibody/lornshill-timetable.git
echo ===================================================
echo.
git push -u origin main
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Push failed. If a browser window opened, complete sign-in and run this again.
    pause
    exit /b %ERRORLEVEL%
)
echo.
echo ===================================================
echo   Successfully pushed main branch!
echo ===================================================
echo.
set /p release="Do you want to publish release tag v1.0.0 now? (Y/N): "
if /i "%release%"=="Y" (
    git tag v1.0.0
    git push origin v1.0.0
    echo.
    echo Tag v1.0.0 pushed! GitHub Actions will compile the release APK automatically.
)
echo.
echo Setup complete! Press any key to close.
pause
