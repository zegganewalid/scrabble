package modele;

public class Case {
    private int x;
    private int y;
    private Lettre lettre;
    private String typeBonus; // Peut être "MT", "MD", "LT", "LD", ou null pour aucune.
    private boolean estOccupe;
    
// Constructeur : Initialise une case vide sans bonus.
    public Case(int x, int y) {
        this.x = x;
        this.y = y;
        this.lettre = null;
        this.typeBonus = null;
        this.estOccupe = false;
    }
 // Constructeur : Initialise une case vide avec un type de bonus.
    public Case(int x, int y, String typeBonus) {
        this.x = x;
        this.y = y;
        this.lettre = null;
        this.typeBonus = typeBonus;
        this.estOccupe = false;
    }
 // Constructeur : Initialise une case occupée avec une lettre et un type de bonus.
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
 // Retourne la lettre placée sur la case, ou null si aucune lettre n'est placée.
    public Lettre getLettre() {
        return lettre;
    }

    public void setLettre(Lettre lettre) {
        this.lettre = lettre;
        this.estOccupe = true; 
    }
    //Libère la case en retirant la lettre et en la marquant comme non occupée.
    public void liberer() {
        this.lettre = null;
        this.estOccupe = false;
    }
// Retourne le type de bonus de la case ("MT", "MD", "LT", "LD", ou null).
    public String getTypeBonus() {
        return typeBonus;
    }

    public void setTypeBonus(String typeBonus) {
        this.typeBonus = typeBonus;
    }
// Si la case est déja occupé par une lettre
    public boolean isEstOccupe() {
        return estOccupe;
    }
    
}
