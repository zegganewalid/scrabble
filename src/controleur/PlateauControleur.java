package controleur;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;         // Pour exécuter du code sur le thread JavaFX
import javafx.scene.control.Alert;          // Pour afficher des boîtes de dialogue d'alerte
import javafx.scene.control.ButtonType;     // Types de boutons pour les boîtes de dialogue
import javafx.scene.control.ChoiceDialog;   // Boîte de dialogue avec choix
import javafx.scene.control.TextInputDialog; // Boîte de dialogue de saisie de texte
import vue.DirectionDialog;                 // Dialogue personnalisé pour choisir la direction
import vue.PlateauVue;                      // Vue du plateau de jeu
import modele.Case;                         // Représente une case du plateau
import modele.Direction;                    // Énumération des directions (HORIZONTAL/VERTICAL)
import modele.Lettre;                       // Représente une lettre avec sa valeur
import modele.Plateau;                      // Modèle du plateau de jeu
import utils.ObservateurPartie;             // Interface pour observer les changements de la partie
import modele.Dictionnaire;                 // Pour vérifier la validité des mots

/**
 * Contrôleur responsable de la gestion du plateau de jeu.
 * S'occupe de la logique de placement des mots, de la validation, et des interactions
 * avec le plateau.
 */
public class PlateauControleur implements ObservateurPartie {
    
    private ClientControleur clientControleur;  // Référence au contrôleur principal
    private LettresControleur lettresControleur; // Contrôleur des lettres du joueur
    private PlateauVue plateauVue;              // Vue du plateau
    private Plateau etatPlateau;                // État local du plateau
    private boolean premierTour;                // Indique si c'est le premier tour (règles spéciales)
    private Dictionnaire dictionnaire = new Dictionnaire(); // Pour vérifier les mots
    
    /**
     * Constructeur du contrôleur de plateau.
     * 
     * @param clientControleur Le contrôleur client principal
     * @param lettresControleur Le contrôleur des lettres du joueur
     */
    public PlateauControleur(ClientControleur clientControleur, LettresControleur lettresControleur) {
        this.clientControleur = clientControleur;
        this.lettresControleur = lettresControleur;
        this.etatPlateau = new Plateau();      // Initialise un plateau vide
        this.premierTour = true;               // Au départ, c'est le premier tour
    }
    
    /**
     * Définit la référence vers la vue du plateau.
     * 
     * @param plateauVue La vue du plateau à utiliser
     */
    public void setPlateauVue(PlateauVue plateauVue) {
        this.plateauVue = plateauVue;
    }

    /**
     * Place un mot sur le plateau.
     * 
     * @param lettresAJouer La liste des lettres à placer
     * @param x La coordonnée X de départ
     * @param y La coordonnée Y de départ
     * @param direction La direction du mot (HORIZONTAL ou VERTICAL)
     * @return true si le placement a réussi, false sinon
     */
    public boolean placerMot(List<Lettre> lettresAJouer, int x, int y, Direction direction) {
        // Vérifier que c'est au tour du joueur
        if (!clientControleur.estMonTour()) {
            afficherErreur("Ce n'est pas votre tour !");
            return false;
        }
        
        // Vérifier que le placement est valide
        if (!verifierPlacementValide(lettresAJouer, x, y, direction)) {
            return false;
        }
        
        // Construire le mot à partir des lettres
        StringBuilder mot = new StringBuilder();
        for (Lettre lettre : lettresAJouer) {
            mot.append(lettre.getCaractere());
        }
        
        // Envoyer la commande au serveur via le clientControleur
        System.out.println("PlateauControleur: placerMot -> mot=" + mot.toString() + ", x=" + x + ", y=" + y + ", direction=" + direction);
        clientControleur.placerMot(mot.toString(), x, y, direction == Direction.HORIZONTAL);
        
        // Réinitialiser la sélection des lettres
        lettresControleur.reinitialiserSelection();
        
        return true;
    }
    
    /**
     * Vérifie si toutes les lettres placées sont alignées horizontalement ou verticalement.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @return true si les lettres sont alignées, false sinon
     */
    private boolean sontLettresAlignees(List<Point> lettresPlacees) {
        if (lettresPlacees.size() <= 1) {
            return true; // Une seule lettre est toujours "alignée"
        }
        
        // Vérifier si toutes les lettres ont la même coordonnée x (alignement vertical)
        boolean memeX = true;
        int x0 = lettresPlacees.get(0).x;
        
        // Vérifier si toutes les lettres ont la même coordonnée y (alignement horizontal)
        boolean memeY = true;
        int y0 = lettresPlacees.get(0).y;
        
        for (Point p : lettresPlacees) {
            if (p.x != x0) memeX = false;
            if (p.y != y0) memeY = false;
        }
        
        return memeX || memeY; // Si l'un des deux est vrai, les lettres sont alignées
    }
    
