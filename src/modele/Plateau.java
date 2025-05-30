package modele;

public class Plateau {
    private static final int TAILLE = 15; // Taille du plateau
    private Case[][] cases; // Matrice représentant le plateau de jeu

    public Plateau() {
        this.cases = new Case[TAILLE][TAILLE];
        initialiserBonus();
    }
    // Constructeur : Initialise le plateau avec des cases vides et des bonus.
    private void initialiserBonus() {
        int[][] MT = {{0, 0}, {0, 7}, {0, 14}, {7, 0}, {7, 14}, {14, 0}, {14, 7}, {14, 14}};
        int[][] MD = {{1, 1}, {2, 2}, {3, 3}, {4, 4}, {10, 10}, {11, 11}, {12, 12}, {13, 13}, 
                      {1, 13}, {2, 12}, {3, 11}, {4, 10}, {10, 4}, {11, 3}, {12, 2}, {13, 1}};
        int[][] LT = {{1, 5}, {1, 9}, {5, 1}, {5, 5}, {5, 9}, {5, 13}, 
                      {9, 1}, {9, 5}, {9, 9}, {9, 13}, {13, 5}, {13, 9}};
        int[][] LD = {{0, 3}, {0, 11}, {2, 6}, {2, 8}, {3, 0}, {3, 7}, {3, 14}, {6, 2}, {6, 6}, 
                      {6, 8}, {6, 12}, {7, 3}, {7, 11}, {8, 2}, {8, 6}, {8, 8}, {8, 12}, {11, 0}, 
                      {11, 7}, {11, 14}, {12, 6}, {12, 8}, {14, 3}, {14, 11}};
    
        // Initialisation des cases en tableau 
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
            }
        }
    
        // Attribution des bonus "Mot Triple" (MT) aux cases spécifiées par leurs coordonnées.
        for (int[] pos : MT) cases[pos[0]][pos[1]].setTypeBonus("MT"); // Mot Triple

        // Attribution des bonus "Mot Double" (MD) aux cases spécifiées par leurs coordonnées.
        for (int[] pos : MD) cases[pos[0]][pos[1]].setTypeBonus("MD"); // Mot Double

        // Attribution des bonus "Lettre Triple" (LT) aux cases spécifiées par leurs coordonnées.
        for (int[] pos : LT) cases[pos[0]][pos[1]].setTypeBonus("LT"); // Lettre Triple

        // Attribution des bonus "Lettre Double" (LD) aux cases spécifiées par leurs coordonnées.
        for (int[] pos : LD) cases[pos[0]][pos[1]].setTypeBonus("LD"); // Lettre Double
    }
    //Retourne l'objet Case à la position (x, y) du plateau.
    public Case getCase(int x, int y) {
        if (x >= 0 && x < TAILLE && y >= 0 && y < TAILLE) {
            return cases[x][y];
        } else {
            throw new IllegalArgumentException("Coordonnées hors limites du plateau.");
        }
    }
    //Affichage du plateau dans la console.
    public void afficherPlateau() {
        // Affichage des numéros de colonnes
        System.out.print("   ");
        for (int j = 0; j < TAILLE; j++) {
            System.out.printf("%2d ", j);
        }
        System.out.println();
    
        for (int i = 0; i < TAILLE; i++) {
            // Affichage des numéros de lignes
            System.out.printf("%2d ", i);
            
            for (int j = 0; j < TAILLE; j++) {
                Case caseActuelle = cases[i][j];
                
                if (caseActuelle.isEstOccupe()) {
                    // Si la case est occupée, afficher la lettre
                    System.out.printf("%2c ", caseActuelle.getLettre().getCaractere());
                } else {
                    // Afficher le type de bonus ou un point si pas de bonus
                    String bonus = caseActuelle.getTypeBonus();
                    if (bonus == null) {
                        System.out.print(" .  ");
                    } else {
                        System.out.printf("%2s ", bonus);
                    }
                }
            }
            System.out.println();
        }
    }
}