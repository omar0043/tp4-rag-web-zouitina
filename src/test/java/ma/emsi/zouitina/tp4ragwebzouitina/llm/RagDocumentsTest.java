package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RagDocumentsTest {
    private static final RagDocuments DOCUMENTS = new RagDocuments();
    private static List<RagDocuments.Source> sources;

    @BeforeAll
    static void indexerLesVraisPdf() {
        sources = DOCUMENTS.sources();
    }

    @Test
    void lesDeuxPdfSontIndexesUneSeuleFois() {
        assertEquals(List.of("rag.pdf", "autre.pdf"), sources.stream().map(RagDocuments.Source::fichier).toList());
        assertSame(sources, DOCUMENTS.sources());
    }

    @Test
    void retrouveUnPassageTouristiqueAvecSonOrigine() {
        var contenus = sources.get(1).retriever().retrieve(
                Query.from("Que voit-on depuis la Colline du Château à Nice ?"));
        assertFalse(contenus.isEmpty());
        assertTrue(contenus.size() <= 2);
        assertTrue(contenus.stream().anyMatch(c -> c.textSegment().text().contains("Colline")));
        assertTrue(contenus.stream().allMatch(c -> "autre.pdf".equals(c.textSegment().metadata().getString("source"))));
    }

    @Test
    void retrouveUnPassageDuCours() {
        var contenus = sources.getFirst().retriever().retrieve(Query.from("À quoi sert le RAG ?"));
        assertFalse(contenus.isEmpty());
        assertTrue(contenus.stream().allMatch(c -> "rag.pdf".equals(c.textSegment().metadata().getString("source"))));
    }
}
