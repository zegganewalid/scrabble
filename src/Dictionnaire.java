import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Dictionnaire {
    private String fichier = System.getProperty("user.dir") + "/database/mots.txt";

    public boolean chercherMotDansFichier(String motRecherche) {
        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (ligne.equalsIgnoreCase(motRecherche)) { // Comparaison insensible à la casse
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
        return false;
    }
}
