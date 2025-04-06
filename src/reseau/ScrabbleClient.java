package reseau;

// Importation des bibliothèques nécessaires
import java.io.*;           // Pour les opérations d'entrée/sortie
import java.net.*;          // Pour les fonctionnalités réseau
import java.util.*;         // Pour les collections et utilitaires

import javafx.application.Platform;       // Pour exécuter du code sur le thread UI de JavaFX
import javafx.scene.control.Alert;        // Pour afficher des boîtes de dialogue
import controleur.ClientControleur;       // Contrôleur côté client
import modele.Lettre;                     // Classe représentant une lettre du jeu

/**
 * Classe qui gère la communication réseau entre le client et le serveur Scrabble.
 * Cette classe s'occupe d'envoyer et de recevoir des messages au/du serveur.
 */
public class ScrabbleClient {
    // Attributs pour la connexion réseau
    private Socket socket;               // Socket de connexion au serveur
    private PrintWriter out;             // Flux de sortie pour envoyer des messages au serveur
    private BufferedReader in;           // Flux d'entrée pour recevoir des messages du serveur
    private ClientControleur controleur; // Référence au contrôleur client qui gère la logique
    private boolean connecte = false;    // État de la connexion
    
    /**
     * Constructeur qui initialise le client avec une référence au contrôleur.
     * 
     * @param controleur Le contrôleur qui gère la logique du client
     */
    public ScrabbleClient(ClientControleur controleur) {
        this.controleur = controleur;
    }
    
