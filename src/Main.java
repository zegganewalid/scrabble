package src;

public class Main {
    public static void main(String[] args) {
       /*  // Initialisation du plateau
        Plateau plateau = new Plateau();
        afficherPlateau(plateau);
        
        // Création de lettres et d'un mot
        Lettre lettre1 = new Lettre('W', 3);
        Lettre lettre2 = new Lettre('A', 5);
        Lettre lettre3 = new Lettre('L', 11);
        Lettre lettre4 = new Lettre('I', 9);
        Lettre lettre5 = new Lettre('D', 4);

        List<Lettre> lettres = List.of(lettre1, lettre2, lettre3, lettre4, lettre5);
        Mot mot = new Mot(lettres);
        
        System.out.println("Mot: " + mot.afficherMot());
        System.out.println("Score du mot: " + mot.calculerScoreMot());

        // Placement du mot sur le plateau
        placerMot(plateau, mot, 7, 7);
        afficherPlateau(plateau);
    }

    public static void afficherPlateau(Plateau plateau) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Case c = plateau.getCase(i, j);
                if (c.isEstOccupe()) {
                    System.out.print(" " + c.getLettre().getCaractere() + " ");
                } else if (c.getTypeBonus() != null) {
                    System.out.print(" " + c.getTypeBonus() + " ");
                } else {
                    System.out.print(" . ");
                }
            }
            System.out.println();
        }
    }

    public static void placerMot(Plateau plateau, Mot mot, int x, int y) {
        List<Lettre> lettres = mot.getLettres();
        for (int i = 0; i < lettres.size(); i++) {
            if (x + i < 15) {
                Case c = plateau.getCase(x + i, y);
                c.setLettre(lettres.get(i));
            }
        }
    }*/
/* 
    // Création de l'objet Dictionnaire
    Dictionnaire dictionnaire = new Dictionnaire();
        
    // Mot à rechercher
    String motRecherche = "bonjour";
    
    // Vérification si le mot existe dans le fichier mots.txt
    if (dictionnaire.chercherMotDansFichier(motRecherche)) {
        System.out.println("Le mot '" + motRecherche + "' existe dans le fichier.");
    } else {
        System.out.println("Le mot '" + motRecherche + "' n'a pas été trouvé dans le fichier.");
    }

    // Test avec un autre mot pour vérifier le fonctionnement
    String motTest = "chattt";
    if (dictionnaire.chercherMotDansFichier(motTest)) {
        System.out.println("Le mot '" + motTest + "' existe dans le fichier.");
    } else {
        System.out.println("Le mot '" + motTest + "' n'a pas été trouvé dans le fichier.");
    } */

    Sac sac = new Sac();
    Sac sac2 = new Sac();
    Sac sac3 = new Sac();
    Sac sac4 = new Sac();
    Sac sac5 = new Sac();

    // Afficher la taille du sac pour vérifier qu'il contient 100 lettres
    System.out.println("Taille du sac après initialisation : " + sac.lettres.size());

    // Afficher les lettres du sac pour vérifier leur répartition
    System.out.println("Lettres dans le sac :");
    for (Lettre lettre : sac.lettres) {
        System.out.print(lettre.getCaractere() + " ");
    }
    System.out.println();

    System.out.println("Lettres dans le sac :");
    for (Lettre lettre : sac2.lettres) {
        System.out.print(lettre.getCaractere() + " ");
    }
    System.out.println();

    System.out.println("Lettres dans le sac :");
    for (Lettre lettre : sac3.lettres) {
        System.out.print(lettre.getCaractere() + " ");
    }
    System.out.println();


    System.out.println("Lettres dans le sac :");
    for (Lettre lettre : sac4.lettres) {
        System.out.print(lettre.getCaractere() + " ");
    }
    System.out.println();

    System.out.println("Lettres dans le sac :");
    for (Lettre lettre : sac5.lettres) {
        System.out.print(lettre.getCaractere() + " ");
    }
    System.out.println();

    // Piocher des lettres aléatoirement et vérifier le tirage
    System.out.println("Lettres piochées :");
    for (int i = 0; i < 10; i++) { // Piocher 10 lettres pour tester
        Lettre lettrePiochée = sac.piocherLettre();
        if (lettrePiochée != null) {
            System.out.print(lettrePiochée.getCaractere() + " ");
        } else {
            System.out.println("Le sac est vide !");
        }
    }
    System.out.println();

    // Vérifier la taille du sac après avoir pioché des lettres
    System.out.println("Taille du sac après avoir pioché 10 lettres : " + sac.lettres.size());

    // Vérifier si le sac est vide
    System.out.println("Le sac est vide ? " + sac.estVide());
}


}

