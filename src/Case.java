public class Case {
    private int x;
    private int y;
    private Lettre lettre;
    private String typeBonus; // Peut être "MT", "MD", "LT", "LD", ou null pour aucune.
    private boolean estOccupe;

    public Case(int x, int y) {
        this.x = x;
        this.y = y;
        this.lettre = null;
        this.typeBonus = null;
        this.estOccupe = false;
    }

    public Case(int x, int y, String typeBonus) {
        this.x = x;
        this.y = y;
        this.lettre = null;
        this.typeBonus = typeBonus;
        this.estOccupe = false;
    }

    public Case(int x, int y, Lettre lettre, String typeBonus) {
        this.x = x;
        this.y = y;
        this.lettre = lettre;
        this.typeBonus = typeBonus;
        this.estOccupe = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Lettre getLettre() {
        return lettre;
    }

    public void setLettre(Lettre lettre) {
        this.lettre = lettre;
        this.estOccupe = true; 
    }

    public void liberer() {
        this.lettre = null;
        this.estOccupe = false;
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
