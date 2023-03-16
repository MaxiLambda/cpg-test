@REM lists all classes in a given maven artifact
@REM %1 - the path to the project with the dependency to analyse is located
@REM %2 - the name of the dependency: groupId:artifactId:version

@echo off
cd %1
@REM todo maybe add -Dtransitive
mvn dependency:list-classes -Dartifact="%2"