package ntou.cse.feelink_backend.chatroom.model;

import ntou.cse.feelink_backend.common.enums.AIPartner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

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
        assertNull(chatroom.getAiPartner());
    }

    @Test
    void testAllArgsConstructor() {
        Chatroom room = new Chatroom("chatroom123", "user456", "測試聊天室", AIPartner.MUSE);

        assertEquals("chatroom123", room.getChatroomId());
        assertEquals("user456", room.getUserId());
        assertEquals("測試聊天室", room.getTitle());
        assertEquals(AIPartner.MUSE, room.getAiPartner());
    }

    @Test
    void testSettersAndGetters() {
        chatroom.setChatroomId("chatroom789");
        chatroom.setUserId("user123");
        chatroom.setTitle("AI 聊天室");
        chatroom.setAiPartner(AIPartner.QIQI);

        assertEquals("chatroom789", chatroom.getChatroomId());
        assertEquals("user123", chatroom.getUserId());
        assertEquals("AI 聊天室", chatroom.getTitle());
        assertEquals(AIPartner.QIQI, chatroom.getAiPartner());
    }

    @Test
    void testNullValues() {
        chatroom.setChatroomId(null);
        chatroom.setUserId(null);
        chatroom.setTitle(null);
        chatroom.setAiPartner(null);

        assertNull(chatroom.getChatroomId());
        assertNull(chatroom.getUserId());
        assertNull(chatroom.getTitle());
        assertNull(chatroom.getAiPartner());
    }

    @Test
    void testEmptyStringValues() {
        chatroom.setChatroomId("");
        chatroom.setUserId("");
        chatroom.setTitle("");

        assertEquals("", chatroom.getChatroomId());
        assertEquals("", chatroom.getUserId());
        assertEquals("", chatroom.getTitle());
    }

    @Test
    void testSetAiPartnerMuse() {
        chatroom.setAiPartner(AIPartner.MUSE);
        assertEquals(AIPartner.MUSE, chatroom.getAiPartner());
    }

    @Test
    void testSetAiPartnerQiQi() {
        chatroom.setAiPartner(AIPartner.QIQI);
        assertEquals(AIPartner.QIQI, chatroom.getAiPartner());
    }

    @Test
    void testSetAiPartnerNuanNuan() {
        chatroom.setAiPartner(AIPartner.NUANNUAN);
        assertEquals(AIPartner.NUANNUAN, chatroom.getAiPartner());
    }

    @Test
    void testEqualsAndHashCode() {
        Chatroom room1 = new Chatroom("chatroom1", "user1", "Room 1", AIPartner.MUSE);
        Chatroom room2 = new Chatroom("chatroom1", "user1", "Room 1", AIPartner.MUSE);
        Chatroom room3 = new Chatroom("chatroom2", "user2", "Room 2", AIPartner.QIQI);

        assertEquals(room1, room2);
        assertNotEquals(room1, room3);
        assertEquals(room1.hashCode(), room2.hashCode());
    }

    @Test
    void testToString() {
        chatroom.setChatroomId("chatroom123");
        chatroom.setUserId("user456");
        chatroom.setTitle("測試標題");
        chatroom.setAiPartner(AIPartner.NUANNUAN);

        String result = chatroom.toString();
        assertNotNull(result);
        assertTrue(result.contains("chatroom123"));
        assertTrue(result.contains("user456"));
        assertTrue(result.contains("測試標題"));
    }
}
