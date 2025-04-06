@echo off
setlocal enabledelayedexpansion

echo ======================================
echo Lanceur de Scrabble en reseau
echo ======================================

:menu
echo.
echo 1. Lancer le serveur
echo 2. Lancer un client
echo 3. Lancer le tout (mode local)
echo 4. Quitter
echo.
set /p choix=Votre choix: 

if "%choix%"=="1" goto serveur
if "%choix%"=="2" goto client
if "%choix%"=="3" goto local
if "%choix%"=="4" goto fin

echo Choix invalide!
goto menu

:serveur
echo.
echo Lancement du serveur Scrabble...
set /p joueurs=Nombre de joueurs (2-4): 
start "Serveur Scrabble" java -cp bin;lib/* ScrabbleServerApp 5000 %joueurs%
echo Serveur lancé sur le port 5000 pour %joueurs% joueurs.
echo.
goto menu

:client
echo.
echo Lancement du client Scrabble...
set /p adresse=Adresse du serveur [localhost]: 
if "!adresse!"=="" set adresse=localhost
start "Client Scrabble" java --module-path lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin;lib/* ScrabbleClientApp
echo Client lancé.
echo.
goto menu

:local
echo.
echo Lancement en mode local (serveur + client)...
start "Serveur Scrabble" java -cp bin;lib/* ScrabbleServerApp 5000 2
timeout /t 2 > nul
start "Client Scrabble 1" java --module-path lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin;lib/* ScrabbleClientApp
start "Client Scrabble 2" java --module-path lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin;lib/* ScrabbleClientApp
echo Mode local lancé.
echo.
goto menu

:fin
echo.
echo Au revoir!
timeout /t 2 > nul
exit