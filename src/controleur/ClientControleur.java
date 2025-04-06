package controleur;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;                  // Pour exécuter du code sur le thread UI de JavaFX
import javafx.scene.control.Alert;                   // Pour afficher des boîtes de dialogue
import javafx.scene.control.ButtonType;              // Types de boutons pour les dialogues
import javafx.scene.control.TextInputDialog;         // Dialogue pour saisir du texte
import javafx.scene.effect.Light.Point;              // Non utilisé, peut être supprimé
import modele.*;                                     // Import de toutes les classes du package modèle
import reseau.ScrabbleClient;                        // Client réseau pour communiquer avec le serveur
import vue.PlateauVue;                               // Vue du plateau de jeu
import vue.MainJoueurVue;                            // Vue des lettres du joueur
import vue.SacInfoVue;                               // Vue des informations sur le sac de lettres
import vue.ScoreVue;                                 // Vue des scores

/**
 * Contrôleur principal côté client du jeu Scrabble.
 * Gère la logique du jeu, coordonne les différentes vues et communique avec le serveur.
 * Implémente le pattern MVC (Modèle-Vue-Contrôleur).
 */
public class ClientControleur {
    // Composante réseau
    private ScrabbleClient client;                   // Client pour communiquer avec le serveur
    
    // Références vers les vues
    private PlateauVue plateauVue;                   // Vue du plateau de jeu
    private MainJoueurVue mainJoueurVue;             // Vue des lettres du joueur
    private ScoreVue scoreVue;                       // Vue des scores
    private SacInfoVue sacInfoVue;                   // Vue des informations du sac
    
    // Données du jeu
    private List<Lettre> lettresEnMain = new ArrayList<>();  // Lettres que le joueur a en main
    private boolean estMonTour = false;              // Indique si c'est au tour du joueur
    private String nomJoueur;                        // Nom du joueur
    private String motImpose;                        // Mot imposé (variante du jeu)
    
    // Sous-contrôleurs pour la séparation des responsabilités
    private PlateauControleur plateauControleur;     // Gère la logique du plateau
    private LettresControleur lettresControleur;     // Gère la logique des lettres
    
    /**
     * Constructeur du contrôleur client.
     * Initialise les composants nécessaires et vérifie le dictionnaire.
     */
    public ClientControleur() {
        // Initialisation du client réseau avec une référence vers ce contrôleur
        client = new ScrabbleClient(this);
        
        // Initialisation des sous-contrôleurs
        // Note: ordre important car les contrôleurs ont des dépendances circulaires
        plateauControleur = new PlateauControleur(this, getLettresControleur());
        lettresControleur = new LettresControleur(this);

        // Vérification du dictionnaire pour s'assurer qu'il est chargé correctement
        Dictionnaire dictionnaire = new Dictionnaire();
        dictionnaire.verifierInitialisation();
        System.out.println("Vérification du dictionnaire terminée");
    }
    
    /**
     * Établit une connexion avec le serveur.
     * 
     * @param adresse L'adresse IP ou nom d'hôte du serveur
     * @param port Le port sur lequel le serveur écoute
     * @return true si la connexion a réussi, false sinon
     */
    public boolean connecterAuServeur(String adresse, int port) {
        return client.connecter(adresse, port);
    }
    
    /**
     * Demande au joueur d'entrer son nom via une boîte de dialogue.
     * Si le nom est déjà défini, le retourne directement.
     * 
     * @return Le nom choisi par le joueur ou null si annulé
     */
    public String demanderNomJoueur() {
        // On utilise directement le nom déjà défini si disponible
        if (nomJoueur != null && !nomJoueur.isEmpty()) {
            return nomJoueur;
        }
        
        // Création d'une boîte de dialogue pour saisir le nom
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Identification");
        dialog.setHeaderText("Veuillez entrer votre nom");
        dialog.setContentText("Nom:");
        
        // Affichage de la boîte de dialogue et attente du résultat
        Optional<String> result = dialog.showAndWait();
        
        // Traitement du résultat
        if (result.isPresent()) {
            this.nomJoueur = result.get();
            return result.get();
        }
        
        return null;
    }
    
