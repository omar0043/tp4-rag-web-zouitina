package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Configuration commune ; la clé reste dans l'environnement du serveur. */
@ApplicationScoped
public class ModelProvider {
    private ChatModel model;

    public synchronized ChatModel model() {
        if (model == null) {
            String cle = System.getenv("GEMINI_KEY");
            if (cle == null || cle.isBlank()) {
                throw new IllegalStateException("La variable GEMINI_KEY est absente du processus Payara.");
            }
            String nom = System.getenv("GEMINI_MODEL");
            if (nom == null || nom.isBlank()) {
                nom = "gemini-3.5-flash-lite";
            }
            boolean traces = Boolean.parseBoolean(System.getenv("RAG_LOG_REQUESTS"));
            if (traces) {
                Logger logger = Logger.getLogger("dev.langchain4j");
                logger.setLevel(Level.FINE);
                logger.setUseParentHandlers(false);
                ConsoleHandler handler = new ConsoleHandler();
                handler.setLevel(Level.FINE);
                logger.addHandler(handler);
            }
            model = GoogleGenAiChatModel.builder()
                    .apiKey(cle)
                    .modelName(nom)
                    .timeout(Duration.ofSeconds(90))
                    .maxRetries(0)
                    .logRequests(traces)
                    .logResponses(traces)
                    .build();
        }
        return model;
    }
}
