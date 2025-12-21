package ntou.cse.feelink_backend.chatroom.model;

import ntou.cse.feelink_backend.common.enums.AIPartner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getUserId());
        assertNull(user.getNickname());
        assertNull(user.getAINickname());
        assertNull(user.getEmail());
        assertNull(user.getPreferredAIPartner());
    }

    @Test
    void testAllArgsConstructor() {
        User testUser = new User("user123", "測試用戶", "小助手", "test@example.com", AIPartner.MUSE);

        assertEquals("user123", testUser.getUserId());
        assertEquals("測試用戶", testUser.getNickname());
        assertEquals("小助手", testUser.getAINickname());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals(AIPartner.MUSE, testUser.getPreferredAIPartner());
    }

    @Test
    void testSettersAndGetters() {
        user.setUserId("user456");
        user.setNickname("海大學生");
        user.setAINickname("AI 夥伴");
        user.setEmail("student@ntou.edu.tw");
        user.setPreferredAIPartner(AIPartner.QIQI);

        assertEquals("user456", user.getUserId());
        assertEquals("海大學生", user.getNickname());
        assertEquals("AI 夥伴", user.getAINickname());
        assertEquals("student@ntou.edu.tw", user.getEmail());
        assertEquals(AIPartner.QIQI, user.getPreferredAIPartner());
    }

    @Test
    void testNullValues() {
        user.setUserId(null);
        user.setNickname(null);
        user.setAINickname(null);
        user.setEmail(null);
        user.setPreferredAIPartner(null);

        assertNull(user.getUserId());
        assertNull(user.getNickname());
        assertNull(user.getAINickname());
        assertNull(user.getEmail());
        assertNull(user.getPreferredAIPartner());
    }

    @Test
    void testEmptyStringValues() {
        user.setUserId("");
        user.setNickname("");
        user.setAINickname("");
        user.setEmail("");

        assertEquals("", user.getUserId());
        assertEquals("", user.getNickname());
        assertEquals("", user.getAINickname());
        assertEquals("", user.getEmail());
    }

    @Test
    void testSetPreferredAIPartnerMuse() {
        user.setPreferredAIPartner(AIPartner.MUSE);
        assertEquals(AIPartner.MUSE, user.getPreferredAIPartner());
    }

    @Test
    void testSetPreferredAIPartnerQiQi() {
        user.setPreferredAIPartner(AIPartner.QIQI);
        assertEquals(AIPartner.QIQI, user.getPreferredAIPartner());
    }

    @Test
    void testSetPreferredAIPartnerNuanNuan() {
        user.setPreferredAIPartner(AIPartner.NUANNUAN);
        assertEquals(AIPartner.NUANNUAN, user.getPreferredAIPartner());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User("user1", "Nick1", "AI1", "email1@test.com", AIPartner.MUSE);
        User user2 = new User("user1", "Nick1", "AI1", "email1@test.com", AIPartner.MUSE);
        User user3 = new User("user2", "Nick2", "AI2", "email2@test.com", AIPartner.QIQI);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        user.setUserId("user123");
        user.setNickname("測試用戶");
        user.setAINickname("AI 助手");
        user.setEmail("test@ntou.edu.tw");
        user.setPreferredAIPartner(AIPartner.NUANNUAN);

        String result = user.toString();
        assertNotNull(result);
        assertTrue(result.contains("user123"));
        assertTrue(result.contains("測試用戶"));
        assertTrue(result.contains("test@ntou.edu.tw"));
    }
}
