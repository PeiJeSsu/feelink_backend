package ntou.cse.feelink_backend.chatroom.dto;

import ntou.cse.feelink_backend.common.enums.AIPartner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;


class ChatroomDTOTest {

    @Test
    @DisplayName("Should create ChatroomDTO with no-args constructor")
    void testNoArgsConstructor() {
        // When
        ChatroomDTO dto = new ChatroomDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getChatroomId());
        assertNull(dto.getUserId());
        assertNull(dto.getTitle());
    }

    @Test
    @DisplayName("Should create ChatroomDTO with all-args constructor")
    void testAllArgsConstructor() {
        // Given
        String chatroomId = "room-123";
        String userId = "user-456";
        String title = "Test Chatroom";
        AIPartner aipartner = AIPartner.fromName("muse");
        // When
        ChatroomDTO dto = new ChatroomDTO(chatroomId, userId, title, aipartner);

        // Then
        assertNotNull(dto);
        assertEquals(chatroomId, dto.getChatroomId());
        assertEquals(userId, dto.getUserId());
        assertEquals(title, dto.getTitle());
    }

    @Test
    @DisplayName("Should set and get chatroomId correctly")
    void testChatroomIdGetterSetter() {
        // Given
        ChatroomDTO dto = new ChatroomDTO();
        String chatroomId = "test-room-id";

        // When
        dto.setChatroomId(chatroomId);

        // Then
        assertEquals(chatroomId, dto.getChatroomId());
    }

    @Test
    @DisplayName("Should set and get userId correctly")
    void testUserIdGetterSetter() {
        // Given
        ChatroomDTO dto = new ChatroomDTO();
        String userId = "test-user-id";

        // When
        dto.setUserId(userId);

        // Then
        assertEquals(userId, dto.getUserId());
    }

    @Test
    @DisplayName("Should set and get title correctly")
    void testTitleGetterSetter() {
        // Given
        ChatroomDTO dto = new ChatroomDTO();
        String title = "Test Room Title";

        // When
        dto.setTitle(title);

        // Then
        assertEquals(title, dto.getTitle());
    }

    @Test
    @DisplayName("Should handle null values in constructor")
    void testConstructorWithNullValues() {
        // When
        ChatroomDTO dto = new ChatroomDTO(null, null, null,null);

        // Then
        assertNotNull(dto);
        assertNull(dto.getChatroomId());
        assertNull(dto.getUserId());
        assertNull(dto.getTitle());
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testSettersWithNullValues() {
        // Given
        ChatroomDTO dto = new ChatroomDTO();

        // When
        dto.setChatroomId(null);
        dto.setUserId(null);
        dto.setTitle(null);

        // Then
        assertNull(dto.getChatroomId());
        assertNull(dto.getUserId());
        assertNull(dto.getTitle());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        // Given
        ChatroomDTO dto = new ChatroomDTO();

        // When
        dto.setChatroomId("");
        dto.setUserId("");
        dto.setTitle("");

        // Then
        assertEquals("", dto.getChatroomId());
        assertEquals("", dto.getUserId());
        assertEquals("", dto.getTitle());
    }
}
