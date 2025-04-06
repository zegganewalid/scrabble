package modele;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Classe centrale qui gère le déroulement d'une partie de Scrabble.
 * Elle contient toute la logique de jeu et utilise le pattern Observer
 * pour notifier les autres composants des changements d'état.
 * Cette classe a été modifiée pour supporter le mode réseau.
 */
public class Partie extends Observable {

    private Plateau plateau;                // Le plateau de jeu
    private List<Joueur> joueurs;           // Liste des joueurs participant à la partie
    private Sac sac;                        // Le sac contenant les lettres disponibles
    private Dictionnaire dictionnaire;      // Le dictionnaire pour vérifier la validité des mots
    private int joueurActuel;               // Index du joueur dont c'est le tour
    private boolean partieTerminee;         // Indique si la partie est terminée
    private int nombreTours;                // Compteur de tours de jeu
    private boolean premierTourJoue = false; // Indique si le premier tour a été joué (important pour les règles)

    /**
     * Crée une nouvelle partie avec la liste des noms de joueurs fournie.
     * Initialise le plateau, le sac de lettres, et distribue les lettres aux joueurs.
     * 
     * @param nomsJoueurs Liste des noms des joueurs
     * @throws IllegalArgumentException si le nombre de joueurs n'est pas entre 2 et 4
     */
    public Partie(List<String> nomsJoueurs) {
        // Vérification du nombre de joueurs
        if (nomsJoueurs.size() < 2 || nomsJoueurs.size() > 4) {
            throw new IllegalArgumentException("Le nombre de joueurs doit être entre 2 et 4");
        }

        // Initialisation des composants du jeu
        this.plateau = new Plateau();        // Création du plateau de jeu
        this.sac = new Sac();                // Création du sac de lettres
        this.dictionnaire = new Dictionnaire(); // Chargement du dictionnaire
        this.joueurs = new ArrayList<>();    // Liste des joueurs
        this.joueurActuel = 0;               // Premier joueur
        this.partieTerminee = false;         // La partie commence
        this.nombreTours = 1;                // Premier tour

        // Création des joueurs et distribution des lettres
        for (String nom : nomsJoueurs) {
            Joueur joueur = new Joueur(nom);
            joueur.completerMain(sac);       // Le joueur pioche 7 lettres
            assignerMotImpose(joueur);       // Assigne un mot imposé au joueur (variante)
            joueurs.add(joueur);
        }
        
        // Notification que la partie est initialisée (pattern Observer)
        setChanged();
        notifyObservers("PARTIE_INITIALISEE");
    }

    /**
     * Assigne un mot imposé aléatoire au joueur.
     * Ce mot, s'il est placé durant la partie, rapporte des points bonus.
     * 
     * @param joueur Le joueur à qui assigner un mot imposé
     */
    public void assignerMotImpose(Joueur joueur) {
        try (BufferedReader br = new BufferedReader(new FileReader("database/mots.txt"))) {
            // Lecture du fichier de mots
            List<String> mots = new ArrayList<>();
            String ligne;
            while ((ligne = br.readLine()) != null) {
                mots.add(ligne);
            }
            
            // S'il y a des mots disponibles, en choisir un au hasard
            if (!mots.isEmpty()) {
                int index = (int) (Math.random() * mots.size());
                joueur.setMotImpose(mots.get(index).toUpperCase());
                
                // Notification du mot imposé (pattern Observer)
                setChanged();
                notifyObservers("MOT_IMPOSE:" + joueur.getNom() + ":" + joueur.getMotImpose());
            }
        } catch (IOException e) {
            System.err.println("Erreur d'assignation du mot imposé: " + e.getMessage());
        }
    }

