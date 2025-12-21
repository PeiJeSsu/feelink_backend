package ntou.cse.feelink_backend.chatroom.util;

import ntou.cse.feelink_backend.chatroom.dto.ChatMessageDTO;
import ntou.cse.feelink_backend.chatroom.dto.ChatroomDTO;
import ntou.cse.feelink_backend.chatroom.dto.UserDTO;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.model.User;
import ntou.cse.feelink_backend.common.enums.AIPartner;
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
        ChatMessage entity = new ChatMessage();
        entity.setMessageId("msg-123");
        entity.setChatroomId("room-456");
        entity.setContent("Hello World");
        entity.setIsUser(true);
        entity.setIsDrawingData(false);
        Date testDate = new Date();
        entity.setSentAt(testDate);

        ChatMessageDTO result = MapperUtil.toDTO(entity);

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
        ChatMessageDTO result = MapperUtil.toDTO((ChatMessage) null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert ChatMessageDTO to entity successfully")
    void testChatMessageDTOToEntity() {
        Date testDate = new Date();
        ChatMessageDTO dto = new ChatMessageDTO(
                "msg-789",
                "room-101",
                "Test message",
                false,
                true,
                testDate
        );

        ChatMessage result = MapperUtil.toEntity(dto);

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
        ChatMessage result = MapperUtil.toEntity((ChatMessageDTO) null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list of ChatMessage entities to DTOs")
    void testChatMessageListToDTO() {
        ChatMessage entity1 = new ChatMessage();
        entity1.setMessageId("msg-1");
        entity1.setContent("Message 1");
        entity1.setIsUser(true);

        ChatMessage entity2 = new ChatMessage();
        entity2.setMessageId("msg-2");
        entity2.setContent("Message 2");
        entity2.setIsUser(false);

        List<ChatMessage> entities = Arrays.asList(entity1, entity2);
        List<ChatMessageDTO> result = MapperUtil.toChatMessageDTOList(entities);

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
        List<ChatMessage> entities = new ArrayList<>();
        List<ChatMessageDTO> result = MapperUtil.toChatMessageDTOList(entities);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Chatroom Tests
    @Test
    @DisplayName("Should convert Chatroom entity to DTO with AIPartner")
    void testChatroomToDTO() {
        Chatroom entity = new Chatroom();
        entity.setChatroomId("room-123");
        entity.setUserId("user-456");
        entity.setTitle("Test Chatroom");
        entity.setAiPartner(AIPartner.MUSE);

        ChatroomDTO result = MapperUtil.toDTO(entity);

        assertNotNull(result);
        assertEquals("room-123", result.getChatroomId());
        assertEquals("user-456", result.getUserId());
        assertEquals("Test Chatroom", result.getTitle());
        assertEquals(AIPartner.MUSE, result.getAiPartner());
    }

    @Test
    @DisplayName("Should return null when Chatroom entity is null")
    void testChatroomToDTOWithNull() {
        ChatroomDTO result = MapperUtil.toDTO((Chatroom) null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert ChatroomDTO to entity with AIPartner")
    void testChatroomDTOToEntity() {
        ChatroomDTO dto = new ChatroomDTO("room-789", "user-101", "Another Chatroom", AIPartner.QIQI);

        Chatroom result = MapperUtil.toEntity(dto);

        assertNotNull(result);
        assertEquals("room-789", result.getChatroomId());
        assertEquals("user-101", result.getUserId());
        assertEquals("Another Chatroom", result.getTitle());
        assertEquals(AIPartner.QIQI, result.getAiPartner());
    }

    @Test
    @DisplayName("Should return null when ChatroomDTO is null")
    void testChatroomEntityWithNullDTO() {
        Chatroom result = MapperUtil.toEntity((ChatroomDTO) null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list of Chatroom entities to DTOs")
    void testChatroomListToDTO() {
        Chatroom entity1 = new Chatroom();
        entity1.setChatroomId("room-1");
        entity1.setUserId("user-1");
        entity1.setTitle("Chatroom 1");
        entity1.setAiPartner(AIPartner.MUSE);

        Chatroom entity2 = new Chatroom();
        entity2.setChatroomId("room-2");
        entity2.setUserId("user-2");
        entity2.setTitle("Chatroom 2");
        entity2.setAiPartner(AIPartner.NUANNUAN);

        List<Chatroom> entities = Arrays.asList(entity1, entity2);
        List<ChatroomDTO> result = MapperUtil.toChatroomDTOList(entities);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("room-1", result.get(0).getChatroomId());
        assertEquals(AIPartner.MUSE, result.get(0).getAiPartner());
        assertEquals("room-2", result.get(1).getChatroomId());
        assertEquals(AIPartner.NUANNUAN, result.get(1).getAiPartner());
    }

    @Test
    @DisplayName("Should handle empty list for Chatroom entities")
    void testChatroomEmptyListToDTO() {
        List<Chatroom> entities = new ArrayList<>();
        List<ChatroomDTO> result = MapperUtil.toChatroomDTOList(entities);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // User Tests
    @Test
    @DisplayName("Should convert User entity to DTO with PreferredAIPartner")
    void testUserToDTO() {
        User entity = new User();
        entity.setUserId("user-123");
        entity.setEmail("test@example.com");
        entity.setNickname("TestUser");
        entity.setAINickname("AI-TestUser");
        entity.setPreferredAIPartner(AIPartner.MUSE);

        UserDTO result = MapperUtil.toDTO(entity);

        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("TestUser", result.getNickname());
        assertEquals("AI-TestUser", result.getAINickname());
        assertEquals(AIPartner.MUSE, result.getPreferredAIPartner());
    }

    @Test
    @DisplayName("Should return null when User entity is null")
    void testUserToDTOWithNull() {
        UserDTO result = MapperUtil.toDTO((User) null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert UserDTO to entity with PreferredAIPartner")
    void testUserDTOToEntity() {
        UserDTO dto = new UserDTO("user-456", "another@example.com", "AnotherUser", "AI-AnotherUser", AIPartner.QIQI);

        User result = MapperUtil.toEntity(dto);

        assertNotNull(result);
        assertEquals("user-456", result.getUserId());
        assertEquals("another@example.com", result.getEmail());
        assertEquals("AnotherUser", result.getNickname());
        assertEquals("AI-AnotherUser", result.getAINickname());
        assertEquals(AIPartner.QIQI, result.getPreferredAIPartner());
    }

    @Test
    @DisplayName("Should return null when UserDTO is null")
    void testUserEntityWithNullDTO() {
        User result = MapperUtil.toEntity((UserDTO) null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list of User entities to DTOs")
    void testUserListToDTO() {
        User entity1 = new User();
        entity1.setUserId("user-1");
        entity1.setEmail("user1@example.com");
        entity1.setNickname("User1");
        entity1.setAINickname("AI-User1");
        entity1.setPreferredAIPartner(AIPartner.MUSE);

        User entity2 = new User();
        entity2.setUserId("user-2");
        entity2.setEmail("user2@example.com");
        entity2.setNickname("User2");
        entity2.setAINickname("AI-User2");
        entity2.setPreferredAIPartner(AIPartner.NUANNUAN);

        List<User> entities = Arrays.asList(entity1, entity2);
        List<UserDTO> result = MapperUtil.toUserDTOList(entities);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user-1", result.get(0).getUserId());
        assertEquals(AIPartner.MUSE, result.get(0).getPreferredAIPartner());
        assertEquals("user-2", result.get(1).getUserId());
        assertEquals(AIPartner.NUANNUAN, result.get(1).getPreferredAIPartner());
    }

    @Test
    @DisplayName("Should handle empty list for User entities")
    void testUserEmptyListToDTO() {
        List<User> entities = new ArrayList<>();
        List<UserDTO> result = MapperUtil.toUserDTOList(entities);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // AIPartner Mapping Tests
    @Test
    @DisplayName("Should map creative personality to MUSE")
    void testMapCreativeToMuse() {
        AIPartner result = MapperUtil.mapPersonalityToAIPartner("creative");
        assertEquals(AIPartner.MUSE, result);
    }

    @Test
    @DisplayName("Should map curious personality to QIQI")
    void testMapCuriousToQiQi() {
        AIPartner result = MapperUtil.mapPersonalityToAIPartner("curious");
        assertEquals(AIPartner.QIQI, result);
    }

    @Test
    @DisplayName("Should map warm personality to NUANNUAN")
    void testMapWarmToNuanNuan() {
        AIPartner result = MapperUtil.mapPersonalityToAIPartner("warm");
        assertEquals(AIPartner.NUANNUAN, result);
    }

    @Test
    @DisplayName("Should be case insensitive for personality mapping")
    void testMapPersonalityCaseInsensitive() {
        assertEquals(AIPartner.MUSE, MapperUtil.mapPersonalityToAIPartner("CREATIVE"));
        assertEquals(AIPartner.QIQI, MapperUtil.mapPersonalityToAIPartner("Curious"));
        assertEquals(AIPartner.NUANNUAN, MapperUtil.mapPersonalityToAIPartner("WARM"));
    }

    @Test
    @DisplayName("Should return null when personality is null")
    void testMapPersonalityWithNull() {
        AIPartner result = MapperUtil.mapPersonalityToAIPartner(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should throw exception for unknown personality")
    void testMapUnknownPersonality() {
        assertThrows(IllegalArgumentException.class, () -> {
            MapperUtil.mapPersonalityToAIPartner("unknown");
        });
    }

    @Test
    @DisplayName("Should map MUSE to creative personality")
    void testMapMuseToCreative() {
        String result = MapperUtil.mapAIPartnerToPersonality(AIPartner.MUSE);
        assertEquals("creative", result);
    }

    @Test
    @DisplayName("Should map QIQI to curious personality")
    void testMapQiQiToCurious() {
        String result = MapperUtil.mapAIPartnerToPersonality(AIPartner.QIQI);
        assertEquals("curious", result);
    }

    @Test
    @DisplayName("Should map NUANNUAN to warm personality")
    void testMapNuanNuanToWarm() {
        String result = MapperUtil.mapAIPartnerToPersonality(AIPartner.NUANNUAN);
        assertEquals("warm", result);
    }

    @Test
    @DisplayName("Should return null when AIPartner is null")
    void testMapAIPartnerWithNull() {
        String result = MapperUtil.mapAIPartnerToPersonality(null);
        assertNull(result);
    }

    // Edge Cases
    @Test
    @DisplayName("Should handle ChatMessage with null boolean fields")
    void testChatMessageWithNullBooleans() {
        ChatMessage entity = new ChatMessage();
        entity.setMessageId("msg-test");
        entity.setChatroomId("room-test");
        entity.setContent("Test content");
        entity.setIsUser(null);
        entity.setIsDrawingData(null);
        entity.setSentAt(new Date());

        ChatMessageDTO result = MapperUtil.toDTO(entity);

        assertNotNull(result);
        assertEquals("msg-test", result.getMessageId());
        assertNull(result.getIsUser());
        assertNull(result.getIsDrawingData());
    }

    @Test
    @DisplayName("Should handle entities with null AIPartner")
    void testEntitiesWithNullAIPartner() {
        User userEntity = new User();
        userEntity.setUserId("user-1");
        userEntity.setEmail("test@test.com");
        userEntity.setPreferredAIPartner(null);

        Chatroom chatroomEntity = new Chatroom();
        chatroomEntity.setChatroomId("room-1");
        chatroomEntity.setAiPartner(null);

        UserDTO userResult = MapperUtil.toDTO(userEntity);
        ChatroomDTO chatroomResult = MapperUtil.toDTO(chatroomEntity);

        assertNotNull(userResult);
        assertNull(userResult.getPreferredAIPartner());
        assertNotNull(chatroomResult);
        assertNull(chatroomResult.getAiPartner());
    }

    @Test
    @DisplayName("Should handle list with null elements")
    void testListWithNullElements() {
        ChatMessage validEntity = new ChatMessage();
        validEntity.setMessageId("valid-msg");
        validEntity.setContent("Valid content");

        List<ChatMessage> entitiesWithNull = Arrays.asList(validEntity, null, validEntity);
        List<ChatMessageDTO> result = MapperUtil.toChatMessageDTOList(entitiesWithNull);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
        assertNotNull(result.get(2));
    }
}