    /**
     * Vérifie si les lettres placées sont connectées à des mots existants.
     * Cette méthode est simplifiée pour toujours retourner true.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @return true (toujours, pour permettre le placement libre)
     */
    private boolean sontLettresConnectees(List<Point> lettresPlacees) {
        // Cette méthode est modifiée pour toujours retourner true
        // pour permettre le placement libre des mots
        return true;
    }
    
    /**
     * Vérifie si une case contient une lettre qui n'a pas été placée dans ce tour.
     * 
     * @param x Coordonnée X de la case
     * @param y Coordonnée Y de la case
     * @return true si la case contient une lettre d'un tour précédent, false sinon
     */
    private boolean estCaseOccupee(int x, int y) {
        // Vérifier que les coordonnées sont dans les limites du plateau
        if (x < 0 || x >= 15 || y < 0 || y >= 15) {
            return false; // Hors du plateau
        }
        
        // Vérifier si la case contient une lettre
        String lettre = plateauVue.getLettre(x, y);
        if (lettre.isEmpty()) {
            return false; // Case vide
        }
        
        // Vérifier si cette case fait partie des lettres nouvellement placées
        for (Point p : plateauVue.getLettresPlacees()) {
            if (p.x == x && p.y == y) {
                return false; // C'est une lettre placée dans ce tour
            }
        }
        
        return true; // C'est une lettre déjà présente d'un tour précédent
    }
    
    /**
     * Détermine la direction du placement (horizontal ou vertical) en fonction
     * des lettres placées.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @return La direction déterminée (HORIZONTAL ou VERTICAL)
     */
    private Direction determinerDirectionPlacement(List<Point> lettresPlacees) {
        // Si une direction forcée est définie, l'utiliser
        if (plateauVue.getDirectionForcee() != null) {
            return plateauVue.getDirectionForcee();
        }
        
        if (lettresPlacees.size() <= 1) {
            // Par défaut, horizontal avec une seule lettre
            return Direction.HORIZONTAL;
        }
        
        // Si toutes les lettres ont le même x, c'est vertical
        boolean memeX = true;
        int x0 = lettresPlacees.get(0).x;
        
        for (Point p : lettresPlacees) {
            if (p.x != x0) {
                memeX = false;
                break;
            }
        }
        
        return memeX ? Direction.VERTICAL : Direction.HORIZONTAL;
    }
    
    /**
     * Trouve le point de départ du mot (le point le plus en haut ou à gauche).
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return Le point de départ du mot
     */
    private Point trouverDebutMot(List<Point> lettresPlacees, Direction direction) {
        if (lettresPlacees.isEmpty()) {
            return new Point(0, 0);
        }
        
        Point debut = new Point(lettresPlacees.get(0));
        
        for (Point p : lettresPlacees) {
            if (direction == Direction.HORIZONTAL) {
                if (p.x < debut.x) {
                    debut = new Point(p);
                }
            } else {
                if (p.y < debut.y) {
                    debut = new Point(p);
                }
            }
        }
        
        return debut;
    }
    
    /**
     * Construit le mot à partir des lettres placées sur le plateau.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return Le mot formé par les lettres placées
     */
    private String construireMot(List<Point> lettresPlacees, Direction direction) {
        if (lettresPlacees.isEmpty()) {
            return "";
        }
        
        // Trier les points selon la direction
        List<Point> pointsTries = new ArrayList<>(lettresPlacees);
        Collections.sort(pointsTries, (p1, p2) -> {
            if (direction == Direction.HORIZONTAL) {
                return Integer.compare(p1.x, p2.x);
            } else {
                return Integer.compare(p1.y, p2.y);
            }
        });
        
        // Construire le mot en concaténant les lettres dans l'ordre
        StringBuilder mot = new StringBuilder();
        for (Point p : pointsTries) {
            mot.append(plateauVue.getLettre(p.x, p.y));
        }
        
        return mot.toString();
    }
    
