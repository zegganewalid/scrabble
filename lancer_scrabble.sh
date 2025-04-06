#!/bin/bash

echo "======================================"
echo "Lanceur de Scrabble en reseau"
echo "======================================"

function menu {
    echo 
    echo "1. Lancer le serveur"
    echo "2. Lancer un client"
    echo "3. Lancer le tout (mode local)"
    echo "4. Quitter"
    echo
    read -p "Votre choix: " choix
    
    case $choix in
        1) serveur ;;
        2) client ;;
        3) local ;;
        4) fin ;;
        *) 
            echo "Choix invalide!"
            menu
            ;;
    esac
}

function serveur {
    echo
    echo "Lancement du serveur Scrabble..."
    read -p "Nombre de joueurs (2-4): " joueurs
    java -cp bin:lib/* ScrabbleServerApp 5000 $joueurs &
    echo "Serveur lancé sur le port 5000 pour $joueurs joueurs."
    echo
    menu
}

function client {
    echo
    echo "Lancement du client Scrabble..."
    read -p "Adresse du serveur [localhost]: " adresse
    adresse=${adresse:-localhost}
    java --module-path lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin:lib/* ScrabbleClientApp &
    echo "Client lancé."
    echo
    menu
}

function local {
    echo
    echo "Lancement en mode local (serveur + client)..."
    java -cp bin:lib/* ScrabbleServerApp 5000 2 &
    sleep 2
    java --module-path lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin:lib/* ScrabbleClientApp &
    java --module-path lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin:lib/* ScrabbleClientApp &
    echo "Mode local lancé."
    echo
    menu
}

function fin {
    echo
    echo "Au revoir!"
    sleep 1
    exit 0
}

# Rendre le script exécutable
chmod +x "$0"

# Lancer le menu
menu