    /**
     * Notifie le joueur que la partie commence.
     * Affiche une boîte de dialogue et demande l'état du sac.
     */
    public void notifierDebutPartie() {
        // Affichage d'une notification sur le thread JavaFX
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Scrabble");
            alert.setHeaderText("La partie commence !");
            alert.setContentText("Tous les joueurs sont connectés, la partie peut commencer.");
            alert.showAndWait();
        });
        
        // Demande l'état du sac au démarrage de la partie
        if (client != null) {
            client.demanderEtatSac();
        }
    }
    
    /**
     * Met à jour l'état du plateau de jeu à partir des données reçues du serveur.
     * Format des données: "x1,y1,lettre1;x2,y2,lettre2;..."
     * 
     * @param plateauData Les données du plateau au format texte
     */
    public void mettreAJourPlateau(String plateauData) {
        if (plateauVue != null) {
            // Exécution sur le thread JavaFX car modification d'UI
            Platform.runLater(() -> {
                // Effacer le plateau avant de le mettre à jour
                plateauVue.effacerPlateau();
                
                // Si nous avons des données
                if (plateauData != null && !plateauData.isEmpty()) {
                    // Division des données en cases individuelles
                    String[] cases = plateauData.split(";");
                    for (String c : cases) {
                        if (!c.isEmpty()) {
                            // Extraction des coordonnées et de la lettre
                            String[] coordonnees = c.split(",");
                            if (coordonnees.length >= 3) {
                                int x = Integer.parseInt(coordonnees[0]);
                                int y = Integer.parseInt(coordonnees[1]);
                                char lettre = coordonnees[2].charAt(0);
                                // Placement de la lettre sur la vue du plateau
                                plateauVue.placerLettre(x, y, lettre);
                            }
                        }
                    }
                }
            });
        }
        
        // Notification également au sous-contrôleur du plateau
        if (plateauControleur != null) {
            plateauControleur.mettreAJourPlateau(plateauData);
        }
    }
    
    /**
     * Met à jour les lettres du joueur à partir des données reçues du serveur.
     * Format des données: "lettre1,valeur1;lettre2,valeur2;..."
     * 
     * @param lettresData Les données des lettres au format texte
     */
    public void mettreAJourLettres(String lettresData) {
        // Effacer les lettres actuelles
        lettresEnMain.clear();
        
        // Traiter les nouvelles lettres
        if (lettresData != null && !lettresData.isEmpty()) {
            String[] lettres = lettresData.split(";");
            for (String l : lettres) {
                if (!l.isEmpty()) {
                    String[] donnees = l.split(",");
                    if (donnees.length >= 2) {
                        char caractere = donnees[0].charAt(0);
                        int valeur = Integer.parseInt(donnees[1]);
                        // Ajout de la lettre à la liste
                        lettresEnMain.add(new Lettre(caractere, valeur));
                    }
                }
            }
        }
        
        // Mise à jour de la vue des lettres
        if (mainJoueurVue != null) {
            Platform.runLater(() -> mainJoueurVue.mettreAJourLettres(lettresEnMain));
        }
    }
    
    /**
     * Met à jour les scores des joueurs à partir des données reçues du serveur.
     * Format des données: "nom1,score1;nom2,score2;..."
     * 
     * @param scoresData Les données des scores au format texte
     */
    public void mettreAJourScores(String scoresData) {
        if (scoreVue != null && scoresData != null && !scoresData.isEmpty()) {
            Platform.runLater(() -> {
                // Effacer les scores actuels
                scoreVue.effacerScores();
                
                // Traiter les nouveaux scores
                String[] joueurs = scoresData.split(";");
                for (String j : joueurs) {
                    if (!j.isEmpty()) {
                        String[] donnees = j.split(",");
                        if (donnees.length >= 2) {
                            String nom = donnees[0];
                            int score = Integer.parseInt(donnees[1]);
                            // Ajout du score à la vue
                            scoreVue.ajouterScore(nom, score);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Met à jour l'information du joueur dont c'est le tour.
     * Détermine si c'est au tour du joueur local et met à jour l'interface en conséquence.
     * 
     * @param nomJoueur Le nom du joueur dont c'est le tour
     */
    public void mettreAJourJoueurActuel(String nomJoueur) {
        // Détermine si c'est au tour du joueur local
        estMonTour = nomJoueur.equals(this.nomJoueur);
        
        Platform.runLater(() -> {
            // Mise en évidence du joueur actuel dans la vue des scores
            if (scoreVue != null) {
                scoreVue.marquerJoueurActuel(nomJoueur);
            }
            
            // Activation/désactivation des contrôles en fonction du tour
            if (mainJoueurVue != null) {
                mainJoueurVue.activerControles(estMonTour);
            }
        });
    }
    
    /**
     * Met à jour le mot imposé pour le joueur (variante du jeu).
     * 
     * @param mot Le mot imposé ou null/vide si aucun
     */
    public void mettreAJourMotImpose(String mot) {
        this.motImpose = mot;
        Platform.runLater(() -> {
            if (mainJoueurVue != null) {
                mainJoueurVue.afficherMotImpose(mot);
            }
        });
    }
    
    /**
     * Met à jour les informations sur le sac de lettres.
     * Format des données: "lettre1,valeur1;lettre2,valeur2;..."
     * 
     * @param sacData Les données du sac au format texte
     */
    public void mettreAJourSac(String sacData) {
        if (sacInfoVue != null && sacData != null && !sacData.isEmpty()) {
            // Traitement des données du sac
            List<Lettre> lettresSac = new ArrayList<>();
            String[] lettres = sacData.split(";");
            
            for (String l : lettres) {
                if (!l.isEmpty()) {
                    String[] donnees = l.split(",");
                    if (donnees.length >= 2) {
                        char caractere = donnees[0].charAt(0);
                        int valeur = Integer.parseInt(donnees[1]);
                        lettresSac.add(new Lettre(caractere, valeur));
                    }
                }
            }
            
            // Mise à jour de la vue du sac
            Platform.runLater(() -> sacInfoVue.mettreAJourLettres(lettresSac));
        }
    }
    
    /**
     * Demande au serveur de placer un mot sur le plateau.
     * Vérifie d'abord que c'est bien au tour du joueur.
     * 
     * @param mot Le mot à placer
     * @param x La coordonnée x de la première lettre
     * @param y La coordonnée y de la première lettre
     * @param horizontal true si le mot est horizontal, false si vertical
     * @return true si la demande a été envoyée, false sinon
     */
    public boolean placerMot(String mot, int x, int y, boolean horizontal) {
        if (estMonTour) {
            client.placerMot(mot, x, y, horizontal);
            return true;
        } else {
            afficherErreur("Ce n'est pas votre tour !");
            return false;
        }
    }
    
    /**
     * Demande au serveur de passer le tour du joueur.
     * Vérifie d'abord que c'est bien au tour du joueur.
     */
    public void passerTour() {
        if (estMonTour) {
            client.passerTour();
        } else {
            afficherErreur("Ce n'est pas votre tour !");
        }
    }
    
    /**
     * Valide le mot provisoirement placé sur le plateau.
     * Délègue l'action au sous-contrôleur du plateau.
     */
    public void validerMotPlace() {
        if (plateauControleur != null) {
            plateauControleur.validerMotPlace();
        }
    }
    
    /**
     * Demande au serveur de terminer la partie prématurément.
     */
    public void terminerPartiePrematurement() {
        if (client != null && client.estConnecte()) {
            client.envoyerMessage("TERMINER_PARTIE");
        }
    }
    
    /**
     * Active ou désactive le bouton de validation de mot.
     * Délègue l'action au sous-contrôleur des lettres.
     * 
     * @param actif true pour activer, false pour désactiver
     */
    public void activerBoutonValider(boolean actif) {
        if (lettresControleur != null) {
            lettresControleur.activerBoutonValider(actif);
        }
    }
    
    /**
     * Gère la déconnexion du serveur.
     * Affiche une boîte de dialogue d'erreur.
     */
    public void deconnexionServeur() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Déconnexion");
            alert.setHeaderText("Déconnecté du serveur");
            alert.setContentText("La connexion avec le serveur a été perdue.");
            alert.showAndWait();
            
            // Possibilité de revenir à l'écran de connexion ici
        });
    }
    
    /**
     * Gère la fin normale d'une partie.
     * Affiche une boîte de dialogue d'information.
     */
    public void terminerPartie() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin de partie");
            alert.setHeaderText("La partie est terminée");
            alert.setContentText("Merci d'avoir joué à Scrabble !");
            alert.showAndWait();
            
            // Possibilité de revenir à l'écran de connexion ici
        });
    }
    
    /**
     * Définit la référence vers la vue des lettres du joueur.
     * 
     * @param mainJoueurVue La vue des lettres
     */
    public void setMainJoueurVue(MainJoueurVue mainJoueurVue) {
        this.mainJoueurVue = mainJoueurVue;
        if (lettresControleur != null) {
            lettresControleur.setMainJoueurVue(mainJoueurVue);
        }
    }
    
    /**
     * Définit la référence vers la vue des scores.
     * 
     * @param scoreVue La vue des scores
     */
    public void setScoreVue(ScoreVue scoreVue) {
        this.scoreVue = scoreVue;
    }
    
    /**
     * Définit la référence vers la vue du sac de lettres.
     * 
     * @param sacInfoVue La vue du sac
     */
    public void setSacInfoVue(SacInfoVue sacInfoVue) {
        this.sacInfoVue = sacInfoVue;
    }
    
    /**
     * Retourne les lettres que le joueur a actuellement en main.
     * 
     * @return Liste des lettres en main
     */
    public List<Lettre> getLettresEnMain() {
        return lettresEnMain;
    }
    
    /**
     * Indique si c'est actuellement au tour du joueur.
     * 
     * @return true si c'est le tour du joueur, false sinon
     */
    public boolean estMonTour() {
        return estMonTour;
    }
    
    /**
     * Retourne le mot imposé actuel (variante du jeu).
     * 
     * @return Le mot imposé ou null si aucun
     */
    public String getMotImpose() {
        return motImpose;
    }
    
    /**
     * Définit le nom du joueur.
     * 
     * @param nom Le nom à définir
     */
    public void setNomJoueur(String nom) {
        this.nomJoueur = nom;
    }
    
    /**
     * Retourne le nom du joueur.
     * 
     * @return Le nom du joueur
     */
    public String getNomJoueur() {
        return nomJoueur;
    }
    
    /**
     * Retourne le sous-contrôleur des lettres, en le créant si nécessaire.
     * 
     * @return Le sous-contrôleur des lettres
     */
    public LettresControleur getLettresControleur() {
        if (lettresControleur == null) {
            lettresControleur = new LettresControleur(this);
        }
        return lettresControleur;
    }
    
    /**
     * Retourne le sous-contrôleur du plateau, en le créant si nécessaire.
     * 
     * @return Le sous-contrôleur du plateau
     */
    public PlateauControleur getPlateauControleur() {
        if (plateauControleur == null) {
            plateauControleur = new PlateauControleur(this, getLettresControleur());
        }
        return plateauControleur;
    }

    /**
     * Remet une lettre dans la main du joueur (utile lors de l'annulation d'un placement).
     * 
     * @param lettre Le caractère de la lettre à remettre en main
     */
    public void retournerLettreALaMain(char lettre) {
        // Vérifier si la lettre existe déjà dans la main
        boolean lettreExistante = false;
        
        for (Lettre l : lettresEnMain) {
            if (l.getCaractere() == lettre) {
                lettreExistante = true;
                break;
            }
        }
        
        // Si la lettre n'existe pas, l'ajouter
        if (!lettreExistante) {
            Lettre nouvelleLettres = new Lettre(lettre, 1); // Valeur par défaut
            lettresEnMain.add(nouvelleLettres);
        }
        
        // Mettre à jour l'affichage
        if (mainJoueurVue != null) {
            Platform.runLater(() -> {
                mainJoueurVue.mettreAJourLettres(lettresEnMain);
                // Réactiver les contrôles si c'est toujours le tour du joueur
                if (estMonTour) {
                    mainJoueurVue.activerControles(true);
                }
            });
        }
    }
    
    /**
     * Vérifie si un mot peut être placé provisoirement avant envoi au serveur.
     * 
     * @param mot Le mot à vérifier
     * @param x La coordonnée x de la première lettre
     * @param y La coordonnée y de la première lettre
     * @param horizontal true si le mot est horizontal, false si vertical
     * @return true si le placement est possible, false sinon
     */
    public boolean verifierMotProvisoire(String mot, int x, int y, boolean horizontal) {
        if (plateauControleur != null) {
            // Convertir la chaîne de caractères en liste de lettres
            List<Lettre> lettres = new ArrayList<>();
            for (char c : mot.toCharArray()) {
                Lettre lettre = lettresControleur.trouverLettre(c);
                if (lettre == null) {
                    afficherErreur("Lettre " + c + " non disponible dans votre main");
                    return false;
                }
                lettres.add(lettre);
            }
            
            // Utiliser explicitement Direction de modele
            return plateauControleur.verifierMotProvisoire(
                lettres, 
                x, 
                y, 
                horizontal ? modele.Direction.HORIZONTAL : modele.Direction.VERTICAL
            );
        }
        return false;
    }

    /**
     * Annule le placement provisoire d'un mot et retourne les lettres dans la main.
     */
    public void annulerPlacementMot() {
        if (plateauVue != null) {
            // Récupérer les lettres placées provisoirement
            List<java.awt.Point> lettresPlacees = plateauVue.getLettresPlacees();
            
            // Pour chaque lettre placée
            for (java.awt.Point p : lettresPlacees) {
                // Récupérer le caractère
                char lettre = plateauVue.getLettre((int)p.getX(), (int)p.getY()).charAt(0);
                
                // Remettre la lettre dans la main du joueur
                retournerLettreALaMain(lettre);
                
                // Effacer visuellement la lettre du plateau
                plateauVue.effacerLettre((int)p.getX(), (int)p.getY());
            }
            
            // Réinitialiser le suivi des lettres placées
            plateauVue.reinitialiserLettresPlacees();
            
            // Désactiver les boutons de validation et d'annulation
            activerBoutonValider(false);
            if (mainJoueurVue != null) {
                mainJoueurVue.activerBoutonAnnulation(false);
            }
        }
    }
    
    /**
     * Définit la référence vers la vue du plateau.
     * 
     * @param plateauVue La vue du plateau
     */
    public void setPlateauVue(PlateauVue plateauVue) {
        this.plateauVue = plateauVue;
        
        // Propager la référence aux sous-contrôleurs
        if (plateauControleur != null) {
            plateauControleur.setPlateauVue(plateauVue);
        }
        
        // Connecter la vue du plateau à la vue des lettres
        if (mainJoueurVue != null) {
            plateauVue.setMainJoueurVue(mainJoueurVue);
        }
    }

    /**
     * Initialise l'application et vérifie le dictionnaire.
     */
    public void initialiserApplication() {
        System.out.println("Démarrage de l'initialisation de l'application...");
        
        // Vérifier l'initialisation du dictionnaire
        Dictionnaire dictionnaire = new Dictionnaire();
        dictionnaire.verifierInitialisation();
        
        System.out.println("Vérification du dictionnaire terminée");
    }

    /**
     * Traite un message d'erreur reçu du serveur et affiche le message approprié.
     * 
     * @param message Le message d'erreur reçu du serveur
     */
    public void traiterMessageErreur(String message) {
        // Traiter le message avant de créer la variable finale
        final String erreurMessage;
        
        // Si le message est au format "ERREUR|Message détaillé"
        if (message.contains("|")) {
            erreurMessage = message.split("\\|")[1]; // Extraire le message détaillé
        } else {
            erreurMessage = message;
        }
        
        // Afficher le message d'erreur précis
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de placement du mot");
            alert.setContentText(erreurMessage);
            alert.showAndWait();
        });
    }

    /**
     * Affiche une erreur générique (pour les erreurs côté client).
     * 
     * @param message Le message d'erreur à afficher
     */
    public void afficherErreur(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur s'est produite");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Met à jour l'affichage du numéro de tour dans la vue des scores.
     * 
     * @param numeroTour Le numéro du tour à afficher
     */
    public void mettreAJourTour(int numeroTour) {
        System.out.println("DEBUG CONTROLEUR - Mise à jour du tour reçue : " + numeroTour);
        
        // Utilisation de Platform.runLater pour garantir l'exécution sur le thread JavaFX
        Platform.runLater(() -> {
            System.out.println("DEBUG CONTROLEUR - Mise à jour du tour sur le thread JavaFX");
            if (scoreVue != null) {
                scoreVue.mettreAJourTourGlobal(numeroTour);
            } else {
                System.err.println("ERREUR - ScoreVue est null");
            }
        });
    }

    /**
     * Met à jour l'affichage du tour global dans la vue des scores.
     * 
     * @param tour Le numéro du tour global
     */
    public void mettreAJourTourGlobal(int tour) {
        System.out.println("DEBUG ScoreVue : Mise à jour du tour global à " + tour);

        if (scoreVue != null) {
            Platform.runLater(() -> {
                scoreVue.mettreAJourTourGlobal(tour);
            });
        }
    }
}