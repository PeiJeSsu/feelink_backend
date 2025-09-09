package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.repository.ChatroomRepository;
import ntou.cse.feelink_backend.chatroom.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatroomServiceTest {

    @Mock
    private ChatroomRepository chatroomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatroomService chatroomService;

    private Chatroom testChatroom;
    private List<Chatroom> testChatrooms;

    @BeforeEach
    void setUp() {
        // 創建測試用的聊天室數據
        testChatroom = createTestChatroom("room1", "user1", "Test Room");

        Chatroom chatroom2 = createTestChatroom("room2", "user1", "Test Room 2");
        Chatroom chatroom3 = createTestChatroom("room3", "user2", "Test Room 3");

        testChatrooms = Arrays.asList(testChatroom, chatroom2, chatroom3);
    }

    private Chatroom createTestChatroom(String chatroomId, String userId, String title) {
        Chatroom chatroom = new Chatroom();
        chatroom.setChatroomId(chatroomId);
        chatroom.setUserId(userId);
        chatroom.setTitle(title);
        return chatroom;
    }

    @Test
    void testGetAllChatrooms_Success() {
        // Arrange
        when(chatroomRepository.findAll()).thenReturn(testChatrooms);

        // Act
        List<Chatroom> result = chatroomService.getAllChatrooms();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(testChatrooms, result);
        verify(chatroomRepository).findAll();
    }

    @Test
    void testGetAllChatrooms_EmptyList() {
        // Arrange
        when(chatroomRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Chatroom> result = chatroomService.getAllChatrooms();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatroomRepository).findAll();
    }

    @Test
    void testGetChatroomById_Found() {
        // Arrange
        String chatroomId = "room1";
        when(chatroomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatroom));

        // Act
        Optional<Chatroom> result = chatroomService.getChatroomById(chatroomId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testChatroom, result.get());
        assertEquals(chatroomId, result.get().getChatroomId());
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void testGetChatroomById_NotFound() {
        // Arrange
        String chatroomId = "nonexistent";
        when(chatroomRepository.findById(chatroomId)).thenReturn(Optional.empty());

        // Act
        Optional<Chatroom> result = chatroomService.getChatroomById(chatroomId);

        // Assert
        assertFalse(result.isPresent());
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void testGetChatroomById_NullId() {
        // Arrange
        when(chatroomRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Chatroom> result = chatroomService.getChatroomById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(chatroomRepository).findById(null);
    }

    @Test
    void testGetChatroomsByUserId_Found() {
        // Arrange
        String userId = "user1";
        List<Chatroom> userChatrooms = Arrays.asList(testChatroom, testChatrooms.get(1));
        when(chatroomRepository.findByUserId(userId)).thenReturn(userChatrooms);

        // Act
        List<Chatroom> result = chatroomService.getChatroomsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(room -> room.getUserId().equals(userId)));
        verify(chatroomRepository).findByUserId(userId);
    }

    @Test
    void testGetChatroomsByUserId_NotFound() {
        // Arrange
        String userId = "nonexistent";
        when(chatroomRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<Chatroom> result = chatroomService.getChatroomsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatroomRepository).findByUserId(userId);
    }

    @Test
    void testGetChatroomsByUserId_NullUserId() {
        // Arrange
        when(chatroomRepository.findByUserId(null)).thenReturn(Collections.emptyList());

        // Act
        List<Chatroom> result = chatroomService.getChatroomsByUserId(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatroomRepository).findByUserId(null);
    }

    @Test
    void testCreateChatroom_Success() {
        // Arrange
        Chatroom newChatroom = createTestChatroom(null, "user1", "New Room");
        Chatroom savedChatroom = createTestChatroom("room4", "user1", "New Room");

        when(chatroomRepository.save(newChatroom)).thenReturn(savedChatroom);

        // Act
        Chatroom result = chatroomService.createChatroom(newChatroom);

        // Assert
        assertNotNull(result);
        assertEquals("room4", result.getChatroomId());
        assertEquals("New Room", result.getTitle());
        assertEquals("user1", result.getUserId());
        verify(chatroomRepository).save(newChatroom);
    }

    @Test
    void testCreateChatroom_WithExistingId() {
        // Arrange
        when(chatroomRepository.save(testChatroom)).thenReturn(testChatroom);

        // Act
        Chatroom result = chatroomService.createChatroom(testChatroom);

        // Assert
        assertNotNull(result);
        assertEquals(testChatroom, result);
        verify(chatroomRepository).save(testChatroom);
    }

    @Test
    void testCreateChatroom_NullChatroom() {
        // Arrange
        when(chatroomRepository.save(null)).thenThrow(new IllegalArgumentException("Chatroom cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            chatroomService.createChatroom(null);
        });
        verify(chatroomRepository).save(null);
    }

    @Test
    void testUpdateChatroom_Success() {
        // Arrange
        testChatroom.setTitle("Updated Room Title");

        when(chatroomRepository.save(testChatroom)).thenReturn(testChatroom);

        // Act
        Chatroom result = chatroomService.updateChatroom(testChatroom);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Room Title", result.getTitle());
        assertEquals(testChatroom, result);
        verify(chatroomRepository).save(testChatroom);
    }

    @Test
    void testUpdateChatroom_NonExistentChatroom() {
        // Arrange
        Chatroom nonExistentChatroom = createTestChatroom("nonexistent", "user1", "Non-existent Room");
        when(chatroomRepository.save(nonExistentChatroom)).thenReturn(nonExistentChatroom);

        // Act
        Chatroom result = chatroomService.updateChatroom(nonExistentChatroom);

        // Assert
        assertNotNull(result);
        assertEquals(nonExistentChatroom, result);
        verify(chatroomRepository).save(nonExistentChatroom);
    }

    @Test
    void testUpdateChatroom_NullChatroom() {
        // Arrange
        when(chatroomRepository.save(null)).thenThrow(new IllegalArgumentException("Chatroom cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            chatroomService.updateChatroom(null);
        });
        verify(chatroomRepository).save(null);
    }

    @Test
    void testDeleteChatroom_Success() {
        // Arrange
        String chatroomId = "room1";
        doNothing().when(chatMessageRepository).deleteByChatroomId(chatroomId);
        doNothing().when(chatroomRepository).deleteById(chatroomId);

        // Act
        chatroomService.deleteChatroom(chatroomId);

        // Assert
        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository).deleteById(chatroomId);
    }

    @Test
    void testDeleteChatroom_NullId() {
        // Arrange
        doNothing().when(chatMessageRepository).deleteByChatroomId(null);
        doNothing().when(chatroomRepository).deleteById(null);

        // Act
        chatroomService.deleteChatroom(null);

        // Assert
        verify(chatMessageRepository).deleteByChatroomId(null);
        verify(chatroomRepository).deleteById(null);
    }

    @Test
    void testDeleteChatroom_WithRepositoryException() {
        // Arrange
        String chatroomId = "room1";
        doThrow(new RuntimeException("Database error")).when(chatMessageRepository).deleteByChatroomId(chatroomId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatroomService.deleteChatroom(chatroomId);
        });

        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        // 確保在刪除訊息失敗時，聊天室也不會被刪除
        verify(chatroomRepository, never()).deleteById(chatroomId);
    }

    @Test
    void testDeleteChatroom_ChatMessageDeletionSucceedsButChatroomDeletionFails() {
        // Arrange
        String chatroomId = "room1";
        doNothing().when(chatMessageRepository).deleteByChatroomId(chatroomId);
        doThrow(new RuntimeException("Failed to delete chatroom")).when(chatroomRepository).deleteById(chatroomId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatroomService.deleteChatroom(chatroomId);
        });

        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository).deleteById(chatroomId);
    }

    @Test
    void testDeleteChatroom_VerifyDeletionOrder() {
        // Arrange
        String chatroomId = "room1";
        doNothing().when(chatMessageRepository).deleteByChatroomId(chatroomId);
        doNothing().when(chatroomRepository).deleteById(chatroomId);

        // Act
        chatroomService.deleteChatroom(chatroomId);

        // Assert - 驗證刪除順序（先刪除訊息，再刪除聊天室）
        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository).deleteById(chatroomId);

        // 使用 InOrder 來確保執行順序
        var inOrder = inOrder(chatMessageRepository, chatroomRepository);
        inOrder.verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        inOrder.verify(chatroomRepository).deleteById(chatroomId);
    }

    @Test
    void testRepositoryInteractions_VerifyNoUnexpectedCalls() {
        // Act
        chatroomService.getAllChatrooms();

        // Assert - 確保只調用了預期的方法
        verify(chatroomRepository).findAll();
        verifyNoMoreInteractions(chatroomRepository);
        verifyNoInteractions(chatMessageRepository);
    }

    // Integration-like test to verify service behavior with multiple operations
    @Test
    void testServiceWorkflow_CreateUpdateDelete() {
        // Arrange
        Chatroom newChatroom = createTestChatroom(null, "user1", "Workflow Test Room");
        Chatroom savedChatroom = createTestChatroom("workflow-room", "user1", "Workflow Test Room");

        when(chatroomRepository.save(newChatroom)).thenReturn(savedChatroom);
        when(chatroomRepository.save(savedChatroom)).thenReturn(savedChatroom);
        doNothing().when(chatMessageRepository).deleteByChatroomId("workflow-room");
        doNothing().when(chatroomRepository).deleteById("workflow-room");

        // Act - Create
        Chatroom created = chatroomService.createChatroom(newChatroom);

        // Act - Update
        created.setTitle("Updated Workflow Room");
        Chatroom updated = chatroomService.updateChatroom(created);

        // Act - Delete
        chatroomService.deleteChatroom(created.getChatroomId());

        // Assert
        assertEquals("workflow-room", created.getChatroomId());
        assertEquals("Updated Workflow Room", updated.getTitle());

        verify(chatroomRepository, times(2)).save(any(Chatroom.class));
        verify(chatMessageRepository).deleteByChatroomId("workflow-room");
        verify(chatroomRepository).deleteById("workflow-room");
    }
}