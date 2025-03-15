package ntou.cse.feelink_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextProcessorTest {

    private final TextProcessor textProcessor = new TextProcessor();

    @Test
    void testProcessText_WhenNull() {
        String result = textProcessor.processText(null);
        assertEquals("請分析這張圖片", result);
    }

    @Test
    void testProcessText_WhenEmptyString() {
        String result = textProcessor.processText("");
        assertEquals("請分析這張圖片", result);
    }

    @Test
    void testProcessText_WhenBlankString() {
        String result = textProcessor.processText("   ");
        assertEquals("請分析這張圖片", result);
    }

    @Test
    void testProcessText_WhenNormalString() {
        String result = textProcessor.processText("Hello World");
        assertEquals("Hello World", result);
    }
}
