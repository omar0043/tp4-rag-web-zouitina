package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.content.Content;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutingPipelineTest {
    @Test
    void laSelectionNiceExclutLeCoursDuPromptEtExposeLaSource() {
        List<String> consultations = new ArrayList<>();
        List<String> prompts = new ArrayList<>();
        ChatModel model = new ChatModel() {
            @Override public ChatResponse doChat(ChatRequest request) {
                String prompt = ((UserMessage) request.messages().getLast()).singleText();
                prompts.add(prompt);
                String texte = prompt.contains("Based on the user query") ? "2" : "Réponse de test Nice";
                return ChatResponse.builder().aiMessage(AiMessage.from(texte)).build();
            }
        };
        var docs = new RagDocuments() {
            @Override public List<Source> sources() {
                return List.of(
                        new Source("rag.pdf", "Cours de RAG", query -> {
                            consultations.add("cours");
                            return List.of(Content.from("CONTENU_COURS"));
                        }),
                        new Source("autre.pdf", "Guide de Nice", query -> {
                            consultations.add("nice");
                            return List.of(Content.from(TextSegment.from("CONTENU_NICE",
                                    Metadata.from("source", "autre.pdf"))));
                        }));
            }
        };
        var client = new LlmClient(new ModelProvider() {
            @Override public ChatModel model() { return model; }
        }, docs);

        assertEquals("Réponse de test Nice", client.chat("Que voir à Nice ?"));
        assertEquals(List.of("nice"), consultations);
        assertTrue(prompts.getLast().contains("CONTENU_NICE"));
        assertFalse(prompts.getLast().contains("CONTENU_COURS"));
        assertEquals("autre.pdf", client.getSourcesUtilisees());
    }
}
