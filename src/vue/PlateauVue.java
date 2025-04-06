package vue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import controleur.ClientControleur;
import modele.Direction;

public class PlateauVue extends GridPane {
    
    private static final int TAILLE = 15;
    private static final int TAILLE_CASE = 40;
    private Direction directionForcee = null;

    public void setDirectionForcee(Direction direction) {
        this.directionForcee = direction;
    }
    
    public Direction getDirectionForcee() {
        return directionForcee;
    }
    
    private StackPane[][] cases = new StackPane[TAILLE][TAILLE];
    private Label[][] lettres = new Label[TAILLE][TAILLE];
    
    private ClientControleur controleur;
    private MainJoueurVue mainJoueurVue;
    
    // Nouvelles variables pour suivre les lettres placées
    private List<Point> lettresPlacees = new ArrayList<>();
    private boolean motEnCours = false;
    
    public PlateauVue(ClientControleur controleur) {
        this.controleur = controleur;
        
        setHgap(2);
        setVgap(2);
        setPadding(new Insets(10));
        
        initialiserPlateau();
        ajouterClickSurCase();
        configurerDragLettresPlacees(); // Ajoutez cette ligne
    }
    
    private void initialiserPlateau() {
        // Type de bonus sur le plateau (MT = Mot Triple, MD = Mot Double, etc.)
        String[][] bonus = new String[TAILLE][TAILLE];
        
        // Initialiser les cases MT (Mot Triple)
        int[][] MT = {{0, 0}, {0, 7}, {0, 14}, {7, 0}, {7, 14}, {14, 0}, {14, 7}, {14, 14}};
        for (int[] pos : MT) {
            bonus[pos[0]][pos[1]] = "MT";
        }
        
        // Initialiser les cases MD (Mot Double)
        int[][] MD = {{1, 1}, {2, 2}, {3, 3}, {4, 4}, {10, 10}, {11, 11}, {12, 12}, {13, 13}, 
                     {1, 13}, {2, 12}, {3, 11}, {4, 10}, {10, 4}, {11, 3}, {12, 2}, {13, 1}};
        for (int[] pos : MD) {
            bonus[pos[0]][pos[1]] = "MD";
        }
        
        // Initialiser les cases LT (Lettre Triple)
        int[][] LT = {{1, 5}, {1, 9}, {5, 1}, {5, 5}, {5, 9}, {5, 13}, 
                     {9, 1}, {9, 5}, {9, 9}, {9, 13}, {13, 5}, {13, 9}};
        for (int[] pos : LT) {
            bonus[pos[0]][pos[1]] = "LT";
        }
        
        // Initialiser les cases LD (Lettre Double)
        int[][] LD = {{0, 3}, {0, 11}, {2, 6}, {2, 8}, {3, 0}, {3, 7}, {3, 14}, {6, 2}, {6, 6}, 
                     {6, 8}, {6, 12}, {7, 3}, {7, 11}, {8, 2}, {8, 6}, {8, 8}, {8, 12}, {11, 0}, 
                     {11, 7}, {11, 14}, {12, 6}, {12, 8}, {14, 3}, {14, 11}};
        for (int[] pos : LD) {
            bonus[pos[0]][pos[1]] = "LD";
        }
        
        // Créer toutes les cases du plateau
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                StackPane casePane = new StackPane();
                casePane.setPrefSize(TAILLE_CASE, TAILLE_CASE);
                
                // Définir la couleur de fond selon le bonus
                if (bonus[i][j] != null) {
                    Color couleurFond = Color.WHITE;
                    
                    switch (bonus[i][j]) {
                        case "MT":
                            couleurFond = Color.RED;
                            break;
                        case "MD":
                            couleurFond = Color.PINK;
                            break;
                        case "LT":
                            couleurFond = Color.BLUE;
                            break;
                        case "LD":
                            couleurFond = Color.LIGHTBLUE;
                            break;
                    }
                    
                    casePane.setBackground(new Background(new BackgroundFill(couleurFond, CornerRadii.EMPTY, Insets.EMPTY)));
                    
                    // Ajouter un label pour le type de bonus
                    Label bonusLabel = new Label(bonus[i][j]);
                    bonusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    bonusLabel.setTextFill(Color.WHITE);
                    casePane.getChildren().add(bonusLabel);
                } else {
                    casePane.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY)));
                }
                
                // Ajouter une bordure
                casePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                
                // Configurer la case pour recevoir des lettres par drag-and-drop
                configurerDragAndDrop(casePane, i, j);
                
                // Créer un label vide pour la lettre
                Label lettreLabel = new Label("");
                lettreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                casePane.getChildren().add(lettreLabel);
                
                // Stocker les références
                cases[i][j] = casePane;
                lettres[i][j] = lettreLabel;
                
                // Ajouter la case à la grille
                add(casePane, j, i);
            }
        }
        
        // Cas spécial : case centrale (7,7)
        cases[7][7].setBackground(new Background(new BackgroundFill(Color.GOLD, CornerRadii.EMPTY, Insets.EMPTY)));
        Label etoileLabel = new Label("★");
        etoileLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        cases[7][7].getChildren().add(0, etoileLabel);
    }
    
    private void configurerDragAndDrop(StackPane casePane, int i, int j) {
        // Accepter le drag-over pour autoriser le drop
        casePane.setOnDragOver(event -> {
            if (event.getGestureSource() != casePane && 
                event.getDragboard().hasString() && 
                lettres[i][j].getText().isEmpty() && 
                controleur.estMonTour()) {
                
                // Vérifier l'alignement avant d'accepter le drag
                if (lettresPlacees.isEmpty() || peutPlacerLettre(i, j)) {
                    // Accepter COPY ou MOVE selon la source
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }
            event.consume();
        });
        
        // Highlight lorsqu'un drag entre dans la case
        casePane.setOnDragEntered(event -> {
            if (event.getGestureSource() != casePane && 
                event.getDragboard().hasString() && 
                lettres[i][j].getText().isEmpty() &&
                controleur.estMonTour()) {
                
                // Highlight en vert seulement si l'emplacement est valide
                if (lettresPlacees.isEmpty() || peutPlacerLettre(i, j)) {
                    casePane.setStyle("-fx-border-color: green; -fx-border-width: 2;");
                } else {
                    // Highlight en rouge si l'emplacement est invalide
                    casePane.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                }
            }
            event.consume();
        });
        
        // Retirer le highlight quand le drag sort de la case
        casePane.setOnDragExited(event -> {
            casePane.setStyle("");
            event.consume();
        });
        
        // Gérer le drop d'une lettre
        casePane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString() && lettres[i][j].getText().isEmpty() && controleur.estMonTour()) {
                String data = db.getString();
                String lettre;
                
                // Vérifier si le format est celui d'une lettre déplacée (lettre,x,y)
                String[] parts = data.split(",");
                if (parts.length >= 3) {
                    // C'est un déplacement d'une lettre déjà sur le plateau
                    lettre = parts[0];
                    int sourceX = Integer.parseInt(parts[1]);
                    int sourceY = Integer.parseInt(parts[2]);
                    
                    // Effacer la lettre de la position source
                    lettres[sourceX][sourceY].setText("");
                    retirerPoint(lettresPlacees, sourceX, sourceY);
                } else {
                    // C'est une nouvelle lettre de la main du joueur
                    lettre = data;
                }
                
                // Vérifier si la lettre pourrait appartenir à deux directions
                // et demander à l'utilisateur de choisir
                if (demanderDirection(i, j, db)) {
                    // Si demanderDirection a géré le placement, on a terminé
                    event.setDropCompleted(true);
                    event.consume();
                    return;
                }
                
                // Vérifier l'alignement avant de placer la lettre
                if (!lettresPlacees.isEmpty() && !peutPlacerLettre(i, j)) {
                    // Afficher un indicateur visuel d'erreur
                    casePane.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    // Revenir à la normale après un court délai
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            Platform.runLater(() -> casePane.setStyle(""));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                }
                
                // Placer la lettre
                lettres[i][j].setText(lettre);
                lettresPlacees.add(new Point(i, j));
                motEnCours = true;
                
                // Activer le bouton de validation
                controleur.activerBoutonValider(true);
                
                // Activer le bouton d'annulation
                if (mainJoueurVue != null) {
                    mainJoueurVue.activerBoutonAnnulation(true);
                }
                
                success = true;
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    
    private boolean peutPlacerLettre(int x, int y) {
        if (lettresPlacees.isEmpty()) {
            return true; // Première lettre, peut être placée n'importe où
        }
        
        // Si une direction forcée est définie, l'utiliser
        if (directionForcee != null) {
            // Vérifier que la lettre est alignée avec la direction forcée
            int refX = lettresPlacees.get(0).x;
            int refY = lettresPlacees.get(0).y;
            
            if (directionForcee == Direction.HORIZONTAL) {
                return y == refY; // Même ligne horizontale
            } else {
                return x == refX; // Même colonne verticale
            }
        }
        
        // Déterminer si les lettres déjà placées sont alignées horizontalement ou verticalement
        boolean alignementHorizontal = true;
        boolean alignementVertical = true;
        
        int refX = lettresPlacees.get(0).x;
        int refY = lettresPlacees.get(0).y;
        
        for (Point p : lettresPlacees) {
            if (p.x != refX) alignementVertical = false;
            if (p.y != refY) alignementHorizontal = false;
        }
        
        // Si une seule lettre est placée (alignement pas encore déterminé)
        if (lettresPlacees.size() == 1) {
            // Accepter soit une même ligne, soit une même colonne
            return x == refX || y == refY;
        }
        
        // Alignement déjà déterminé par au moins deux lettres
        if (alignementHorizontal) {
            return y == refY; // Doit être sur la même ligne
        } else if (alignementVertical) {
            return x == refX; // Doit être sur la même colonne
        }
        
        // Si on arrive ici, c'est que les lettres ne sont pas alignées correctement
        return false;
    }
    
    // Ajouter la fonctionnalité de clic pour annuler le placement d'une lettre
    public void ajouterClickSurCase() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                final int x = i;
                final int y = j;
                
                // Configurer drag-and-drop pour les lettres déjà placées
                StackPane casePane = cases[x][y];
                
                // Permettre le déplacement des lettres placées
                casePane.setOnDragDetected(e -> {
                    if (!lettres[x][y].getText().isEmpty() && contientPoint(lettresPlacees, x, y) && controleur.estMonTour()) {
                        Dragboard db = casePane.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(lettres[x][y].getText());
                        db.setContent(content);
                        
                        // Mémoriser la source du drag pour pouvoir l'effacer plus tard
                        db.setDragView(casePane.snapshot(null, null));
                        
                        // Stocker les coordonnées de la source pour les utiliser dans onDragDone
                        content.putString(lettres[x][y].getText() + "," + x + "," + y);
                        
                        e.consume();
                    }
                });
                
                casePane.setOnDragDone(e -> {
                    if (e.getTransferMode() == TransferMode.MOVE) {
                        // Récupérer les coordonnées source
                        String data = e.getDragboard().getString();
                        String[] parts = data.split(",");
                        if (parts.length >= 3) {
                            int sourceX = Integer.parseInt(parts[1]);
                            int sourceY = Integer.parseInt(parts[2]);
                            
                            // Effacer la lettre de la position source
                            lettres[sourceX][sourceY].setText("");
                            retirerPoint(lettresPlacees, sourceX, sourceY);
                        }
                    }
                    e.consume();
                });
                
                // Ajouter également la gestion du clic
                casePane.setOnMouseClicked(e -> {
                    // Vérifier si la case contient une lettre placée récemment
                    if (!lettres[x][y].getText().isEmpty() && contientPoint(lettresPlacees, x, y)) {
                        // Récupérer la lettre à retirer
                        char lettreARetirer = lettres[x][y].getText().charAt(0);
                        
                        // Ouvrir un dialogue de confirmation ou de choix
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Retirer une lettre");
                        alert.setHeaderText("Que voulez-vous faire avec cette lettre ?");
                        
                        ButtonType retirerButton = new ButtonType("Retirer à la main");
                        ButtonType annulerButton = new ButtonType("Annuler", ButtonType.CANCEL.getButtonData());
                        
                        alert.getButtonTypes().setAll(retirerButton, annulerButton);
                        
                        Optional<ButtonType> result = alert.showAndWait();
                        
                        if (result.get() == retirerButton) {
                            // Retourner la lettre à la main
                            controleur.retournerLettreALaMain(lettreARetirer);
                            // Effacer visuellement la lettre
                            lettres[x][y].setText("");
                            retirerPoint(lettresPlacees, x, y);
                            
                            // Vérifier si toutes les lettres ont été retirées
                            if (lettresPlacees.isEmpty()) {
                                motEnCours = false;
                                controleur.activerBoutonValider(false);
                                
                                // Désactiver le bouton d'annulation si plus aucune lettre n'est placée
                                if (mainJoueurVue != null) {
                                    mainJoueurVue.activerBoutonAnnulation(false);
                                }
                            }
                        }
                    }
                });
            }
        }
        configurerDragLettresPlacees();
    }
    
    
    private boolean contientPoint(List<Point> points, int x, int y) {
        for (Point p : points) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }
    
    private void retirerPoint(List<Point> points, int x, int y) {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (p.x == x && p.y == y) {
                points.remove(i);
                return;
            }
        }
    }
    
    public void placerLettre(int x, int y, char lettre) {
        if (x >= 0 && x < TAILLE && y >= 0 && y < TAILLE) {
            lettres[x][y].setText(String.valueOf(lettre));
        }
    }
    
    public void effacerPlateau() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                lettres[i][j].setText("");
            }
        }
        reinitialiserLettresPlacees();
    }
    
    // Méthodes pour gérer les lettres placées en jeu
    
    public List<Point> getLettresPlacees() {
        return new ArrayList<>(lettresPlacees);
    }

    public boolean estMotEnCours() {
        if(getLettresPlacees().size() == 0) {
            return false;
        }
        return true;
    }
    
    public void reinitialiserLettresPlacees() {
        lettresPlacees.clear();
        motEnCours = false;
        directionForcee = null;
        controleur.activerBoutonValider(false);
        
        if (mainJoueurVue != null) {
            mainJoueurVue.activerBoutonAnnulation(false);
        }
    }
    
    public String getLettre(int x, int y) {
        if (x >= 0 && x < TAILLE && y >= 0 && y < TAILLE) {
            return lettres[x][y].getText();
        }
        return "";
    }
    
    public void setMainJoueurVue(MainJoueurVue mainJoueurVue) {
        this.mainJoueurVue = mainJoueurVue;
    }

    public void effacerLettre(int x, int y) {
        if (x >= 0 && x < TAILLE && y >= 0 && y < TAILLE) {
            lettres[x][y].setText("");
        }
    }

    public void configurerDragLettresPlacees() {
        // Déboguer pour vérifier que cette méthode est appelée
        System.out.println("Configuration des événements drag-and-drop pour les lettres placées");
        
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                final int x = i;
                final int y = j;
                final StackPane casePane = cases[i][j];
                
                // Permettre le dragging des lettres déjà placées
                casePane.setOnDragDetected(event -> {
                    // Déboguer pour voir si cet événement est déclenché
                    System.out.println("Drag détecté sur la case " + x + "," + y);
                    
                    if (!lettres[x][y].getText().isEmpty() && 
                        contientPoint(lettresPlacees, x, y) && 
                        controleur.estMonTour()) {
                        
                        System.out.println("Conditions remplies pour draguer la lettre");
                        Dragboard db = casePane.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        
                        // Format spécial pour indiquer une lettre déjà placée : lettre,x,y
                        content.putString(lettres[x][y].getText() + "," + x + "," + y);
                        db.setContent(content);
                        db.setDragView(casePane.snapshot(null, null));
                        
                        event.consume();
                    }
                });
            }
        }
    }

