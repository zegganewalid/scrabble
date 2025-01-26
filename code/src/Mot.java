import java.util.List;

public class Mot {
    private List<Lettre> lettres;
    private Position position; // Classe Position à définir (ligne, colonne)
    private Direction direction; // Enumération Direction (HORIZONTAL ou VERTICAL)
    private boolean estImpose;

    // Constructeur
    public Mot(List<Lettre> lettres, Position position, Direction direction, boolean estImpose) {
        this.lettres = lettres;
        this.position = position;
        this.direction = direction;
        this.estImpose = estImpose;
    }

    // Getters et Setters
    public List<Lettre> getLettres() {
        return lettres;
    }

    public void setLettres(List<Lettre> lettres) {
        this.lettres = lettres;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isEstImpose() {
        return estImpose;
    }

    public void setEstImpose(boolean estImpose) {
        this.estImpose = estImpose;
    }

    // Calcul du score du mot
    public int calculerScore() {
        int score = 0;
        for (Lettre lettre : lettres) {
            score += lettre.getValeur();
        }
        return score;
    }

    // Vérification de la validité du mot
    public boolean verifierValidite(Dictionnaire dictionnaire, List<Mot> motsJoues) {
        // Vérifier si le mot existe dans le dictionnaire
        StringBuilder motForme = new StringBuilder();
        for (Lettre lettre : lettres) {
            motForme.append(lettre.getCaractere());
        }

        if (!dictionnaire.contientMot(motForme.toString())) {
            return false; // Le mot n'est pas valide dans le dictionnaire
        }

        // Vérifier si le mot n'a pas déjà été joué
        for (Mot mot : motsJoues) {
            if (motForme.toString().equals(mot.toString())) {
                return false; // Le mot a déjà été joué
            }
        }

        // D'autres règles peuvent être ajoutées ici, selon le cahier des charges
        return true; // Si toutes les vérifications passent, le mot est valide
    }

    // Obtenir la longueur du mot
    public int getLongueur() {
        return lettres.size();
    }

    @Override
    public String toString() {
        StringBuilder motForme = new StringBuilder();
        for (Lettre lettre : lettres) {
            motForme.append(lettre.getCaractere());
        }
        return motForme.toString();
    }
}
