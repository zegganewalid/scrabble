package src;

public class Lettre {
    private char caractere;
    private int valeur;
    
    public Lettre(char caractere, int valeur) {
        this.caractere = caractere;
        this.valeur = valeur;
    }

    public char getCaractere() {
        return caractere;
    }

    public int getValeur() {
        return valeur;
    }
}
