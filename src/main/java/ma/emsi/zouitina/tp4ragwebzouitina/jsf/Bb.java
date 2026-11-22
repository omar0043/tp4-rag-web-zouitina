package ma.emsi.zouitina.tp4ragwebzouitina.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.zouitina.tp4ragwebzouitina.llm.LlmClient;

import java.io.Serializable;
import java.util.List;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation qui dure pendant plusieurs requêtes HTTP.
 * La portée view nécessite l'implémentation de Serializable
 * (le backing bean peut être mis en mémoire secondaire).
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    /**
     * Identifiant stable du rôle choisi ; le texte du prompt reste côté serveur.
     */
    private String roleSysteme = "documentaire";

    /**
     * Après le premier échange, le rôle ne peut plus être modifié
     * depuis la liste déroulante, sauf en démarrant un nouveau chat.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste de tous les rôles de l'API prédéfinis.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse de l'API Gemini.
     */
    private String reponse;

    /**
     * La conversation depuis le début.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Client qui envoie les questions à Gemini et conserve le contexte du chat.
     */
    @Inject
    private LlmClient llmClient;

    /**
     * Contexte JSF. Utilisé pour qu'un message d'erreur s'affiche dans le formulaire.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Obligatoire pour un bean CDI (classe gérée par CDI), s'il y a un autre constructeur.
     */
    public Bb() {
    }

    public String getRoleSysteme() {
        return this.roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return this.roleSystemeChangeable;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSourcesUtilisees() {
        return llmClient.getSourcesUtilisees();
    }

    public String getReponse() {
        return this.reponse;
    }

    /**
     * Setter indispensable pour le textarea.
     *
     * @param reponse la réponse à la question.
     */
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return this.conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie la question à Gemini et ajoute l'échange à la conversation.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (this.question == null || this.question.isBlank()) {
            // Le formulaire est réaffiché avec un message d'erreur.
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Définit le rôle système avant le premier échange.
            if (this.roleSystemeChangeable) {
                this.llmClient.setSystemRole(texteRole(this.roleSysteme));
            }

            this.reponse = this.llmClient.chat(this.question);

            // Fige la liste déroulante après la première réponse reçue.
            this.roleSystemeChangeable = false;

            // La conversation contient l'historique des questions et réponses.
            afficherConversation();
        } catch (RuntimeException e) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Erreur Gemini",
                    "Impossible d’obtenir une réponse de Gemini.");
            facesContext.addMessage(null, message);
        }

        return null;
    }

    /**
     * Redirige vers index.xhtml pour ouvrir une nouvelle vue
     * et démarrer une conversation vide.
     *
     * @return le résultat de navigation avec redirection.
     */
    public String nouveauChat() {
        return "index?faces-redirect=true";
    }

    /**
     * Pour afficher la conversation dans le textarea de la page JSF.
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n")
                .append(this.question)
                .append("\n== Serveur:\n")
                .append(this.reponse)
                .append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = List.of(
                    new SelectItem("documentaire", "Assistant documentaire"),
                    new SelectItem("traducteur", "Traducteur Anglais-Français"),
                    new SelectItem("guide", "Guide touristique")
            );
        }
        return this.listeRolesSysteme;
    }

    static String texteRole(String identifiant) {
        return switch (identifiant) {
            case "documentaire" -> """
                    Tu es un assistant documentaire. Réponds en français à partir des extraits fournis.
                    Indique le nom du fichier source lorsqu'il est présent dans les extraits.
                    Si les extraits ne suffisent pas, explique cette limite sans inventer de contenu.
                    """;
            case "traducteur" -> """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user types a French text, translate it into English.
                    If the user types an English text, translate it into French.
                    For one to three words, give examples of usage in English.
                    """;
            case "guide" -> """
                    You are a travel guide. Use the supplied documents to describe places to visit.
                    Do not invent prices or information missing from the documents.
                    """;
            default -> throw new IllegalArgumentException("Rôle inconnu");
        };
    }
}
