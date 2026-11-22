package ma.emsi.zouitina.tp4ragwebzouitina.llm;

import dev.langchain4j.service.Result;

public interface Assistant {
    Result<String> chat(String prompt);
}