    /**
     * Place un mot sur le plateau.
     * Cette méthode a été modifiée pour supporter les mots connectés
     * et prendre en compte les lettres existantes sur le plateau.
     * 
     * @param lettres Liste des lettres à placer
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return true si le placement a réussi, false sinon
     */
    public boolean placerMot(List<Lettre> lettres, int x, int y, Direction direction) {
        Joueur joueurCourant = joueurs.get(joueurActuel);

        // Construction du mot à partir des lettres nouvellement placées
        StringBuilder motSb = new StringBuilder();
        for (Lettre lettre : lettres) {
            motSb.append(lettre.getCaractere());
        }
        String motPlacé = motSb.toString();
        
        System.out.println("Tentative de placement du mot: " + motPlacé + " à la position (" + x + "," + y + ") en direction " + direction);

        // Si c'est le premier tour, vérifier que le mot passe par le centre (règle du Scrabble)
        if (premierTourJoue == false) {
            boolean passeParleCentre = false;
            for (int i = 0; i < lettres.size(); i++) {
                int posX = direction == Direction.HORIZONTAL ? x + i : x;
                int posY = direction == Direction.HORIZONTAL ? y : y + i;
                if (posX == 7 && posY == 7) {  // Le centre du plateau est à (7,7)
                    passeParleCentre = true;
                    break;
                }
            }
            
            // Si le mot ne passe pas par le centre au premier tour, échec
            if (!passeParleCentre) {
                setChanged();
                notifyObservers("ERREUR:Le premier mot doit passer par le centre du plateau");
                return false;
            }
        }

        // Récupérer le mot complet formé (incluant les lettres déjà sur le plateau)
        String motComplet = extraireMotComplet(lettres, x, y, direction);
        System.out.println("Mot complet formé: " + motComplet);
        
        // Si le mot est vide, échec du placement
        if (motComplet.isEmpty()) {
            setChanged();
            notifyObservers("ERREUR:Placement du mot impossible");
            return false;
        }

        // Vérifier si le mot complet est dans le dictionnaire
        if (!dictionnaire.chercherMotDansFichier(motComplet)) {
            setChanged();
            notifyObservers("ERREUR:Le mot n'existe pas dans le dictionnaire");
            return false;
        }

        // Vérifier les mots secondaires formés perpendiculairement
        if (!verifierMotsSecondaires(lettres, x, y, direction)) {
            setChanged();
            notifyObservers("ERREUR:Un mot secondaire formé n'est pas valide");
            return false;
        }

        // Calculer le score du mot principal
        int score = calculerScoreMot(lettres, x, y, direction);

        // Calculer le score des mots secondaires
        score += calculerScoreMotsSecondaires(lettres, x, y, direction);

        // Vérifier si c'est le mot imposé (bonus de points)
        if (motComplet.equalsIgnoreCase(joueurCourant.getMotImpose())) {
            score += 15;  // Bonus de 15 points pour avoir placé le mot imposé
            joueurCourant.marquerMotImposePlace();
            
            // Notification que le mot imposé a été placé
            setChanged();
            notifyObservers("MOT_IMPOSE_PLACE:" + joueurCourant.getNom());
        }

        // Placer les lettres sur le plateau
        placerLettresSurPlateau(lettres, x, y, direction);

        // Ajouter les points au joueur
        joueurCourant.ajouterPoints(score);

        // Compléter la main du joueur avec de nouvelles lettres
        joueurCourant.completerMain(sac);

        // Marquer le premier tour comme joué
        premierTourJoue = true;
        
        // Notification des changements (pattern Observer)
        setChanged();
        notifyObservers("PLATEAU_MODIFIE");
        
        setChanged();
        notifyObservers("SCORE_MODIFIE:" + joueurCourant.getNom() + ":" + joueurCourant.getScore());

        return true;
    }

