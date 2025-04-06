package vue;

import java.util.List;

import javafx.application.Platform;         // Pour exécuter du code sur le thread UI
import javafx.geometry.Insets;              // Pour les marges
import javafx.geometry.Pos;                 // Pour l'alignement des éléments
import javafx.scene.control.Button;         // Composant bouton
import javafx.scene.control.Label;          // Composant texte
import javafx.scene.input.ClipboardContent; // Pour le contenu du presse-papier lors du drag-and-drop
import javafx.scene.input.Dragboard;        // Pour gérer le drag-and-drop
import javafx.scene.input.TransferMode;     // Modes de transfert pour le drag-and-drop
import javafx.scene.layout.Background;      // Fond des composants
import javafx.scene.layout.BackgroundFill;  // Remplissage de fond
import javafx.scene.layout.Border;          // Bordure des composants
import javafx.scene.layout.BorderStroke;    // Style de bordure
import javafx.scene.layout.BorderStrokeStyle; // Type de bordure
import javafx.scene.layout.BorderWidths;    // Épaisseur de bordure
import javafx.scene.layout.CornerRadii;     // Rayons des coins
import javafx.scene.layout.HBox;            // Conteneur horizontal
import javafx.scene.layout.StackPane;       // Empilement de composants
import javafx.scene.layout.VBox;            // Conteneur vertical
import javafx.scene.paint.Color;            // Couleurs
import javafx.scene.text.Font;              // Police de caractères
import javafx.scene.text.FontWeight;        // Style de police

import controleur.ClientControleur;         // Contrôleur client
import modele.Lettre;                       // Classe représentant une lettre

/**
 * Vue représentant la main du joueur (ses lettres disponibles).
 * Cette classe permet d'afficher les lettres que le joueur peut jouer,
 * et gère le drag-and-drop pour placer ces lettres sur le plateau.
 * Elle hérite de VBox, un conteneur vertical JavaFX.
 */
public class MainJoueurVue extends VBox {
    
    private HBox lettresBox;                // Conteneur pour les lettres
    private Label motImposeLabel;           // Étiquette pour afficher le mot imposé
    private ClientControleur controleur;    // Référence au contrôleur
    private StackPane[] casesLettres;       // Tableau des cases pour les lettres
    private Label[] labelsLettres;          // Tableau des étiquettes de lettres
    private Button annulerPlacementButton;  // Bouton pour annuler un placement
    private Button validerMotButton;        // Bouton pour valider un mot placé
    
    /**
     * Constructeur qui initialise la vue de la main du joueur.
     * 
     * @param controleur Le contrôleur client qui gère la logique du jeu
     */
    public MainJoueurVue(ClientControleur controleur) {
        this.controleur = controleur;
        
        // Configuration du conteneur principal (VBox)
        setPadding(new Insets(10));     // Marge interne
        setSpacing(10);                 // Espacement entre les éléments
        setAlignment(Pos.CENTER);       // Centrage des éléments
        
        // Création de l'étiquette pour le mot imposé (variante du jeu)
        motImposeLabel = new Label("Mot imposé: -");
        motImposeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Création du conteneur pour les lettres (horizontal)
        lettresBox = new HBox(10);      // HBox avec 10 pixels d'espacement
        lettresBox.setAlignment(Pos.CENTER);
        
        // Initialisation des cases pour les lettres (maximum 7 dans Scrabble)
        casesLettres = new StackPane[7];
        labelsLettres = new Label[7];
        
        // Création des 7 cases pour les lettres
        for (int i = 0; i < 7; i++) {
            // Création de la case (conteneur StackPane)
            StackPane caseLettre = new StackPane();
            caseLettre.setPrefSize(50, 50);  // Taille de 50x50 pixels
            
            // Apparence de la case (fond beige et bordure noire)
            caseLettre.setBackground(new Background(new BackgroundFill(Color.BURLYWOOD, CornerRadii.EMPTY, Insets.EMPTY)));
            caseLettre.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            
            // Création de l'étiquette pour la lettre
            Label lettreLabel = new Label("");
            lettreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            caseLettre.getChildren().add(lettreLabel);
            
            // Stockage des références dans les tableaux
            casesLettres[i] = caseLettre;
            labelsLettres[i] = lettreLabel;
            
            // Configuration du mécanisme de drag-and-drop
            final int index = i;  // Index final pour l'utiliser dans les lambdas
            
            // Événement pour commencer le drag
            caseLettre.setOnDragDetected(event -> {
                // Vérifie que c'est le tour du joueur et que la case contient une lettre
                if (controleur.estMonTour() && !labelsLettres[index].getText().isEmpty()) {
                    // Démarre l'opération de drag-and-drop
                    Dragboard db = caseLettre.startDragAndDrop(TransferMode.COPY);
                    
                    // Prépare le contenu à transférer (la lettre)
                    ClipboardContent content = new ClipboardContent();
                    content.putString(labelsLettres[index].getText());
                    db.setContent(content);
                    
                    event.consume();
                }
            });

            // Événement quand le drag est terminé
            caseLettre.setOnDragDone(event -> {
                // Si le transfert a été effectué avec succès
                if (event.getTransferMode() == TransferMode.COPY) {
                    // Cache la lettre dans la main après un drop réussi
                    labelsLettres[index].setText("");
                    
                    // Retire également l'étiquette de valeur si elle existe
                    if (casesLettres[index].getChildren().size() > 1) {
                        casesLettres[index].getChildren().remove(1);
                    }
                }
                event.consume();
            });
            
            // Ajoute la case au conteneur des lettres
            lettresBox.getChildren().add(caseLettre);
        }
        
        // Création du bouton de validation de mot
        validerMotButton = new Button("Valider mot");
        validerMotButton.setDisable(true);  // Désactivé par défaut
        validerMotButton.setOnAction(e -> {
            controleur.validerMotPlace();
        });
        
        // Création du bouton pour passer le tour
        Button passerTourButton = new Button("Passer le tour");
        passerTourButton.setOnAction(e -> {
            controleur.passerTour();
        });

        // Création du bouton d'annulation de placement
        annulerPlacementButton = new Button("Annuler placement");
        annulerPlacementButton.setDisable(true);  // Désactivé par défaut
        annulerPlacementButton.setOnAction(e -> {
            // Demande au contrôleur d'annuler le placement en cours
            controleur.annulerPlacementMot();
        });
        
        // Création d'une barre de boutons horizontale
        HBox buttonsBox = new HBox(10, validerMotButton, passerTourButton, annulerPlacementButton);
        buttonsBox.setAlignment(Pos.CENTER);
        
        // Ajout de tous les éléments au conteneur principal (VBox)
        getChildren().addAll(motImposeLabel, lettresBox, buttonsBox);
    }
    