/**
 * Gère la demande de direction lorsqu'une lettre est placée à un emplacement ambigu
 * Cette méthode est désactivée pour éviter les fenêtres de dialogue automatiques
 * @param x La coordonnée x de la case
 * @param y La coordonnée y de la case
 * @param db Le Dragboard contenant les informations de drag-and-drop
 * @return true si le placement a été géré, false sinon
 */
private boolean demanderDirection(int x, int y, Dragboard db) {
    // La méthode est complètement désactivée pour éviter les fenêtres de dialogue automatiques
    // Elle ne sera utilisée que lors de la validation du mot via le bouton "Valider"
    
    // Extraire simplement la lettre et la placer sans demander de direction
    String lettre = db.getString();
    
    // Gérer le cas où la lettre contient des informations de position (format: lettre,x,y)
    if (lettre.contains(",")) {
        lettre = lettre.split(",")[0];
    }
    
    // Placer la lettre sur le plateau
    lettres[x][y].setText(lettre);
    lettresPlacees.add(new Point(x, y));
    motEnCours = true;
    
    // Activer le bouton de validation
    controleur.activerBoutonValider(true);
    
    // Activer le bouton d'annulation
    if (mainJoueurVue != null) {
        mainJoueurVue.activerBoutonAnnulation(true);
    }
    
    return true; // On a géré le placement sans demander de direction
}