    /**
     * Extrait le mot complet formé, incluant les lettres déjà présentes sur le plateau.
     * Cette méthode a été complètement révisée pour corriger les problèmes de connexion.
     * 
     * @param lettresPlacees Liste des nouvelles lettres placées
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot
     * @return Le mot complet formé
     */
    private String extraireMotComplet(List<Lettre> lettresPlacees, int x, int y, Direction direction) {
        if (lettresPlacees.isEmpty()) return "";
        
        // Trouver le début réel du mot (en remontant jusqu'à la première lettre)
        int startX = x;
        int startY = y;
        
        // Reculer jusqu'au début du mot
        if (direction == Direction.HORIZONTAL) {
            // Chercher le début du mot (à gauche)
            while (startX > 0 && plateau.getCase(startX - 1, startY).isEstOccupe()) {
                startX--;
            }
        } else {
            // Chercher le début du mot (en haut)
            while (startY > 0 && plateau.getCase(startX, startY - 1).isEstOccupe()) {
                startY--;
            }
        }
        
        System.out.println("Début du mot: (" + startX + "," + startY + ")");
        
        // Créer une Map pour accéder facilement aux lettres placées par position
        Map<String, Lettre> lettresMap = new HashMap<>();
        for (int i = 0; i < lettresPlacees.size(); i++) {
            int posX = direction == Direction.HORIZONTAL ? x + i : x;
            int posY = direction == Direction.HORIZONTAL ? y : y + i;
            lettresMap.put(posX + "," + posY, lettresPlacees.get(i));
        }
        
        // Construire le mot complet
        StringBuilder motComplet = new StringBuilder();
        int currentX = startX;
        int currentY = startY;
        boolean continueMot = true;
        
        while (continueMot) {
            // Position actuelle sous forme de clé pour la Map
            String posKey = currentX + "," + currentY;
            
            if (plateau.getCase(currentX, currentY).isEstOccupe()) {
                // Si la case est déjà occupée, utiliser sa lettre
                motComplet.append(plateau.getCase(currentX, currentY).getLettre().getCaractere());
            } else if (lettresMap.containsKey(posKey)) {
                // Si on a une nouvelle lettre à cette position
                motComplet.append(lettresMap.get(posKey).getCaractere());
            } else {
                // Si on ne trouve pas de lettre à cette position, c'est la fin du mot
                break;
            }
            
            // Avancer à la position suivante
            if (direction == Direction.HORIZONTAL) {
                currentX++;
                // Arrêter si on atteint le bord du plateau
                if (currentX >= 15) {
                    break;
                }
            } else {
                currentY++;
                // Arrêter si on atteint le bord du plateau
                if (currentY >= 15) {
                    break;
                }
            }
            
            // Vérifier si on doit continuer à construire le mot
            // On continue si la case suivante est occupée ou contient une de nos nouvelles lettres
            String nextPosKey = currentX + "," + currentY;
            if (!plateau.getCase(currentX, currentY).isEstOccupe() && 
                !lettresMap.containsKey(nextPosKey)) {
                continueMot = false;
            }
        }
        
        String motFinal = motComplet.toString();
        System.out.println("Mot complet construit: " + motFinal);
        return motFinal;
    }

    /**
     * Vérifie les mots secondaires formés perpendiculairement au mot principal.
     * Cette méthode a été modifiée pour ignorer la validité des mots secondaires.
     * 
     * @param lettresPlacees Liste des nouvelles lettres placées
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot principal
     * @return true si tous les mots secondaires sont valides, false sinon
     */
    private boolean verifierMotsSecondaires(List<Lettre> lettresPlacees, int x, int y, Direction direction) {
        // On affiche toujours les mots secondaires formés pour information, mais on ne vérifie pas leur validité
        for (int i = 0; i < lettresPlacees.size(); i++) {
            int currentX = direction == Direction.HORIZONTAL ? x + i : x;
            int currentY = direction == Direction.HORIZONTAL ? y : y + i;
            
            // Vérifier si cette lettre forme un mot dans la direction perpendiculaire
            String motSecondaire = extraireMotSecondaire(lettresPlacees.get(i), currentX, currentY, 
                                                        direction == Direction.HORIZONTAL ? Direction.VERTICAL : Direction.HORIZONTAL);
            
            // Si un mot secondaire est formé (plus d'une lettre), l'afficher
            if (motSecondaire.length() > 1) {
                System.out.println("Mot secondaire détecté: " + motSecondaire);
            }
        }
        
        // Toujours retourner true pour ignorer la validité des mots secondaires (simplification des règles)
        return true;
    }

