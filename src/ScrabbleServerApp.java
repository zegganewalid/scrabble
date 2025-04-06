import reseau.ScrabbleServer;

/**
 * Lanceur du serveur Scrabble
 */
public class ScrabbleServerApp {
    public static void main(String[] args) {
        int port = 5000;
        int nombreJoueurs = 2;
        
        // Vérifier les arguments de ligne de commande
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide, utilisation du port par défaut: 5000");
            }
        }
        
        if (args.length >= 2) {
            try {
                nombreJoueurs = Integer.parseInt(args[1]);
                if (nombreJoueurs < 2 || nombreJoueurs > 4) {
                    System.err.println("Le nombre de joueurs doit être entre 2 et 4. Utilisation de 2 joueurs.");
                    nombreJoueurs = 2;
                }
            } catch (NumberFormatException e) {
                System.err.println("Nombre de joueurs invalide, utilisation de 2 joueurs par défaut");
            }
        }
        
        System.out.println("Démarrage du serveur Scrabble sur le port " + port + " pour " + nombreJoueurs + " joueurs...");
        ScrabbleServer serveur = new ScrabbleServer(port, nombreJoueurs);
        serveur.demarrer();
    }
}