    /**
     * Active ou désactive le bouton d'annulation.
     * Utilise Platform.runLater pour s'assurer que cela se fait sur le thread JavaFX.
     * 
     * @param actif true pour activer le bouton, false pour le désactiver
     */
    public void activerBoutonAnnulation(boolean actif) {
        Platform.runLater(() -> {
            annulerPlacementButton.setDisable(!actif);
        });
    }
    
    /**
     * Met à jour l'affichage des lettres dans la main du joueur.
     * 
     * @param lettres La liste des lettres à afficher
     */
    public void mettreAJourLettres(List<Lettre> lettres) {
        // Efface d'abord toutes les lettres existantes
        for (int i = 0; i < 7; i++) {
            // Vide le texte de l'étiquette
            labelsLettres[i].setText("");
            
            // Retire l'étiquette de valeur si elle existe
            if (casesLettres[i].getChildren().size() > 1) {
                casesLettres[i].getChildren().remove(1);
            }
            
            // Couleur de fond en fonction du tour actuel
            // Vert clair si c'est le tour du joueur, beige sinon
            Color couleurFond = controleur.estMonTour() ? Color.LIGHTGREEN : Color.BURLYWOOD;
            casesLettres[i].setBackground(new Background(new BackgroundFill(couleurFond, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        
        // Affiche les nouvelles lettres
        int nombreLettres = Math.min(lettres.size(), 7);  // Maximum 7 lettres
        System.out.println("Nombre de lettres: " + nombreLettres);  // Log pour débogage
        
        // Pour chaque lettre à afficher
        for (int i = 0; i < nombreLettres; i++) {
            if (i < lettres.size()) {  // Vérification supplémentaire
                Lettre lettre = lettres.get(i);
                
                // Affiche le caractère de la lettre
                labelsLettres[i].setText(String.valueOf(lettre.getCaractere()));
                
                // Ajoute une étiquette pour la valeur de la lettre (en petit)
                Label valeurLabel = new Label(String.valueOf(lettre.getValeur()));
                valeurLabel.setFont(Font.font("Arial", 10));
                valeurLabel.setTranslateX(15);  // Décalage vers la droite
                valeurLabel.setTranslateY(10);  // Décalage vers le bas
                
                // Remplace l'ancien label de valeur s'il existe
                if (casesLettres[i].getChildren().size() > 1) {
                    casesLettres[i].getChildren().remove(1);
                }
                casesLettres[i].getChildren().add(valeurLabel);
            }
        }
        
        // Réapplique la couleur appropriée selon le tour actuel
        activerControles(controleur.estMonTour());
    }
    
    /**
     * Affiche le mot imposé (variante du jeu).
     * 
     * @param mot Le mot imposé à afficher, ou null/vide si aucun
     */
    public void afficherMotImpose(String mot) {
        if (mot != null && !mot.isEmpty()) {
            motImposeLabel.setText("Mot imposé: " + mot);
        } else {
            motImposeLabel.setText("Mot imposé: -");
        }
    }
    
    /**
     * Active ou désactive les contrôles en fonction du tour actuel.
     * Change la couleur de fond des cases de lettres.
     * 
     * @param actif true si c'est le tour du joueur, false sinon
     */
    public void activerControles(boolean actif) {
        // Couleur de fond: vert clair si actif, beige sinon
        Color couleurFond = actif ? Color.LIGHTGREEN : Color.BURLYWOOD;
        
        // Applique la couleur aux cases qui contiennent des lettres
        for (int i = 0; i < 7; i++) {
            if (!labelsLettres[i].getText().isEmpty()) {
                casesLettres[i].setBackground(new Background(new BackgroundFill(couleurFond, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }
    
    /**
     * Indique visuellement que le mot imposé a été placé avec succès.
     */
    public void marquerMotImposePlace() {
        motImposeLabel.setText("Mot imposé: placé ! (+15 points)");
        motImposeLabel.setStyle("-fx-text-fill: green;");  // Texte en vert
    }
    
    /**
     * Active ou désactive le bouton de validation de mot.
     * 
     * @param actif true pour activer le bouton, false pour le désactiver
     */
    public void activerBoutonValider(boolean actif) {
        validerMotButton.setDisable(!actif);
    }
    
    /**
     * Rafraîchit l'affichage des lettres en main en demandant
     * la liste actuelle au contrôleur.
     */
    public void reactualiserMain() {
        // Demande au contrôleur les lettres actuelles et met à jour l'affichage
        mettreAJourLettres(controleur.getLettresEnMain());
    }
}