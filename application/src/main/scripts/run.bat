@echo off

SETLOCAL EnableDelayedExpansion

@REM set "JAVA_OPTS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"

if "%home%" == "" (
   set "home=%~dp0"
)

java %JAVA_OPTS% -Dlog4j.configuration=log4j.properties -jar %home%/libs/application-1.0.0-SNAPSHOT.jar %1

ENDLOCAL