package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.content.Content;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class LlmClientTest {
    static class ModeleSimule implements ChatModel {
        final List<ChatRequest> requetes = new ArrayList<>();
        final AtomicBoolean echouer = new AtomicBoolean(false);

        @Override
        public ChatResponse doChat(ChatRequest request) {
            requetes.add(request);
            if (echouer.getAndSet(false)) {
                throw new IllegalStateException("quota simulé");
            }
            return ChatResponse.builder().aiMessage(AiMessage.from("Réponse simulée")).build();
        }
    }

    private LlmClient client(ModeleSimule modele) {
        return new LlmClient(new ModelProvider() {
            @Override public ChatModel model() { return modele; }
        }, new RagDocuments() {
            @Override public List<Source> sources() {
                return List.of(new Source("test.pdf", "Source de test",
                        query -> List.of(Content.from("Un passage documentaire de test."))));
            }
        });
    }

    @Test
    void conserveLaConversationSansLaPartagerEntreDeuxVues() {
        var modele = new ModeleSimule();
        var premier = client(modele);
        premier.chat("Message privé A");
        premier.chat("Question suivante A");
        assertTrue(modele.requetes.getLast().messages().toString().contains("Message privé A"));
        var second = client(modele);
        second.chat("Message B");
        assertFalse(modele.requetes.getLast().messages().toString().contains("Message privé A"));
    }

    @Test
    void unEchecNePolluePasLaQuestionSuivante() {
        var modele = new ModeleSimule();
        var client = client(modele);
        client.chat("Premier échange réussi");
        modele.echouer.set(true);
        assertThrows(IllegalStateException.class, () -> client.chat("Question refusée"));
        client.chat("Nouvelle question");
        var messages = modele.requetes.getLast().messages();
        assertFalse(messages.stream().filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast).anyMatch(m -> m.singleText().contains("Question refusée")));
        assertTrue(messages.toString().contains("Premier échange réussi"));
    }
}
