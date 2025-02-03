import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Initialisation du plateau
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
    }
}
