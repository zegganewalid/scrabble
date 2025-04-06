package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe utilitaire qui gère la configuration du jeu Scrabble.
 * Cette classe permet de charger les paramètres depuis un fichier de configuration
 * ou d'utiliser des valeurs par défaut si le fichier n'est pas trouvé.
 * Elle suit le pattern Singleton pour l'initialisation.
 */
public class ConfigurationJeu {
    // Objet Properties qui stockera les paires clé-valeur de configuration
    private static Properties properties = new Properties();
    
    // Drapeau qui indique si la configuration a déjà été initialisée
    private static boolean initialized = false;
    
    // Définition des valeurs par défaut pour les différents paramètres
    private static final String DEFAULT_PORT = "5000";              // Port par défaut du serveur
    private static final String DEFAULT_SERVER_ADDRESS = "localhost"; // Adresse par défaut du serveur
    private static final String DEFAULT_PLAYERS = "2";              // Nombre de joueurs par défaut
    private static final String DEFAULT_DICT_PATH = "database/mots.txt"; // Chemin par défaut vers le dictionnaire
    
    /**
     * Initialise la configuration en lisant le fichier config.properties.
     * Si le fichier n'est pas trouvé, utilise les valeurs par défaut.
     * Cette méthode implémente une initialisation paresseuse (lazy initialization)
     * et ne charge le fichier qu'une seule fois.
     */
    public static void initialiser() {
        // Vérifie si l'initialisation a déjà été effectuée
        if (!initialized) {
            try (InputStream input = new FileInputStream("config.properties")) {
                // Charge les propriétés depuis le fichier
                properties.load(input);
                initialized = true;
                
                // Note: Le bloc try-with-resources fermera automatiquement l'InputStream
            } catch (IOException ex) {
                // En cas d'erreur (fichier non trouvé, etc.), utilise les valeurs par défaut
                System.out.println("Fichier de configuration non trouvé, utilisation des valeurs par défaut");
                
                // Définit manuellement les valeurs par défaut dans l'objet Properties
                properties.setProperty("port", DEFAULT_PORT);
                properties.setProperty("serverAddress", DEFAULT_SERVER_ADDRESS);
                properties.setProperty("players", DEFAULT_PLAYERS);
                properties.setProperty("dictionaryPath", DEFAULT_DICT_PATH);
                
                // Marque comme initialisé même si on utilise les valeurs par défaut
                initialized = true;
            }
        }
    }
    
    /**
     * Obtient le port du serveur depuis la configuration.
     * 
     * @return Le numéro de port configuré ou la valeur par défaut (5000)
     */
    public static int getPort() {
        // S'assure que la configuration est initialisée avant d'y accéder
        initialiser();
        try {
            // Récupère la valeur et la convertit en entier
            return Integer.parseInt(properties.getProperty("port", DEFAULT_PORT));
        } catch (NumberFormatException e) {
            // En cas d'erreur de format (si la valeur n'est pas un nombre)
            // utilise la valeur par défaut
            return Integer.parseInt(DEFAULT_PORT);
        }
    }
    
    /**
     * Obtient l'adresse du serveur depuis la configuration.
     * 
     * @return L'adresse du serveur configurée ou la valeur par défaut (localhost)
     */
    public static String getServerAddress() {
        // S'assure que la configuration est initialisée avant d'y accéder
        initialiser();
        // Récupère la valeur ou utilise la valeur par défaut si non définie
        return properties.getProperty("serverAddress", DEFAULT_SERVER_ADDRESS);
    }
    
    /**
     * Obtient le nombre de joueurs par défaut depuis la configuration.
     * 
     * @return Le nombre de joueurs configuré ou la valeur par défaut (2)
     */
    public static int getPlayers() {
        // S'assure que la configuration est initialisée avant d'y accéder
        initialiser();
        try {
            // Récupère la valeur et la convertit en entier
            return Integer.parseInt(properties.getProperty("players", DEFAULT_PLAYERS));
        } catch (NumberFormatException e) {
            // En cas d'erreur de format (si la valeur n'est pas un nombre)
            // utilise la valeur par défaut
            return Integer.parseInt(DEFAULT_PLAYERS);
        }
    }
    
    /**
     * Obtient le chemin vers le fichier dictionnaire depuis la configuration.
     * 
     * @return Le chemin vers le dictionnaire configuré ou la valeur par défaut (database/mots.txt)
     */
    public static String getDictionaryPath() {
        // S'assure que la configuration est initialisée avant d'y accéder
        initialiser();
        // Récupère la valeur ou utilise la valeur par défaut si non définie
        return properties.getProperty("dictionaryPath", DEFAULT_DICT_PATH);
    }
}