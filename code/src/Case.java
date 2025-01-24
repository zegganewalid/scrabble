public class Case {
    private int x;
    private int y;
    private Lettre lettre;
    private String typeBonus; // Peut être "MT", "MD", "LT", "LD", ou null pour aucune.
    private boolean estOccupe; // Indique si une case est déjà occupée

    public Case(int x, int y) {
        this.x = x;
        this.y = y;
        this.lettre = null;
        this.typeBonus = null;
        this.estOccupe = false;
    }

    // Getters et setters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Lettre getLettre() {
        return lettre;
    }

    public void setLettre(Lettre lettre) { // (utilisé pour le test)
        this.lettre = lettre;
        this.estOccupe = (lettre != null); 
    }
     public void occuper(Lettre lettre) { // Permet d'occuper une case avec une lettre.(utilisé pour le plateau de jeu)
        this.lettre = lettre;
        this.estOccupe = true; 
    }

    public void liberer() {
        this.lettre = null;     // Retire la lettre de la case.
        this.estOccupe = false; // Marque la case comme libre.
    }

    public String getTypeBonus() {
        return typeBonus;
    }

    public void setTypeBonus(String typeBonus) {
        this.typeBonus = typeBonus;
    }

    public boolean isEstOccupe() {
        return estOccupe;
    }
}
