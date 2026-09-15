@echo off
rem Sin argumentos abre el modo consola; con rutas .txt analiza esos archivos
setlocal
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

"%JAVA%" -cp "%~dp0build;%~dp0..\lib\antlr-4.13.2-complete.jar" Main %*
