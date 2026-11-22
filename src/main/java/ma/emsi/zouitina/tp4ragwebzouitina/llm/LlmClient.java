package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import jakarta.inject.Inject;
import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    @Inject
    private ModelProvider modelProvider;
    @Inject
    private RagDocuments documents;

    public LlmClient() { }

    LlmClient(ModelProvider modelProvider, RagDocuments documents) {
        this.modelProvider = modelProvider;
        this.documents = documents;
    }

    private transient Assistant assistant;
    private transient ChatMemory memoire;
    private String historique = "[]";
    private String sourcesUtilisees = "Aucune réponse pour le moment.";

    public String getSourcesUtilisees() {
        return sourcesUtilisees;
    }

    public void setSystemRole(String role) {
        initialiser();
        memoire.clear();

        if (role != null && !role.isBlank()) {
            memoire.add(SystemMessage.from(role));
        }

        sauvegarderHistorique();
    }

    public String chat(String question) {
        initialiser();
        try {
            var resultat = assistant.chat(question);
            sourcesUtilisees = resultat.sources() == null ? "" : resultat.sources().stream()
                    .map(c -> c.textSegment().metadata().getString("source"))
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(", "));
            if (sourcesUtilisees.isBlank()) {
                sourcesUtilisees = "Aucun passage au-dessus du seuil de similarité.";
            }
            sauvegarderHistorique();
            return resultat.content();
        } catch (RuntimeException e) {
            // Un appel refusé (quota, délai, réseau) ne doit pas polluer le prochain tour.
            memoire.clear();
            ChatMessageDeserializer.messagesFromJson(historique).forEach(memoire::add);
            throw e;
        }
    }

    private void initialiser() {
        if (assistant != null) {
            return;
        }

        var modele = modelProvider.model();
        var augmentor = RagPipeline.creer(modele, documents.sources());

        memoire = MessageWindowChatMemory.withMaxMessages(20);
        ChatMessageDeserializer.messagesFromJson(historique).forEach(memoire::add);

        assistant = AiServices.builder(Assistant.class)
                .chatModel(modele)
                .chatMemory(memoire)
                .retrievalAugmentor(augmentor)
                .build();
    }

    private void sauvegarderHistorique() {
        historique = ChatMessageSerializer.messagesToJson(memoire.messages());
    }
}
