package serveur;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.List;

public class Serveur {
    private static final int PORT = 12345;
    private static ExecutorService pool = Executors.newFixedThreadPool(2);
    private static Partie partie;

    public static void main(String[] args) {
        partie = new Partie();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau joueur connecté : " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                out.println("Bienvenue sur le serveur Scarbell!");
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Message reçu: " + message);
                    if (message.startsWith("PLACER")) {
                        // Handle placing a word
                        String[] parts = message.split(" ");
                        String mot = parts[1];
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        Direction direction = Direction.valueOf(parts[4]);
                        List<Lettre> lettres = creerMot(mot);
                        boolean success = partie.placerMot(lettres, x, y, direction);
                        out.println(success ? "Mot placé avec succès" : "Échec du placement du mot");
                    } else {
                        out.println("Echo: " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private List<Lettre> creerMot(String mot) {
            List<Lettre> lettres = new ArrayList<>();
            for (char c : mot.toCharArray()) {
                lettres.add(new Lettre(c, 1));
            }
            return lettres;
        }
    }
}
