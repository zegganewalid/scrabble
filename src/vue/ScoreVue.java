
package vue;

// Importation des classes nécessaires pour l'interface graphique avec JavaFX
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// Classe ScoreVue : cette classe gère l'affichage graphique des scores et des tours des joueurs
// Elle hérite de VBox, qui est une boîte verticale dans JavaFX permettant d'empiler des éléments graphiques les uns sous les autres
public class ScoreVue extends VBox {

    // Map (ou dictionnaire) contenant pour chaque joueur un label (zone de texte) qui affiche son score
    private Map<String, Label> scoreLabels = new HashMap<>();

    // Map contenant pour chaque joueur un label qui affiche le nombre de tours joués
    private Map<String, Label> toursLabels = new HashMap<>();

    // Label qui affiche le numéro du tour global pour tous les joueurs
    private Label tourGlobalLabel;

    // Constructeur de la vue, appelé quand l'objet est créé
    public ScoreVue() {
        // Définit l'espace vertical entre les éléments (10 pixels)
        setSpacing(10);

        // Définit les marges internes autour du composant (haut, bas, gauche, droite)
        setPadding(new Insets(10));

        // Fixe une largeur minimale pour que la boîte ne soit pas trop petite
        setMinWidth(200);

        // Création du titre principal affiché en haut de la boîte
        Label titreLabel = new Label("SCORES ET TOURS");
        titreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Texte en gras, taille 18, police Arial

        // Création du label affichant le numéro du tour global (ex : Tour global : 1)
        tourGlobalLabel = new Label("Tour global : 1");
        tourGlobalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14)); // Texte en gras, taille 14

        // Ajoute le titre et le label du tour global dans la VBox (dans l'ordre vertical)
        getChildren().addAll(titreLabel, tourGlobalLabel);

        // Applique une bordure noire autour de toute la VBox
        setBorder(new Border(new BorderStroke(
            Color.BLACK, // Couleur noire
            BorderStrokeStyle.SOLID, // Bordure pleine
            CornerRadii.EMPTY, // Pas de coins arrondis
            BorderWidths.DEFAULT // Épaisseur par défaut
        )));

        // Définit la couleur de fond de la boîte (jaune clair)
        setBackground(new Background(new BackgroundFill(
            Color.LIGHTYELLOW, // Couleur de fond
            CornerRadii.EMPTY, // Coins non arrondis
            Insets.EMPTY // Aucune marge intérieure spéciale
        )));
    }

    // Méthode qui ajoute un joueur avec son score OU met à jour le score si le joueur existe déjà
    public void ajouterScore(String nomJoueur, int score) {
        if (scoreLabels.containsKey(nomJoueur)) {
            // Si le joueur est déjà présent, on met à jour uniquement le texte de son score
            Label scoreLabel = scoreLabels.get(nomJoueur);
            scoreLabel.setText(nomJoueur + ": " + score); // Ex : "Ali : 24"
        } else {
            // Si le joueur n'existe pas encore, on crée deux labels : un pour son score et un pour ses tours

            // Création du label pour afficher le score du joueur
            Label scoreLabel = new Label(nomJoueur + ": " + score);
            scoreLabel.setFont(Font.font("Arial", 14)); // Police normale, taille 14

            // Création du label pour afficher le nombre de tours du joueur (1 par défaut au début)
            Label tourLabel = new Label(nomJoueur + " - Tours: 1");
            tourLabel.setFont(Font.font("Arial", 12));

            // Enregistre ces deux labels dans les maps pour pouvoir les retrouver plus tard
            scoreLabels.put(nomJoueur, scoreLabel);
            toursLabels.put(nomJoueur, tourLabel);

            // Crée une petite boîte verticale contenant les deux labels du joueur
            VBox joueurBox = new VBox(5, scoreLabel, tourLabel); // Espace de 5 pixels entre les deux

            // Ajoute cette boîte (score + tours) à la grande VBox globale
            getChildren().add(joueurBox);
        }
    }

    // Méthode pour mettre à jour le nombre de tours d'un joueur spécifique
    public void mettreAJourTours(String nomJoueur, int nombreTours) {
        if (toursLabels.containsKey(nomJoueur)) {
            Label tourLabel = toursLabels.get(nomJoueur); // On récupère le label existant
            tourLabel.setText(nomJoueur + " - Tours: " + nombreTours); // Mise à jour du texte
        }
    }

    // Cette méthode sert à mettre en évidence (visuellement) le joueur qui joue actuellement
    public void marquerJoueurActuel(String nomJoueur) {
        for (Map.Entry<String, Label> entry : scoreLabels.entrySet()) {
            Label label = entry.getValue(); // Label du score
            Label tourLabel = toursLabels.get(entry.getKey()); // Label du tour associé

            if (entry.getKey().equals(nomJoueur)) {
                // Si c'est le joueur qui joue maintenant, on met le texte en vert et gras
                label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                label.setTextFill(Color.GREEN);
                tourLabel.setTextFill(Color.GREEN);
            } else {
                // Sinon, on remet les styles par défaut : police normale, texte noir
                label.setFont(Font.font("Arial", 14));
                label.setTextFill(Color.BLACK);
                tourLabel.setTextFill(Color.BLACK);
            }
        }
    }

    // Méthode pour réinitialiser complètement l'affichage des scores et des tours
    public void effacerScores() {
        // On garde uniquement les deux premiers éléments : le titre et le tour global
        if (getChildren().size() > 2) {
            getChildren().remove(2, getChildren().size()); // Supprime tous les enfants à partir de l'index 2
        }

        // On vide les deux maps pour supprimer les données des anciens joueurs
        scoreLabels.clear();
        toursLabels.clear();
    }

    // Méthode pour mettre à jour le tour global (affiché en haut), et aussi chaque joueur si besoin
    public void mettreAJourTourGlobal(int numeroTour) {
        // Affiche une ligne de debug dans la console (utile pendant les tests)
        System.out.println("DEBUG SCORE VUE - Mise à jour du tour global à " + numeroTour);

        // Met à jour le label du tour global
        if (tourGlobalLabel != null) {
            tourGlobalLabel.setText("Tour global : " + numeroTour);
        }

        // Met à jour le texte de chaque joueur pour afficher le nouveau numéro de tour
        for (Map.Entry<String, Label> entry : toursLabels.entrySet()) {
            String nomJoueur = entry.getKey();
            Label tourLabel = entry.getValue();
            tourLabel.setText(nomJoueur + " - Tours: " + numeroTour);
        }
    }
}
