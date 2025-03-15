package ntou.cse.feelink_backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModelClassTests {

    @Test
    public void testChatMessage() {
        // Test constructor
        ChatMessage message1 = new ChatMessage("Hello", true, "session123");
        assertEquals("Hello", message1.getContent());
        assertTrue(message1.isUser());
        assertEquals("session123", message1.getSessionId());

        // Test setters
        message1.setId(1L);
        message1.setContent("Updated content");
        message1.setUser(false);
        message1.setSessionId("newSession");

        // Test getters
        assertEquals(1L, message1.getId());
        assertEquals("Updated content", message1.getContent());
        assertFalse(message1.isUser());
        assertEquals("newSession", message1.getSessionId());

        // Test default constructor
        ChatMessage message2 = new ChatMessage();
        assertNull(message2.getContent());
        assertNull(message2.getSessionId());
    }

    @Test
    public void testAiResponse() {
        // Test constructor
        AiResponse response1 = new AiResponse("Test response", true);
        assertEquals("Test response", response1.getMessage());
        assertTrue(response1.isSuccess());

        // Test setters
        response1.setMessage("Updated response");
        response1.setSuccess(false);

        // Test getters
        assertEquals("Updated response", response1.getMessage());
        assertFalse(response1.isSuccess());

        // Test default constructor
        AiResponse response2 = new AiResponse();
        assertNull(response2.getMessage());
        assertFalse(response2.isSuccess());
    }
}