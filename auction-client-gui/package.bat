@echo off

setlocal

cd /d "%~dp0\.."

mvn -q -pl auction-client-gui -am clean package

if errorlevel 1 goto :error


echo.

echo Build completed.

echo Run the app with:

echo   mvn -pl auction-client-gui -am javafx:run

echo.

exit /b 0



:error

echo.

echo Build failed.

exit /b 1

