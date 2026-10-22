@echo off
@title Otono v83
cd /d "%~dp0"
java -Xmx2048m -Dwz-path=wz -jar target\Cosmic.jar
pause
