package vue;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import controleur.ClientControleur;
import utils.ConfigurationJeu;

/**
 * Vue pour l'écran de connexion au serveur
 */
public class ConnexionVue extends VBox {
    
    private TextField adresseField;
    private TextField portField;
    private TextField nomField;
    private Button connecterButton;
    private Button quitterButton;
    private Label statutLabel;
    
    private ClientControleur controleur;
    private Runnable onConnexionReussie;
    
    public ConnexionVue(ClientControleur controleur) {
        this.controleur = controleur;
        
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);
        
        // Titre
        Text titre = new Text("Scrabble en réseau");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Formulaire de connexion
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        // Adresse du serveur
        Label adresseLabel = new Label("Adresse du serveur:");
        adresseField = new TextField(ConfigurationJeu.getServerAddress());
        adresseField.setPromptText("localhost");
        grid.add(adresseLabel, 0, 0);
        grid.add(adresseField, 1, 0);
        
        // Port
        Label portLabel = new Label("Port:");
        portField = new TextField(String.valueOf(ConfigurationJeu.getPort()));
        portField.setPromptText("5000");
        grid.add(portLabel, 0, 1);
        grid.add(portField, 1, 1);
        
        // Nom du joueur
        Label nomLabel = new Label("Votre nom:");
        nomField = new TextField();
        nomField.setPromptText("Joueur");
        grid.add(nomLabel, 0, 2);
        grid.add(nomField, 1, 2);
        
        // Boutons
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER);
        
        connecterButton = new Button("Se connecter");
        connecterButton.setDefaultButton(true);
        
        quitterButton = new Button("Quitter");
        
        boutons.getChildren().addAll(connecterButton, quitterButton);
        
        // Label de statut
        statutLabel = new Label("");
        statutLabel.setVisible(false);
        
        // Ajouter les composants à la vue
        getChildren().addAll(titre, grid, boutons, statutLabel);
        
        // Configurer les événements
        configurerEvenements();
    }
    
    private void configurerEvenements() {
        connecterButton.setOnAction(e -> {
            tenterConnexion();
        });
        
        quitterButton.setOnAction(e -> {
            System.exit(0);
        });
        
        // Permettre la connexion en appuyant sur Entrée dans les champs
        nomField.setOnAction(e -> tenterConnexion());
        adresseField.setOnAction(e -> tenterConnexion());
        portField.setOnAction(e -> tenterConnexion());
    }
    
    private void tenterConnexion() {
        // Valider les champs
        if (nomField.getText().trim().isEmpty()) {
            afficherErreur("Veuillez entrer votre nom");
            return;
        }
        
        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            adresse = "localhost";
            adresseField.setText(adresse);
        }
        
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
            if (port < 1024 || port > 65535) {
                afficherErreur("Le port doit être entre 1024 et 65535");
                return;
            }
        } catch (NumberFormatException ex) {
            afficherErreur("Le port doit être un nombre");
            return;
        }
        
        // Désactiver les contrôles pendant la tentative de connexion
        setControlesActifs(false);
        afficherStatut("Connexion en cours...");
        
        // Enregistrer le nom du joueur
        controleur.setNomJoueur(nomField.getText().trim());
        
        // Tenter la connexion via le contrôleur
        if (controleur.connecterAuServeur(adresse, port)) {
            afficherStatut("Connexion réussie. En attente des autres joueurs...");
            
            // Déclencher le callback si la connexion a réussi
            if (onConnexionReussie != null) {
                onConnexionReussie.run();
            }
        } else {
            afficherErreur("Impossible de se connecter au serveur");
            setControlesActifs(true);
        }
    }
    
    public void afficherErreur(String message) {
        statutLabel.setText("Erreur: " + message);
        statutLabel.setStyle("-fx-text-fill: red;");
        statutLabel.setVisible(true);
    }
    
    public void afficherStatut(String message) {
        statutLabel.setText(message);
        statutLabel.setStyle("-fx-text-fill: black;");
        statutLabel.setVisible(true);
    }
    
    private void setControlesActifs(boolean actif) {
        adresseField.setDisable(!actif);
        portField.setDisable(!actif);
        nomField.setDisable(!actif);
        connecterButton.setDisable(!actif);
    }
    
    // Getters pour les valeurs des champs
    public String getAdresse() {
        return adresseField.getText().trim();
    }
    
    public int getPort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            return ConfigurationJeu.getPort();
        }
    }
    
    public String getNomJoueur() {
        return nomField.getText().trim();
    }
    
    // Méthode pour définir le callback de connexion réussie
    public void setOnConnexionReussie(Runnable onConnexionReussie) {
        this.onConnexionReussie = onConnexionReussie;
    }
}