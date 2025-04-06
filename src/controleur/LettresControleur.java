package controleur;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import vue.MainJoueurVue;
import modele.Lettre;
import utils.ObservateurPartie;

/**
 * Contrôleur responsable de la gestion des lettres du joueur
 */
public class LettresControleur implements ObservateurPartie {
    
    private ClientControleur clientControleur;
    private MainJoueurVue mainJoueurVue;
    private List<Lettre> lettresEnMain;
    private List<Lettre> lettresSelectionnees;
    private String motImpose;
    private boolean motImposePlace;
    
    public LettresControleur(ClientControleur clientControleur) {
        this.clientControleur = clientControleur;
        this.lettresEnMain = new ArrayList<>();
        this.lettresSelectionnees = new ArrayList<>();
        this.motImposePlace = false;
    }
    
    public void setMainJoueurVue(MainJoueurVue mainJoueurVue) {
        this.mainJoueurVue = mainJoueurVue;
    }
    
    /**
     * Sélectionne une lettre pour placement sur le plateau
     * @param index Position de la lettre dans la main du joueur
     * @return La lettre sélectionnée ou null si pas disponible
     */
    public Lettre selectionnerLettre(int index) {
        if (index >= 0 && index < lettresEnMain.size() && clientControleur.estMonTour()) {
            Lettre lettre = lettresEnMain.get(index);
            lettresSelectionnees.add(lettre);
            return lettre;
        }
        return null;
    }
    
    /**
     * Désélectionne une lettre préalablement sélectionnée
     * @param lettre La lettre à désélectionner
     */
    public void deselectionnerLettre(Lettre lettre) {
        lettresSelectionnees.remove(lettre);
    }
    
    /**
     * Vide la liste des lettres sélectionnées
     */
    public void reinitialiserSelection() {
        lettresSelectionnees.clear();
    }
    
    /**
     * Vérifie si le mot formé par les lettres sélectionnées correspond au mot imposé
     * @return true si c'est le mot imposé, false sinon
     */
    public boolean estMotImpose() {
        if (motImpose == null || motImposePlace) {
            return false;
        }
        
        StringBuilder mot = new StringBuilder();
        for (Lettre lettre : lettresSelectionnees) {
            mot.append(lettre.getCaractere());
        }
        
        return mot.toString().equalsIgnoreCase(motImpose);
    }
    
    /**
     * Met à jour la liste des lettres du joueur
     * @param lettres Nouvelles lettres
     */
    public void mettreAJourLettres(List<Lettre> lettres) {
        lettresEnMain.clear();
        lettresEnMain.addAll(lettres);
        
        if (mainJoueurVue != null) {
            //mettre à jour l'affichage des lettres du joueur dans l'interface graphique depuis le thread JavaFX
            Platform.runLater(() -> mainJoueurVue.mettreAJourLettres(lettresEnMain));
        }
    }
    
    /**
     * Met à jour le mot imposé pour le joueur
     * @param motImpose Nouveau mot imposé
     */
    public void mettreAJourMotImpose(String motImpose) {
        this.motImpose = motImpose;
        this.motImposePlace = false;
        
        if (mainJoueurVue != null) {
            //mettre à jour l'affichage du mot imposé dans l'interface graphique depuis le thread JavaFX
            Platform.runLater(() -> mainJoueurVue.afficherMotImpose(motImpose));
        }
    }
    
    /**
     * Marque le mot imposé comme placé
     */
    public void marquerMotImposePlace() {
        this.motImposePlace = true;
        
        if (mainJoueurVue != null) {
            Platform.runLater(() -> mainJoueurVue.marquerMotImposePlace());
        }
    }
    
    /**
     * Modifie l'état des contrôles selon que c'est le tour du joueur ou non
     * @param estMonTour true si c'est le tour du joueur
     */
    public void setEstMonTour(boolean estMonTour) {
        if (mainJoueurVue != null) {
            Platform.runLater(() -> mainJoueurVue.activerControles(estMonTour));
        }
    }
    
    /**
     * Vérifie si une lettre spécifique est disponible dans la main du joueur
     * @param caractere Le caractère à chercher
     * @return La lettre si disponible, null sinon
     */
    public Lettre trouverLettre(char caractere) {
        for (Lettre lettre : lettresEnMain) {
            if (lettre.getCaractere() == caractere) {
                return lettre;
            }
        }
        return null;
    }
    
    /**
     * Retourne la liste des lettres dans la main du joueur
     */
    public List<Lettre> getLettresEnMain() {
        return new ArrayList<>(lettresEnMain);
    }
    
    /**
     * Retourne la liste des lettres actuellement sélectionnées
     */
    public List<Lettre> getLettresSelectionnees() {
        return new ArrayList<>(lettresSelectionnees);
    }
    
    /**
     * Active ou désactive le bouton de validation de mot
     */
    public void activerBoutonValider(boolean actif) {
        if (mainJoueurVue != null) {
            Platform.runLater(() -> mainJoueurVue.activerBoutonValider(actif));
        }
    }
    
    /**
     * Méthode de l'interface ObservateurPartie pour recevoir les notifications
     */
    @Override
    public void notifier(String evenement, Object data) {
        // Réagit aux différents événements
        switch (evenement) {
            // Si les lettres du joueur ont été modifiées
            case LETTRES_MODIFIEES:
                if (data instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Lettre> lettres = (List<Lettre>) data;
                    mettreAJourLettres(lettres);
                }
                break;
                
            case MOT_IMPOSE_ATTRIBUE:
              // Si un nouveau mot imposé a été attribué au joueur
                if (data instanceof String) {
                    mettreAJourMotImpose((String) data);
                }
                break;
                
            case MOT_IMPOSE_PLACE:
            // Si le mot imposé a été placé par le joueur
                marquerMotImposePlace();
                break;
                
            case JOUEUR_ACTUEL_CHANGE:
             // Si le tour du joueur a changé
                if (data instanceof Boolean) {
                    setEstMonTour((Boolean) data);
                }
                break;
                
            case PARTIE_TERMINEE:
                // Désactiver les contrôles à la fin de la partie
                setEstMonTour(false);
                break;
        }
    }
}