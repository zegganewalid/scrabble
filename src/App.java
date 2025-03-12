import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {
    
    private Partie partie;
    private GridPane plateauGrid;
    private HBox mainJoueur;
    private Label scoreLabel;
    private Label lettresRestantesLabel;
    private Label joueurActuelLabel;
    private Label motImposeLabel;
    private Stage primaryStage;
    private List<PlacementTemporaire> placementsTemporaires = new ArrayList<>();
    private static final DataFormat LETTRE_FORMAT = new DataFormat("application/lettre");
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Scrabble");
        
        // Adapter la taille de la fenêtre à l'écran de l'utilisateur
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.95;
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight() * 0.95;
        
        VBox startScreen = createStartScreen();
        Scene startScene = new Scene(startScreen, screenWidth * 0.8, screenHeight * 0.8);
        
        primaryStage.setScene(startScene);
        primaryStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - screenWidth * 0.8) / 2);
        primaryStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - screenHeight * 0.8) / 2);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    
    private VBox createStartScreen() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        
        Label titleLabel = new Label("SCRABBLE");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        
        Label promptLabel = new Label("Nombre de joueurs (2-4):");
        
        Spinner<Integer> playerCountSpinner = new Spinner<>(2, 4, 2);
        playerCountSpinner.setEditable(true);
        playerCountSpinner.setPrefWidth(100);
        
        Button startButton = new Button("Commencer");
        startButton.setOnAction(e -> {
            int nbJoueurs = playerCountSpinner.getValue();
            List<String> nomsJoueurs = demanderNomsJoueurs(nbJoueurs);
            if (nomsJoueurs != null) {
                demarrerPartie(nomsJoueurs);
            }
        });
        
        vbox.getChildren().addAll(titleLabel, promptLabel, playerCountSpinner, startButton);
        return vbox;
    }
    
    private List<String> demanderNomsJoueurs(int nbJoueurs) {
        List<String> noms = new ArrayList<>();
        
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Noms des joueurs");
        dialog.setHeaderText("Entrez les noms des " + nbJoueurs + " joueurs");
        
        ButtonType buttonTypeOk = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        List<TextField> textFields = new ArrayList<>();
        
        for (int i = 0; i < nbJoueurs; i++) {
            TextField nameField = new TextField();
            nameField.setPromptText("Joueur " + (i+1));
            grid.add(new Label("Joueur " + (i+1) + ":"), 0, i);
            grid.add(nameField, 1, i);
            textFields.add(nameField);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk) {
                List<String> result = new ArrayList<>();
                for (TextField textField : textFields) {
                    String nom = textField.getText().trim();
                    result.add(nom.isEmpty() ? "Joueur" : nom);
                }
                return result;
            }
            return null;
        });
        
        Optional<List<String>> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    private void demarrerPartie(List<String> nomsJoueurs) {
        partie = new Partie(nomsJoueurs);
        creerEcranDeJeu();
    }
    
    private void creerEcranDeJeu() {
        // Adapter la taille de la fenêtre à l'écran de l'utilisateur
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.95;
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight() * 0.95;
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // En-tête avec les informations de jeu
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        joueurActuelLabel = new Label("Joueur actuel: " + partie.getJoueurActuel().getNom());
        joueurActuelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        scoreLabel = new Label("Score: " + partie.getJoueurActuel().getScore());
        lettresRestantesLabel = new Label("Lettres restantes: " + partie.getSac().lettres.size());
        
        Joueur joueurActuel = partie.getJoueurActuel();
        String motImpose = joueurActuel.getMotImpose();
        motImposeLabel = new Label("Mot imposé: " + (motImpose != null ? motImpose : "Aucun"));
        
        headerBox.getChildren().addAll(joueurActuelLabel, scoreLabel, lettresRestantesLabel, motImposeLabel);
        root.setTop(headerBox);
        
        // Plateau central
        plateauGrid = creerPlateauUI();
        
        // Création d'un conteneur pour centrer le plateau
        StackPane centeredPlateauContainer = new StackPane(plateauGrid);
        centeredPlateauContainer.setAlignment(Pos.CENTER);
        
        ScrollPane plateauScroll = new ScrollPane(centeredPlateauContainer);
        plateauScroll.setFitToWidth(true);
        plateauScroll.setFitToHeight(true);
        plateauScroll.setPannable(true);  // Permet de déplacer le contenu avec la souris
        root.setCenter(plateauScroll);
        
        // Main du joueur en bas
        mainJoueur = new HBox(10);
        mainJoueur.setPadding(new Insets(10));
        mainJoueur.setAlignment(Pos.CENTER);
        mettreAJourMainJoueur();
        
        // Ajouter une zone pour déposer les lettres (pour annuler les placements)
        StackPane dropZone = new StackPane();
        dropZone.setPrefSize(400, 60);
        dropZone.setStyle("-fx-border-color: grey; -fx-border-style: dashed; -fx-background-color: #f0f0f0;");
        Label dropLabel = new Label("Glissez ici pour annuler un placement");
        dropZone.getChildren().add(dropLabel);
        
        // Configurer la zone de dépôt pour accepter les lettres
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && 
                event.getDragboard().hasContent(LETTRE_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasContent(LETTRE_FORMAT)) {
                String data = (String) db.getContent(LETTRE_FORMAT);
                try {
                    // Vérifier si c'est une lettre du plateau temporaire
                    if (data.startsWith("temp:")) {
                        String[] parts = data.substring(5).split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        
                        // Trouver et supprimer le placement temporaire
                        PlacementTemporaire placement = trouverPlacementTemporaire(x, y);
                        if (placement != null) {
                            placementsTemporaires.remove(placement);
                            mettreAJourInterface();
                            success = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button validerButton = new Button("Valider placement");
        validerButton.setOnAction(e -> validerPlacementTemporaire());
        
        Button annulerButton = new Button("Annuler placement");
        annulerButton.setOnAction(e -> annulerPlacementTemporaire());
        
        Button passerButton = new Button("Passer");
        passerButton.setOnAction(e -> {
            partie.passerAuJoueurSuivant();
            mettreAJourInterface();
        });
        
        actionButtons.getChildren().addAll(validerButton, annulerButton, passerButton);
        bottomBox.getChildren().addAll(new Label("Zone d'annulation:"), dropZone, new Label("Votre main:"), mainJoueur, actionButtons);
        
        root.setBottom(bottomBox);
        
        Scene gameScene = new Scene(root, screenWidth, screenHeight);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Scrabble - Tour de " + partie.getJoueurActuel().getNom());
        primaryStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - screenWidth) / 2);
        primaryStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - screenHeight) / 2);
        primaryStage.setMaximized(true);
    }
    
    private GridPane creerPlateauUI() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);
        
        // Calculer la taille des cellules en fonction de la taille de l'écran
        double cellSize = Math.min(
            Screen.getPrimary().getVisualBounds().getWidth() * 0.045,
            Screen.getPrimary().getVisualBounds().getHeight() * 0.045
        );
        
        // Ajouter les en-têtes de colonnes
        for (int i = 0; i < 15; i++) {
            Label colLabel = new Label(String.valueOf(i));
            colLabel.setPrefWidth(cellSize);
            colLabel.setAlignment(Pos.CENTER);
            grid.add(colLabel, i + 1, 0);
        }
        
        // Ajouter les en-têtes de lignes
        for (int i = 0; i < 15; i++) {
            Label rowLabel = new Label(String.valueOf(i));
            rowLabel.setPrefHeight(cellSize);
            rowLabel.setAlignment(Pos.CENTER);
            grid.add(rowLabel, 0, i + 1);
        }
        
        // Créer les cases du plateau
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 15; x++) {
                StackPane cellPane = new StackPane();
                cellPane.setPrefSize(cellSize, cellSize);
                final int finalX = x;
                final int finalY = y;
                
                Case caseActuelle = partie.getPlateau().getCase(x, y);
                Label cellLabel = new Label();
                cellLabel.setAlignment(Pos.CENTER);
                
                // Vérifier s'il y a un placement temporaire sur cette case
                PlacementTemporaire placementTemp = trouverPlacementTemporaire(x, y);
                
                if (placementTemp != null) {
                    // Afficher le placement temporaire
                    cellLabel.setText(String.valueOf(placementTemp.getLettre().getCaractere()));
                    cellPane.setStyle("-fx-background-color: lightgreen;");
                    
                    // Permettre de glisser-déposer une lettre placée temporairement
                    cellPane.setOnDragDetected(event -> {
                        Dragboard db = cellPane.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        // Ajouter un préfixe pour indiquer qu'il s'agit d'un placement temporaire
                        content.put(LETTRE_FORMAT, "temp:" + finalX + "," + finalY);
                        db.setContent(content);
                        event.consume();
                    });
                } else if (caseActuelle.isEstOccupe()) {
                    cellLabel.setText(String.valueOf(caseActuelle.getLettre().getCaractere()));
                    cellPane.setStyle("-fx-background-color: lightgrey;");
                } else {
                    String bonus = caseActuelle.getTypeBonus();
                    if (bonus != null) {
                        switch (bonus) {
                            case "MT":
                                cellPane.setStyle("-fx-background-color: red;");
                                cellLabel.setText("MT");
                                break;
                            case "MD":
                                cellPane.setStyle("-fx-background-color: pink;");
                                cellLabel.setText("MD");
                                break;
                            case "LT":
                                cellPane.setStyle("-fx-background-color: darkblue;");
                                cellLabel.setText("LT");
                                cellLabel.setTextFill(Color.WHITE);
                                break;
                            case "LD":
                                cellPane.setStyle("-fx-background-color: lightblue;");
                                cellLabel.setText("LD");
                                break;
                        }
                    } else {
                        cellPane.setStyle("-fx-background-color: white; -fx-border-color: grey;");
                    }
                    
                    // Configurer la case pour accepter les lettres déposées
                    cellPane.setOnDragOver(event -> {
                        if (event.getGestureSource() != cellPane && 
                            event.getDragboard().hasContent(LETTRE_FORMAT) && 
                            !partie.getPlateau().getCase(finalX, finalY).isEstOccupe() &&
                            trouverPlacementTemporaire(finalX, finalY) == null) {
                            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        }
                        event.consume();
                    });
                    
                    cellPane.setOnDragDropped(event -> {
                        Dragboard db = event.getDragboard();
                        boolean success = false;
                        
                        if (db.hasContent(LETTRE_FORMAT)) {
                            String lettreInfo = (String) db.getContent(LETTRE_FORMAT);
                            
                            // Vérifier si c'est une lettre de la main du joueur (pas de préfixe)
                            if (!lettreInfo.startsWith("temp:")) {
                                int index = Integer.parseInt(lettreInfo);
                                Lettre lettre = partie.getJoueurActuel().getLettresEnMain().get(index);
                                
                                // Ajouter un placement temporaire
                                placementsTemporaires.add(new PlacementTemporaire(lettre, finalX, finalY, index));
                                
                                // Mettre à jour l'interface
                                mettreAJourInterface();
                                
                                success = true;
                            }
                        }
                        
                        event.setDropCompleted(success);
                        event.consume();
                    });
                }
                
                cellPane.getChildren().add(cellLabel);
                grid.add(cellPane, x + 1, y + 1);
            }
        }
        
        return grid;
    }
    
    private void mettreAJourMainJoueur() {
        mainJoueur.getChildren().clear();
        
        Joueur joueur = partie.getJoueurActuel();
        List<Lettre> lettres = joueur.getLettresEnMain();
        
        for (int i = 0; i < lettres.size(); i++) {
            Lettre lettre = lettres.get(i);
            
            // Vérifier si la lettre est déjà placée temporairement
            boolean estPlacee = false;
            for (PlacementTemporaire placement : placementsTemporaires) {
                if (placement.getIndexMain() == i) {
                    estPlacee = true;
                    break;
                }
            }
            
            if (!estPlacee) {
                StackPane lettrePane = new StackPane();
                lettrePane.setPrefSize(45, 45);
                lettrePane.setStyle("-fx-background-color: beige; -fx-border-color: black;");
                
                VBox lettreBox = new VBox(2);
                lettreBox.setAlignment(Pos.CENTER);
                
                Label caractereLabel = new Label(String.valueOf(lettre.getCaractere()));
                caractereLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                
                Label valeurLabel = new Label(String.valueOf(lettre.getValeur()));
                valeurLabel.setFont(Font.font("Arial", 10));
                valeurLabel.setAlignment(Pos.BOTTOM_RIGHT);
                
                lettreBox.getChildren().addAll(caractereLabel, valeurLabel);
                lettrePane.getChildren().add(lettreBox);
                
                // Index pour identifier la lettre lors du glisser-déposer
                final int index = i;
                
                // Configurer le glisser-déposer
                lettrePane.setOnDragDetected(event -> {
                    Dragboard db = lettrePane.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.put(LETTRE_FORMAT, String.valueOf(index));
                    db.setContent(content);
                    event.consume();
                });
                
                mainJoueur.getChildren().add(lettrePane);
            } else {
                // Espace vide pour les lettres déjà placées
                StackPane emptyPane = new StackPane();
                emptyPane.setPrefSize(45, 45);
                emptyPane.setStyle("-fx-border-color: lightgrey; -fx-border-style: dashed;");
                mainJoueur.getChildren().add(emptyPane);
            }
        }
    }
    
    private PlacementTemporaire trouverPlacementTemporaire(int x, int y) {
        for (PlacementTemporaire placement : placementsTemporaires) {
            if (placement.getX() == x && placement.getY() == y) {
                return placement;
            }
        }
        return null;
    }
    
    private void validerPlacementTemporaire() {
        if (placementsTemporaires.isEmpty()) {
            afficherErreur("Aucun placement à valider !");
            return;
        }
        
        // Vérifier si les lettres sont placées en ligne (horizontale ou verticale)
        Direction direction = determinerDirection();
        if (direction == null) {
            afficherErreur("Les lettres doivent être placées en ligne (horizontale ou verticale) !");
            return;
        }
        
        // Trier les placements par position
        placementsTemporaires.sort((p1, p2) -> {
            if (direction == Direction.HORIZONTAL) {
                return Integer.compare(p1.getX(), p2.getX());
            } else {
                return Integer.compare(p1.getY(), p2.getY());
            }
        });
        
        // Vérifier s'il y a des trous dans le placement
        boolean estContinuous = verifierContinuite(direction);
        if (!estContinuous) {
            afficherErreur("Le mot doit être continu (sans trous) !");
            return;
        }
        
        // Extraire les lettres à placer
        List<Lettre> lettresAJouer = new ArrayList<>();
        for (PlacementTemporaire placement : placementsTemporaires) {
            lettresAJouer.add(placement.getLettre());
        }
        
        // Coordonnées du premier placement
        int x = placementsTemporaires.get(0).getX();
        int y = placementsTemporaires.get(0).getY();
        
        // Tenter de placer le mot
        try {
            if (partie.placerMot(lettresAJouer, x, y, direction)) {
                // Supprimer les lettres placées de la main du joueur
                // Note: on les supprime dans l'ordre décroissant des indices pour ne pas perturber les indices
                List<Integer> indicesASupprimer = new ArrayList<>();
                for (PlacementTemporaire placement : placementsTemporaires) {
                    indicesASupprimer.add(placement.getIndexMain());
                }
                indicesASupprimer.sort((a, b) -> Integer.compare(b, a)); // Tri décroissant
                
                for (int index : indicesASupprimer) {
                    partie.getJoueurActuel().getLettresEnMain().remove(index);
                }
                
                // Piocher de nouvelles lettres
                partie.getJoueurActuel().piocherLettres(partie.getSac(), indicesASupprimer.size());
                
                // Vider les placements temporaires
                placementsTemporaires.clear();
                
                mettreAJourInterface();
            } else {
                afficherErreur("Le mot ne peut pas être placé ici !");
            }
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
        }
    }
    
    private Direction determinerDirection() {
        if (placementsTemporaires.size() == 1) {
            // Un seul placement peut être soit horizontal soit vertical
            return Direction.HORIZONTAL; // Par défaut
        }
        
        boolean estHorizontal = true;
        boolean estVertical = true;
        int yRef = placementsTemporaires.get(0).getY();
        int xRef = placementsTemporaires.get(0).getX();
        
        for (PlacementTemporaire placement : placementsTemporaires) {
            if (placement.getY() != yRef) {
                estHorizontal = false;
            }
            if (placement.getX() != xRef) {
                estVertical = false;
            }
        }
        
        if (estHorizontal) {
            return Direction.HORIZONTAL;
        } else if (estVertical) {
            return Direction.VERTICAL;
        } else {
            return null; // Ni horizontal ni vertical
        }
    }
    
    private boolean verifierContinuite(Direction direction) {
        if (placementsTemporaires.size() <= 1) {
            return true; // Un seul placement est toujours continu
        }
        
        // Trier les placements par position
        placementsTemporaires.sort((p1, p2) -> {
            if (direction == Direction.HORIZONTAL) {
                return Integer.compare(p1.getX(), p2.getX());
            } else {
                return Integer.compare(p1.getY(), p2.getY());
            }
        });
        
        // Vérifier s'il y a des trous
        for (int i = 1; i < placementsTemporaires.size(); i++) {
            int posPrecedente, posActuelle;
            
            if (direction == Direction.HORIZONTAL) {
                posPrecedente = placementsTemporaires.get(i-1).getX();
                posActuelle = placementsTemporaires.get(i).getX();
            } else {
                posPrecedente = placementsTemporaires.get(i-1).getY();
                posActuelle = placementsTemporaires.get(i).getY();
            }
            
            // Vérifier s'il y a un trou entre deux positions
            if (posActuelle - posPrecedente > 1) {
                // Vérifier si les cases intermédiaires sont occupées
                for (int pos = posPrecedente + 1; pos < posActuelle; pos++) {
                    int x = direction == Direction.HORIZONTAL ? pos : placementsTemporaires.get(0).getX();
                    int y = direction == Direction.VERTICAL ? pos : placementsTemporaires.get(0).getY();
                    
                    if (!partie.getPlateau().getCase(x, y).isEstOccupe()) {
                        return false; // Trou détecté
                    }
                }
            }
        }
        
        return true;
    }
    
    private void annulerPlacementTemporaire() {
        placementsTemporaires.clear();
        mettreAJourInterface();
    }
    
    private void mettreAJourInterface() {
        joueurActuelLabel.setText("Joueur actuel: " + partie.getJoueurActuel().getNom());
        scoreLabel.setText("Score: " + partie.getJoueurActuel().getScore());
        lettresRestantesLabel.setText("Lettres restantes: " + partie.getSac().lettres.size());
        
        Joueur joueurActuel = partie.getJoueurActuel();
        String motImpose = joueurActuel.getMotImpose();
        motImposeLabel.setText("Mot imposé: " + (motImpose != null ? motImpose : "Aucun"));
        
        // Mettre à jour le plateau
        plateauGrid = creerPlateauUI();
        StackPane centeredPlateauContainer = new StackPane(plateauGrid);
        centeredPlateauContainer.setAlignment(Pos.CENTER);
        ((ScrollPane) ((BorderPane) primaryStage.getScene().getRoot()).getCenter()).setContent(centeredPlateauContainer);
        
        // Mettre à jour la main du joueur
        mettreAJourMainJoueur();
        
        primaryStage.setTitle("Scrabble - Tour de " + partie.getJoueurActuel().getNom());
        
        // Vérifier si la partie est terminée
        if (partie.estPartieTerminee()) {
            afficherFinDePartie();
        }
    }
    
    private void afficherFinDePartie() {
        partie.terminerPartie();
        Joueur vainqueur = partie.determinerVainqueur();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin de la partie");
        alert.setHeaderText("La partie est terminée !");
        
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Vainqueur: ").append(vainqueur.getNom())
                     .append(" avec ").append(vainqueur.getScore()).append(" points\n\n");
        
        messageBuilder.append("Résultats:\n");
        for (Joueur joueur : partie.getJoueurs()) {
            messageBuilder.append("- ").append(joueur.getNom())
                         .append(": ").append(joueur.getScore()).append(" points\n");
        }
        
        alert.setContentText(messageBuilder.toString());
        alert.showAndWait();
        
        // Retour à l'écran principal
        VBox startScreen = createStartScreen();
        Scene startScene = new Scene(startScreen, 600, 400);
        primaryStage.setScene(startScene);
    }
    
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<String> chargerMotsDepuisFichier() {
        List<String> mots = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/database/mots.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                mots.add(line);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dictionnaire: " + e.getMessage());
        }
        return mots;
    }
    
    private class PlacementTemporaire {
        private Lettre lettre;
        private int x;
        private int y;
        private int indexMain;
        
        public PlacementTemporaire(Lettre lettre, int x, int y, int indexMain) {
            this.lettre = lettre;
            this.x = x;
            this.y = y;
            this.indexMain = indexMain;
        }
        
        public Lettre getLettre() {
            return lettre;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getIndexMain() {
            return indexMain;
        }
    }
}