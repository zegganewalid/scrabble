import java.util.List;
public class Mot {
    private List<Lettre> lettres;
    private Case positionDebut;
    private boolean estImpose;

    public Mot(List<Lettre> lettres, Case position, Direction direction, boolean estImpose) {
        this.lettres = lettres;
        this.positionDebut = position;
        this.estImpose = estImpose;
    }

    public Mot(List<Lettre> lettres) {
        this.lettres = lettres;
    }

    public List<Lettre> getLettres() {
        return lettres;
    }

    public Case getPositionDebut() {
        return positionDebut;
    }

    public void setPosition(Case positionDebut) {
        this.positionDebut = positionDebut;
    }

    public boolean isEstImpose() {
        return estImpose;
    }

    public String afficherMot(){
        String mot = "";
        for (Lettre lettre : lettres) {
            mot += lettre.getCaractere();
        }
        return mot;
    }

    public int calculerScoreMot() {
        int score = 0;
        for (Lettre lettre : lettres) {
            score += lettre.getValeur();
        }
        return score;
    }

    
}