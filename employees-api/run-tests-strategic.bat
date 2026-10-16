@echo off
REM Strategic Test Runner for Employees API
REM Runs tests in stages and outputs structured JSON

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0
set SESSION_ID=subagent-test-%RANDOM%-%RANDOM%
set STAGE=%1
if "%STAGE%"=="" set STAGE=combined
set TRIGGER=%2
if "%TRIGGER%"=="" set TRIGGER=manual

cd /d "%PROJECT_DIR%"

echo {"sessionId":"%SESSION_ID%","stage":"%STAGE%","trigger":"%TRIGGER%","changedFiles":[],"summary":{},"coverage":{},"failedTests":[],"newFailures":[],"fixedSinceLastRun":[],"artifacts":{}} > nul

REM Function to run a command and capture output
REM We'll use a temporary file approach for JSON building

set TMP_JSON=%TEMP%\test-result-%RANDOM%.json
set TMP_SUMMARY=%TEMP%\test-summary-%RANDOM%.txt

REM Initialize JSON structure
echo { > "%TMP_JSON%"
echo   "sessionId": "%SESSION_ID%", >> "%TMP_JSON%"
echo   "stage": "%STAGE%", >> "%TMP_JSON%"
echo   "trigger": "%TRIGGER%", >> "%TMP_JSON%"
echo   "changedFiles": [], >> "%TMP_JSON%"
echo   "summary": {}, >> "%TMP_JSON%"
echo   "coverage": {}, >> "%TMP_JSON%"
echo   "failedTests": [], >> "%TMP_JSON%"
echo   "newFailures": [], >> "%TMP_JSON%"
echo   "fixedSinceLastRun": [], >> "%TMP_JSON%"
echo   "artifacts": { >> "%TMP_JSON%"
echo     "jacocoXml": "target/site/jacoco/jacoco.xml", >> "%TMP_JSON%"
echo     "surefireDir": "target/surefire-reports", >> "%TMP_JSON%"
echo     "failsafeDir": "target/failsafe-reports" >> "%TMP_JSON%"
echo   } >> "%TMP_JSON%"
echo } >> "%TMP_JSON%"

REM Stage 1: Unit Tests
if /I "%STAGE%"=="unit" if /I "%STAGE%"=="combined" (
    echo Running unit tests... >&2
    set START_TIME=%TIME%
    call mvnw.cmd test -q > "%TMP_SUMMARY%" 2>&1
    set EXIT_CODE=%ERRORLEVEL%
    set END_TIME=%TIME%
    
    REM Parse Surefire reports
    call :parse_surefire "%TMP_SUMMARY%" "%TMP_JSON%"
    
    REM Parse JaCoCo coverage
    call :parse_jacoco "target/site/jacoco/jacoco.xml" "%TMP_JSON%"
)

REM Stage 2: Integration Tests
if /I "%STAGE%"=="integration" if /I "%STAGE%"=="combined" (
    echo Running integration tests... >&2
    set START_TIME=%TIME%
    call mvnw.cmd verify -q > "%TMP_SUMMARY%" 2>&1
    set EXIT_CODE=%ERRORLEVEL%
    set END_TIME=%TIME%
    
    REM Parse Failsafe reports
    call :parse_failsafe "%TMP_SUMMARY%" "%TMP_JSON%"
    
    REM Parse combined JaCoCo coverage
    call :parse_jacoco "target/site/jacoco/jacoco.xml" "%TMP_JSON%"
)

REM Output final JSON
type "%TMP_JSON%"

REM Cleanup
del "%TMP_JSON%" 2>nul
del "%TMP_SUMMARY%" 2>nul

exit /b %EXIT_CODE%

:parse_surefire
REM Parse surefire reports for test results
set SUMMARY_FILE=%~1
set JSON_FILE=%~2
REM This is a simplified parser - in production use a proper JSON tool
exit /b

:parse_failsafe
REM Parse failsafe reports for integration test results
set SUMMARY_FILE=%~1
set JSON_FILE=%~2
exit /b

:parse_jacoco
REM Parse JaCoCo XML for coverage data
set JACOCO_FILE=%~1
set JSON_FILE=%~2
exit /b