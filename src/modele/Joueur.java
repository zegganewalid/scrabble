package modele;

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
    
    // Méthode modifiée pour utiliser la distribution intelligente
    public void completerMain(Sac sac) {
        int lettresManquantes = 7 - lettresEnMain.size();
        
        if (lettresManquantes <= 0 || sac.estVide()) {
            return;
        }
        
        // Si la main est vide, utiliser la distribution intelligente
        if (lettresEnMain.isEmpty()) {
            List<Lettre> nouvelleMain = sac.distribuerMainJouable(7);
            lettresEnMain.addAll(nouvelleMain);
        } else {
            // Sinon, compléter la main de façon standard
            // mais vérifier si elle devient jouable
            
            // D'abord, ajouter les lettres manquantes
            List<Lettre> nouvellesLettres = new ArrayList<>();
            for (int i = 0; i < lettresManquantes && !sac.estVide(); i++) {
                Lettre lettre = sac.piocherLettre();
                if (lettre != null) {
                    nouvellesLettres.add(lettre);
                }
            }
            
            // Créer une main temporaire pour vérification
            List<Lettre> mainTemporaire = new ArrayList<>(lettresEnMain);
            mainTemporaire.addAll(nouvellesLettres);
            
            if (sac.peutFormerUnMot(mainTemporaire)) {
                // Si la main est jouable, l'adopter
                lettresEnMain.addAll(nouvellesLettres);
            } else {
                // Sinon, essayer de remplacer des lettres nouvellement piochées
                // pour rendre la main jouable
                int tentatives = 0;
                final int MAX_TENTATIVES = 10;
                
                while (!sac.peutFormerUnMot(mainTemporaire) && tentatives < MAX_TENTATIVES && !sac.estVide()) {
                    // Remettre les nouvelles lettres dans le sac
                    for (Lettre lettre : nouvellesLettres) {
                        sac.lettres.add(lettre);
                    }
                    
                    // Piocher de nouvelles lettres
                    nouvellesLettres.clear();
                    for (int i = 0; i < lettresManquantes && !sac.estVide(); i++) {
                        Lettre lettre = sac.piocherLettre();
                        if (lettre != null) {
                            nouvellesLettres.add(lettre);
                        }
                    }
                    
                    // Mettre à jour la main temporaire
                    mainTemporaire = new ArrayList<>(lettresEnMain);
                    mainTemporaire.addAll(nouvellesLettres);
                    
                    tentatives++;
                }
                
                // Adopter la meilleure main trouvée
                lettresEnMain.addAll(nouvellesLettres);
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

    // Méthode modifiée pour utiliser la distribution intelligente
    public void piocherLettres(Sac sac, int nombre) {
        if (nombre <= 0 || sac.estVide()) {
            return;
        }
        
        // Compléter la main en vérifiant qu'elle reste jouable
        List<Lettre> nouvellesLettres = new ArrayList<>();
        for (int i = 0; i < nombre && !sac.estVide(); i++) {
            Lettre lettre = sac.piocherLettre();
            if (lettre != null) {
                nouvellesLettres.add(lettre);
            }
        }
        
        // Créer une main temporaire pour vérification
        List<Lettre> mainTemporaire = new ArrayList<>(lettresEnMain);
        mainTemporaire.addAll(nouvellesLettres);
        
        if (sac.peutFormerUnMot(mainTemporaire)) {
            // Si la main est jouable, l'adopter
            lettresEnMain.addAll(nouvellesLettres);
        } else {
            // Sinon, essayer de remplacer des lettres
            int tentatives = 0;
            final int MAX_TENTATIVES = 10;
            
            while (!sac.peutFormerUnMot(mainTemporaire) && tentatives < MAX_TENTATIVES && !sac.estVide()) {
                // Remettre les nouvelles lettres dans le sac
                for (Lettre lettre : nouvellesLettres) {
                    sac.lettres.add(lettre);
                }
                
                // Piocher de nouvelles lettres
                nouvellesLettres.clear();
                for (int i = 0; i < nombre && !sac.estVide(); i++) {
                    Lettre lettre = sac.piocherLettre();
                    if (lettre != null) {
                        nouvellesLettres.add(lettre);
                    }
                }
                
                // Mettre à jour la main temporaire
                mainTemporaire = new ArrayList<>(lettresEnMain);
                mainTemporaire.addAll(nouvellesLettres);
                
                tentatives++;
            }
            
            // Adopter la meilleure main trouvée
            lettresEnMain.addAll(nouvellesLettres);
        }
    }
}