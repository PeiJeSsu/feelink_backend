package ntou.cse.feelink_backend.chatroom.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;


class UserDTOTest {

    @Test
    @DisplayName("Should create UserDTO with no-args constructor")
    void testNoArgsConstructor() {
        // When
        UserDTO dto = new UserDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getEmail());
        assertNull(dto.getNickname());
        assertNull(dto.getAINickname());
    }

    @Test
    @DisplayName("Should create UserDTO with all-args constructor")
    void testAllArgsConstructor() {
        // Given
        String userId = "user-123";
        String email = "test@example.com";
        String nickname = "TestUser";
        String aiNickname = "AI-TestUser";

        // When
        UserDTO dto = new UserDTO(userId, email, nickname, aiNickname);

        // Then
        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
        assertEquals(email, dto.getEmail());
        assertEquals(nickname, dto.getNickname());
        assertEquals(aiNickname, dto.getAINickname());
    }

    @Test
    @DisplayName("Should set and get userId correctly")
    void testUserIdGetterSetter() {
        // Given
        UserDTO dto = new UserDTO();
        String userId = "test-user-id";

        // When
        dto.setUserId(userId);

        // Then
        assertEquals(userId, dto.getUserId());
    }

    @Test
    @DisplayName("Should set and get email correctly")
    void testEmailGetterSetter() {
        // Given
        UserDTO dto = new UserDTO();
        String email = "test@example.com";

        // When
        dto.setEmail(email);

        // Then
        assertEquals(email, dto.getEmail());
    }

    @Test
    @DisplayName("Should set and get nickname correctly")
    void testNicknameGetterSetter() {
        // Given
        UserDTO dto = new UserDTO();
        String nickname = "TestNickname";

        // When
        dto.setNickname(nickname);

        // Then
        assertEquals(nickname, dto.getNickname());
    }

    @Test
    @DisplayName("Should set and get AINickname correctly")
    void testAINicknameGetterSetter() {
        // Given
        UserDTO dto = new UserDTO();
        String aiNickname = "AI-TestNickname";

        // When
        dto.setAINickname(aiNickname);

        // Then
        assertEquals(aiNickname, dto.getAINickname());
    }

    @Test
    @DisplayName("Should handle null values in constructor")
    void testConstructorWithNullValues() {
        // When
        UserDTO dto = new UserDTO(null, null, null, null);

        // Then
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getEmail());
        assertNull(dto.getNickname());
        assertNull(dto.getAINickname());
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testSettersWithNullValues() {
        // Given
        UserDTO dto = new UserDTO();

        // When
        dto.setUserId(null);
        dto.setEmail(null);
        dto.setNickname(null);
        dto.setAINickname(null);

        // Then
        assertNull(dto.getUserId());
        assertNull(dto.getEmail());
        assertNull(dto.getNickname());
        assertNull(dto.getAINickname());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        // Given
        UserDTO dto = new UserDTO();

        // When
        dto.setUserId("");
        dto.setEmail("");
        dto.setNickname("");
        dto.setAINickname("");

        // Then
        assertEquals("", dto.getUserId());
        assertEquals("", dto.getEmail());
        assertEquals("", dto.getNickname());
        assertEquals("", dto.getAINickname());
    }

    @Test
    @DisplayName("Should handle special characters in strings")
    void testSpecialCharacters() {
        // Given
        UserDTO dto = new UserDTO();
        String specialChars = "test@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`";

        // When
        dto.setUserId(specialChars);
        dto.setEmail(specialChars);
        dto.setNickname(specialChars);
        dto.setAINickname(specialChars);

        // Then
        assertEquals(specialChars, dto.getUserId());
        assertEquals(specialChars, dto.getEmail());
        assertEquals(specialChars, dto.getNickname());
        assertEquals(specialChars, dto.getAINickname());
    }
}