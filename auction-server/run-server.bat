@echo off

setlocal

cd /d "%~dp0\.."

mvn -q -pl auction-server -am exec:java