private Direction determinerDirectionAutomatique(List<Point> lettresPlacees) {
    // Si une seule lettre est placée, chercher des lettres adjacentes
    if (lettresPlacees.size() == 1) {
        Point p = lettresPlacees.get(0);
        boolean lettreAGauche = p.x > 0 && !getLettre(p.x - 1, p.y).isEmpty();
        boolean lettreADroite = p.x < 14 && !getLettre(p.x + 1, p.y).isEmpty();
        boolean lettreEnHaut = p.y > 0 && !getLettre(p.x, p.y - 1).isEmpty();
        boolean lettreEnBas = p.y < 14 && !getLettre(p.x, p.y + 1).isEmpty();
        
        // S'il y a plus de lettres horizontalement qu'verticalement
        if ((lettreAGauche || lettreADroite) && !(lettreEnHaut || lettreEnBas)) {
            return Direction.HORIZONTAL;
        }
        // S'il y a plus de lettres verticalement qu'horizontalement
        else if (!(lettreAGauche || lettreADroite) && (lettreEnHaut || lettreEnBas)) {
            return Direction.VERTICAL;
        }
    }
    
    // Si plusieurs lettres sont placées, déterminer leur alignement
    if (lettresPlacees.size() > 1) {
        // Vérifier si toutes les lettres ont le même x (alignement vertical)
        boolean memeX = true;
        int x0 = lettresPlacees.get(0).x;
        for (Point p : lettresPlacees) {
            if (p.x != x0) {
                memeX = false;
                break;
            }
        }
        if (memeX) return Direction.VERTICAL;
        
        // Vérifier si toutes les lettres ont le même y (alignement horizontal)
        boolean memeY = true;
        int y0 = lettresPlacees.get(0).y;
        for (Point p : lettresPlacees) {
            if (p.y != y0) {
                memeY = false;
                break;
            }
        }
        if (memeY) return Direction.HORIZONTAL;
    }
    
    // Par défaut
    return Direction.HORIZONTAL;
}
}