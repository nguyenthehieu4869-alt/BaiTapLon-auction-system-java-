@echo off

setlocal

cd /d "%~dp0\.."

mvn -q -pl auction-client-gui -am javafx:run

