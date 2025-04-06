package reseau;

// Importation des bibliothèques nécessaires
import java.io.*;                         // Pour les opérations d'entrée/sortie
import java.net.*;                        // Pour les fonctionnalités réseau
import java.util.*;                       // Pour les collections standards
import java.util.concurrent.CopyOnWriteArrayList;  // Collection thread-safe pour les clients
import modele.Direction;                  // Énumération pour la direction des mots (H/V)
import modele.Joueur;                     // Classe représentant un joueur
import modele.Lettre;                     // Classe représentant une lettre
import modele.Partie;                     // Classe gérant la logique de jeu

/**
 * Classe principale du serveur Scrabble.
 * Elle gère la connexion des clients, l'initialisation de la partie et la communication réseau.
 */
public class ScrabbleServer {
    private ServerSocket serverSocket;            // Socket d'écoute du serveur
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();  // Liste thread-safe des clients connectés
    private Partie partie;                        // Instance de la partie en cours
    private int nombreJoueurs;                    // Nombre de joueurs attendus pour démarrer
    private int joueursCourants = 0;              // Nombre de joueurs actuellement connectés
    private boolean partieEnCours = false;        // État de la partie (démarrée ou non)
    
    /**
     * Constructeur du serveur Scrabble.
     * 
     * @param port Le port sur lequel le serveur écoutera
     * @param nombreJoueurs Le nombre de joueurs nécessaires pour démarrer la partie
     */
    public ScrabbleServer(int port, int nombreJoueurs) {
        this.nombreJoueurs = nombreJoueurs;
        try {
            // Création du socket serveur sur le port spécifié
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur démarré sur le port " + port);
            System.out.println("En attente de " + nombreJoueurs + " joueurs...");
        } catch (IOException e) {
            // En cas d'erreur lors de la création du socket (port déjà utilisé, etc.)
            System.err.println("Erreur lors de la création du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Démarre le serveur et attend les connexions des joueurs.
     * Une fois tous les joueurs connectés, initialise et démarre la partie.
     */
    public void demarrer() {
        try {
            // Boucle d'attente des connexions clients
            while (joueursCourants < nombreJoueurs) {
                // Attente bloquante d'une nouvelle connexion
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté: " + clientSocket);
                
                // Création d'un gestionnaire pour ce client
                ClientHandler clientHandler = new ClientHandler(clientSocket, joueursCourants);
                clients.add(clientHandler);
                
                // Démarrage d'un thread dédié pour ce client
                Thread thread = new Thread(clientHandler);
                thread.start();
                
                joueursCourants++;
            }
            
            // Tous les joueurs sont connectés, on peut démarrer la partie
            // Collecte des noms des joueurs
            List<String> nomsJoueurs = new ArrayList<>();
            for (ClientHandler client : clients) {
                nomsJoueurs.add(client.getNomJoueur());
            }
            
            // Initialisation du modèle de jeu
            partie = new Partie(nomsJoueurs);
            partieEnCours = true;
            
            // Informe tous les clients que la partie commence
            diffuserMessage("PARTIE_COMMENCE");
            
            // Envoie l'état initial du jeu à tous les joueurs
            envoyerEtatPartie();
            
            // Envoie l'état initial du sac de lettres à tous les joueurs
            diffuserMessage("SAC|" + genererEtatSac());
            
        } catch (IOException e) {
            System.err.println("Erreur dans le serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Envoie un message à tous les clients connectés.
     * 
     * @param message Le message à diffuser
     */
    public void diffuserMessage(String message) {
        for (ClientHandler client : clients) {
            client.envoyerMessage(message);
        }
    }
    
    /**
     * Envoie l'état actuel de la partie à tous les clients.
     * Chaque client reçoit les informations spécifiques à son joueur.
     */
    public void envoyerEtatPartie() {
        if (partie == null) return;
        
        // Pour chaque client connecté...
        for (ClientHandler client : clients) {
            int indexJoueur = client.getIndexJoueur();
            Joueur joueur = partie.getJoueurs().get(indexJoueur);
            
            // Envoie l'état du plateau
            client.envoyerMessage("PLATEAU|" + convertirPlateauEnChaine());
            
            // Envoie les lettres du joueur
            client.envoyerMessage("LETTRES|" + convertirLettresEnChaine(joueur.getLettresEnMain()));
            
            // Envoie le mot imposé si applicable (variante de jeu)
            if (joueur.getMotImpose() != null) {
                client.envoyerMessage("MOT_IMPOSE|" + joueur.getMotImpose());
            }
            
            // Construit et envoie les scores de tous les joueurs
            StringBuilder scores = new StringBuilder("SCORES|");
            for (Joueur j : partie.getJoueurs()) {
                scores.append(j.getNom()).append(",").append(j.getScore()).append(";");
            }
            client.envoyerMessage(scores.toString());
            
            // Indique quel joueur doit jouer actuellement
            client.envoyerMessage("JOUEUR_ACTUEL|" + partie.getJoueurActuel().getNom());
        }
    }
    
    /**
     * Génère une représentation textuelle du contenu actuel du sac de lettres.
     * Format: "lettre1,valeur1;lettre2,valeur2;..."
     * 
     * @return Une chaîne représentant l'état du sac
     */
    private String genererEtatSac() {
        StringBuilder sb = new StringBuilder();
        List<Lettre> lettresSac = partie.getSac().lettres; // Accès direct au champ public
        
        for (int i = 0; i < lettresSac.size(); i++) {
            Lettre lettre = lettresSac.get(i);
            sb.append(lettre.getCaractere()).append(",").append(lettre.getValeur());
            
            // Ajoute un séparateur sauf pour la dernière lettre
            if (i < lettresSac.size() - 1) {
                sb.append(";");
            }
        }
        
        return sb.toString();
    }

    /**
     * Convertit l'état actuel du plateau en chaîne de caractères.
     * Format: "x1,y1,lettre1;x2,y2,lettre2;..."
     * 
     * @return Une chaîne représentant l'état du plateau
     */
    private String convertirPlateauEnChaine() {
        StringBuilder sb = new StringBuilder();
        // Parcours de toutes les cases du plateau (15x15)
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                // Si la case contient une lettre, l'ajouter à la chaîne
                if (partie.getPlateau().getCase(i, j).isEstOccupe()) {
                    sb.append(i).append(",")
                      .append(j).append(",")
                      .append(partie.getPlateau().getCase(i, j).getLettre().getCaractere())
                      .append(";");
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * Convertit une liste de lettres en chaîne de caractères.
     * Format: "lettre1,valeur1;lettre2,valeur2;..."
     * 
     * @param lettres La liste de lettres à convertir
     * @return Une chaîne représentant les lettres
     */
    private String convertirLettresEnChaine(List<Lettre> lettres) {
        StringBuilder sb = new StringBuilder();
        for (Lettre l : lettres) {
            sb.append(l.getCaractere()).append(",").append(l.getValeur()).append(";");
        }
        return sb.toString();
    }
    
    /**
     * Arrête proprement le serveur et ferme toutes les connexions.
     */
    public void arreter() {
        try {
            // Fermeture du socket serveur
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Fermeture de toutes les connexions client
            for (ClientHandler client : clients) {
                client.fermer();
            }
            
            System.out.println("Serveur arrêté");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }
    
    /**
     * Méthode principale pour démarrer le serveur.
     * Accepte des arguments optionnels pour le port et le nombre de joueurs.
     * 
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        int port = 5000;           // Port par défaut
        int nombreJoueurs = 2;     // Nombre de joueurs par défaut
        
        // Traitement du premier argument (port)
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide, utilisation du port par défaut: 5000");
            }
        }
        
        // Traitement du deuxième argument (nombre de joueurs)
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
        
        // Création et démarrage du serveur
        ScrabbleServer serveur = new ScrabbleServer(port, nombreJoueurs);
        serveur.demarrer();
    }
    
    /**
     * Classe interne qui gère la communication avec un client spécifique.
     * Chaque instance s'exécute dans son propre thread.
     */
    class ClientHandler implements Runnable {
        private Socket clientSocket;      // Socket de communication avec ce client
        private PrintWriter out;          // Flux de sortie vers le client
        private BufferedReader in;        // Flux d'entrée depuis le client
        private int indexJoueur;          // Index de ce joueur dans la partie
        private String nomJoueur;         // Nom choisi par ce joueur
        
        /**
         * Constructeur du gestionnaire de client.
         * 
         * @param socket Socket de connexion au client
         * @param indexJoueur Index de ce joueur dans la partie
         */
        public ClientHandler(Socket socket, int indexJoueur) {
            this.clientSocket = socket;
            this.indexJoueur = indexJoueur;
            
            try {
                // Initialise les flux d'entrée/sortie
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                // Demande le nom du joueur au client
                envoyerMessage("DEMANDE_NOM");
                nomJoueur = in.readLine();
                System.out.println("Joueur " + indexJoueur + " s'est identifié comme: " + nomJoueur);
                
            } catch (IOException e) {
                System.err.println("Erreur avec le client " + indexJoueur + ": " + e.getMessage());
            }
        }
        
        /**
         * Méthode principale du thread, écoute les messages du client
         * et les traite tant que la connexion est active.
         */
        @Override
        public void run() {
            try {
                String messageClient;
                // Boucle de lecture des messages
                while ((messageClient = in.readLine()) != null) {
                    traiterMessage(messageClient);
                }
            } catch (IOException e) {
                System.err.println("Erreur de communication avec le client " + indexJoueur + ": " + e.getMessage());
            } finally {
                // Fermeture propre des ressources
                fermer();
            }
        }
        
        /**
         * Traite un message reçu du client selon son type.
         * 
         * @param message Le message à traiter
         */
        private void traiterMessage(String message) {
            System.out.println("Message reçu de " + nomJoueur + ": " + message);
            
            // Découpage du message en commande et paramètres
            String[] parts = message.split("\\|");
            String commande = parts[0];
            
            // Traitement selon le type de commande
            switch (commande) {
                case "ETAT_SAC":
                    // Le client demande l'état actuel du sac de lettres
                    if (partie != null) {
                        envoyerMessage("SAC|" + genererEtatSac());
                    }
                    break;
                    
                case "PASSER_TOUR":
                    // Le client veut passer son tour
                    // Vérifie d'abord que c'est bien son tour
                    if (partie.getJoueurActuel() == partie.getJoueurs().get(indexJoueur)) {
                        // Complète la main du joueur avec de nouvelles lettres
                        partie.getJoueurs().get(indexJoueur).completerMain(partie.getSac());
                        
                        // Passe au joueur suivant
                        partie.passerAuJoueurSuivant();
                        
                        // Informe tous les clients du nouveau numéro de tour
                        diffuserMessage("TOUR|" + partie.getNombreTours());
                        
                        // Envoie l'état mis à jour du jeu à tous les clients
                        envoyerEtatPartie();
                    } else {
                        // Erreur si ce n'est pas le tour de ce joueur
                        envoyerMessage("ERREUR|Ce n'est pas votre tour");
                    }
                    break;

                case "TERMINER_PARTIE":
                    // Le client demande d'arrêter la partie prématurément
                    if (partie != null && !partie.estPartieTerminee()) {
                        partie.terminerPartie();
                
                        // Détermine le vainqueur selon les règles du jeu
                        Joueur vainqueur = partie.determinerVainqueur();
                
                        // Informe tous les clients de la fin de partie et du vainqueur
                        diffuserMessage("FIN_PARTIE_PREMATUREMENT|" + vainqueur.getNom());
                    }
                    break;

                case "PLACER_MOT":
                    // Le client veut placer un mot sur le plateau
                    // Vérifie d'abord que c'est bien son tour
                    if (partie.getJoueurActuel() == partie.getJoueurs().get(indexJoueur)) {
                        // Extraction des paramètres du message
                        String mot = parts[1];
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        Direction direction = parts[4].equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;
                        
                        System.out.println("PLACER_MOT reçu: mot=" + mot + ", x=" + x + ", y=" + y + ", direction=" + direction);
                        
                        // Vérifie que le joueur possède toutes les lettres nécessaires
                        List<Lettre> lettresAJouer = new ArrayList<>();
                        boolean toutesLettresDisponibles = true;
                        
                        for (char c : mot.toCharArray()) {
                            Lettre lettre = trouverLettreDansMain(partie.getJoueurs().get(indexJoueur), c);
                            if (lettre == null) {
                                // Si une lettre n'est pas disponible, envoie une erreur
                                envoyerMessage("ERREUR|Lettre " + c + " non disponible !");
                                toutesLettresDisponibles = false;
                                break;
                            }
                            lettresAJouer.add(lettre);
                        }
                        
                        if (!toutesLettresDisponibles) {
                            return;
                        }
                        
                        // Essaie de placer le mot selon les règles du jeu
                        boolean placementReussi = partie.placerMot(lettresAJouer, x, y, direction);
                        
                        if (placementReussi) {
                            // Retire les lettres utilisées de la main du joueur
                            for (Lettre l : lettresAJouer) {
                                partie.getJoueurs().get(indexJoueur).retirerLettre(l);
                            }
                            
                            // Complète la main du joueur avec de nouvelles lettres
                            partie.getJoueurs().get(indexJoueur).completerMain(partie.getSac());
                            
                            // Passe au joueur suivant
                            partie.passerAuJoueurSuivant();
                            
                            // Envoie l'état mis à jour du jeu à tous les clients
                            envoyerEtatPartie();
                        } else {
                            // Erreur si le placement est impossible selon les règles
                            envoyerMessage("ERREUR|Placement du mot impossible");
                        }
                    } else {
                        // Erreur si ce n'est pas le tour de ce joueur
                        envoyerMessage("ERREUR|Ce n'est pas votre tour");
                    }
                    break;

                default:
                    // Commande non reconnue
                    System.out.println("Commande non reconnue: " + commande);
            }
        }
        
        /**
         * Cherche une lettre spécifique dans la main d'un joueur.
         * 
         * @param joueur Le joueur à vérifier
         * @param c Le caractère recherché
         * @return La lettre trouvée ou null si non trouvée
         */
        private Lettre trouverLettreDansMain(Joueur joueur, char c) {
            return joueur.getLettresEnMain().stream()
                .filter(l -> l.getCaractere() == c)
                .findFirst()
                .orElse(null);
        }
        
        /**
         * Envoie un message à ce client.
         * 
         * @param message Le message à envoyer
         */
        public void envoyerMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
        
        /**
         * Ferme proprement les ressources de ce client.
         */
        public void fermer() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du client: " + e.getMessage());
            }
        }
        
        /**
         * Obtient l'index de ce joueur dans la partie.
         * 
         * @return L'index du joueur
         */
        public int getIndexJoueur() {
            return indexJoueur;
        }
        
        /**
         * Obtient le nom choisi par ce joueur.
         * 
         * @return Le nom du joueur
         */
        public String getNomJoueur() {
            return nomJoueur;
        }
    }
}