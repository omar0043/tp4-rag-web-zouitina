package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;

import java.util.List;

/** Assemble les composants RAG utilisés par le client de la page JSF. */
final class RagPipeline {
    private RagPipeline() { }

    static RetrievalAugmentor creer(ChatModel model, List<RagDocuments.Source> sources) {
        var retrievers = sources.stream().map(RagDocuments.Source::retriever).toList();
        return DefaultRetrievalAugmentor.builder()
                // Reformule les questions de suivi à partir de l'historique de cette vue.
                .queryTransformer(new CompressingQueryTransformer(model))
                .queryRouter(new DefaultQueryRouter(retrievers))
                .build();
    }
}
