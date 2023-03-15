@REM lists all dependencies of a given maven project
@REM %1 - the path to the project to analyse

@echo off
cd %1
mvn dependency:analyze