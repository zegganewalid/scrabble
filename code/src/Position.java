public class Position {
    private int ligne;
    private int colonne;

    // Constructeur
    public Position(int ligne, int colonne) {
        this.ligne = ligne;
        this.colonne = colonne;
    }

    // Getters et Setters
    public int getLigne() {
        return ligne;
    }

    public void setLigne(int ligne) {
        this.ligne = ligne;
    }

    public int getColonne() {
        return colonne;
    }

    public void setColonne(int colonne) {
        this.colonne = colonne;
    }

    @Override
    public String toString() {
        return "(" + ligne + ", " + colonne + ")";
    }
}