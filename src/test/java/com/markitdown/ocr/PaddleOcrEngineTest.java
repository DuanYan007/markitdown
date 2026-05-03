package com.markitdown.ocr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaddleOcrEngineTest {

    @Test
    void extractsMarkdownTextFromJsonlPayload() throws Exception {
        String jsonl = "{\"result\":{\"layoutParsingResults\":[{\"markdown\":{\"text\":\"# Page 1\\n\\nHello\"}},"
                + "{\"markdown\":{\"text\":\"## Table\\n\\n|A|B|\"}}]}}\n"
                + "{\"result\":{\"layoutParsingResults\":[{\"markdown\":{\"text\":\"# Page 2\\n\\nWorld\"}}]}}";

        String markdown = PaddleOcrEngine.extractMarkdownFromJsonl(jsonl);

        assertEquals("# Page 1\n\nHello\n\n## Table\n\n|A|B|\n\n# Page 2\n\nWorld", markdown);
    }
}
