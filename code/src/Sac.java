import java.util.ArrayList;
import java.util.List;

public class Sac {
    private List<Lettre> lettres; // Liste des lettres disponibles dans le sac

    // Constructeur : initialise le sac avec toutes les lettres du Scrabble
    public Sac() {
        lettres = new ArrayList<>();
        initialiserLettres();
    }

    // Initialise les lettres avec leur fréquence et leur valeur en points
    private void initialiserLettres() {
        ajouterLettres('A', 1, 9);
        ajouterLettres('B', 3, 2);
        // Ajouter ici toutes les autres lettres avec leur valeur et quantité
    }

    // Ajoute une lettre avec une valeur et une quantité spécifique au sac
    private void ajouterLettres(char c, int valeur, int quantite) {
        for (int i = 0; i < quantite; i++) {
            lettres.add(new Lettre(c, valeur));
        }
    }

    // Pioche une lettre au hasard et la retire du sac
    public Lettre piocherLettre() {
        if (lettres.isEmpty()) return null; // Vérifie si le sac est vide
        int index = (int) (Math.random() * lettres.size()); // Choisit une lettre aléatoire
        return lettres.remove(index); // Supprime et retourne la lettre choisie
    }

    // Vérifie si le sac est vide
    public boolean estVide() {
        return lettres.isEmpty();
    }
}