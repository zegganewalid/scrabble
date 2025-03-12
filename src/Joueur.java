import java.util.ArrayList;
import java.util.List;

public class Joueur {
    private List<Lettre> lettresEnMain;
    private String nom;
    private int score;
    private String motImpose;
    private boolean aPlaceMotImpose;
    
    public Joueur() {
        this.lettresEnMain = new ArrayList<>();
        this.nom = "JoueurGhust";
        this.score = 0;
        this.aPlaceMotImpose = false;
    }

    public Joueur(String nom) {
        this.lettresEnMain = new ArrayList<>();
        this.nom = nom;
        this.score = 0;
        this.aPlaceMotImpose = false;
    }

    public String getNom() {
        return nom;
    }
    
    public void ajouterLettre(Lettre lettre) {
        lettresEnMain.add(lettre);
    }
    
    public void retirerLettre(Lettre lettre) {
        lettresEnMain.remove(lettre);
    }
    
    public List<Lettre> getLettresEnMain() {
        return lettresEnMain;
    }
    
    public void setMotImpose(String motImpose) {
        this.motImpose = motImpose;
        this.aPlaceMotImpose = false;
    }
    
    public String getMotImpose() {
        return motImpose;
    }
    
    public boolean aPlaceMotImpose() {
        return aPlaceMotImpose;
    }
    
    public void marquerMotImposePlace() {
        this.aPlaceMotImpose = true;
    }
    
    public void ajouterPoints(int points) {
        this.score += points;
    }
    
    public int getScore() {
        return score;
    }
    
    public void completerMain(Sac sac) {
        while (lettresEnMain.size() < 7 && !sac.estVide()) {
            Lettre lettre = sac.piocherLettre();
            if (lettre != null) {
                ajouterLettre(lettre);
            }
        }
    }
    
    public int calculerPointsLettresRestantes() {
        int points = 0;
        for (Lettre lettre : lettresEnMain) {
            points += lettre.getValeur();
        }
        return points;
    }

    public void piocherLettres(Sac sac, int nombre) {
        for (int i = 0; i < nombre; i++) {
            if (!sac.estVide()) {
                Lettre lettre = sac.piocherLettre();
                if (lettre != null) {
                    ajouterLettre(lettre);
                }
            } else {
                break;
            }
        }
    }
}