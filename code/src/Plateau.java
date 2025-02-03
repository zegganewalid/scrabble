import java.util.List;

public class Plateau {
    private static final int TAILLE = 15; // Taille du plateau standard
    private Case[][] cases; // Grille du plateau

    // Constructeur qui initialise le plateau et place les bonus
    public Plateau() {
        this.cases = new Case[TAILLE][TAILLE];
        initialiserCases(); // Assure que toutes les cases sont créées
        initialiserBonus(); // Ajoute les bonus aux cases concernées
    }

    // Initialisation des cases du plateau
    private void initialiserCases() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
            }
        }
    }

    // Ajoute les bonus aux cases spécifiques du plateau
    private void initialiserBonus() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                if (i == 7 && j == 7) {
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
        Position position = mot.getPosition(); // Position de départ
        Direction direction = mot.getDirection(); // HORIZONTAL ou VERTICAL
        List<Lettre> lettres = mot.getLettres(); // Liste des lettres du mot

        int x = position.getLigne();
        int y = position.getColonne();

        // Vérification des limites du plateau
        if (direction == Direction.HORIZONTAL) {
            if (y + lettres.size() > TAILLE) return false;
        } else { // Direction VERTICAL
            if (x + lettres.size() > TAILLE) return false;
        }

        // Vérification des cases occupées
        for (int i = 0; i < lettres.size(); i++) {
            int nx = direction == Direction.HORIZONTAL ? x : x + i;
            int ny = direction == Direction.HORIZONTAL ? y + i : y;

            Case caseActuelle = cases[nx][ny];

            // Vérifier si la case est déjà occupée par une lettre différente
            if (caseActuelle.isEstOccupe()) {
                Lettre lettreExistante = caseActuelle.getLettre();
                if (lettreExistante.getCaractere() != lettres.get(i).getCaractere()) {
                    return false; // Conflit avec une lettre existante
                }
            }
        }

        return true; // Placement possible
    }

    // Place un mot sur le plateau si le placement est valide
    public boolean placerMot(Mot mot) {
        if (!verifierPlacement(mot)) {
            return false; // Placement invalide
        }

        Position position = mot.getPosition();
        Direction direction = mot.getDirection();
        List<Lettre> lettres = mot.getLettres();

        int x = position.getLigne();
        int y = position.getColonne();

        // Placement des lettres
        for (int i = 0; i < lettres.size(); i++) {
            int nx = direction == Direction.HORIZONTAL ? x : x + i;
            int ny = direction == Direction.HORIZONTAL ? y + i : y;

            // Vérification pour éviter une NullPointerException
            if (cases[nx][ny] != null) {
                cases[nx][ny].occuper(lettres.get(i));
            }
        }

        return true; // Placement réussi
    }

    // Vérifie si un mot est connecté à d'autres mots déjà placés
    private boolean motEstConnecte(Mot mot) {
        Position position = mot.getPosition();
        Direction direction = mot.getDirection();
        List<Lettre> lettres = mot.getLettres();

        int x = position.getLigne();
        int y = position.getColonne();

        for (int i = 0; i < lettres.size(); i++) {
            int nx = direction == Direction.HORIZONTAL ? x : x + i;
            int ny = direction == Direction.HORIZONTAL ? y + i : y;

            // Vérifier les cases adjacentes (haut, bas, gauche, droite)
            if (nx > 0 && cases[nx - 1][ny] != null && cases[nx - 1][ny].isEstOccupe()) return true;
            if (nx < TAILLE - 1 && cases[nx + 1][ny] != null && cases[nx + 1][ny].isEstOccupe()) return true;
            if (ny > 0 && cases[nx][ny - 1] != null && cases[nx][ny - 1].isEstOccupe()) return true;
            if (ny < TAILLE - 1 && cases[nx][ny + 1] != null && cases[nx][ny + 1].isEstOccupe()) return true;
        }

        return false; // Si aucune connexion trouvée
    }
}