    /**
     * Extrait un mot secondaire formé perpendiculairement au mot principal.
     * 
     * @param lettrePlacee La lettre qui forme le mot secondaire
     * @param x Coordonnée X de la lettre
     * @param y Coordonnée Y de la lettre
     * @param direction Direction du mot secondaire
     * @return Le mot secondaire formé
     */
    private String extraireMotSecondaire(Lettre lettrePlacee, int x, int y, Direction direction) {
        // Trouver le début du mot secondaire
        int startX = x;
        int startY = y;

        if (direction == Direction.HORIZONTAL) {
            // Chercher le début du mot secondaire (à gauche)
            while (startX > 0 && plateau.getCase(startX - 1, startY).isEstOccupe()) {
                startX--;
            }
        } else {
            // Chercher le début du mot secondaire (en haut)
            while (startY > 0 && plateau.getCase(startX, startY - 1).isEstOccupe()) {
                startY--;
            }
        }

        // Construire le mot secondaire
        StringBuilder motSecondaire = new StringBuilder();
        int currentX = startX;
        int currentY = startY;
        boolean continueMot = true;

        while (continueMot) {
            if (currentX == x && currentY == y) {
                // Position de la lettre qu'on place
                motSecondaire.append(lettrePlacee.getCaractere());
            } else if (plateau.getCase(currentX, currentY).isEstOccupe()) {
                // Position d'une lettre déjà sur le plateau
                motSecondaire.append(plateau.getCase(currentX, currentY).getLettre().getCaractere());
            } else {
                // Fin du mot
                continueMot = false;
            }

            // Avancer à la position suivante
            if (direction == Direction.HORIZONTAL) {
                currentX++;
                if (currentX >= 15 || !plateau.getCase(currentX, currentY).isEstOccupe() && !(currentX == x && currentY == y)) {
                    continueMot = false;
                }
            } else {
                currentY++;
                if (currentY >= 15 || !plateau.getCase(currentX, currentY).isEstOccupe() && !(currentX == x && currentY == y)) {
                    continueMot = false;
                }
            }
        }

        return motSecondaire.toString();
    }

    /**
     * Calcule le score total des mots secondaires formés.
     * 
     * @param lettresPlacees Liste des nouvelles lettres placées
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot principal
     * @return Le score total des mots secondaires
     */
    private int calculerScoreMotsSecondaires(List<Lettre> lettresPlacees, int x, int y, Direction direction) {
        int scoreTotal = 0;
        
        for (int i = 0; i < lettresPlacees.size(); i++) {
            int currentX = direction == Direction.HORIZONTAL ? x + i : x;
            int currentY = direction == Direction.HORIZONTAL ? y : y + i;
            
            // Compter uniquement les mots secondaires (plus d'une lettre)
            String motSecondaire = extraireMotSecondaire(lettresPlacees.get(i), currentX, currentY,
                                                        direction == Direction.HORIZONTAL ? Direction.VERTICAL : Direction.HORIZONTAL);
            
            if (motSecondaire.length() > 1) {
                // Calculer le score du mot secondaire
                int scoreMotSecondaire = calculerScoreMotSecondaire(lettresPlacees.get(i), currentX, currentY, 
                                                                    direction == Direction.HORIZONTAL ? Direction.VERTICAL : Direction.HORIZONTAL);
                scoreTotal += scoreMotSecondaire;
            }
        }
        
        return scoreTotal;
    }

