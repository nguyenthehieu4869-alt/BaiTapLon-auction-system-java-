@echo off

setlocal

cd /d "%~dp0\.."

mvn -q -N install
if errorlevel 1 exit /b %errorlevel%

mvn -q -pl auction-common install
if errorlevel 1 exit /b %errorlevel%

mvn -q -pl auction-server -am compile
if errorlevel 1 exit /b %errorlevel%

mvn -q -pl auction-server exec:java
