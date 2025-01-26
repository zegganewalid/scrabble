// Classe Lettre
public class Lettre {
    private char caractere;
    private int valeur;

    // Constructeur
    public Lettre(char caractere, int valeur) {
        this.caractere = caractere;
        this.valeur = valeur;
    }

    // Getter pour le caractère
    public char getCaractere() {
        return caractere;
    }

    // Setter pour le caractère
    public void setCaractere(char caractere) {
        this.caractere = caractere;
    }

    // Getter pour la valeur
    public int getValeur() {
        return valeur;
    }

    // Setter pour la valeur
    public void setValeur(int valeur) {
        this.valeur = valeur;
    }

    @Override
    public String toString() {
        return caractere + " (" + valeur + ")";
    }
}
