@echo off
echo ======================================
echo Recompilation des fichiers Java...
echo ======================================

REM Créez une liste de tous les fichiers .java dans le répertoire src et ses sous-répertoires
setlocal enabledelayedexpansion
set sources=
for /r src %%f in (*.java) do (
    set sources=!sources! "%%f"
)

REM Chemin vers le répertoire lib de JavaFX
set JAVAFX_LIB=C:\Users\zegga\Downloads\scrabble\lib\javafx-sdk-23.0.2\lib

REM Compilez les fichiers .java en incluant JavaFX dans le classpath
javac -d bin -cp lib/*;%JAVAFX_LIB%\* !sources!

echo Recompilation terminee.
pause