    /**
     * Calcule le score d'un mot secondaire spécifique.
     * 
     * @param lettrePlacee La lettre qui forme le mot secondaire
     * @param x Coordonnée X de la lettre
     * @param y Coordonnée Y de la lettre
     * @param direction Direction du mot secondaire
     * @return Le score du mot secondaire
     */
    private int calculerScoreMotSecondaire(Lettre lettrePlacee, int x, int y, Direction direction) {
        int score = 0;
        int multiplicateurMot = 1;  // Multiplicateur de score du mot entier
        
        // Trouver le début du mot secondaire
        int startX = x;
        int startY = y;

        if (direction == Direction.HORIZONTAL) {
            while (startX > 0 && plateau.getCase(startX - 1, startY).isEstOccupe()) {
                startX--;
            }
        } else {
            while (startY > 0 && plateau.getCase(startX, startY - 1).isEstOccupe()) {
                startY--;
            }
        }

        // Calculer le score de chaque lettre du mot secondaire
        int currentX = startX;
        int currentY = startY;
        boolean continueMot = true;

        while (continueMot) {
            int valeurLettre;
            Case currentCase = plateau.getCase(currentX, currentY);
            
            if (currentX == x && currentY == y) {
                // C'est la lettre qu'on place (appliquer les bonus de case)
                valeurLettre = lettrePlacee.getValeur();
                
                // Appliquer les bonus de case pour cette lettre
                if (currentCase.getTypeBonus() != null) {
                    switch (currentCase.getTypeBonus()) {
                        case "LT":  // Lettre compte triple
                            valeurLettre *= 3;
                            break;
                        case "LD":  // Lettre compte double
                            valeurLettre *= 2;
                            break;
                        case "MT":  // Mot compte triple
                            multiplicateurMot *= 3;
                            break;
                        case "MD":  // Mot compte double
                            multiplicateurMot *= 2;
                            break;
                    }
                }
                
            } else if (currentCase.isEstOccupe()) {
                // C'est une lettre déjà sur le plateau (pas de bonus, ils ont déjà été appliqués)
                valeurLettre = currentCase.getLettre().getValeur();
            } else {
                // Fin du mot
                break;
            }
            
            score += valeurLettre;
            
            // Avancer à la position suivante
            if (direction == Direction.HORIZONTAL) {
                currentX++;
                if (currentX >= 15 || (!plateau.getCase(currentX, currentY).isEstOccupe() && !(currentX == x && currentY == y))) {
                    continueMot = false;
                }
            } else {
                currentY++;
                if (currentY >= 15 || (!plateau.getCase(currentX, currentY).isEstOccupe() && !(currentX == x && currentY == y))) {
                    continueMot = false;
                }
            }
        }
        
        // Appliquer le multiplicateur de mot au score total
        return score * multiplicateurMot;
    }
    
    /**
     * Place physiquement les lettres sur le plateau.
     * Gère les cas où certaines positions sont déjà occupées.
     * 
     * @param lettres Liste des lettres à placer
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot
     */
    public void placerLettresSurPlateau(List<Lettre> lettres, int x, int y, Direction direction) {
        // Si la position de départ est occupée, ajuster la position
        if (plateau.getCase(x, y).isEstOccupe()) {
            System.out.println("Ajustement de la position pour le placement des lettres...");
            // Trouver une position adjacente libre
            if (direction == Direction.HORIZONTAL) {
                // Chercher à droite
                while (x < 15 && plateau.getCase(x, y).isEstOccupe()) {
                    x++;
                }
                if (x >= 15) {
                    // Chercher à gauche
                    x = x - lettres.size();
                    while (x >= 0 && plateau.getCase(x, y).isEstOccupe()) {
                        x--;
                    }
                    x++; // Revenir à la première position libre
                }
            } else {
                // Chercher en bas
                while (y < 15 && plateau.getCase(x, y).isEstOccupe()) {
                    y++;
                }
                if (y >= 15) {
                    // Chercher en haut
                    y = y - lettres.size();
                    while (y >= 0 && plateau.getCase(x, y).isEstOccupe()) {
                        y--;
                    }
                    y++; // Revenir à la première position libre
                }
            }
            System.out.println("Nouvelle position de placement: (" + x + "," + y + ")");
        }
        
        // Placer chaque lettre sur le plateau
        for (int i = 0; i < lettres.size(); i++) {
            int caseX = direction == Direction.HORIZONTAL ? x + i : x;
            int caseY = direction == Direction.HORIZONTAL ? y : y + i;
    
            // Vérifier que les coordonnées sont valides
            if (caseX >= 0 && caseX < 15 && caseY >= 0 && caseY < 15) {
                Case caseActuelle = plateau.getCase(caseX, caseY);
                
                // Ne placer la lettre que si la case n'est pas déjà occupée
                if (caseActuelle != null && !caseActuelle.isEstOccupe()) {
                    caseActuelle.setLettre(lettres.get(i));
                    System.out.println("Lettre " + lettres.get(i).getCaractere() + " placée à (" + caseX + "," + caseY + ")");
                } else {
                    System.out.println("Case (" + caseX + "," + caseY + ") déjà occupée, lettre " + lettres.get(i).getCaractere() + " non placée");
                }
            } else {
                System.out.println("Coordonnées (" + caseX + "," + caseY + ") hors limites du plateau.");
            }
        }
    }

