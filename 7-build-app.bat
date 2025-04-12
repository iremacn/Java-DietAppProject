@echo off

:: Enable necessary extensions
@setlocal enableextensions

echo Get the current directory
set "currentDir=%CD%"

echo Change the current working directory to the script directory
@cd /d "%~dp0"

echo Create target directory if it doesn't exist
if not exist "dietapp-app\target\site" (
  mkdir "dietapp-app\target\site"
)

echo Delete output directories if they exist
if exist "dietapp-app\target\site\coverxygen" rd /S /Q "dietapp-app\target\site\coverxygen"
if exist "dietapp-app\target\site\coveragereport" rd /S /Q "dietapp-app\target\site\coveragereport"
if exist "dietapp-app\target\site\doxygen" rd /S /Q "dietapp-app\target\site\doxygen"

echo Delete and Create the "release" folder and its contents
if exist "release" rd /S /Q "release"
mkdir release

echo Change directory to dietapp-app
cd dietapp-app

echo Perform Maven clean, test, and packaging
call mvn clean test package

echo Return to the previous directory
cd ..

echo Create Required Folders coverxygen/coveragereport/doxygen
cd dietapp-app
if not exist "target" mkdir target
cd target
if not exist "site" mkdir site
cd site
if not exist "coverxygen" mkdir coverxygen
if not exist "coveragereport" mkdir coveragereport
if not exist "doxygen" mkdir doxygen
cd ..
cd ..
cd ..

echo Generate Doxygen HTML and XML Documentation
if exist "Doxyfile" (
    call doxygen Doxyfile
) else (
    echo Warning: Doxyfile not found, skipping Doxygen documentation generation.
)

echo Change directory to dietapp-app
cd dietapp-app

echo Generate ReportGenerator HTML Report
call reportgenerator "-reports:target\site\jacoco\jacoco.xml" "-sourcedirs:src\main\java" "-targetdir:target\site\coveragereport" -reporttypes:Html

echo Generate ReportGenerator Badges
call reportgenerator "-reports:target\site\jacoco\jacoco.xml" "-sourcedirs:src\main\java" "-targetdir:target\site\coveragereport" -reporttypes:Badges

echo Display information about the binary file
echo Our Binary is a Single Jar With Dependencies. You Do Not Need to Compress It.

echo Return to the previous directory
cd ..

if exist "dietapp-app\target\site\doxygen\xml" (
    echo Run Coverxygen
    call python -m coverxygen --xml-dir ./dietapp-app/target/site/doxygen/xml --src-dir ./ --format lcov --output ./dietapp-app/target/site/coverxygen/lcov.info --prefix %currentDir%/dietapp-app/

    echo Run lcov genhtml
    call perl C:\ProgramData\chocolatey\lib\lcov\tools\bin\genhtml --legend --title "Documentation Coverage Report" ./dietapp-app/target/site/coverxygen/lcov.info -o dietapp-app/target/site/coverxygen
) else (
    echo Warning: Doxygen XML not found, skipping documentation coverage report generation.
)

echo Check if badge files exist before copying
if exist "dietapp-app\target\site\coveragereport\badge_combined.svg" (
    echo Copy badge files to the "assets" directory
    if not exist "assets" mkdir assets
    call copy "dietapp-app\target\site\coveragereport\badge_combined.svg" "assets\badge_combined.svg"
    call copy "dietapp-app\target\site\coveragereport\badge_branchcoverage.svg" "assets\badge_branchcoverage.svg"
    call copy "dietapp-app\target\site\coveragereport\badge_linecoverage.svg" "assets\badge_linecoverage.svg"
    call copy "dietapp-app\target\site\coveragereport\badge_methodcoverage.svg" "assets\badge_methodcoverage.svg"
) else (
    echo Warning: Badge files not found, skipping badge copy.
)

if exist "assets\rteu_logo.jpg" (
    if not exist "dietapp-app\src\site\resources\images" mkdir "dietapp-app\src\site\resources\images"
    call copy "assets\rteu_logo.jpg" "dietapp-app\src\site\resources\images\rteu_logo.jpg"
)

echo Copy the "assets" folder if it exists
if exist "assets" (
    if not exist "dietapp-app\src\site\resources\assets" mkdir "dietapp-app\src\site\resources\assets"
    call robocopy assets "dietapp-app\src\site\resources\assets" /E
)

echo Copy the "README.md" file if it exists
if exist "README.md" (
    if not exist "dietapp-app\src\site\markdown" mkdir "dietapp-app\src\site\markdown"
    call copy README.md "dietapp-app\src\site\markdown\readme.md"
)

cd dietapp-app
echo Perform Maven site generation
call mvn site
cd ..

if exist "dietapp-app\target\*.jar" (
    echo Package Output Jar Files
    tar -czvf release\application-binary.tar.gz -C dietapp-app\target *.jar
) else (
    echo Warning: No JAR files found to package.
)

if exist "dietapp-app\target\site\jacoco" (
    echo Package Jacoco Test Coverage Report
    call tar -czvf release\test-jacoco-report.tar.gz -C dietapp-app\target\site\jacoco .
)

if exist "dietapp-app\target\site\coveragereport" (
    echo Package ReportGenerator Test Coverage Report
    call tar -czvf release\test-coverage-report.tar.gz -C dietapp-app\target\site\coveragereport .
)

if exist "dietapp-app\target\site\doxygen" (
    echo Package Code Documentation
    call tar -czvf release\application-documentation.tar.gz -C dietapp-app\target\site\doxygen .
)

if exist "dietapp-app\target\site\coverxygen" (
    echo Package Documentation Coverage
    call tar -czvf release\doc-coverage-report.tar.gz -C dietapp-app\target\site\coverxygen .
)

if exist "dietapp-app\target\site" (
    echo Package Product Site
    call tar -czvf release\application-site.tar.gz -C dietapp-app\target\site .
)

echo ....................
echo Operation Completed!
echo ....................
pause
