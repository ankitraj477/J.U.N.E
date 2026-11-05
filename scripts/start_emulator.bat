@echo off
setlocal

set AVD_NAME=%1
if "%AVD_NAME%"=="" set AVD_NAME=Pixel_6

echo ==============================================
echo  Launching J.U.N.E. Android Emulator: %AVD_NAME%
echo ==============================================

set EMULATOR_EXE=
if defined ANDROID_HOME if exist "%ANDROID_HOME%\emulator\emulator.exe" set EMULATOR_EXE=%ANDROID_HOME%\emulator\emulator.exe
if not defined EMULATOR_EXE if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\emulator\emulator.exe" set EMULATOR_EXE=%ANDROID_SDK_ROOT%\emulator\emulator.exe
if not defined EMULATOR_EXE if exist "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" set EMULATOR_EXE=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe

if not defined EMULATOR_EXE (
    echo Error: Android emulator executable not found.
    echo Please set ANDROID_HOME or ANDROID_SDK_ROOT.
    exit /b 1
)

start "" "%EMULATOR_EXE%" -avd %AVD_NAME%
exit /b 0
