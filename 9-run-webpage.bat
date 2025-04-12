@echo off
@setlocal enableextensions
@cd /d "%~dp0"

if not exist "dietapp-app\target\site" (
    echo Error: Site files not found. Please run 7-build-app.bat first.
    goto end
)

cd dietapp-app

echo Running Web Site on http://localhost:9000/
echo To Exit Use CTRL+Z CTRL+C
start http://localhost:9000/
mvn site:run

:end
echo Operation Completed!
pause