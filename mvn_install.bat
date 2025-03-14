REM
REM compile and install the project
REM

SET JAVA_HOME=%JAVA_HOME_11%
SET set PATH=%JAVA_HOME%\bin;%PATH%
SET java -version
call mvn clean install
pause