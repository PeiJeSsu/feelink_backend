package ntou.cse.feelink_backend.ChatRoom.Model;

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
}