    /**
     * Établit une connexion avec le serveur à l'adresse et au port spécifiés.
     * 
     * @param adresse L'adresse IP ou le nom d'hôte du serveur
     * @param port Le port sur lequel le serveur écoute
     * @return true si la connexion a réussi, false sinon
     */
    public boolean connecter(String adresse, int port) {
        try {
            // Création de la socket de connexion
            socket = new Socket(adresse, port);
            
            // Initialisation des flux d'entrée/sortie
            out = new PrintWriter(socket.getOutputStream(), true); // true pour auto-flush
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Marquer comme connecté
            connecte = true;
            
            // Démarrer un thread séparé pour écouter les messages du serveur
            // Cela permet de ne pas bloquer l'interface utilisateur
            new Thread(this::ecouterServeur).start();
            
            return true;
        } catch (IOException e) {
            // Affichage de l'erreur en cas d'échec de connexion
            System.err.println("Erreur de connexion au serveur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Méthode exécutée dans un thread séparé pour écouter continuellement
     * les messages provenant du serveur.
     */
    private void ecouterServeur() {
        try {
            String messageServeur;
            
            // Boucle continue tant que des messages sont reçus
            while ((messageServeur = in.readLine()) != null) {
                // Capture le message dans une variable finale pour l'utiliser dans le lambda
                final String message = messageServeur;
                
                // Exécute le traitement du message sur le thread JavaFX pour éviter les problèmes de concurrence
                Platform.runLater(() -> traiterMessage(message));
            }
        } catch (IOException e) {
            // Vérifie si l'erreur survient pendant que le client est connecté
            if (connecte) {
                System.err.println("Erreur lors de la communication avec le serveur: " + e.getMessage());
                
                // Notifie le contrôleur de la déconnexion sur le thread JavaFX
                Platform.runLater(() -> controleur.deconnexionServeur());
            }
        } finally {
            // Dans tous les cas, ferme la connexion
            deconnecter();
        }
    }
    
    /**
     * Traite les messages reçus du serveur en fonction de leur type.
     * Les messages suivent un format "COMMANDE|DONNÉES".
     * 
     * @param message Le message reçu du serveur
     */
    private void traiterMessage(String message) {
        // Affichage du message pour débogage
        System.out.println("DEBUG - Message du serveur reçu: " + message);
        
        // Découpage du message en parties (commande et données)
        String[] parts = message.split("\\|");
        String commande = parts[0];
        
        // Traitement selon le type de commande
        switch (commande) {
            case "DEMANDE_NOM":
                // Le serveur demande le nom du joueur
                envoyerNomJoueur();
                break;
                
            case "PARTIE_COMMENCE":
                // La partie commence
                controleur.notifierDebutPartie();
                // Demander l'état initial du sac de lettres
                demanderEtatSac();
                break;
                
            case "PLATEAU":
                // Mise à jour de l'état du plateau de jeu
                if (parts.length > 1) {
                    System.out.println("DEBUG - Mise à jour du plateau: " + parts[1]);
                    controleur.mettreAJourPlateau(parts[1]);
                }
                break;
                
            case "LETTRES":
                // Mise à jour des lettres du joueur
                if (parts.length > 1) {
                    System.out.println("DEBUG - Mise à jour des lettres: " + parts[1]);
                    controleur.mettreAJourLettres(parts[1]);
                }
                break;
                
            case "SCORES":
                // Mise à jour des scores des joueurs
                if (parts.length > 1) {
                    System.out.println("DEBUG - Mise à jour des scores: " + parts[1]);
                    controleur.mettreAJourScores(parts[1]);
                }
                break;
                
            case "JOUEUR_ACTUEL":
                // Information sur le joueur dont c'est le tour
                if (parts.length > 1) {
                    System.out.println("DEBUG - Joueur actuel: " + parts[1]);
                    controleur.mettreAJourJoueurActuel(parts[1]);
                }
                break;
                
            case "MOT_IMPOSE":
                // Mise à jour du mot imposé (si mode de jeu avec mot imposé)
                if (parts.length > 1) {
                    System.out.println("DEBUG - Mot imposé: " + parts[1]);
                    controleur.mettreAJourMotImpose(parts[1]);
                }
                break;
                
            case "TOUR":
                // Mise à jour du numéro de tour actuel
                if (parts.length > 1) {
                    try {
                        int numeroTour = Integer.parseInt(parts[1]);
                        System.out.println("DEBUG CLIENT - Réception du numéro de tour : " + numeroTour);
                        
                        // Exécution sur le thread JavaFX pour éviter les problèmes de concurrence
                        Platform.runLater(() -> {
                            System.out.println("DEBUG CLIENT - Mise à jour du tour sur le thread JavaFX");
                            controleur.mettreAJourTour(numeroTour);
                        });
                    } catch (NumberFormatException e) {
                        // En cas de format incorrect pour le numéro de tour
                        System.err.println("ERREUR - Format incorrect pour le numéro de tour");
                        e.printStackTrace();
                    }
                }
                break;
                
            case "SAC":
                // Mise à jour des informations sur les lettres restantes dans le sac
                if (parts.length > 1) {
                    System.out.println("DEBUG - Mise à jour du sac: " + parts[1]);
                    controleur.mettreAJourSac(parts[1]);
                }
                break;
                
            case "ERREUR":
                // Message d'erreur envoyé par le serveur
                if (parts.length > 1) {
                    System.err.println("ERREUR SERVEUR: " + parts[1]);
                    controleur.traiterMessageErreur(parts[1]);
                }
                break;
                
            case "FIN_PARTIE":
                // Notification de fin de partie normale
                System.out.println("DEBUG - Fin de partie normale");
                controleur.terminerPartie();
                break;
                
            case "FIN_PARTIE_PREMATUREMENT":
                // Notification de fin de partie prématurée (abandon, etc.)
                if (parts.length > 1) {
                    String vainqueur = parts[1];
                    System.out.println("DEBUG - Fin de partie prématurée. Vainqueur : " + vainqueur);
                    
                    // Affichage d'une alerte sur le thread JavaFX
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Fin de partie");
                        alert.setHeaderText("Partie terminée prématurément");
                        alert.setContentText("Le vainqueur est : " + vainqueur);
                        alert.showAndWait();
                    });
                }
                controleur.terminerPartie();
                break;
                
            default:
                // Commande non reconnue
                System.err.println("ERREUR - Commande non reconnue: " + commande);
        }
    }
    
    /**
     * Envoie le nom du joueur au serveur.
     * Si aucun nom n'est fourni, génère un nom aléatoire.
     */
    public void envoyerNomJoueur() {
        // Demande le nom au contrôleur
        String nom = controleur.demanderNomJoueur();
        
        // Si un nom valide est fourni, l'envoie
        if (nom != null && !nom.isEmpty()) {
            envoyerMessage(nom);
        } else {
            // Sinon, génère un nom aléatoire
            envoyerMessage("Joueur" + new Random().nextInt(1000));
        }
    }
    
    /**
     * Envoie un message au serveur.
     * 
     * @param message Le message à envoyer
     */
    public void envoyerMessage(String message) {
        // Vérifie que la connexion est active
        if (out != null && connecte) {
            // Envoie le message
            out.println(message);
            System.out.println("Message envoyé au serveur: " + message);
        }
    }
    
    /**
     * Envoie une demande de placement de mot au serveur.
     * 
     * @param mot Le mot à placer
     * @param x La coordonnée x de la première lettre
     * @param y La coordonnée y de la première lettre
     * @param horizontal true si le mot est placé horizontalement, false si vertical
     */
    public void placerMot(String mot, int x, int y, boolean horizontal) {
        // Détermine la direction (H pour horizontal, V pour vertical)
        String direction = horizontal ? "H" : "V";
        
        // Format attendu par le serveur : "PLACER_MOT|mot|x|y|direction"
        String message = "PLACER_MOT|" + mot + "|" + x + "|" + y + "|" + direction;
        System.out.println("DEBUG - Envoi du placement de mot au serveur: " + message);
        
        // Envoie la requête
        envoyerMessage(message);
    }
    
    /**
     * Envoie une demande de fin de partie prématurée au serveur.
     */
    public void terminerPartiePrematurement() {
        envoyerMessage("TERMINER_PARTIE");
    }
    
    /**
     * Envoie une demande pour passer son tour au serveur.
     */
    public void passerTour() {
        envoyerMessage("PASSER_TOUR");
        
        // Demande l'état du sac après avoir passé le tour
        demanderEtatSac();
    }
    
    /**
     * Demande l'état actuel du sac de lettres au serveur.
     */
    public void demanderEtatSac() {
        envoyerMessage("ETAT_SAC");
    }
    
    /**
     * Ferme la connexion avec le serveur.
     */
    public void deconnecter() {
        // Marque comme déconnecté
        connecte = false;
        
        try {
            // Ferme tous les flux et la socket
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
    
    /**
     * Indique si le client est actuellement connecté au serveur.
     * 
     * @return true si connecté, false sinon
     */
    public boolean estConnecte() {
        return connecte;
    }
}