    /**
     * Vérifie si le placement d'un mot est valide selon les règles du jeu.
     * 
     * @param lettres Liste des lettres à placer
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return true si le placement est valide, false sinon
     */
    private boolean verifierPlacementValide(List<Lettre> lettres, int x, int y, Direction direction) {
        // Vérifier que les coordonnées sont valides
        if (x < 0 || x >= 15 || y < 0 || y >= 15) {
            afficherErreur("Coordonnées hors limites du plateau");
            return false;
        }
        
        // Vérifier que le mot ne dépasse pas du plateau
        int longueurMot = lettres.size();
        if (direction == Direction.HORIZONTAL) {
            if (x + longueurMot > 15) {
                afficherErreur("Le mot dépasse du plateau horizontalement");
                return false;
            }
        } else {
            if (y + longueurMot > 15) {
                afficherErreur("Le mot dépasse du plateau verticalement");
                return false;
            }
        }
        
        // Au premier tour, vérifier que le mot passe par le centre (7,7)
        if (premierTour) {
            boolean passeParleCentre = false;
            for (int i = 0; i < longueurMot; i++) {
                int posX = direction == Direction.HORIZONTAL ? x + i : x;
                int posY = direction == Direction.HORIZONTAL ? y : y + i;
                
                if (posX == 7 && posY == 7) {
                    passeParleCentre = true;
                    break;
                }
            }
            
            if (!passeParleCentre) {
                afficherErreur("Au premier tour, le mot doit passer par le centre du plateau");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Demande la direction pour le placement d'un mot.
     * 
     * @return Direction choisie (HORIZONTAL ou VERTICAL)
     */
    public Direction demanderDirection() {
        // Utiliser notre dialogue de direction personnalisé
        DirectionDialog dialog = new DirectionDialog();
        Direction directionChoisie = dialog.afficherEtAttendre();
        
        // Afficher clairement la direction choisie pour faciliter le débogage
        if (directionChoisie != null) {
            System.out.println("Direction choisie dans demanderDirection: " + 
                              (directionChoisie == Direction.HORIZONTAL ? "HORIZONTAL" : "VERTICAL"));
        } else {
            System.out.println("Aucune direction choisie (annulation)");
            // Par défaut, on retourne HORIZONTAL si aucune direction n'est choisie
            directionChoisie = Direction.HORIZONTAL;
        }
        
        return directionChoisie;
    }
    
    /**
     * Valide le mot actuellement placé sur le plateau.
     * Cette méthode a été complètement réécrite pour corriger les problèmes de direction
     * et de prise en compte des lettres existantes sur le plateau.
     * 
     * @return true si la validation a réussi, false sinon
     */
    public boolean validerMotPlace() {
        System.out.println("=== DÉBUT VALIDATION DU MOT ===");
        
        // Récupérer les lettres placées
        List<Point> lettresPlacees = plateauVue.getLettresPlacees();
        System.out.println("Nombre de lettres placées: " + lettresPlacees.size());
        
        // Vérifications de base
        if (lettresPlacees.isEmpty()) {
            afficherErreur("Aucune lettre n'a été placée !");
            return false;
        }
        
        if (!clientControleur.estMonTour()) {
            afficherErreur("Ce n'est pas votre tour !");
            return false;
        }
        
        // VÉRIFICATION: Le mot doit être connecté à un mot existant (sauf au premier tour)
        if (!premierTour && !verifierConnexionMotExistant(lettresPlacees)) {
            afficherErreur("Le mot doit être connecté à au moins une lettre existante sur le plateau !");
            return false;
        }
        
        // Déterminer automatiquement la direction si possible
        Direction directionAutomatique = determinerDirectionAutomatique(lettresPlacees);
        
        // Demander la direction à l'utilisateur
        Direction direction = demanderDirection();
        if (direction == null) {
            // L'utilisateur a annulé le choix
            return false;
        }
        
        System.out.println("Direction choisie: " + direction);
        
        // Forcer cette direction dans la vue
        plateauVue.setDirectionForcee(direction);
        
        // Trier les lettres placées selon la direction
        List<Point> lettresTriees = new ArrayList<>(lettresPlacees);
        if (direction == Direction.HORIZONTAL) {
            Collections.sort(lettresTriees, (p1, p2) -> Integer.compare(p1.x, p2.x));
        } else { // VERTICAL
            Collections.sort(lettresTriees, (p1, p2) -> Integer.compare(p1.y, p2.y));
        }
        
        // Vérifier que les lettres sont alignées correctement selon la direction choisie
        if (!verifierAlignement(lettresTriees, direction)) {
            afficherErreur("Les lettres ne sont pas correctement alignées pour la direction " + 
                          (direction == Direction.HORIZONTAL ? "horizontale" : "verticale") + " !");
            return false;
        }
        
        // NOUVEAU: Construire le mot complet en tenant compte des lettres existantes
        StringBuilder motComplet = new StringBuilder();
        Point premierPoint = lettresTriees.get(0);
        
        if (direction == Direction.HORIZONTAL) {
            // Trouver le début réel du mot (à gauche)
            int startX = premierPoint.x;
            int y = premierPoint.y;
            while (startX > 0 && !plateauVue.getLettre(startX - 1, y).isEmpty()) {
                startX--;
            }
            
            // Trouver la fin réelle du mot (à droite)
            int endX = lettresTriees.get(lettresTriees.size() - 1).x;
            while (endX < 14 && !plateauVue.getLettre(endX + 1, y).isEmpty()) {
                endX++;
            }
            
            System.out.println("Mot horizontal de (" + startX + "," + y + ") à (" + endX + "," + y + ")");
            
            // Construire le mot complet
            for (int x = startX; x <= endX; x++) {
                String lettre = plateauVue.getLettre(x, y);
                if (!lettre.isEmpty()) {
                    motComplet.append(lettre);
                } else {
                    // Chercher une lettre placée à cette position
                    boolean trouve = false;
                    for (Point p : lettresTriees) {
                        if (p.x == x && p.y == y) {
                            lettre = plateauVue.getLettre(p.x, p.y);
                            if (!lettre.isEmpty()) {
                                motComplet.append(lettre);
                                trouve = true;
                                break;
                            }
                        }
                    }
                    
                    if (!trouve) {
                        // Trou dans le mot - invalide
                        afficherErreur("Le mot contient un espace vide - position (" + x + "," + y + ")");
                        return false;
                    }
                }
            }
        } else { // VERTICAL
            // Trouver le début réel du mot (en haut)
            int x = premierPoint.x;
            int startY = premierPoint.y;
            while (startY > 0 && !plateauVue.getLettre(x, startY - 1).isEmpty()) {
                startY--;
            }
            
            // Trouver la fin réelle du mot (en bas)
            int endY = lettresTriees.get(lettresTriees.size() - 1).y;
            while (endY < 14 && !plateauVue.getLettre(x, endY + 1).isEmpty()) {
                endY++;
            }
            
            System.out.println("Mot vertical de (" + x + "," + startY + ") à (" + x + "," + endY + ")");
            
            // Construire le mot complet
            for (int y = startY; y <= endY; y++) {
                String lettre = plateauVue.getLettre(x, y);
                if (!lettre.isEmpty()) {
                    motComplet.append(lettre);
                } else {
                    // Chercher une lettre placée à cette position
                    boolean trouve = false;
                    for (Point p : lettresTriees) {
                        if (p.x == x && p.y == y) {
                            lettre = plateauVue.getLettre(p.x, p.y);
                            if (!lettre.isEmpty()) {
                                motComplet.append(lettre);
                                trouve = true;
                                break;
                            }
                        }
                    }
                    
                    if (!trouve) {
                        // Trou dans le mot - invalide
                        afficherErreur("Le mot contient un espace vide - position (" + x + "," + y + ")");
                        return false;
                    }
                }
            }
        }
        
        String motAEnvoyerStr = motComplet.toString();
        System.out.println("Mot complet construit: " + motAEnvoyerStr);
        
        // Vérifier si le mot existe dans le dictionnaire
        if (!dictionnaire.chercherMotDansFichier(motAEnvoyerStr.toLowerCase())) {
            // Le mot n'existe pas dans le dictionnaire
            afficherErreur("Le mot '" + motAEnvoyerStr + "' n'existe pas dans le dictionnaire !");
            return false;
        }
        
        // Extraire uniquement les lettres nouvellement placées pour les envoyer au serveur
        StringBuilder lettresAEnvoyer = new StringBuilder();
        for (Point p : lettresTriees) {
            lettresAEnvoyer.append(plateauVue.getLettre(p.x, p.y));
        }
        
        String lettresAEnvoyerStr = lettresAEnvoyer.toString();
        System.out.println("Lettres placées à envoyer: " + lettresAEnvoyerStr);
        
        // Trouver le point de départ pour l'envoi au serveur
        Point debutMot = lettresTriees.get(0);
        System.out.println("Point de départ pour l'envoi: (" + debutMot.x + "," + debutMot.y + ")");
        
        // Envoyer la commande au serveur
        boolean resultat = clientControleur.placerMot(
            lettresAEnvoyerStr, 
            debutMot.x, 
            debutMot.y, 
            direction == Direction.HORIZONTAL
        );
        
        System.out.println("Résultat de l'envoi au serveur: " + resultat);
        
        // Une fois que le mot a été placé, ce n'est plus le premier tour
        premierTour = false;
        
        // Réinitialiser les lettres placées
        if (resultat) {
            plateauVue.reinitialiserLettresPlacees();
        }
        
        return resultat;
    }

    /**
     * Vérifie si les lettres sont bien alignées selon la direction choisie.
     * 
     * @param lettresTriees Liste des positions des lettres placées, triées
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return true si les lettres sont alignées, false sinon
     */
    private boolean verifierAlignement(List<Point> lettresTriees, Direction direction) {
        if (lettresTriees.size() <= 1) {
            return true; // Une seule lettre est toujours alignée
        }
        
        if (direction == Direction.HORIZONTAL) {
            // Toutes les lettres doivent avoir le même y
            int yRef = lettresTriees.get(0).y;
            for (Point p : lettresTriees) {
                if (p.y != yRef) {
                    return false;
                }
            }
            
            // Vérifier que les lettres sont contiguës horizontalement
            for (int i = 1; i < lettresTriees.size(); i++) {
                // Si ce n'est pas la position suivante, vérifier si la case intermédiaire est occupée
                if (lettresTriees.get(i).x > lettresTriees.get(i-1).x + 1) {
                    for (int x = lettresTriees.get(i-1).x + 1; x < lettresTriees.get(i).x; x++) {
                        // Si une case entre les deux n'est pas occupée, c'est un problème
                        if (plateauVue.getLettre(x, yRef).isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        } else { // VERTICAL
            // Toutes les lettres doivent avoir le même x
            int xRef = lettresTriees.get(0).x;
            for (Point p : lettresTriees) {
                if (p.x != xRef) {
                    return false;
                }
            }
            
            // Vérifier que les lettres sont contiguës verticalement
            for (int i = 1; i < lettresTriees.size(); i++) {
                // Si ce n'est pas la position suivante, vérifier si la case intermédiaire est occupée
                if (lettresTriees.get(i).y > lettresTriees.get(i-1).y + 1) {
                    for (int y = lettresTriees.get(i-1).y + 1; y < lettresTriees.get(i).y; y++) {
                        // Si une case entre les deux n'est pas occupée, c'est un problème
                        if (plateauVue.getLettre(xRef, y).isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * Vérifie si les lettres placées sont connectées à au moins une lettre
     * existante sur le plateau (condition nécessaire sauf au premier tour).
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @return true si les lettres sont connectées, false sinon
     */
    private boolean verifierConnexionMotExistant(List<Point> lettresPlacees) {
        // Au premier tour, pas besoin de vérifier la connexion
        if (premierTour) {
            return true;
        }
        
        // Vérifier si au moins une des lettres placées est adjacente à une lettre existante
        for (Point p : lettresPlacees) {
            int x = p.x;
            int y = p.y;
            
            // Vérifier les quatre directions adjacentes
            // Gauche
            if (x > 0 && !plateauVue.getLettre(x - 1, y).isEmpty() && !contientPoint(lettresPlacees, x - 1, y)) {
                return true;
            }
            // Droite
            if (x < 14 && !plateauVue.getLettre(x + 1, y).isEmpty() && !contientPoint(lettresPlacees, x + 1, y)) {
                return true;
            }
            // Haut
            if (y > 0 && !plateauVue.getLettre(x, y - 1).isEmpty() && !contientPoint(lettresPlacees, x, y - 1)) {
                return true;
            }
            // Bas
            if (y < 14 && !plateauVue.getLettre(x, y + 1).isEmpty() && !contientPoint(lettresPlacees, x, y + 1)) {
                return true;
            }
        }
        
        // Aucune lettre connectée trouvée
        return false;
    }

    /**
     * Demande le mot à placer à l'utilisateur.
     * 
     * @param lettreInitiale Lettre initiale à proposer
     * @return Mot saisi par le joueur
     */
    public String demanderMot(String lettreInitiale) {
        TextInputDialog dialog = new TextInputDialog(lettreInitiale);
        dialog.setTitle("Placement de mot");
        dialog.setHeaderText("Entrez le mot à placer");
        dialog.setContentText("Mot:");
        
        return dialog.showAndWait().orElse("");
    }
    
    /**
     * Met à jour l'état du plateau local à partir des données reçues du serveur.
     * 
     * @param plateauData Données du plateau au format chaîne
     */
    public void mettreAJourPlateau(String plateauData) {
        // Réinitialiser l'état du plateau
        etatPlateau = new Plateau();
        
        // Si le plateau est vide, pas besoin de traitement
        if (plateauData == null || plateauData.isEmpty()) {
            Platform.runLater(() -> {
                if (plateauVue != null) {
                    plateauVue.effacerPlateau();
                }
            });
            return;
        }
        
        // Parser les données du plateau
        boolean plateauNonVide = false;
        String[] cases = plateauData.split(";");
        for (String c : cases) {
            if (!c.isEmpty()) {
                String[] coordonnees = c.split(",");
                if (coordonnees.length >= 3) {
                    plateauNonVide = true;
                    int x = Integer.parseInt(coordonnees[0]);
                    int y = Integer.parseInt(coordonnees[1]);
                    char lettre = coordonnees[2].charAt(0);
                    
                    // Mettre à jour notre modèle local
                    Lettre nouvelleLettre = new Lettre(lettre, 0); // Valeur pas importante ici
                    etatPlateau.getCase(x, y).setLettre(nouvelleLettre);
                    
                    // Mettre à jour la vue (sur le thread JavaFX)
                    final int finalX = x;
                    final int finalY = y;
                    final char finalLettre = lettre;
                    
                    Platform.runLater(() -> {
                        if (plateauVue != null) {
                            plateauVue.placerLettre(finalX, finalY, finalLettre);
                        }
                    });
                }
            }
        }
        
        // Si le plateau n'est plus vide, ce n'est plus le premier tour
        if (plateauNonVide) {
            premierTour = false;
        }
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur.
     * 
     * @param message Le message d'erreur à afficher
     */
    private void afficherErreur(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de placer le mot");
            alert.showAndWait();
        });
    }
    
    /**
     * Méthode de l'interface ObservateurPartie pour recevoir les notifications
     * des changements dans la partie.
     * 
     * @param evenement Type d'événement survenu
     * @param data Données associées à l'événement
     */
    @Override
    public void notifier(String evenement, Object data) {
        switch (evenement) {
            case PLATEAU_MODIFIE:
                if (data instanceof String) {
                    mettreAJourPlateau((String) data);
                }
                break;
                
            case PARTIE_COMMENCEE:
                // Réinitialiser l'état pour une nouvelle partie
                premierTour = true;
                etatPlateau = new Plateau();
                
                if (plateauVue != null) {
                    Platform.runLater(() -> plateauVue.effacerPlateau());
                }
                break;
                
            case ERREUR:
                if (data instanceof String) {
                    afficherErreur((String) data);
                }
                break;
        }
    }

    /**
     * Vérifie rapidement si un mot peut être placé (vérification provisoire).
     * Utilisé pour prévalider les mots avant de les placer définitivement.
     * 
     * @param lettres Liste des lettres à placer
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return true si le placement est possible, false sinon
     */
    public boolean verifierMotProvisoire(List<Lettre> lettres, int x, int y, Direction direction) {
        // Vérifier les contraintes de base comme les limites du plateau
        if (!verifierPlacementValide(lettres, x, y, direction)) {
            return false;
        }
        
        // Construire le mot
        StringBuilder mot = new StringBuilder();
        for (Lettre lettre : lettres) {
            mot.append(lettre.getCaractere());
        }
        
        // Vérifier le mot dans le dictionnaire
        return dictionnaire.chercherMotDansFichier(mot.toString());
    }

   /**
     * Vérifie tous les mots secondaires formés perpendiculairement au mot principal.
     * 
     * @param lettresPlacees Liste des positions des lettres nouvellement placées
     * @param directionPrincipale Direction principale du placement (HORIZONTAL ou VERTICAL)
     * @return true si tous les mots secondaires sont valides, false sinon
     */
    private boolean verifierMotsSecondaires(List<Point> lettresPlacees, Direction directionPrincipale) {
        // Pour chaque lettre placée
        for (Point p : lettresPlacees) {
            // Vérifie s'il y a des lettres adjacentes dans la direction perpendiculaire
            Direction directionSecondaire = (directionPrincipale == Direction.HORIZONTAL) 
                                          ? Direction.VERTICAL 
                                          : Direction.HORIZONTAL;
            
            // Chercher le début du mot secondaire
            int startX = p.x;
            int startY = p.y;
            
            if (directionSecondaire == Direction.VERTICAL) {
                // Chercher vers le haut
                while (startY > 0 && !plateauVue.getLettre(startX, startY - 1).isEmpty()) {
                    startY--;
                }
            } else {
                // Chercher vers la gauche
                while (startX > 0 && !plateauVue.getLettre(startX - 1, startY).isEmpty()) {
                    startX--;
                }
            }
            
            // Chercher la fin du mot secondaire
            int endX = p.x;
            int endY = p.y;
            
            if (directionSecondaire == Direction.VERTICAL) {
                // Chercher vers le bas
                while (endY < 14 && !plateauVue.getLettre(endX, endY + 1).isEmpty()) {
                    endY++;
                }
            } else {
                // Chercher vers la droite
                while (endX < 14 && !plateauVue.getLettre(endX + 1, endY).isEmpty()) {
                    endX++;
                }
            }
            
            // Si un mot secondaire est formé (plus d'une lettre)
            if ((directionSecondaire == Direction.VERTICAL && endY > startY) || 
                (directionSecondaire == Direction.HORIZONTAL && endX > startX)) {
                
                // Construire le mot secondaire
                StringBuilder motSecondaire = new StringBuilder();
                
                if (directionSecondaire == Direction.VERTICAL) {
                    for (int y = startY; y <= endY; y++) {
                        motSecondaire.append(plateauVue.getLettre(startX, y));
                    }
                } else {
                    for (int x = startX; x <= endX; x++) {
                        motSecondaire.append(plateauVue.getLettre(x, startY));
                    }
                }
                
                // Vérifier si le mot secondaire est valide (au moins 2 lettres)
                String motSecondaireStr = motSecondaire.toString();
                System.out.println("Mot secondaire détecté: " + motSecondaireStr + " à la position (" + startX + "," + startY + ")");
                
                if (motSecondaireStr.length() > 1) {
                    // Vérifier dans le dictionnaire
                    if (!dictionnaire.chercherMotDansFichier(motSecondaireStr)) {
                        afficherErreur("Le mot secondaire '" + motSecondaireStr + "' n'existe pas dans le dictionnaire !");
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * Trouve le point de départ du mot principal formé.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return Le point de départ (coin supérieur gauche) du mot
     */
    private Point trouverPointDepart(List<Point> lettresPlacees, Direction direction) {
        if (lettresPlacees.isEmpty()) {
            return new Point(0, 0);
        }
        
        // Trouver la lettre la plus à gauche (pour horizontal) ou la plus haute (pour vertical)
        Point debut = lettresPlacees.get(0);
        
        for (Point p : lettresPlacees) {
            if (direction == Direction.HORIZONTAL) {
                if (p.x < debut.x) {
                    debut = p;
                }
            } else {
                if (p.y < debut.y) {
                    debut = p;
                }
            }
        }
        
        return debut;
    }

    /**
     * Vérifie si la liste de points contient une position spécifique.
     * 
     * @param points Liste de points à vérifier
     * @param x Coordonnée X recherchée
     * @param y Coordonnée Y recherchée
     * @return true si la position est présente dans la liste, false sinon
     */
    private boolean contientPoint(List<Point> points, int x, int y) {
        for (Point p : points) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collecte le mot principal formé dans la direction spécifiée.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return Le mot complet formé
     */
    private String collecterMotDansDirection(List<Point> lettresPlacees, Direction direction) {
        if (lettresPlacees.isEmpty()) {
            return "";
        }
        
        // Trouver les extrémités du mot
        Point reference = lettresPlacees.get(0);
        int startX = reference.x;
        int startY = reference.y;
        int endX = reference.x;
        int endY = reference.y;
        
        // Pour la direction horizontale
        if (direction == Direction.HORIZONTAL) {
            // Rechercher la première lettre du mot (à gauche)
            while (startX > 0 && !plateauVue.getLettre(startX - 1, startY).isEmpty()) {
                startX--;
            }
            
            // Rechercher la dernière lettre du mot (à droite)
            while (endX < 14 && !plateauVue.getLettre(endX + 1, startY).isEmpty()) {
                endX++;
            }
            
            // Construire le mot complet
            StringBuilder mot = new StringBuilder();
            for (int x = startX; x <= endX; x++) {
                mot.append(plateauVue.getLettre(x, startY));
            }
            
            return mot.toString();
        } 
        // Pour la direction verticale
        else {
            // Rechercher la première lettre du mot (en haut)
            while (startY > 0 && !plateauVue.getLettre(startX, startY - 1).isEmpty()) {
                startY--;
            }
            
            // Rechercher la dernière lettre du mot (en bas)
            while (endY < 14 && !plateauVue.getLettre(startX, endY + 1).isEmpty()) {
                endY++;
            }
            
            // Construire le mot complet
            StringBuilder mot = new StringBuilder();
            for (int y = startY; y <= endY; y++) {
                mot.append(plateauVue.getLettre(startX, y));
            }
            
            return mot.toString();
        }
    }

    /**
     * Collecte un mot perpendiculaire à partir d'une position donnée.
     * 
     * @param position Position à partir de laquelle collecter le mot
     * @param direction Direction du mot à collecter (HORIZONTAL ou VERTICAL)
     * @return Le mot formé perpendiculairement
     */
    private String collecterMotPerpendiculaire(Point position, Direction direction) {
        int x = position.x;
        int y = position.y;
        
        // Trouver le début et la fin du mot
        int startX = x;
        int startY = y;
        int endX = x;
        int endY = y;
        
        // Pour la direction horizontale
        if (direction == Direction.HORIZONTAL) {
            // Rechercher la première lettre du mot (à gauche)
            while (startX > 0 && !plateauVue.getLettre(startX - 1, y).isEmpty()) {
                startX--;
            }
            
            // Rechercher la dernière lettre du mot (à droite)
            while (endX < 14 && !plateauVue.getLettre(endX + 1, y).isEmpty()) {
                endX++;
            }
            
            // Si le mot ne contient qu'une seule lettre, ce n'est pas un mot
            if (startX == endX) {
                return "";
            }
            
            // Construire le mot complet
            StringBuilder mot = new StringBuilder();
            for (int currentX = startX; currentX <= endX; currentX++) {
                mot.append(plateauVue.getLettre(currentX, y));
            }
            
            return mot.toString();
        } 
        // Pour la direction verticale
        else {
            // Rechercher la première lettre du mot (en haut)
            while (startY > 0 && !plateauVue.getLettre(x, startY - 1).isEmpty()) {
                startY--;
            }
            
            // Rechercher la dernière lettre du mot (en bas)
            while (endY < 14 && !plateauVue.getLettre(x, endY + 1).isEmpty()) {
                endY++;
            }
            
            // Si le mot ne contient qu'une seule lettre, ce n'est pas un mot
            if (startY == endY) {
                return "";
            }
            
            // Construire le mot complet
            StringBuilder mot = new StringBuilder();
            for (int currentY = startY; currentY <= endY; currentY++) {
                mot.append(plateauVue.getLettre(x, currentY));
            }
            
            return mot.toString();
        }
    }

    /**
     * Vérifie si les lettres placées sont connectées à des lettres existantes.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @return true si au moins une lettre est connectée, false sinon
     */
    private boolean estConnecteALettresExistantes(List<Point> lettresPlacees) {
        if (premierTour) {
            return true; // Au premier tour, pas besoin d'être connecté
        }
        
        for (Point p : lettresPlacees) {
            int x = p.x;
            int y = p.y;
            
            // Vérifier les 4 directions adjacentes
            if (x > 0 && !plateauVue.getLettre(x - 1, y).isEmpty() && !contientPoint(lettresPlacees, x - 1, y)) {
                return true; // Lettre existante à gauche
            }
            if (x < 14 && !plateauVue.getLettre(x + 1, y).isEmpty() && !contientPoint(lettresPlacees, x + 1, y)) {
                return true; // Lettre existante à droite
            }
            if (y > 0 && !plateauVue.getLettre(x, y - 1).isEmpty() && !contientPoint(lettresPlacees, x, y - 1)) {
                return true; // Lettre existante en haut
            }
            if (y < 14 && !plateauVue.getLettre(x, y + 1).isEmpty() && !contientPoint(lettresPlacees, x, y + 1)) {
                return true; // Lettre existante en bas
            }
        }
        
        return false;
    }

    /**
     * Trouve le point de départ du mot principal.
     * 
     * @param reference Point de référence dans le mot
     * @param direction Direction du mot (HORIZONTAL ou VERTICAL)
     * @return Le point de départ du mot
     */
    private Point trouverDebutMotPrincipal(Point reference, Direction direction) {
        int x = reference.x;
        int y = reference.y;
        
        if (direction == Direction.HORIZONTAL) {
            // Rechercher vers la gauche
            while (x > 0 && !plateauVue.getLettre(x - 1, y).isEmpty()) {
                x--;
            }
        } else {
            // Rechercher vers le haut
            while (y > 0 && !plateauVue.getLettre(x, y - 1).isEmpty()) {
                y--;
            }
        }
        
        return new Point(x, y);
    }

    /**
     * Détermine automatiquement la direction probable du mot.
     * 
     * @param lettresPlacees Liste des positions des lettres placées
     * @return La direction la plus probable (HORIZONTAL ou VERTICAL)
     */
    private Direction determinerDirectionAutomatique(List<Point> lettresPlacees) {
        // Si une seule lettre est placée, chercher des lettres adjacentes
        if (lettresPlacees.size() == 1) {
            Point p = lettresPlacees.get(0);
            boolean lettreAGauche = p.x > 0 && 
                !plateauVue.getLettre(p.x - 1, p.y).isEmpty();
            boolean lettreADroite = p.x < 14 && 
                !plateauVue.getLettre(p.x + 1, p.y).isEmpty();
            boolean lettreEnHaut = p.y > 0 && 
                !plateauVue.getLettre(p.x, p.y - 1).isEmpty();
            boolean lettreEnBas = p.y < 14 && 
                !plateauVue.getLettre(p.x, p.y + 1).isEmpty();
            
            // S'il y a des lettres horizontalement mais pas verticalement
            if ((lettreAGauche || lettreADroite) && !(lettreEnHaut || lettreEnBas)) {
                return Direction.HORIZONTAL;
            }
            // S'il y a des lettres verticalement mais pas horizontalement
            else if (!(lettreAGauche || lettreADroite) && (lettreEnHaut || lettreEnBas)) {
                return Direction.VERTICAL;
            }
        }
        
        // Si plusieurs lettres sont placées, déterminer leur alignement
        if (lettresPlacees.size() > 1) {
            // Vérifier si toutes les lettres ont le même x (alignement vertical)
            boolean memeX = true;
            int x0 = lettresPlacees.get(0).x;
            for (Point p : lettresPlacees) {
                if (p.x != x0) {
                    memeX = false;
                    break;
                }
            }
            if (memeX) return Direction.VERTICAL;
            
            // Vérifier si toutes les lettres ont le même y (alignement horizontal)
            boolean memeY = true;
            int y0 = lettresPlacees.get(0).y;
            for (Point p : lettresPlacees) {
                if (p.y != y0) {
                    memeY = false;
                    break;
                }
            }
            if (memeY) return Direction.HORIZONTAL;
        }
        
        // Par défaut, direction horizontale
        return Direction.HORIZONTAL;
    }
}