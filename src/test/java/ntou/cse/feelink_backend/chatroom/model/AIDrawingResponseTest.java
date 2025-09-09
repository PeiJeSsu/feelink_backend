package ntou.cse.feelink_backend.chatroom.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AIDrawingResponse model
 */
class AIDrawingResponseTest {

    private AIDrawingResponse aiDrawingResponse;

    @BeforeEach
    void setUp() {
        aiDrawingResponse = new AIDrawingResponse();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(aiDrawingResponse);
        assertFalse(aiDrawingResponse.isSuccess());
        assertNull(aiDrawingResponse.getContent());
        assertNull(aiDrawingResponse.getError());
    }

    @Test
    void testSuccessConstructor() {
        GeminiResponse geminiResponse = new GeminiResponse(true, "success message", "image data");
        AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);

        assertTrue(response.isSuccess());
        assertEquals(geminiResponse, response.getContent());
        assertNull(response.getError());
    }

    @Test
    void testErrorConstructor() {
        AIDrawingResponse response = new AIDrawingResponse(false, "Error occurred");

        assertFalse(response.isSuccess());
        assertNull(response.getContent());
        assertEquals("Error occurred", response.getError());
    }

    @Test
    void testSettersAndGetters() {
        GeminiResponse geminiResponse = new GeminiResponse();

        aiDrawingResponse.setSuccess(true);
        aiDrawingResponse.setContent(geminiResponse);
        aiDrawingResponse.setError("test error");

        assertTrue(aiDrawingResponse.isSuccess());
        assertEquals(geminiResponse, aiDrawingResponse.getContent());
        assertEquals("test error", aiDrawingResponse.getError());
    }

    @Test
    void testSuccessScenario() {
        GeminiResponse geminiResponse = new GeminiResponse(true, "Drawing generated", "base64data");
        aiDrawingResponse.setSuccess(true);
        aiDrawingResponse.setContent(geminiResponse);

        assertTrue(aiDrawingResponse.isSuccess());
        assertNotNull(aiDrawingResponse.getContent());
        assertTrue(aiDrawingResponse.getContent().isSuccess());
        assertEquals("Drawing generated", aiDrawingResponse.getContent().getMessage());
        assertNull(aiDrawingResponse.getError());
    }

    @Test
    void testErrorScenario() {
        aiDrawingResponse.setSuccess(false);
        aiDrawingResponse.setError("Failed to process request");

        assertFalse(aiDrawingResponse.isSuccess());
        assertEquals("Failed to process request", aiDrawingResponse.getError());
        assertNull(aiDrawingResponse.getContent());
    }

    @Test
    void testNullError() {
        aiDrawingResponse.setError(null);
        assertNull(aiDrawingResponse.getError());
    }

    @Test
    void testEmptyError() {
        aiDrawingResponse.setError("");
        assertEquals("", aiDrawingResponse.getError());
    }
}