    /**
     * Calcule le score d'un mot principal.
     * 
     * @param lettres Liste des lettres du mot
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot
     * @return Le score du mot
     */
    public int calculerScoreMot(List<Lettre> lettres, int x, int y, Direction direction) {
        int score = 0;
        int multiplicateurMot = 1;  // Multiplicateur de score du mot entier
    
        // Calculer le score pour chaque lettre
        for (int i = 0; i < lettres.size(); i++) {
            int valeurLettre = lettres.get(i).getValeur();
            Case caseActuelle;
    
            // Déterminer la case en fonction de la direction
            if (direction == Direction.HORIZONTAL) {
                caseActuelle = plateau.getCase(x + i, y);
            } else {
                caseActuelle = plateau.getCase(x, y + i);
            }
    
            // Vérifier si caseActuelle et getTypeBonus() ne sont pas null
            if (caseActuelle != null && caseActuelle.getTypeBonus() != null) {
                // Appliquer les bonus de case
                switch (caseActuelle.getTypeBonus()) {
                    case "LT":  // Lettre compte triple
                        valeurLettre *= 3;
                        break;
                    case "LD":  // Lettre compte double
                        valeurLettre *= 2;
                        break;
                    case "MT":  // Mot compte triple
                        multiplicateurMot *= 3;
                        break;
                    case "MD":  // Mot compte double
                        multiplicateurMot *= 2;
                        break;
                }
            }
    
            score += valeurLettre;
        }
    
        // Appliquer le multiplicateur de mot au score total
        return score * multiplicateurMot;
    }

    /**
     * Passe au joueur suivant et met à jour le compteur de tours si nécessaire.
     */
    public void passerAuJoueurSuivant() {
        System.out.println("DEBUG: Avant changement - Joueur actuel : " + getJoueurActuel().getNom() + ", Index : " + joueurActuel);
        
        // Toujours incrémenter le numéro du joueur actuel
        joueurActuel = (joueurActuel + 1) % joueurs.size();
        
        System.out.println("DEBUG: Après changement - Joueur actuel : " + getJoueurActuel().getNom() + ", Index : " + joueurActuel);
        
        // Notifier du changement de joueur
        setChanged();
        notifyObservers("JOUEUR_ACTUEL:" + getJoueurActuel().getNom());
        
        // Incrémenter le nombre de tours UNIQUEMENT quand on revient au premier joueur
        if (joueurActuel == 0) {
            nombreTours++;
            System.out.println("DEBUG: Nouveau tour : " + nombreTours);
            setChanged();
            notifyObservers("TOUR:" + nombreTours);
        }
    }

    /**
     * Vérifie si la partie est terminée.
     * Une partie est terminée si le sac est vide et qu'un joueur n'a plus de lettres,
     * ou si la partie a été explicitement terminée.
     * 
     * @return true si la partie est terminée, false sinon
     */
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

