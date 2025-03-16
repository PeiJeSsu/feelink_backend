package ntou.cse.feelink_backend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    @Test
    void testChatMessageGettersAndSetters() {
        ChatMessage message = new ChatMessage();
        message.setId(1L);
        message.setContent("Hello");
        message.setUser(true);
        message.setSessionId("session123");

        assertEquals(1L, message.getId());
        assertEquals("Hello", message.getContent());
        assertTrue(message.isUser());
        assertEquals("session123", message.getSessionId());
    }

    @Test
    void testChatMessageConstructor() {
        ChatMessage message = new ChatMessage("Hello", true, "session123");

        assertNull(message.getId()); // 預設 id 應該是 null
        assertEquals("Hello", message.getContent());
        assertTrue(message.isUser());
        assertEquals("session123", message.getSessionId());
    }
}
