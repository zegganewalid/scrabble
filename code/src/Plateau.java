
public class Plateau {
    private static final int TAILLE = 15;
    private Case[][] cases;

    public Plateau() {
        this.cases = new Case[TAILLE][TAILLE];
        initialiserBonus();
    }

    private void initialiserBonus() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
                if ((i == 7 && j == 7)) {
                    cases[i][j].setTypeBonus("MT"); // Mot compte triple
                } else if ((i == 0 && j == 0) || (i == 14 && j == 14)) {
                    cases[i][j].setTypeBonus("MD"); // Mot compte double
                } else if ((i == 1 && j == 5) || (i == 5 && j == 1)) {
                    cases[i][j].setTypeBonus("LD"); // Lettre compte double
                } else if ((i == 2 && j == 6) || (i == 6 && j == 2)) {
                    cases[i][j].setTypeBonus("LT"); // Lettre compte triple
                }
            }
        }
    }

    public Case getCase(int x, int y) {
        if (x >= 0 && x < TAILLE && y >= 0 && y < TAILLE) {
            return cases[x][y];
        } else {
            throw new IllegalArgumentException("Coordonnées hors limites du plateau.");
        }
    }
    
}
