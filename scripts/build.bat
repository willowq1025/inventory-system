@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0.."

set "MVN_CMD="
if exist "%~dp0..\mvnw.cmd" (
  set "MVN_CMD=call mvnw.cmd"
) else (
  where mvn >nul 2>nul
  if not errorlevel 1 set "MVN_CMD=call mvn"
)
if "%MVN_CMD%"=="" (
  echo [ERROR] 未检测到 Maven，请保留项目内的 mvnw.cmd / .mvn\wrapper\maven-wrapper.jar
  exit /b 1
)

%MVN_CMD% -T 1C clean package -DskipTests %*
if errorlevel 1 exit /b 1
echo [OK] 产物: inventory-admin\target\inventory-admin.jar
endlocal
