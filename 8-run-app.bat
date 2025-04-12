@echo off
@setlocal enableextensions
@cd /d "%~dp0"

echo Checking for JAR file...
if not exist "dietapp-app\target\dietapp-app-1.0-SNAPSHOT.jar" (
    echo Error: JAR file not found. Please run 7-build-app.bat first.
    goto end
)

echo Running Application
java -cp dietapp-app/target/dietapp-app-1.0-SNAPSHOT.jar com.berkant.kagan.haluk.irem.dietapp.DietappApp

:end
echo Operation Completed!
pause