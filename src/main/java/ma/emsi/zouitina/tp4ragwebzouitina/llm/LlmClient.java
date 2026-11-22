package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.inject.Inject;
import dev.langchain4j.service.AiServices;
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
            String reponse = assistant.chat(question);
            sauvegarderHistorique();
            return reponse;
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