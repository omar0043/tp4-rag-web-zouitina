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

import static org.junit.jupiter.api.Assertions.*;

/** Vérification du branchement de la reformulation au client utilisé par JSF. */
class ConversationRagTest {
    @Test
    void uneQuestionDeSuiviEstReformuleeAvantLaRecherche() {
        List<String> recherches = new ArrayList<>();
        List<String> prompts = new ArrayList<>();
        String autonome = "Dans le cours, à quoi le fine-tuning est-il comparé ?";
        ChatModel model = new ChatModel() {
            @Override public ChatResponse doChat(ChatRequest request) {
                String prompt = ((UserMessage) request.messages().getLast()).singleText();
                prompts.add(prompt);
                String texte = prompt.contains("Based on the user query") ? "1"
                        : prompt.contains("Reformulate this query") ? autonome : "Réponse de test sur le fine-tuning";
                return ChatResponse.builder().aiMessage(AiMessage.from(texte)).build();
            }
        };
        var documents = new RagDocuments() {
            @Override public List<Source> sources() {
                return List.of(new Source("cours-test.pdf", "Cours de test", query -> {
                    recherches.add(query.text());
                    return List.of(Content.from("Passage sur le fine-tuning."));
                }));
            }
        };
        var client = new LlmClient(new ModelProvider() {
            @Override public ChatModel model() { return model; }
        }, documents);

        client.chat("Qu’est-ce que le fine-tuning ?");
        client.chat("Dans le support fourni, à quoi c’est comparé ?");

        assertEquals(List.of("Qu’est-ce que le fine-tuning ?", autonome), recherches);
        assertTrue(prompts.stream().anyMatch(p -> p.contains("Reformulate this query")
                && p.contains("fine-tuning") && p.contains("à quoi c’est comparé")));
        assertTrue(prompts.getLast().contains("Passage sur le fine-tuning."));
    }
}
