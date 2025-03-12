import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Partie partie;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        demarrerPartie();
        deroulementPartie();
        terminerPartie();
    }

    private static void demarrerPartie() {
        System.out.println("=== NOUVELLE PARTIE DE SCRABBLE ===");
        List<String> nomsJoueurs = demanderNomsJoueurs();
        partie = new Partie(nomsJoueurs);
    }

    private static List<String> demanderNomsJoueurs() {
        List<String> noms = new ArrayList<>();
        System.out.print("Nombre de joueurs (2-4): ");
        int nb = scanner.nextInt();
        scanner.nextLine(); // Consommer la ligne
        
        for (int i = 0; i < nb; i++) {
            System.out.print("Nom du joueur " + (i+1) + ": ");
            noms.add(scanner.nextLine());
        }
        return noms;
    }

    private static void deroulementPartie() {
        while (!partie.estPartieTerminee()) {
            Joueur joueur = partie.getJoueurActuel();
            System.out.println("\n=== TOUR DE " + joueur.getNom().toUpperCase() + " ===");
            afficherEtatJeu(joueur);
            
            if (joueur.getMotImpose() != null && !joueur.aPlaceMotImpose()) {
                System.out.println("[Mot imposé à placer: " + joueur.getMotImpose() + "]");
            }
            
            if (demanderAction()) {
                // Si placement réussi
                if (joueur.aPlaceMotImpose()) {
                    partie.assignerMotImpose(joueur);
                }
            }
            partie.passerAuJoueurSuivant();
        }
    }

    private static boolean demanderAction() {
        System.out.println("\nActions possibles:");
        System.out.println("1. Placer un mot");
        System.out.println("2. Passer son tour");
        System.out.print("Choix: ");
        
        int choix = scanner.nextInt();
        scanner.nextLine();
        
        if (choix == 1) {
            return traiterPlacementMot();
        }
        return false;
    }

    private static boolean traiterPlacementMot() {
        Joueur joueur = partie.getJoueurActuel();
        
        // Afficher les lettres disponibles
        System.out.println("Lettres en main: " + 
            joueur.getLettresEnMain().stream()
                .map(l -> String.valueOf(l.getCaractere()))
                .collect(Collectors.joining(" ")));

        // Saisie des paramètres
        System.out.print("Entrez les lettres à placer (sans espace): ");
        String mot = scanner.nextLine().toUpperCase();
        
        System.out.print("Coordonnée X de départ: ");
        int x = scanner.nextInt();
        
        System.out.print("Coordonnée Y de départ: ");
        int y = scanner.nextInt();
        
        System.out.print("Direction (H pour horizontal, V pour vertical): ");
        Direction direction = scanner.next().equalsIgnoreCase("H") ? 
            Direction.HORIZONTAL : Direction.VERTICAL;
        
        scanner.nextLine(); // Nettoyer le buffer

        // Valider les lettres
        List<Lettre> lettresAJouer = new ArrayList<>();
        for (char c : mot.toCharArray()) {
            Lettre lettre = trouverLettreDansMain(joueur, c);
            if (lettre == null) {
                System.out.println("Lettre " + c + " non disponible !");
                return false;
            }
            lettresAJouer.add(lettre);
        }

        // Tenter le placement
        if (partie.placerMot(lettresAJouer, x, y, direction)) {
            // Retirer les lettres utilisées
            lettresAJouer.forEach(joueur::retirerLettre);
            return true;
        }
        return false;
    }

    private static Lettre trouverLettreDansMain(Joueur joueur, char c) {
        return joueur.getLettresEnMain().stream()
            .filter(l -> l.getCaractere() == c)
            .findFirst()
            .orElse(null);
    }

    private static void afficherEtatJeu(Joueur joueur) {
        System.out.println("\n-- Plateau --");
        partie.getPlateau().afficherPlateau();
        
        System.out.println("\n-- Main actuelle --");
        joueur.getLettresEnMain().forEach(l -> 
            System.out.print(l.getCaractere() + "(" + l.getValeur() + ") "));
        
        System.out.println("\n\nScore actuel: " + joueur.getScore());
        System.out.println("Lettres restantes dans le sac: " + 
            partie.getSac().lettres.size());
    }

    private static void terminerPartie() {
        partie.terminerPartie();
        System.out.println("\n=== PARTIE TERMINÉE ===");
        
        partie.getJoueurs().forEach(j -> 
            System.out.println(j.getNom() + ": " + j.getScore() + " points"));
        
        Joueur vainqueur = partie.determinerVainqueur();
        System.out.println("\nVAINQUEUR: " + vainqueur.getNom() + " !");
    }
}