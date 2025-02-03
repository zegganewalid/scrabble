
public class Plateau {
    private static final int TAILLE = 15;
    private Case[][] cases;

    public Plateau() {
        this.cases = new Case[TAILLE][TAILLE];
        initialiserBonus();
    }

    private void initialiserBonus() {
        int[][] MT = {{0, 0}, {0, 7}, {0, 14}, {7, 0}, {7, 14}, {14, 0}, {14, 7}, {14, 14}};
        int[][] MD = {{1, 1}, {2, 2}, {3, 3}, {4, 4}, {10, 10}, {11, 11}, {12, 12}, {13, 13}, 
                      {1, 13}, {2, 12}, {3, 11}, {4, 10}, {10, 4}, {11, 3}, {12, 2}, {13, 1}};
        int[][] LT = {{1, 5}, {1, 9}, {5, 1}, {5, 5}, {5, 9}, {5, 13}, 
                      {9, 1}, {9, 5}, {9, 9}, {9, 13}, {13, 5}, {13, 9}};
        int[][] LD = {{0, 3}, {0, 11}, {2, 6}, {2, 8}, {3, 0}, {3, 7}, {3, 14}, {6, 2}, {6, 6}, 
                      {6, 8}, {6, 12}, {7, 3}, {7, 11}, {8, 2}, {8, 6}, {8, 8}, {8, 12}, {11, 0}, 
                      {11, 7}, {11, 14}, {12, 6}, {12, 8}, {14, 3}, {14, 11}};
    
        // Initialisation des cases
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
            }
        }
    
        // Attribution des bonus
        for (int[] pos : MT) cases[pos[0]][pos[1]].setTypeBonus("MT"); // Mot Triple
        for (int[] pos : MD) cases[pos[0]][pos[1]].setTypeBonus("MD"); // Mot Double
        for (int[] pos : LT) cases[pos[0]][pos[1]].setTypeBonus("LT"); // Lettre Triple
        for (int[] pos : LD) cases[pos[0]][pos[1]].setTypeBonus("LD"); // Lettre Double
    }
    

    public Case getCase(int x, int y) {
        if (x >= 0 && x < TAILLE && y >= 0 && y < TAILLE) {
            return cases[x][y];
        } else {
            throw new IllegalArgumentException("Coordonnées hors limites du plateau.");
        }
    }
    public void afficherPlateau() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                String bonus = cases[i][j].getTypeBonus();
                if (bonus == null) {
                    System.out.print(" .  "); // Case normale
                } else {
                    System.out.print(bonus + " ");
                }
            }
            System.out.println();
        }
    }

    
}
