
// Déclaration du package dans lequel se trouve cette classe (utile pour organiser le code)
package modele;

// Importation des classes Java nécessaires à la lecture de fichiers texte et à la gestion des ensembles
import java.io.BufferedReader; // Classe permettant de lire un fichier ligne par ligne efficacement
import java.io.FileReader;    // Classe utilisée pour ouvrir un fichier texte en lecture
import java.io.IOException;   // Classe utilisée pour gérer les erreurs liées aux entrées/sorties (comme l'ouverture d'un fichier)
import java.util.HashSet;     // Classe représentant un ensemble de données sans doublons (non ordonné)
import java.util.Set;         // Interface de base pour déclarer un ensemble (Set)

// Déclaration de la classe Dictionnaire. Cette classe permet de gérer une liste de mots utilisables dans un jeu comme le Scrabble
public class Dictionnaire {

    // Variable privée contenant le chemin du fichier texte contenant les mots (le dictionnaire). 
    // 'System.getProperty("user.dir")' permet d'obtenir le chemin du dossier courant du projet.
    private String fichier = System.getProperty("user.dir") + "/database/mots.txt";

    // Ensemble de mots (sans doublons) stocké en mémoire pour permettre des recherches rapides
    private Set<String> motsCaches = new HashSet<>();

    // Booléen qui indique si les mots du fichier ont déjà été chargés en mémoire (évite de relire plusieurs fois le fichier)
    private boolean estInitialise = false;

