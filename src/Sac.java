package src;

import java.util.ArrayList;
import java.util.List;

public class Sac {
    public List<Lettre> lettres;

    public Sac() {
        lettres = new ArrayList<>();
        initialiserLettres();
    }

    private void initialiserLettres() {
        // Liste des lettres de l'alphabet français avec leurs valeurs respectives
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        int[] valeurs = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1, 4, 10, 10, 10, 10};

        // Ajouter au moins une occurrence de chaque lettre
        for (int i = 0; i < alphabet.length; i++) {
            ajouterLettres(alphabet[i], valeurs[i], 1);
        }

        // Remplir le reste du sac avec des lettres aléatoires
        int lettresRestantes = 100 - alphabet.length;
        for (int i = 0; i < lettresRestantes; i++) {
            int index = (int) (Math.random() * alphabet.length);
            ajouterLettres(alphabet[index], valeurs[index], 1);
        }
    }

    private void ajouterLettres(char c, int valeur, int quantite) {
        for (int i = 0; i < quantite; i++) {
            lettres.add(new Lettre(c, valeur));
        }
    }

    public Lettre piocherLettre() {
        if (lettres.isEmpty()) return null; // Vérifie si le sac est vide
        int index = (int) (Math.random() * lettres.size()); // Choisit une lettre aléatoire
        return lettres.remove(index); // Supprime et retourne la lettre choisie
    }

    public boolean estVide() {
        return lettres.isEmpty();
    }
}