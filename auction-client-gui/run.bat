@echo off

setlocal

cd /d "%~dp0\.."

mvn -q -pl auction-client-gui -am compile
if errorlevel 1 exit /b %errorlevel%

mvn -q -pl auction-client-gui -am javafx:run
