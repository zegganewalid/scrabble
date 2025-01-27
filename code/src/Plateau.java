import java.util.List;

public class Plateau {
    private static final int TAILLE = 15;
    private Case[][] cases; // Grille du plateau

    public Plateau() {
        this.cases = new Case[TAILLE][TAILLE];
        initialiserBonus();
    }

    // Initialisation des cases et des bonus
    private void initialiserBonus() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
                // Bonus classiques du Scrabble (simplifié ici)
                if ((i == 7 && j == 7)) {
                    cases[i][j].setTypeBonus("MT"); // Mot compte triple
                } else if ((i == 0 && j == 0) || (i == 14 && j == 14)) {
                    cases[i][j].setTypeBonus("MD"); // Mot compte double
                } else if ((i == 1 && j == 5) || (i == 5 && j == 1)) {
                    cases[i][j].setTypeBonus("LD"); // Lettre compte double
                } else if ((i == 2 && j == 6) || (i == 6 && j == 2)) {
                    cases[i][j].setTypeBonus("LT"); // Lettre compte triple
                }
            }
        }
    }

    // Vérifie si un mot peut être placé à une position donnée
    public boolean verifierPlacement(Mot mot) {
        int x = mot.getPosition().getX(); // Position de départ (x)
        int y = mot.getPosition().getY(); // Position de départ (y)
        Direction direction = mot.getDirection(); // Direction du mot (HORIZONTAL ou VERTICAL)
        List<Lettre> lettres = mot.getLettres(); // Liste des lettres du mot

        // Vérification des limites du plateau
        if (direction == Direction.HORIZONTAL) {
            if (y + lettres.size() > TAILLE) return false; // Dépassement horizontal
        } else if (direction == Direction.VERTICAL) {
            if (x + lettres.size() > TAILLE) return false; // Dépassement vertical
        }

        // Vérification des cases pour le mot
        for (int i = 0; i < lettres.size(); i++) {
            int nx = direction == Direction.HORIZONTAL ? x : x + i; // Calcul de la coordonnée x
            int ny = direction == Direction.HORIZONTAL ? y + i : y; // Calcul de la coordonnée y

            Case caseActuelle = cases[nx][ny];

            // Si la case est occupée, la lettre doit correspondre
            if (caseActuelle.isEstOccupe()) {
                Lettre lettreExistante = caseActuelle.getLettre();
                if (lettreExistante.getCaractere() != lettres.get(i).getCaractere()) {
                    return false; // Conflit avec une lettre existante
                }
            }
        }

        // Toutes les vérifications sont respectées
        return true;
    }

    // Place un mot sur le plateau en utilisant la méthode occuper de la classe Case
    public boolean placerMot(Mot mot) {
        if (!verifierPlacement(mot)) {
            return false; // Placement invalide
        }

        int x = mot.getPosition().getX();
        int y = mot.getPosition().getY();
        Direction direction = mot.getDirection();
        List<Lettre> lettres = mot.getLettres();

        // Place chaque lettre sur le plateau
        for (int i = 0; i < lettres.size(); i++) {
            int nx = direction == Direction.HORIZONTAL ? x : x + i;
            int ny = direction == Direction.HORIZONTAL ? y + i : y;

            cases[nx][ny].occuper(lettres.get(i)); // Utilisation de la méthode occuper
        }

        return true; // Placement réussi
    }

    // Vérifie si le mot est connecté à d'autres mots déjà placés
    private boolean motEstConnecte(Mot mot) {
        int x = mot.getPosition().getX();
        int y = mot.getPosition().getY();
        Direction direction = mot.getDirection();
        List<Lettre> lettres = mot.getLettres();

        for (int i = 0; i < lettres.size(); i++) {
            int nx = direction == Direction.HORIZONTAL ? x : x + i;
            int ny = direction == Direction.HORIZONTAL ? y + i : y;

            // Vérifier les cases adjacentes (haut, bas, gauche, droite)
            if (nx > 0 && cases[nx - 1][ny].isEstOccupe()) return true;
            if (nx < TAILLE - 1 && cases[nx + 1][ny].isEstOccupe()) return true;
            if (ny > 0 && cases[nx][ny - 1].isEstOccupe()) return true;
            if (ny < TAILLE - 1 && cases[nx][ny + 1].isEstOccupe()) return true;
        }

        // Si aucune connexion trouvée
        return false;
    }
}
