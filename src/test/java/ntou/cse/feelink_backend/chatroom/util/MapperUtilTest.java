package ntou.cse.feelink_backend.chatroom.util;

import ntou.cse.feelink_backend.chatroom.dto.ChatMessageDTO;
import ntou.cse.feelink_backend.chatroom.dto.ChatroomDTO;
import ntou.cse.feelink_backend.chatroom.dto.UserDTO;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class MapperUtilTest {

    // ChatMessage Tests
    @Test
    @DisplayName("Should convert ChatMessage entity to DTO successfully")
    void testChatMessageToDTO() {
        // Given
        ChatMessage entity = new ChatMessage();
        entity.setMessageId("msg-123");
        entity.setChatroomId("room-456");
        entity.setContent("Hello World");
        entity.setIsUser(true);
        entity.setIsDrawingData(false);
        Date testDate = new Date();
        entity.setSentAt(testDate);

        // When
        ChatMessageDTO result = MapperUtil.toDTO(entity);

        // Then
        assertNotNull(result);
        assertEquals("msg-123", result.getMessageId());
        assertEquals("room-456", result.getChatroomId());
        assertEquals("Hello World", result.getContent());
        assertTrue(result.getIsUser());
        assertFalse(result.getIsDrawingData());
        assertEquals(testDate, result.getSentAt());
    }

    @Test
    @DisplayName("Should return null when ChatMessage entity is null")
    void testChatMessageToDTOWithNull() {
        // When
        ChatMessageDTO result = MapperUtil.toDTO((ChatMessage) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert ChatMessageDTO to entity successfully")
    void testChatMessageDTOToEntity() {
        // Given
        Date testDate = new Date();
        ChatMessageDTO dto = new ChatMessageDTO(
                "msg-789",
                "room-101",
                "Test message",
                false,
                true,
                testDate
        );

        // When
        ChatMessage result = MapperUtil.toEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals("msg-789", result.getMessageId());
        assertEquals("room-101", result.getChatroomId());
        assertEquals("Test message", result.getContent());
        assertFalse(result.getIsUser());
        assertTrue(result.getIsDrawingData());
        assertEquals(testDate, result.getSentAt());
    }

    @Test
    @DisplayName("Should return null when ChatMessageDTO is null")
    void testChatMessageEntityWithNullDTO() {
        // When
        ChatMessage result = MapperUtil.toEntity((ChatMessageDTO) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list of ChatMessage entities to DTOs")
    void testChatMessageListToDTO() {
        // Given
        ChatMessage entity1 = new ChatMessage();
        entity1.setMessageId("msg-1");
        entity1.setContent("Message 1");
        entity1.setIsUser(true);

        ChatMessage entity2 = new ChatMessage();
        entity2.setMessageId("msg-2");
        entity2.setContent("Message 2");
        entity2.setIsUser(false);

        List<ChatMessage> entities = Arrays.asList(entity1, entity2);

        // When
        List<ChatMessageDTO> result = MapperUtil.toChatMessageDTOList(entities);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("msg-1", result.get(0).getMessageId());
        assertEquals("Message 1", result.get(0).getContent());
        assertTrue(result.get(0).getIsUser());
        assertEquals("msg-2", result.get(1).getMessageId());
        assertEquals("Message 2", result.get(1).getContent());
        assertFalse(result.get(1).getIsUser());
    }

    @Test
    @DisplayName("Should handle empty list for ChatMessage entities")
    void testChatMessageEmptyListToDTO() {
        // Given
        List<ChatMessage> entities = new ArrayList<>();

        // When
        List<ChatMessageDTO> result = MapperUtil.toChatMessageDTOList(entities);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Chatroom Tests
    @Test
    @DisplayName("Should convert Chatroom entity to DTO successfully")
    void testChatroomToDTO() {
        // Given
        Chatroom entity = new Chatroom();
        entity.setChatroomId("room-123");
        entity.setUserId("user-456");
        entity.setTitle("Test Chatroom");

        // When
        ChatroomDTO result = MapperUtil.toDTO(entity);

        // Then
        assertNotNull(result);
        assertEquals("room-123", result.getChatroomId());
        assertEquals("user-456", result.getUserId());
        assertEquals("Test Chatroom", result.getTitle());
    }

    @Test
    @DisplayName("Should return null when Chatroom entity is null")
    void testChatroomToDTOWithNull() {
        // When
        ChatroomDTO result = MapperUtil.toDTO((Chatroom) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert ChatroomDTO to entity successfully")
    void testChatroomDTOToEntity() {
        // Given
        ChatroomDTO dto = new ChatroomDTO("room-789", "user-101", "Another Chatroom");

        // When
        Chatroom result = MapperUtil.toEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals("room-789", result.getChatroomId());
        assertEquals("user-101", result.getUserId());
        assertEquals("Another Chatroom", result.getTitle());
    }

    @Test
    @DisplayName("Should return null when ChatroomDTO is null")
    void testChatroomEntityWithNullDTO() {
        // When
        Chatroom result = MapperUtil.toEntity((ChatroomDTO) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list of Chatroom entities to DTOs")
    void testChatroomListToDTO() {
        // Given
        Chatroom entity1 = new Chatroom();
        entity1.setChatroomId("room-1");
        entity1.setUserId("user-1");
        entity1.setTitle("Chatroom 1");

        Chatroom entity2 = new Chatroom();
        entity2.setChatroomId("room-2");
        entity2.setUserId("user-2");
        entity2.setTitle("Chatroom 2");

        List<Chatroom> entities = Arrays.asList(entity1, entity2);

        // When
        List<ChatroomDTO> result = MapperUtil.toChatroomDTOList(entities);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("room-1", result.get(0).getChatroomId());
        assertEquals("user-1", result.get(0).getUserId());
        assertEquals("Chatroom 1", result.get(0).getTitle());
        assertEquals("room-2", result.get(1).getChatroomId());
        assertEquals("user-2", result.get(1).getUserId());
        assertEquals("Chatroom 2", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should handle empty list for Chatroom entities")
    void testChatroomEmptyListToDTO() {
        // Given
        List<Chatroom> entities = new ArrayList<>();

        // When
        List<ChatroomDTO> result = MapperUtil.toChatroomDTOList(entities);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // User Tests
    @Test
    @DisplayName("Should convert User entity to DTO successfully")
    void testUserToDTO() {
        // Given
        User entity = new User();
        entity.setUserId("user-123");
        entity.setEmail("test@example.com");
        entity.setNickname("TestUser");
        entity.setAINickname("AI-TestUser");

        // When
        UserDTO result = MapperUtil.toDTO(entity);

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("TestUser", result.getNickname());
        assertEquals("AI-TestUser", result.getAINickname());
    }

    @Test
    @DisplayName("Should return null when User entity is null")
    void testUserToDTOWithNull() {
        // When
        UserDTO result = MapperUtil.toDTO((User) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert UserDTO to entity successfully")
    void testUserDTOToEntity() {
        // Given
        UserDTO dto = new UserDTO("user-456", "another@example.com", "AnotherUser", "AI-AnotherUser");

        // When
        User result = MapperUtil.toEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals("user-456", result.getUserId());
        assertEquals("another@example.com", result.getEmail());
        assertEquals("AnotherUser", result.getNickname());
        assertEquals("AI-AnotherUser", result.getAINickname());
    }

    @Test
    @DisplayName("Should return null when UserDTO is null")
    void testUserEntityWithNullDTO() {
        // When
        User result = MapperUtil.toEntity((UserDTO) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list of User entities to DTOs")
    void testUserListToDTO() {
        // Given
        User entity1 = new User();
        entity1.setUserId("user-1");
        entity1.setEmail("user1@example.com");
        entity1.setNickname("User1");
        entity1.setAINickname("AI-User1");

        User entity2 = new User();
        entity2.setUserId("user-2");
        entity2.setEmail("user2@example.com");
        entity2.setNickname("User2");
        entity2.setAINickname("AI-User2");

        List<User> entities = Arrays.asList(entity1, entity2);

        // When
        List<UserDTO> result = MapperUtil.toUserDTOList(entities);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user-1", result.get(0).getUserId());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("User1", result.get(0).getNickname());
        assertEquals("AI-User1", result.get(0).getAINickname());
        assertEquals("user-2", result.get(1).getUserId());
        assertEquals("user2@example.com", result.get(1).getEmail());
        assertEquals("User2", result.get(1).getNickname());
        assertEquals("AI-User2", result.get(1).getAINickname());
    }

    @Test
    @DisplayName("Should handle empty list for User entities")
    void testUserEmptyListToDTO() {
        // Given
        List<User> entities = new ArrayList<>();

        // When
        List<UserDTO> result = MapperUtil.toUserDTOList(entities);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Edge Cases and Integration Tests
    @Test
    @DisplayName("Should handle ChatMessage with null boolean fields")
    void testChatMessageWithNullBooleans() {
        // Given
        ChatMessage entity = new ChatMessage();
        entity.setMessageId("msg-test");
        entity.setChatroomId("room-test");
        entity.setContent("Test content");
        entity.setIsUser(null);
        entity.setIsDrawingData(null);
        entity.setSentAt(new Date());

        // When
        ChatMessageDTO result = MapperUtil.toDTO(entity);

        // Then
        assertNotNull(result);
        assertEquals("msg-test", result.getMessageId());
        assertEquals("room-test", result.getChatroomId());
        assertEquals("Test content", result.getContent());
        assertNull(result.getIsUser());
        assertNull(result.getIsDrawingData());
    }

    @Test
    @DisplayName("Should handle entities with null or empty string fields")
    void testEntitiesWithNullFields() {
        // Given
        User userEntity = new User();
        userEntity.setUserId(null);
        userEntity.setEmail("");
        userEntity.setNickname(null);
        userEntity.setAINickname("");

        Chatroom chatroomEntity = new Chatroom();
        chatroomEntity.setChatroomId("");
        chatroomEntity.setUserId(null);
        chatroomEntity.setTitle("");

        // When
        UserDTO userResult = MapperUtil.toDTO(userEntity);
        ChatroomDTO chatroomResult = MapperUtil.toDTO(chatroomEntity);

        // Then
        assertNotNull(userResult);
        assertNull(userResult.getUserId());
        assertEquals("", userResult.getEmail());
        assertNull(userResult.getNickname());
        assertEquals("", userResult.getAINickname());

        assertNotNull(chatroomResult);
        assertEquals("", chatroomResult.getChatroomId());
        assertNull(chatroomResult.getUserId());
        assertEquals("", chatroomResult.getTitle());
    }

    @Test
    @DisplayName("Should handle list with null elements")
    void testListWithNullElements() {
        // Given
        ChatMessage validEntity = new ChatMessage();
        validEntity.setMessageId("valid-msg");
        validEntity.setContent("Valid content");

        List<ChatMessage> entitiesWithNull = Arrays.asList(validEntity, null, validEntity);

        // When
        List<ChatMessageDTO> result = MapperUtil.toChatMessageDTOList(entitiesWithNull);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertNotNull(result.get(0));
        assertEquals("valid-msg", result.get(0).getMessageId());
        assertNull(result.get(1)); // null entity should result in null DTO
        assertNotNull(result.get(2));
        assertEquals("valid-msg", result.get(2).getMessageId());
    }
}