import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dictionnaire {
    private Set<String> mots; // Ensemble contenant tous les mots valides du dictionnaire
    private List<String> motsImposables; // Liste des mots imposés pour le jeu

    // Constructeur : Initialise le dictionnaire et charge les mots
    public Dictionnaire() {
        mots = new HashSet<>();
        motsImposables = new ArrayList<>();
        chargerMots();
    }

    // Charge une liste de mots prédéfinis (peut être remplacée par un fichier externe)
    private void chargerMots() {
        mots.add("BONJOUR");
        mots.add("SCRABBLE");
        mots.add("ORDINATEUR");
        mots.add("PYTHON");
        mots.add("JAVA");
        mots.add("PROGRAMME");
        mots.add("ALGORITHME");

        // Ajout de certains mots comme mots imposés
        motsImposables.add("TEST");
        motsImposables.add("JEU");
        motsImposables.add("CODE");
    }

    // Vérifie si un mot existe dans le dictionnaire
    public boolean verifierMot(String mot) {
        return mots.contains(mot.toUpperCase()); // Vérification insensible à la casse
    }

    // Tire un mot imposé au hasard pour un joueur
    public String piocherMotImpose() {
        if (motsImposables.isEmpty()) return null; // Si aucun mot imposé n'est disponible
        int index = (int) (Math.random() * motsImposables.size()); // Sélection aléatoire
        return motsImposables.get(index); // Retourne le mot imposé
    }
}