    // Méthode privée utilisée pour lire les mots depuis le fichier et les ajouter dans le Set motsCaches
    private void initialiserCache() {
        // Vérifie si les mots ont déjà été initialisés (pour éviter de refaire le travail inutilement)
        if (!estInitialise) {
            // Affiche un message dans la console pour indiquer le début de l'initialisation
            System.out.println("Démarrage de l'initialisation du dictionnaire...");
            // Affiche le chemin du fichier qui sera lu
            System.out.println("Chemin du fichier: " + fichier);

            // Bloc try-with-resources qui permet d'ouvrir automatiquement le fichier et de le fermer après utilisation
            try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
                String ligne; // Variable temporaire pour stocker chaque ligne lue du fichier
                int compteur = 0; // Compteur pour compter le nombre de mots lus

                // Boucle qui lit ligne par ligne le fichier jusqu'à la fin (null signifie fin du fichier)
                while ((ligne = br.readLine()) != null) {
                    // Ajoute chaque mot lu (converti en minuscule) dans l'ensemble motsCaches
                    motsCaches.add(ligne.toLowerCase());
                    compteur++; // Incrémente le compteur de mots

                    // Pour les 10 premiers mots seulement, affiche une ligne de vérification dans la console
                    if (compteur <= 10) {
                        System.out.println("Mot ajouté: " + ligne.toLowerCase());
                    }
                }

                // Affiche un message indiquant que l'initialisation est terminée avec le nombre total de mots lus
                System.out.println("Initialisation terminée. " + compteur + " mots chargés.");
                // Affiche la taille finale de l'ensemble (utile pour vérifier que tous les mots ont été ajoutés)
                System.out.println("Taille du cache: " + motsCaches.size());

                // Boucle pour vérifier si certaines lettres seules (ex : "a", "b") sont reconnues comme mots valides
                System.out.println("Vérification des lettres uniques dans le dictionnaire:");
                for (char c = 'a'; c <= 'z'; c++) {
                    // Convertit une lettre (char) en chaîne de caractères (String)
                    String lettre = String.valueOf(c);
                    // Vérifie si cette lettre existe comme mot dans le dictionnaire
                    if (motsCaches.contains(lettre)) {
                        // Affiche dans la console que cette lettre est présente dans le dictionnaire
                        System.out.println("La lettre '" + lettre + "' est présente dans le dictionnaire.");
                    }
                }

                // Marque l'initialisation comme terminée (évite une relecture lors du prochain appel)
                estInitialise = true;
            } catch (IOException e) {
                // Bloc exécuté s'il y a une erreur lors de la lecture du fichier (fichier introuvable, etc.)
                System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage()); // Affiche l'erreur principale
                e.printStackTrace(); // Affiche les détails techniques de l'erreur
            }
        }
    }

    // Méthode publique (accessible depuis l'extérieur) permettant de vérifier l'initialisation et d'afficher des infos de debug
    public void verifierInitialisation() {
        initialiserCache(); // Appelle la méthode qui lit le fichier si ce n'est pas déjà fait
        // Affiche dans la console si l'initialisation a été faite ou non
        System.out.println("État d'initialisation: " + estInitialise);
        // Affiche le nombre de mots actuellement dans le dictionnaire
        System.out.println("Nombre de mots dans le cache: " + motsCaches.size());

        // Liste de mots à tester pour vérifier s'ils existent bien dans le dictionnaire
        String[] motsTest = {"abaca", "maison", "chat", "a", "e", "i", "o", "u"};
        // Boucle sur chaque mot de test
        for (String mot : motsTest) {
            // Vérifie si le mot (en minuscule) est présent dans le dictionnaire
            boolean existe = motsCaches.contains(mot.toLowerCase());
            // Affiche le résultat de la vérification
            System.out.println("Le mot '" + mot + "' existe dans le dictionnaire: " + existe);
        }
    }

    // Méthode qui vérifie si un mot donné existe dans le dictionnaire, en prenant en compte les accents
    public boolean chercherMotDansFichier(String motRecherche) {
        initialiserCache(); // S'assure que le dictionnaire est chargé avant la recherche

        // Appelle la méthode pour retirer les accents du mot recherché (pour comparaison simplifiée)
        String motNormalise = normaliserMot(motRecherche.toLowerCase());

        // Si le mot fait moins de 2 lettres, on considère qu'il est invalide dans le jeu (ex : "a")
        if (motRecherche.length() < 2) {
            return false;
        }

        // Si le mot exact (en minuscule) est dans le dictionnaire, on le retourne immédiatement
        if (motsCaches.contains(motRecherche.toLowerCase())) {
            return true;
        }

        // Sinon, on parcourt tous les mots du dictionnaire pour comparer la version normalisée
        for (String mot : motsCaches) {
            if (normaliserMot(mot).equals(motNormalise)) {
                return true; // Si on trouve une correspondance, on retourne true
            }
        }

        // Si aucune correspondance trouvée, on retourne false
        return false;
    }

    // Méthode privée qui enlève tous les accents d'un mot (utile pour permettre la recherche avec ou sans accents)
    private String normaliserMot(String mot) {
        return mot.replaceAll("[éèêë]", "e") // Remplace les lettres accentuées é, è, ê, ë par "e"
                 .replaceAll("[àâä]", "a")   // Idem pour les variantes de "a"
                 .replaceAll("[ùûü]", "u")   // Pour les variantes de "u"
                 .replaceAll("[ïî]", "i")    // Pour les variantes de "i"
                 .replaceAll("[ôö]", "o")    // Pour les variantes de "o"
                 .replaceAll("ç", "c")       // Le "ç" est remplacé par "c"
                 .replaceAll("ñ", "n");      // Le "ñ" devient "n"
    }

    // Méthode qui permet de savoir si un mot dans le dictionnaire commence par un certain préfixe (utile pour optimisation)
    public boolean prefixeExiste(String prefixe) {
        initialiserCache(); // Assure que le dictionnaire est chargé
        prefixe = prefixe.toLowerCase(); // Met tout en minuscule pour éviter les problèmes de casse

        // Parcourt tous les mots du dictionnaire pour voir s'il en existe un qui commence par ce préfixe
        for (String mot : motsCaches) {
            if (mot.startsWith(prefixe)) { // startsWith vérifie le début du mot
                return true; // Dès qu'on trouve un mot qui commence par ce préfixe, on retourne vrai
            }
        }
        return false; // Aucun mot ne commence par ce préfixe
    }

    // Méthode qui retourne tous les mots valides que l'on peut construire avec un ensemble donné de lettres
    public Set<String> trouverMotsPossibles(String lettres) {
        initialiserCache(); // Charge les mots si ce n'est pas déjà fait
        Set<String> motsPossibles = new HashSet<>(); // Crée un nouvel ensemble pour stocker les résultats

        // Lance une recherche récursive avec les lettres données
        trouverMotsRecursive(lettres.toLowerCase(), "", motsPossibles);

        // Retourne la liste des mots trouvés
        return motsPossibles;
    }

    // Méthode récursive qui essaie toutes les combinaisons de lettres pour trouver des mots valides
    private void trouverMotsRecursive(String lettres, String motCourant, Set<String> motsTrouves) {
        // Si le mot actuel a au moins 2 lettres et qu'il est dans le dictionnaire, on l'ajoute
        if (motCourant.length() >= 2 && chercherMotDansFichier(motCourant)) {
            motsTrouves.add(motCourant);
        }

        // On boucle sur chaque lettre restante pour créer des mots plus longs
        for (int i = 0; i < lettres.length(); i++) {
            char lettreCourante = lettres.charAt(i); // On récupère la lettre à la position i
            String nouveauMot = motCourant + lettreCourante; // On crée un nouveau mot en ajoutant cette lettre

            // Si ce nouveau mot est un début possible de mot valide, on continue à chercher
            if (prefixeExiste(nouveauMot)) {
                // On enlève la lettre qu'on vient d'utiliser pour éviter de la reprendre
                String lettresRestantes = lettres.substring(0, i) + lettres.substring(i + 1);
                // Appel récursif pour explorer cette nouvelle piste
                trouverMotsRecursive(lettresRestantes, nouveauMot, motsTrouves);
            }
        }
    }
}
