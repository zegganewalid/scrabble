package vue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import modele.Lettre;

/**
 * Vue pour afficher les informations sur les lettres restantes dans le sac
 */
public class SacInfoVue extends VBox {
    
    private GridPane lettresGrid;
    private Label totalLabel;
    private Map<Character, Label> lettresLabels = new HashMap<>();
    
    public SacInfoVue() {
        setSpacing(10);
        setPadding(new Insets(10));
        setMinWidth(200);
        setMaxHeight(300);
        
        // Titre
        Label titreLabel = new Label("LETTRES RESTANTES");
        titreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Grille pour les lettres
        lettresGrid = new GridPane();
        lettresGrid.setHgap(10);
        lettresGrid.setVgap(5);
        lettresGrid.setPadding(new Insets(5));
        
        // Ajouter un ScrollPane en cas de nombreuses lettres
        ScrollPane scrollPane = new ScrollPane(lettresGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        
        // Label pour le total des lettres
        totalLabel = new Label("Total: 0 lettres");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        // Ajouter les éléments à la vue
        getChildren().addAll(titreLabel, scrollPane, totalLabel);
        
        // Styles
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Initialiser les labels pour chaque lettre
        initialiserLabelsLettres();
    }
    
    /**
     * Initialise les labels pour chaque lettre de l'alphabet
     */
    private void initialiserLabelsLettres() {
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        int row = 0;
        int col = 0;
        
        for (char c : alphabet) {
            Label lettreLabel = new Label(c + ": 0");
            lettreLabel.setFont(Font.font("Arial", 12));
            lettresLabels.put(c, lettreLabel);
            
            lettresGrid.add(lettreLabel, col, row);
            
            // Passer à la colonne suivante, ou à la ligne suivante si nécessaire
            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }
    }
    
    /**
     * Met à jour l'affichage des lettres restantes dans le sac
     * @param lettres La liste des lettres restantes dans le sac
     */
    public void mettreAJourLettres(List<Lettre> lettres) {
        // Réinitialiser les compteurs
        for (Map.Entry<Character, Label> entry : lettresLabels.entrySet()) {
            entry.getValue().setText(entry.getKey() + ": 0");
        }
        
        // Compter les occurrences de chaque lettre
        Map<Character, Integer> compteur = new HashMap<>();
        for (Lettre lettre : lettres) {
            char c = lettre.getCaractere();
            compteur.put(c, compteur.getOrDefault(c, 0) + 1);
        }
        
        // Mettre à jour les labels avec les nouvelles valeurs
        for (Map.Entry<Character, Integer> entry : compteur.entrySet()) {
            Label label = lettresLabels.get(entry.getKey());
            if (label != null) {
                label.setText(entry.getKey() + ": " + entry.getValue());
            }
        }
        
        // Mettre à jour le total
        totalLabel.setText("Total: " + lettres.size() + " lettres");
    }
}