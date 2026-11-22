package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.logging.Logger;

/** Index partagé par l'application ; aucune conversation n'est stockée ici. */
@ApplicationScoped
public class RagDocuments {
    private static final Logger LOG = Logger.getLogger(RagDocuments.class.getName());
    private List<Source> sources;

    public record Source(String fichier, String description, ContentRetriever retriever) { }

    /** L'index est construit une seule fois, lors de sa première utilisation. */
    public synchronized List<Source> sources() {
        if (sources == null) {
            var modele = new AllMiniLmL6V2EmbeddingModel();
            sources = List.of(
                    indexer("rag.pdf", "Support de cours de Richard Grin sur le RAG, le fine-tuning, "
                            + "les embeddings, les bases vectorielles et les modèles de langage.", modele),
                    indexer("autre.pdf", "Guide touristique sur Nice : Promenade des Anglais, Vieux-Nice, "
                            + "Cours Saleya, Colline du Château et itinéraire de visite.", modele)
            );
        }
        return sources;
    }

    private Source indexer(String fichier, String description, AllMiniLmL6V2EmbeddingModel modele) {
        Document document = ClassPathDocumentLoader.loadDocument(fichier, new ApacheTikaDocumentParser());
        document.metadata().put("source", fichier);
        List<TextSegment> segments = DocumentSplitters.recursive(1000, 100).split(document);
        var store = new InMemoryEmbeddingStore<TextSegment>();
        store.addAll(modele.embedAll(segments).content(), segments);
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(modele)
                .maxResults(2)
                .minScore(0.5)
                .build();
        LOG.info(() -> "Indexation de " + fichier + " : " + segments.size() + " segments");
        ContentRetriever avecOrigine = query -> retriever.retrieve(query).stream()
                .map(c -> Content.from(TextSegment.from(
                        "Source : " + fichier + "\n" + c.textSegment().text(),
                        c.textSegment().metadata())))
                .toList();
        return new Source(fichier, description, avecOrigine);
    }
}
