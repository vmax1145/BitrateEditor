setlocal
@echo off
echo **************************************************************
echo * Authour do not take any responsibility and 
echo * is not liable for any damage caused by using this software.
echo *
echo * !!! All you are doing is at your own risk !!!
echo *
echo **************************************************************
:PROMPT
SET /P AREYOUSURE=Accept (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

java -jar bitrate-editor-1.0-SNAPSHOT.jar SJ8.v1.3.0.config.json


:END
endlocal


