@echo off

echo "Running rest-at with undertow quickstart"

mvn clean package
IF %ERRORLEVEL% NEQ 0 exit -1

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

"%JAVA%" -jar target\rts-undertow-qs.jar
IF %ERRORLEVEL% NEQ 0 exit -1
