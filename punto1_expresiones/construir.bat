@echo off
rem Genera el lexer y el parser en Python a partir de Expr.g4
rem   construir.bat              genera el parser
rem   ejecutar.bat               modo consola
rem   ejecutar.bat ejemplos.txt  analiza un archivo
rem   pruebas.bat                corre las pruebas
setlocal
cd /d "%~dp0"

set "ANTLR=..\lib\antlr-4.13.2-complete.jar"
rem ANTLR 4.13 necesita Java 11 o superior; si JAVA_HOME existe se usa ese java
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

"%JAVA%" -jar "%ANTLR%" -Dlanguage=Python3 Expr.g4 || exit /b 1

echo OK. Uso: ejecutar.bat   ^|   ejecutar.bat ejemplos.txt   ^|   pruebas.bat
