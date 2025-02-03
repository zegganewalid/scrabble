import java.util.List;

/**
 * Représente un mot à placer sur le plateau de Scrabble.
 */
public class Mot {
    private List<Lettre> lettres; // Liste des lettres du mot
    private Position position; // Position de départ du mot
    private Direction direction; // Direction du mot (HORIZONTAL ou VERTICAL)
    private boolean estImpose; // Indique si le mot est imposé (ex: mot de départ)

    /**
     * Constructeur de la classe Mot
     * @param lettres Liste des lettres qui composent le mot
     * @param position Position de départ du mot
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @param estImpose Indique si le mot est imposé
     */
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

    /**
     * Calcule le score du mot en additionnant la valeur des lettres.
     * @return Score total du mot
     */
    public int calculerScore() {
        int score = 0;
        for (Lettre lettre : lettres) {
            score += lettre.getValeur();
        }
        return score;
    }

    /**
     * Vérifie si le mot est valide en consultant un dictionnaire.
     * @param dictionnaire Dictionnaire contenant les mots valides
     * @param motsJoues Liste des mots déjà joués
     * @return true si le mot est valide, sinon false
     */
    public boolean verifierValidite(Dictionnaire dictionnaire, List<Mot> motsJoues) {
        StringBuilder motForme = new StringBuilder();
        for (Lettre lettre : lettres) {
            motForme.append(lettre.getCaractere());
        }
        String motString = motForme.toString();

        // Vérification dans le dictionnaire
        if (!dictionnaire.contientMot(motString)) {
            return false;
        }

        // Vérification si le mot a déjà été joué
        for (Mot mot : motsJoues) {
            if (motString.equals(mot.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtient la longueur du mot.
     * @return Longueur du mot
     */
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
