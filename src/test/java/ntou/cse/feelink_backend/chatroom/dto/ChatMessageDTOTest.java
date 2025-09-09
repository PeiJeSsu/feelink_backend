package ntou.cse.feelink_backend.chatroom.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class ChatMessageDTOTest {

    @Test
    @DisplayName("Should create ChatMessageDTO with no-args constructor")
    void testNoArgsConstructor() {
        // When
        ChatMessageDTO dto = new ChatMessageDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getMessageId());
        assertNull(dto.getChatroomId());
        assertNull(dto.getContent());
        assertNull(dto.getIsUser());
        assertNull(dto.getIsDrawingData());
        assertNull(dto.getSentAt());
    }

    @Test
    @DisplayName("Should create ChatMessageDTO with all-args constructor")
    void testAllArgsConstructor() {
        // Given
        String messageId = "msg-123";
        String chatroomId = "room-456";
        String content = "Hello World";
        Boolean isUser = true;
        Boolean isDrawingData = false;
        Date sentAt = new Date();

        // When
        ChatMessageDTO dto = new ChatMessageDTO(messageId, chatroomId, content, isUser, isDrawingData, sentAt);

        // Then
        assertNotNull(dto);
        assertEquals(messageId, dto.getMessageId());
        assertEquals(chatroomId, dto.getChatroomId());
        assertEquals(content, dto.getContent());
        assertEquals(isUser, dto.getIsUser());
        assertEquals(isDrawingData, dto.getIsDrawingData());
        assertEquals(sentAt, dto.getSentAt());
    }

    @Test
    @DisplayName("Should set and get messageId correctly")
    void testMessageIdGetterSetter() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();
        String messageId = "test-msg-id";

        // When
        dto.setMessageId(messageId);

        // Then
        assertEquals(messageId, dto.getMessageId());
    }

    @Test
    @DisplayName("Should set and get chatroomId correctly")
    void testChatroomIdGetterSetter() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();
        String chatroomId = "test-room-id";

        // When
        dto.setChatroomId(chatroomId);

        // Then
        assertEquals(chatroomId, dto.getChatroomId());
    }

    @Test
    @DisplayName("Should set and get content correctly")
    void testContentGetterSetter() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();
        String content = "Test message content";

        // When
        dto.setContent(content);

        // Then
        assertEquals(content, dto.getContent());
    }

    @Test
    @DisplayName("Should set and get isUser correctly")
    void testIsUserGetterSetter() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();
        Boolean isUser = true;

        // When
        dto.setIsUser(isUser);

        // Then
        assertEquals(isUser, dto.getIsUser());
    }

    @Test
    @DisplayName("Should set and get isDrawingData correctly")
    void testIsDrawingDataGetterSetter() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();
        Boolean isDrawingData = false;

        // When
        dto.setIsDrawingData(isDrawingData);

        // Then
        assertEquals(isDrawingData, dto.getIsDrawingData());
    }

    @Test
    @DisplayName("Should set and get sentAt correctly")
    void testSentAtGetterSetter() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();
        Date sentAt = new Date();

        // When
        dto.setSentAt(sentAt);

        // Then
        assertEquals(sentAt, dto.getSentAt());
    }

    @Test
    @DisplayName("Should handle null values in constructor")
    void testConstructorWithNullValues() {
        // When
        ChatMessageDTO dto = new ChatMessageDTO(null, null, null, null, null, null);

        // Then
        assertNotNull(dto);
        assertNull(dto.getMessageId());
        assertNull(dto.getChatroomId());
        assertNull(dto.getContent());
        assertNull(dto.getIsUser());
        assertNull(dto.getIsDrawingData());
        assertNull(dto.getSentAt());
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testSettersWithNullValues() {
        // Given
        ChatMessageDTO dto = new ChatMessageDTO();

        // When
        dto.setMessageId(null);
        dto.setChatroomId(null);
        dto.setContent(null);
        dto.setIsUser(null);
        dto.setIsDrawingData(null);
        dto.setSentAt(null);

        // Then
        assertNull(dto.getMessageId());
        assertNull(dto.getChatroomId());
        assertNull(dto.getContent());
        assertNull(dto.getIsUser());
        assertNull(dto.getIsDrawingData());
        assertNull(dto.getSentAt());
    }
}