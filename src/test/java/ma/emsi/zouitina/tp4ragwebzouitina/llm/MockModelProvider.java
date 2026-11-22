package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/** Uniquement dans le WAR de test créé par scripts/smoke_payara.py --mock. */
@Alternative
@Priority(1)
@ApplicationScoped
public class MockModelProvider extends ModelProvider {
    private final ChatModel simulation = new SimulatedModel();

    @Override public ChatModel model() { return simulation; }

    public static class SimulatedModel implements ChatModel {
        @Override public ChatResponse doChat(ChatRequest request) {
            String prompt = ((UserMessage) request.messages().getLast()).singleText();
            String reponse;
            if (prompt.contains("Based on the user query")) {
                String question = prompt.substring(prompt.lastIndexOf("User query:"));
                reponse = question.contains("Nice") || question.contains("Colline") ? "2" : "1";
                System.out.println("TEST_ROUTE=" + reponse);
            } else if (prompt.contains("Reformulate this query")) {
                String question = prompt.substring(prompt.lastIndexOf("User query:") + 11)
                        .split("It is very important")[0].trim();
                reponse = question.contains("comparé")
                        ? "Dans le cours fourni, à quoi le fine-tuning est-il comparé ?" : question;
                System.out.println("TEST_REWRITE=" + reponse);
            } else {
                String question = prompt.split("Answer using the following information:")[0];
                reponse = question.contains("Colline du Château")
                        ? "SIMULATION : vue sur la vieille ville, le port et la Promenade des Anglais."
                        : "SIMULATION : réponse de test sur le fine-tuning à partir du cours.";
                System.out.println("TEST_GENERATION");
            }
            return ChatResponse.builder().aiMessage(AiMessage.from(reponse)).build();
        }
    }
}
