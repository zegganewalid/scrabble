package vue;

import java.net.URL;
import java.util.Optional;

import javafx.application.Application;      // Classe de base pour les applications JavaFX
import javafx.geometry.Insets;              // Pour les marges et espacements
import javafx.scene.Scene;                  // Conteneur principal de l'interface utilisateur
import javafx.scene.control.Alert;          // Pour afficher des messages d'alerte
import javafx.scene.control.Button;         // Composant bouton
import javafx.scene.control.ButtonType;     // Types de boutons pour les boîtes de dialogue
import javafx.scene.control.Label;          // Composant pour afficher du texte
import javafx.scene.control.TextField;      // Champ de saisie de texte
import javafx.scene.layout.BorderPane;      // Layout à 5 zones (haut, bas, gauche, droite, centre)
import javafx.scene.layout.GridPane;        // Layout en grille
import javafx.scene.layout.HBox;            // Layout horizontal
import javafx.scene.layout.VBox;            // Layout vertical
import javafx.stage.Stage;                  // Fenêtre principale

import controleur.ClientControleur;         // Contrôleur gérant la logique client
import vue.SacInfoVue;                      // Vue affichant les informations sur le sac de lettres

/**
 * Classe principale de l'application Scrabble.
 * Elle gère l'interface utilisateur et coordonne les différentes vues.
 * Cette classe étend Application, le point d'entrée standard pour les applications JavaFX.
 */
public class ScrabbleApp extends Application {
    
    // Références aux composants principaux de l'application
    private ClientControleur controleur;    // Contrôleur gérant la logique du jeu
    private PlateauVue plateauVue;          // Vue du plateau de jeu
    private MainJoueurVue mainJoueurVue;    // Vue des lettres du joueur
    private ScoreVue scoreVue;              // Vue affichant les scores
    private SacInfoVue sacInfoVue;          // Vue affichant les informations sur le sac de lettres
    private Stage primaryStage;             // Fenêtre principale de l'application
    
    /**
     * Méthode de démarrage appelée automatiquement par JavaFX.
     * Initialise l'application et affiche l'écran de connexion.
     * 
     * @param primaryStage La fenêtre principale fournie par JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialisation du contrôleur
        this.controleur = new ClientControleur();
        this.controleur.initialiserApplication();
        
        // Affiche l'écran de connexion au démarrage
        afficherEcranConnexion();
        
        // Configure la fenêtre principale
        primaryStage.setTitle("Scrabble en réseau");
        
        // Gère la fermeture propre de l'application
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);  // Force la fermeture complète de l'application
        });
        
        // Affiche la fenêtre
        primaryStage.show();
    }
    
    /**
     * Crée et affiche l'écran de connexion au serveur.
     * C'est le premier écran que voit l'utilisateur.
     */
    private void afficherEcranConnexion() {
        // Crée la vue de connexion en lui passant le contrôleur
        ConnexionVue connexionVue = new ConnexionVue(controleur);
        
        // Configure un callback pour passer à l'interface de jeu une fois connecté
        // Utilise une expression lambda qui appelle creerInterfaceJeu()
        connexionVue.setOnConnexionReussie(() -> creerInterfaceJeu());
        
        // Crée une scène contenant la vue de connexion
        Scene scene = new Scene(connexionVue, 400, 300);
        
        // Associe la scène à la fenêtre principale
        primaryStage.setScene(scene);
    }
    
    /**
     * Crée l'interface principale du jeu après une connexion réussie.
     * Organise les différentes vues dans la fenêtre et configure les interactions.
     */
    private void creerInterfaceJeu() {
        // Crée un conteneur principal de type BorderPane (layout à 5 zones)
        BorderPane root = new BorderPane();
        
        // Initialise les différentes vues du jeu
        plateauVue = new PlateauVue(controleur);         // Vue du plateau
        mainJoueurVue = new MainJoueurVue(controleur);   // Vue des lettres du joueur
        scoreVue = new ScoreVue();                       // Vue des scores
        sacInfoVue = new SacInfoVue();                   // Vue des infos du sac
        
        // Fournit les références des vues au contrôleur pour qu'il puisse les mettre à jour
        controleur.setPlateauVue(plateauVue);
        controleur.setMainJoueurVue(mainJoueurVue);
        controleur.setScoreVue(scoreVue);
        controleur.setSacInfoVue(sacInfoVue);
        
        // Configure les interactions entre les vues
        // Par exemple, pour pouvoir glisser-déposer des lettres du rack vers le plateau
        plateauVue.setMainJoueurVue(mainJoueurVue);
        
        // Crée le bouton pour passer son tour
        Button passerTourButton = new Button("Passer le tour");
        passerTourButton.setOnAction(e -> controleur.passerTour());
        
        // Crée le bouton pour terminer la partie prématurément
        Button terminerPartieButton = new Button("Terminer la partie");
        terminerPartieButton.setOnAction(e -> {
            // Demande confirmation avant de terminer
            boolean confirme = demanderConfirmationFinPartie();
            if (confirme) {
                controleur.terminerPartiePrematurement();
            }
        });
        
        // Organise les boutons dans un conteneur horizontal
        HBox buttonsBox = new HBox(10, passerTourButton, terminerPartieButton);
        buttonsBox.setPadding(new Insets(10));  // Ajoute des marges internes
        
        // Placement des vues dans le BorderPane
        root.setCenter(plateauVue);      // Le plateau au centre
        root.setBottom(mainJoueurVue);   // Les lettres du joueur en bas
        
        // Regroupe les informations (scores, sac) dans un panneau vertical à droite
        VBox infoPanel = new VBox(10, scoreVue, sacInfoVue);
        root.setRight(infoPanel);
        
        // Place les boutons en haut
        root.setTop(buttonsBox);
        
        // Ajoute des marges externes aux composants principaux
        BorderPane.setMargin(plateauVue, new Insets(10));
        BorderPane.setMargin(mainJoueurVue, new Insets(10));
        BorderPane.setMargin(infoPanel, new Insets(10));
        
        // Crée une nouvelle scène contenant l'interface de jeu
        Scene scene = new Scene(root, 950, 700);
        
        // Tente de charger une feuille de style CSS
        URL cssUrl = getClass().getResource("/vue/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        
        // Associe la scène à la fenêtre principale et met à jour le titre
        primaryStage.setScene(scene);
        primaryStage.setTitle("Scrabble - En attente de joueurs...");
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation pour terminer la partie.
     * 
     * @return true si l'utilisateur confirme, false sinon
     */
    private boolean demanderConfirmationFinPartie() {
        // Crée une boîte de dialogue de type confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminer la partie");
        alert.setHeaderText("Êtes-vous sûr de vouloir terminer la partie ?");
        alert.setContentText("Les scores seront calculés et le vainqueur sera déterminé.");
        
        // Affiche la boîte de dialogue et attend la réponse de l'utilisateur
        Optional<ButtonType> result = alert.showAndWait();
        
        // Retourne true si l'utilisateur a cliqué sur OK
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Affiche un message d'erreur via le contrôleur.
     * 
     * @param message Le message d'erreur à afficher
     */
    private void afficherErreur(String message) {
        controleur.afficherErreur(message);
    }
    
    /**
     * Méthode appelée lors de la fermeture de l'application.
     * Assure une fermeture propre de l'application.
     */
    @Override
    public void stop() {
        // Force la fermeture complète de l'application
        System.exit(0);
    }
    
    /**
     * Point d'entrée principal de l'application.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Lance l'application JavaFX
        launch(args);
    }
}