    /**
     * Termine la partie et calcule les scores finaux.
     * Applique les règles de fin de partie du Scrabble:
     * - Les joueurs perdent des points pour les lettres non jouées
     * - Le joueur qui a joué toutes ses lettres gagne un bonus
     */
    public void terminerPartie() {
        // Soustraire les points des lettres restantes
        for (Joueur joueur : joueurs) {
            int pointsPenalite = joueur.calculerPointsLettresRestantes();
            joueur.ajouterPoints(-pointsPenalite);
        }

        // Bonus pour le joueur qui a joué toutes ses lettres
        for (Joueur joueur : joueurs) {
            if (joueur.getLettresEnMain().isEmpty()) {
                joueur.ajouterPoints(50);  // Bonus de 50 points
            }
        }

        partieTerminee = true;
        
        // Notifier de la fin de partie
        setChanged();
        notifyObservers("PARTIE_TERMINEE");
    }

    /**
     * Détermine le vainqueur de la partie en fonction des scores.
     * 
     * @return Le joueur avec le score le plus élevé
     */
    public Joueur determinerVainqueur() {
        Joueur vainqueur = joueurs.get(0);
        for (Joueur joueur : joueurs) {
            if (joueur.getScore() > vainqueur.getScore()) {
                vainqueur = joueur;
            }
        }
        return vainqueur;
    }

    // Getters/Setters
    
    /**
     * Retourne la liste des joueurs de la partie.
     * 
     * @return Liste des joueurs
     */
    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    /**
     * Retourne le plateau de jeu.
     * 
     * @return Le plateau
     */
    public Plateau getPlateau() {
        return plateau;
    }

    /**
     * Retourne le sac de lettres.
     * 
     * @return Le sac
     */
    public Sac getSac() {
        return sac;
    }

    /**
     * Retourne le joueur dont c'est actuellement le tour.
     * 
     * @return Le joueur actuel
     * @throws IllegalStateException si aucun joueur n'est disponible
     */
    public Joueur getJoueurActuel() {
        if (joueurs == null || joueurs.isEmpty()) {
            throw new IllegalStateException("Aucun joueur dans la partie");
        }
        return joueurs.get(joueurActuel);
    }

    /**
     * Retourne le multiplicateur de valeur d'une lettre selon la case.
     * 
     * @param c La case à vérifier
     * @return Le multiplicateur (1, 2 ou 3)
     */
    public int getMultiplicateurLettre(Case c) {
        switch (c.getTypeBonus()) {
            case "LT": return 3;  // Lettre compte triple
            case "LD": return 2;  // Lettre compte double
            default: return 1;    // Pas de bonus
        }
    }

    /**
     * Retourne le multiplicateur de valeur d'un mot selon la case.
     * 
     * @param c La case à vérifier
     * @return Le multiplicateur (1, 2 ou 3)
     */
    public int getMultiplicateurMot(Case c) {
        switch (c.getTypeBonus()) {
            case "MT": return 3;  // Mot compte triple
            case "MD": return 2;  // Mot compte double
            default: return 1;    // Pas de bonus
        }
    }
    
    /**
     * Indique si c'est le premier tour de jeu.
     * Cette information est importante car des règles spéciales s'appliquent au premier tour.
     * 
     * @return true si c'est le premier tour, false sinon
     */
    public boolean estPremierTour() {
        return !premierTourJoue;
    }
    
    /**
     * Retourne le dictionnaire utilisé pour la validation des mots.
     * 
     * @return Le dictionnaire
     */
    public Dictionnaire getDictionnaire() {
        return dictionnaire;
    }
    
    /**
     * Retourne le nombre de tours joués depuis le début de la partie.
     * 
     * @return Le nombre de tours
     */
    public int getNombreTours() {
        return nombreTours;
    }
    
    /**
     * Incrémente le compteur de tours.
     */
    public void incrementerNombreTours() {
        this.nombreTours++;
    }
}