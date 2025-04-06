package utils;

/**
 * Interface pour implémenter le pattern Observateur 
 * pour les événements de la partie
 */
public interface ObservateurPartie {
    
    /**
     * Méthode appelée lorsque l'état de la partie change
     * @param evenement Type d'événement
     * @param data Données associées à l'événement
     */
    void notifier(String evenement, Object data);
    
    /**
     * Types d'événements constants pour une meilleure lisibilité
     */
    public static final String PLATEAU_MODIFIE = "PLATEAU_MODIFIE";
    public static final String LETTRES_MODIFIEES = "LETTRES_MODIFIEES";
    public static final String SCORES_MODIFIES = "SCORES_MODIFIES";
    public static final String JOUEUR_ACTUEL_CHANGE = "JOUEUR_ACTUEL_CHANGE";
    public static final String MOT_IMPOSE_ATTRIBUE = "MOT_IMPOSE_ATTRIBUE";
    public static final String MOT_IMPOSE_PLACE = "MOT_IMPOSE_PLACE";
    public static final String TOUR_PASSE = "TOUR_PASSE";
    public static final String PARTIE_COMMENCEE = "PARTIE_COMMENCEE";
    public static final String PARTIE_TERMINEE = "PARTIE_TERMINEE";
    public static final String ERREUR = "ERREUR";
}