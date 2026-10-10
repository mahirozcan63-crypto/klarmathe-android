@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -File "%~dp0build-android.ps1"
if errorlevel 1 goto failed
echo APK dosyasi delivery klasorunde.
pause
exit /b 0
:failed
echo APK olusmadi. Yukaridaki hata mesajini iletin.
pause
exit /b 1
