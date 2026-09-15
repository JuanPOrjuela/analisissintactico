@echo off
rem Genera los dos parsers con ANTLR y compila todo en build\
rem   construir.bat              genera + compila
rem   ejecutar.bat               modo consola
rem   ejecutar.bat ejemplos.txt  analiza un archivo
rem   pruebas.bat                corre las pruebas
setlocal
cd /d "%~dp0"

set "ANTLR=..\lib\antlr-4.13.2-complete.jar"
rem El codigo usa Java 17; si JAVA_HOME existe se usa ese java
set "JAVA=java"
set "JAVAC=javac"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac"

if exist gen rmdir /s /q gen
if exist build rmdir /s /q build

"%JAVA%" -jar "%ANTLR%" -no-listener -o gen Ambigua.g4 AmbiguaInvertida.g4 || exit /b 1
"%JAVAC%" -encoding UTF-8 -cp "%ANTLR%" -d build gen\*.java *.java || exit /b 1

echo OK. Uso: ejecutar.bat   ^|   ejecutar.bat ejemplos.txt   ^|   pruebas.bat
