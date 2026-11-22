package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** Assemble les composants RAG utilisés par le client de la page JSF. */
final class RagPipeline {
    private static final Logger LOG = Logger.getLogger(RagPipeline.class.getName());

    private RagPipeline() { }

    static RetrievalAugmentor creer(ChatModel model, List<RagDocuments.Source> sources) {
        Map<ContentRetriever, String> descriptions = new LinkedHashMap<>();
        sources.forEach(source -> descriptions.put(source.retriever(), source.description()));
        var routeurLlm = new LanguageModelQueryRouter(model, descriptions);
        QueryRouter routeur = query -> {
            var choix = routeurLlm.route(query);
            var fichiers = sources.stream().filter(s -> choix.contains(s.retriever()))
                    .map(RagDocuments.Source::fichier).toList();
            LOG.info(() -> "Sources sélectionnées par le routeur : " + fichiers);
            return choix;
        };
        return DefaultRetrievalAugmentor.builder()
                // La reformulation précède le routage et la recherche documentaire.
                .queryTransformer(new CompressingQueryTransformer(model))
                .queryRouter(routeur)
                .build();
    }
}
