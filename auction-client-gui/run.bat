@echo off

setlocal

cd /d "%~dp0"

mvn -q javafx:run

