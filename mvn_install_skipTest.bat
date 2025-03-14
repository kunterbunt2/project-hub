REM
REM compile and install the project
REM

SET SET JAVA_HOME=%JAVA_HOME_11%
SET set PATH=%JAVA_HOME%\bin;%PATH%
SET java -version
call mvn clean install -DskipTests
pause