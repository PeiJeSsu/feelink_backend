package ntou.cse.feelink_backend.chatroom.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

/**
 * Unit tests for ChatMessage model
 */
class ChatMessageTest {

    private ChatMessage chatMessage;
    private Date testDate;

    @BeforeEach
    void setUp() {
        chatMessage = new ChatMessage();
        testDate = new Date();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(chatMessage);
        assertNull(chatMessage.getMessageId());
        assertNull(chatMessage.getChatroomId());
        assertNull(chatMessage.getIsUser());
        assertNull(chatMessage.getIsDrawingData());
        assertNull(chatMessage.getContent());
        assertNull(chatMessage.getSentAt());
    }

    @Test
    void testAllArgsConstructor() {
        ChatMessage message = new ChatMessage("msg123", "chat123", true, false, "Hello", testDate);

        assertEquals("msg123", message.getMessageId());
        assertEquals("chat123", message.getChatroomId());
        assertTrue(message.getIsUser());
        assertFalse(message.getIsDrawingData());
        assertEquals("Hello", message.getContent());
        assertEquals(testDate, message.getSentAt());
    }

    @Test
    void testSettersAndGetters() {
        chatMessage.setMessageId("msg456");
        chatMessage.setChatroomId("chat456");
        chatMessage.setIsUser(false);
        chatMessage.setIsDrawingData(true);
        chatMessage.setContent("AI response");
        chatMessage.setSentAt(testDate);

        assertEquals("msg456", chatMessage.getMessageId());
        assertEquals("chat456", chatMessage.getChatroomId());
        assertFalse(chatMessage.getIsUser());
        assertTrue(chatMessage.getIsDrawingData());
        assertEquals("AI response", chatMessage.getContent());
        assertEquals(testDate, chatMessage.getSentAt());
    }

    @Test
    void testUserTextMessageScenario() {
        chatMessage.setMessageId("user_msg_1");
        chatMessage.setChatroomId("chatroom_1");
        chatMessage.setIsUser(true);
        chatMessage.setIsDrawingData(false);
        chatMessage.setContent("User's text message");
        chatMessage.setSentAt(testDate);

        assertTrue(chatMessage.getIsUser());
        assertFalse(chatMessage.getIsDrawingData());
        assertEquals("User's text message", chatMessage.getContent());
    }

    @Test
    void testAIDrawingMessageScenario() {
        chatMessage.setMessageId("ai_drawing_1");
        chatMessage.setChatroomId("chatroom_1");
        chatMessage.setIsUser(false);
        chatMessage.setIsDrawingData(true);
        chatMessage.setContent("base64_drawing_data");
        chatMessage.setSentAt(testDate);

        assertFalse(chatMessage.getIsUser());
        assertTrue(chatMessage.getIsDrawingData());
        assertEquals("base64_drawing_data", chatMessage.getContent());
    }

    @Test
    void testUserDrawingMessageScenario() {
        chatMessage.setMessageId("user_drawing_1");
        chatMessage.setChatroomId("chatroom_1");
        chatMessage.setIsUser(true);
        chatMessage.setIsDrawingData(true);
        chatMessage.setContent("user_drawing_canvas_data");
        chatMessage.setSentAt(testDate);

        assertTrue(chatMessage.getIsUser());
        assertTrue(chatMessage.getIsDrawingData());
        assertEquals("user_drawing_canvas_data", chatMessage.getContent());
    }

    @Test
    void testAITextMessageScenario() {
        chatMessage.setMessageId("ai_text_1");
        chatMessage.setChatroomId("chatroom_1");
        chatMessage.setIsUser(false);
        chatMessage.setIsDrawingData(false);
        chatMessage.setContent("AI text response");
        chatMessage.setSentAt(testDate);

        assertFalse(chatMessage.getIsUser());
        assertFalse(chatMessage.getIsDrawingData());
        assertEquals("AI text response", chatMessage.getContent());
    }

    @Test
    void testNullValues() {
        chatMessage.setMessageId(null);
        chatMessage.setChatroomId(null);
        chatMessage.setIsUser(null);
        chatMessage.setIsDrawingData(null);
        chatMessage.setContent(null);
        chatMessage.setSentAt(null);

        assertNull(chatMessage.getMessageId());
        assertNull(chatMessage.getChatroomId());
        assertNull(chatMessage.getIsUser());
        assertNull(chatMessage.getIsDrawingData());
        assertNull(chatMessage.getContent());
        assertNull(chatMessage.getSentAt());
    }

    @Test
    void testEmptyStrings() {
        chatMessage.setMessageId("");
        chatMessage.setChatroomId("");
        chatMessage.setContent("");

        assertEquals("", chatMessage.getMessageId());
        assertEquals("", chatMessage.getChatroomId());
        assertEquals("", chatMessage.getContent());
    }

    @Test
    void testDateHandling() {
        Date now = new Date();
        Date futureDate = new Date(now.getTime() + 86400000); // +1 day
        Date pastDate = new Date(now.getTime() - 86400000); // -1 day

        chatMessage.setSentAt(futureDate);
        assertEquals(futureDate, chatMessage.getSentAt());

        chatMessage.setSentAt(pastDate);
        assertEquals(pastDate, chatMessage.getSentAt());
    }
}