
package vue;

// Importation des composants JavaFX pour créer une fenêtre graphique
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

// Importation de l'énumération Direction définie dans le package modele
import modele.Direction;

/**
 * Classe DirectionDialog : représente une petite fenêtre (dialogue) qui s'affiche 
 * pour demander à l'utilisateur de choisir une direction (horizontale ou verticale)
 * pour placer un mot sur la grille de jeu.
 */
public class DirectionDialog {

    // La direction choisie par l'utilisateur (sera soit HORIZONTAL soit VERTICAL)
    private Direction directionChoisie;

    // La fenêtre (popup) JavaFX qui sera affichée
    private Stage dialog;

    // Constructeur : définit l'apparence et le comportement de la fenêtre
    public DirectionDialog() {
        dialog = new Stage(); // Création d'une nouvelle fenêtre

        // Ce dialogue bloque les autres fenêtres jusqu'à ce qu'on ait choisi une direction
        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.setTitle("Choisir la direction"); // Titre de la fenêtre
        dialog.setMinWidth(300); // Largeur minimale
        dialog.setMinHeight(200); // Hauteur minimale
        dialog.setResizable(false); // L'utilisateur ne peut pas redimensionner la fenêtre

        // Texte affiché au-dessus des boutons
        Text message = new Text("Choisissez la direction du mot :");
        message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        /**
         * Deux boutons sont présentés : un pour choisir la direction horizontale,
         * et un autre pour la direction verticale. Les labels sont visuellement inversés
         * pour guider correctement l'utilisateur.
         */

        // Bouton pour choisir une direction horizontale (de gauche à droite)
        Button horizontalBtn = new Button("Vertical ↓"); // Affiche une flèche vers le bas
        horizontalBtn.setPrefWidth(150); // Largeur fixe du bouton
        horizontalBtn.setStyle("-fx-font-size: 14px;"); // Taille du texte dans le bouton

        // Lorsque l'utilisateur clique sur ce bouton, on enregistre la direction horizontale et on ferme le dialogue
        horizontalBtn.setOnAction(e -> {
            directionChoisie = Direction.HORIZONTAL; // Stocke la direction
            dialog.close(); // Ferme la fenêtre
        });

        // Bouton pour choisir une direction verticale (de haut en bas)
        Button verticalBtn = new Button("Horizontal →"); // Affiche une flèche vers la droite
        verticalBtn.setPrefWidth(150);
        verticalBtn.setStyle("-fx-font-size: 14px;");

        // Lorsque ce bouton est cliqué, on enregistre la direction verticale et on ferme
        verticalBtn.setOnAction(e -> {
            directionChoisie = Direction.VERTICAL;
            dialog.close();
        });

        // HBox = conteneur horizontal pour aligner les deux boutons côte à côte
        HBox boutons = new HBox(20, horizontalBtn, verticalBtn); // 20 = espace entre les boutons
        boutons.setAlignment(Pos.CENTER); // Centre les boutons dans la ligne

        // VBox = conteneur vertical pour empiler le texte et les boutons
        VBox layout = new VBox(20); // 20 = espace vertical entre les éléments
        layout.getChildren().addAll(message, boutons); // On ajoute le texte + les boutons à la fenêtre
        layout.setAlignment(Pos.CENTER); // Centre tout au milieu
        layout.setStyle("-fx-padding: 20px;"); // Ajoute des marges internes

        // Création de la scène (contenu) de la fenêtre, à partir de notre mise en page
        Scene scene = new Scene(layout);
        dialog.setScene(scene); // On associe la scène à la fenêtre
    }

    /**
     * Affiche le dialogue à l'écran et attend que l'utilisateur fasse un choix.
     * Retourne la direction choisie, ou HORIZONTAL par défaut si aucune n'est choisie.
     */
    public Direction afficherEtAttendre() {
        directionChoisie = null; // On réinitialise la valeur à chaque affichage

        // On essaie de centrer la fenêtre sur la première fenêtre principale visible
        Window mainWindow = Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null); // Si aucune fenêtre n'est visible, retourne null

        if (mainWindow != null) {
            dialog.setX(mainWindow.getX() + (mainWindow.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(mainWindow.getY() + (mainWindow.getHeight() - dialog.getHeight()) / 2);
        }

        // Affiche le dialogue et bloque le reste jusqu'à fermeture
        dialog.showAndWait();

        // Affiche en console la direction choisie (utile pour debug)
        System.out.println("Direction choisie: " + directionChoisie);

        // Retourne la direction choisie. Si rien n'a été choisi, on retourne HORIZONTAL par défaut
        return directionChoisie != null ? directionChoisie : Direction.HORIZONTAL;
    }
}
