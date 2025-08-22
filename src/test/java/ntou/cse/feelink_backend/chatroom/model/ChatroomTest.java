package ntou.cse.feelink_backend.chatroom.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Chatroom model
 */
class ChatroomTest {

    private Chatroom chatroom;

    @BeforeEach
    void setUp() {
        chatroom = new Chatroom();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(chatroom);
        assertNull(chatroom.getChatroomId());
        assertNull(chatroom.getUserId());
        assertNull(chatroom.getTitle());
    }

    @Test
    void testAllArgsConstructor() {
        Chatroom room = new Chatroom("chat123", "user456", "My Chatroom");

        assertEquals("chat123", room.getChatroomId());
        assertEquals("user456", room.getUserId());
        assertEquals("My Chatroom", room.getTitle());
    }

    @Test
    void testSettersAndGetters() {
        chatroom.setChatroomId("newChatroom");
        chatroom.setUserId("newUser");
        chatroom.setTitle("New Title");

        assertEquals("newChatroom", chatroom.getChatroomId());
        assertEquals("newUser", chatroom.getUserId());
        assertEquals("New Title", chatroom.getTitle());
    }

    @Test
    void testEmptyValues() {
        chatroom.setChatroomId("");
        chatroom.setUserId("");
        chatroom.setTitle("");

        assertEquals("", chatroom.getChatroomId());
        assertEquals("", chatroom.getUserId());
        assertEquals("", chatroom.getTitle());
    }

    @Test
    void testNullValues() {
        chatroom.setChatroomId(null);
        chatroom.setUserId(null);
        chatroom.setTitle(null);

        assertNull(chatroom.getChatroomId());
        assertNull(chatroom.getUserId());
        assertNull(chatroom.getTitle());
    }

    @Test
    void testLongTitles() {
        String longTitle = "This is a very long chatroom title that might be used for testing purposes and contains many characters to test the boundary conditions";
        chatroom.setTitle(longTitle);

        assertEquals(longTitle, chatroom.getTitle());
    }

    @Test
    void testSpecialCharactersInTitle() {
        String specialTitle = "Chat@#$%^&*()_+-=[]{}|;':\",./<>?";
        chatroom.setTitle(specialTitle);

        assertEquals(specialTitle, chatroom.getTitle());
    }

    @Test
    void testUnicodeCharactersInTitle() {
        String unicodeTitle = "聊天室標題 ";
        chatroom.setTitle(unicodeTitle);

        assertEquals(unicodeTitle, chatroom.getTitle());
    }

    @Test
    void testChatroomCreationScenario() {
        // Simulate creating a new chatroom
        chatroom.setChatroomId("chatroom_" + System.currentTimeMillis());
        chatroom.setUserId("user_123");
        chatroom.setTitle("AI Drawing Chat");

        assertNotNull(chatroom.getChatroomId());
        assertTrue(chatroom.getChatroomId().startsWith("chatroom_"));
        assertEquals("user_123", chatroom.getUserId());
        assertEquals("AI Drawing Chat", chatroom.getTitle());
    }

    @Test
    void testEquality() {
        Chatroom chatroom1 = new Chatroom("chat1", "user1", "Title1");
        Chatroom chatroom2 = new Chatroom("chat1", "user1", "Title1");
        Chatroom chatroom3 = new Chatroom("chat2", "user1", "Title1");

        // Note: Since the class uses Lombok @Data, it should have equals method
        assertEquals(chatroom1, chatroom2);
        assertNotEquals(chatroom1, chatroom3);
    }
}