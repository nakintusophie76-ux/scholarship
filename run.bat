@echo off
setlocal
cd /d "%~dp0"

if not exist "target\ScholarManager.jar" (
    echo Scholar Manager is not built yet. Building now...
    where mvn >nul 2>nul
    if not errorlevel 1 (
        call mvn -B -DskipTests package
    ) else if exist "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" (
        call "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" -B -DskipTests package
    ) else (
        echo Maven was not found.
        echo Open this folder in NetBeans and press F6, or install Maven.
        pause
        exit /b 1
    )
)

if not exist "target\ScholarManager.jar" (
    echo The build did not produce target\ScholarManager.jar.
    pause
    exit /b 1
)

start "Scholar Manager" javaw -jar "target\ScholarManager.jar"
exit /b 0
