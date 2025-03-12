import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Partie {

    private Plateau plateau;
    private List<Joueur> joueurs;
    private Sac sac;
    private Dictionnaire dictionnaire;
    private int joueurActuel;
    private boolean partieTerminee;

    public Partie(List<String> nomsJoueurs) {
        if (nomsJoueurs.size() < 2 || nomsJoueurs.size() > 4) {
            throw new IllegalArgumentException("Le nombre de joueurs doit être entre 2 et 4");
        }

        this.plateau = new Plateau();
        this.sac = new Sac();
        this.dictionnaire = new Dictionnaire();
        this.joueurs = new ArrayList<>();
        this.joueurActuel = 0;
        this.partieTerminee = false;

        for (String nom : nomsJoueurs) {
            Joueur joueur = new Joueur(nom);
            joueur.completerMain(sac);
            assignerMotImpose(joueur); // Assigner un mot imposé au joueur
            joueurs.add(joueur);
        }
    }

    public void assignerMotImpose(Joueur joueur) {
        try (BufferedReader br = new BufferedReader(new FileReader("database/mots.txt"))) {
            List<String> mots = new ArrayList<>();
            String ligne;
            while ((ligne = br.readLine()) != null) {
                mots.add(ligne);
            }
            if (!mots.isEmpty()) {
                int index = (int) (Math.random() * mots.size());
                joueur.setMotImpose(mots.get(index).toUpperCase());
            }
        } catch (IOException e) {
            System.err.println("Erreur d'assignation du mot imposé: " + e.getMessage());
        }
    }

    public boolean placerMot(List<Lettre> lettres, int x, int y, Direction direction) {
        Joueur joueurCourant = joueurs.get(joueurActuel);

        String mot = "";
        for (Lettre lettre : lettres) {
            mot += lettre.getCaractere();
        }

        if (!dictionnaire.chercherMotDansFichier(mot)) {
            System.out.println("Le mot n'existe pas dans le dictionnaire.");
            return false;
        }

        int score = calculerScoreMot(lettres, x, y, direction);

        if (mot.equalsIgnoreCase(joueurCourant.getMotImpose())) {
            score += 15;
            joueurCourant.marquerMotImposePlace();
        }

        placerLettresSurPlateau(lettres, x, y, direction);

        joueurCourant.ajouterPoints(score);

        joueurCourant.completerMain(sac);

        passerAuJoueurSuivant();

        return true;
    }

    public void placerLettresSurPlateau(List<Lettre> lettres, int x, int y, Direction direction) {
        for (int i = 0; i < lettres.size(); i++) {
            int caseX = direction == Direction.HORIZONTAL ? x + i : x;
            int caseY = direction == Direction.HORIZONTAL ? y : y + i;
    
            // Vérifier que les coordonnées sont valides
            if (caseX >= 0 && caseX < 15 && caseY >= 0 && caseY < 15) {
                Case caseActuelle = plateau.getCase(caseX, caseY);
                if (caseActuelle != null) {
                    caseActuelle.setLettre(lettres.get(i));
                }
            } else {
                throw new IllegalArgumentException("Coordonnées hors limites du plateau.");
            }
        }
    }

    public int calculerScoreMot(List<Lettre> lettres, int x, int y, Direction direction) {
        int score = 0;
        int multiplicateurMot = 1;
    
        for (int i = 0; i < lettres.size(); i++) {
            int valeurLettre = lettres.get(i).getValeur();
            Case caseActuelle;
    
            if (direction == Direction.HORIZONTAL) {
                caseActuelle = plateau.getCase(x + i, y);
            } else {
                caseActuelle = plateau.getCase(x, y + i);
            }
    
            // Vérifier si caseActuelle et getTypeBonus() ne sont pas null
            if (caseActuelle != null && caseActuelle.getTypeBonus() != null) {
                switch (caseActuelle.getTypeBonus()) {
                    case "LT":
                        valeurLettre *= 3;
                        break;
                    case "LD":
                        valeurLettre *= 2;
                        break;
                    case "MT":
                        multiplicateurMot *= 3;
                        break;
                    case "MD":
                        multiplicateurMot *= 2;
                        break;
                }
            }
    
            score += valeurLettre;
        }
    
        return score * multiplicateurMot;
    }

    public void passerAuJoueurSuivant() {
        joueurActuel = (joueurActuel + 1) % joueurs.size();
    }

    public boolean estPartieTerminee() {
        if (sac.estVide()) {
            for (Joueur joueur : joueurs) {
                if (joueur.getLettresEnMain().isEmpty()) {
                    return true;
                }
            }
        }
        return partieTerminee;
    }

    public void terminerPartie() {
        for (Joueur joueur : joueurs) {
            int pointsPenalite = joueur.calculerPointsLettresRestantes();
            joueur.ajouterPoints(-pointsPenalite);
        }

        for (Joueur joueur : joueurs) {
            if (joueur.getLettresEnMain().isEmpty()) {
                joueur.ajouterPoints(50);
            }
        }

        partieTerminee = true;
    }

    public Joueur determinerVainqueur() {
        Joueur vainqueur = joueurs.get(0);
        for (Joueur joueur : joueurs) {
            if (joueur.getScore() > vainqueur.getScore()) {
                vainqueur = joueur;
            }
        }
        return vainqueur;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public Sac getSac() {
        return sac;
    }

    public Joueur getJoueurActuel() {
        return joueurs.get(joueurActuel);
    }

    public int getMultiplicateurLettre(Case c) {
        switch (c.getTypeBonus()) {
            case "LT": return 3;
            case "LD": return 2;
            default: return 1;
        }
    }

    public int getMultiplicateurMot(Case c) {
        switch (c.getTypeBonus()) {
            case "MT": return 3;
            case "MD": return 2;
            default: return 1;
        }
    }
}