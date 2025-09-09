package ntou.cse.feelink_backend.chatroom.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        User user = new User();
        user.setUserId("123");
        user.setNickname("Tom");
        user.setAINickname("AI-Tom");
        user.setEmail("tom@example.com");

        assertEquals("123", user.getUserId());
        assertEquals("Tom", user.getNickname());
        assertEquals("AI-Tom", user.getAINickname());
        assertEquals("tom@example.com", user.getEmail());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        User user = new User("456", "Jerry", "AI-Jerry", "jerry@example.com");

        assertEquals("456", user.getUserId());
        assertEquals("Jerry", user.getNickname());
        assertEquals("AI-Jerry", user.getAINickname());
        assertEquals("jerry@example.com", user.getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User("1", "Nick", "AI-Nick", "nick@example.com");
        User user2 = new User("1", "Nick", "AI-Nick", "nick@example.com");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        User user = new User("789", "Alice", "AI-Alice", "alice@example.com");
        String toStringResult = user.toString();

        assertTrue(toStringResult.contains("Alice"));
        assertTrue(toStringResult.contains("AI-Alice"));
        assertTrue(toStringResult.contains("alice@example.com"));
    }
}
