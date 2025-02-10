
import java.util.ArrayList;
import java.util.List;

public class TestPartie {

    public static void main(String[] args) {
        Partie partie = new  Partie();
        List<Joueur> joueurs = partie.getJoueurs();
        Joueur joueur1 = joueurs.get(0);
        Joueur joueur2 = joueurs.get(1);
        
        Plateau plateau = partie.getPlateau();
        Sac sac = partie.getSac();
        
        joueur1.setMotImpose("BONJOUR");
        joueur2.setMotImpose("SCRABBLE");
        
        System.out.println("=== Début de la partie ===");
        System.out.println("Joueur 1 : " + joueur1.getNom());
        System.out.println("Mot imposé : " + joueur1.getMotImpose());
        System.out.println("Joueur 2 : " + joueur2.getNom());
        System.out.println("Mot imposé : " + joueur2.getMotImpose());
        
        System.out.println("\nPlateau initial :");
        afficherPlateau(plateau);
        
        List<Lettre> lettresMotBonjour = creerMot("BONJOUR");
        if (partie.placerMot(lettresMotBonjour, 7, 7, Direction.HORIZONTAL)) {
            System.out.println("\n=== Tour 1 - " + joueur1.getNom() + " ===");
            System.out.println("Mot 'BONJOUR' placé avec succès");
            
            System.out.println("Plateau après premier mot :");
            afficherPlateau(plateau);
            
            System.out.println("Score de " + joueur1.getNom() + " : " + joueur1.getScore());
        }
        
        List<Lettre> lettresMotScrabble = creerMot("SCRABBLE");
        if (partie.placerMot(lettresMotScrabble, 7, 8, Direction.VERTICAL)) {
            System.out.println("\n=== Tour 2 - " + joueur2.getNom() + " ===");
            System.out.println("Mot 'SCRABBLE' placé avec succès");
            System.out.println("Plateau après deuxième mot :");
            afficherPlateau(plateau);
            System.out.println("Score de " + joueur2.getNom() + " : " + joueur2.getScore());
        }
        
        if (partie.estPartieTerminee()) {
            partie.terminerPartie();
            Joueur vainqueur = partie.determinerVainqueur();
            System.out.println("\n=== Fin de la partie ===");
            System.out.println("Vainqueur : " + vainqueur.getNom());
            System.out.println("Score final : " + vainqueur.getScore());
        }
    }
    
    private static List<Lettre> creerMot(String mot) {
        List<Lettre> lettres = new ArrayList<>();
        for (char c : mot.toCharArray()) {
            lettres.add(new Lettre(c, 1));
        }
        return lettres;
    }
    
    private static void afficherPlateau(Plateau plateau) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Case c = plateau.getCase(i, j);
                if (c.isEstOccupe()) {
                    System.out.print(" " + c.getLettre().getCaractere() + " ");
                } else if (c.getTypeBonus() != null) {
                    System.out.print(" " + c.getTypeBonus() + " ");
                } else {
                    System.out.print(" . ");
                }
            }
            System.out.println();
        }
    }
}