package modele;

import java.util.ArrayList;
import java.util.List;

public class Sac {
    public List<Lettre> lettres;
    private Dictionnaire dictionnaire;

    public Sac() {
        lettres = new ArrayList<>();
        initialiserLettres();
        dictionnaire = new Dictionnaire();
    }

    private void initialiserLettres() {
        // Liste des lettres de l'alphabet français standard avec leurs valeurs
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        int[] valeurs = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1, 4, 10, 10, 10, 10};
        
        // Fréquence des lettres dans la langue française (approximation)
        int[] frequence = {
            9,  // A
            1,  // B
            3,  // C
            4,  // D
            15, // E
            1,  // F
            1,  // G
            1,  // H
            8,  // I
            1,  // J
            1,  // K
            5,  // L
            3,  // M
            7,  // N
            6,  // O
            3,  // P
            1,  // Q
            6,  // R
            8,  // S
            7,  // T
            6,  // U
            2,  // V
            1,  // W
            1,  // X
            1,  // Y
            1   // Z
        };

        // Ajouter les lettres selon leur fréquence
        for (int i = 0; i < alphabet.length; i++) {
            // Ajuster la fréquence pour avoir un bon équilibre
            int quantite = Math.max(1, frequence[i] / 2);
            ajouterLettres(alphabet[i], valeurs[i], quantite);
        }

        // Ajouter plus de voyelles pour faciliter la formation de mots
        ajouterLettres('A', 1, 5);
        ajouterLettres('E', 1, 10);
        ajouterLettres('I', 1, 5);
        ajouterLettres('O', 1, 3);
        ajouterLettres('U', 1, 3);
        
        // Ajouter quelques consonnes courantes supplémentaires
        ajouterLettres('R', 1, 3);
        ajouterLettres('S', 1, 3);
        ajouterLettres('T', 1, 3);
        ajouterLettres('N', 1, 3);
        ajouterLettres('L', 1, 3);
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
    
    // Nouvelle méthode pour vérifier si une main peut former au moins un mot
    public boolean peutFormerUnMot(List<Lettre> main) {
        // Si la main contient moins de 2 lettres, impossible de former un mot
        if (main.size() < 2) {
            return false;
        }
        
        // Convertir la main en chaîne de caractères
        StringBuilder sb = new StringBuilder();
        for (Lettre lettre : main) {
            sb.append(lettre.getCaractere());
        }
        String lettresDisponibles = sb.toString().toLowerCase();
        
        // Liste de mots courts et communs à vérifier en priorité
        String[] motsCommuns = {
            "et", "la", "le", "un", "de", "du", "en", "au", "ce", "ca",
            "sa", "me", "te", "se", "tu", "ou", "il", "je", "on", "si",
            "avec", "pour", "dans", "par", "sur", "tout", "bien", "mais",
            "plus", "moins", "trop", "peu", "tres", "qui", "que", "quoi",
            "deux", "trois", "six", "dix", "pas", "bon", "mal", "vie",
            "eau", "feu", "air", "vent", "jour", "nuit", "an", "mois"
        };
        
        // Vérifier d'abord les mots communs (plus rapide)
        for (String mot : motsCommuns) {
            if (peutFormerMot(lettresDisponibles, mot)) {
                return true;
            }
        }
        
        // Si aucun mot commun n'est formable, essayer une recherche plus approfondie mais limitée
        return verifierCombinaisons(lettresDisponibles, "", dictionnaire, 0, 4);
    }
    
    // Vérifie si un mot spécifique peut être formé avec les lettres disponibles
    private boolean peutFormerMot(String lettresDisponibles, String mot) {
        // Copie des lettres disponibles pour ne pas les modifier
        String lettresRestantes = lettresDisponibles;
        
        for (char c : mot.toCharArray()) {
            int index = lettresRestantes.indexOf(c);
            if (index == -1) {
                return false; // Lettre nécessaire non disponible
            }
            // Retirer la lettre utilisée
            lettresRestantes = lettresRestantes.substring(0, index) + lettresRestantes.substring(index + 1);
        }
        return true;
    }
    
    // Méthode récursive pour vérifier les combinaisons, avec limite de profondeur
    private boolean verifierCombinaisons(String lettres, String motCourant, Dictionnaire dico, 
                                        int profondeur, int profondeurMax) {
        // Limiter la profondeur de récursion
        if (profondeur >= profondeurMax) {
            return false;
        }
        
        // Si le mot courant est non vide et valide, retourner vrai
        if (motCourant.length() >= 2 && dico.chercherMotDansFichier(motCourant)) {
            return true;
        }
        
        // Pour chaque lettre restante, essayer de l'ajouter au mot courant
        for (int i = 0; i < lettres.length(); i++) {
            char lettreCourante = lettres.charAt(i);
            String nouveauMot = motCourant + lettreCourante;
            String lettresRestantes = lettres.substring(0, i) + lettres.substring(i + 1);
            
            // Si la combinaison avec cette lettre forme un mot, retourner vrai
            if (verifierCombinaisons(lettresRestantes, nouveauMot, dico, profondeur + 1, profondeurMax)) {
                return true;
            }
        }
        
        // Aucune combinaison valide trouvée
        return false;
    }
    
    // Nouvelle méthode pour garantir une main jouable
    public List<Lettre> distribuerMainJouable(int nombreLettres) {
        List<Lettre> main = new ArrayList<>();
        int tentatives = 0;
        final int MAX_TENTATIVES = 1000;
        
        // Première tentative de distribution
        for (int i = 0; i < nombreLettres && !estVide(); i++) {
            main.add(piocherLettre());
        }
        
        // Vérifier si la main est jouable
        while (!peutFormerUnMot(main) && tentatives < MAX_TENTATIVES && !estVide()) {
            // Remplacer une lettre au hasard
            int indexARemplacer = (int)(Math.random() * main.size());
            Lettre lettreRetour = main.remove(indexARemplacer);
            
            // Remettre la lettre dans le sac
            lettres.add(lettreRetour);
            
            // Piocher une nouvelle lettre
            Lettre nouvelleLettre = piocherLettre();
            main.add(nouvelleLettre);
            
            tentatives++;
        }
        
        return main;
    }

    public List<Lettre> getLettres() {
        return new ArrayList<>(lettres); // Retourne une copie pour protéger l'encapsulation
    }
}