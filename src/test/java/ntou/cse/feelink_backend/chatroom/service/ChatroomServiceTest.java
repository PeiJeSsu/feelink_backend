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
        when(chatroomRepository.findAll()).thenReturn(testChatrooms);

        List<Chatroom> result = chatroomService.getAllChatrooms();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(testChatrooms, result);
        verify(chatroomRepository).findAll();
    }

    @Test
    void testGetAllChatrooms_EmptyList() {
        when(chatroomRepository.findAll()).thenReturn(Collections.emptyList());

        List<Chatroom> result = chatroomService.getAllChatrooms();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatroomRepository).findAll();
    }

    @Test
    void testGetChatroomById_Found() {
        String chatroomId = "room1";
        when(chatroomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatroom));

        Optional<Chatroom> result = chatroomService.getChatroomById(chatroomId);

        assertTrue(result.isPresent());
        assertEquals(testChatroom, result.get());
        assertEquals(chatroomId, result.get().getChatroomId());
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void testGetChatroomById_NotFound() {
        String chatroomId = "nonexistent";
        when(chatroomRepository.findById(chatroomId)).thenReturn(Optional.empty());

        Optional<Chatroom> result = chatroomService.getChatroomById(chatroomId);

        assertFalse(result.isPresent());
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void testGetChatroomById_NullId() {
        when(chatroomRepository.findById(null)).thenReturn(Optional.empty());

        Optional<Chatroom> result = chatroomService.getChatroomById(null);

        assertFalse(result.isPresent());
        verify(chatroomRepository).findById(null);
    }

    @Test
    void testGetChatroomsByUserId_Found() {
        String userId = "user1";
        List<Chatroom> userChatrooms = Arrays.asList(testChatroom, testChatrooms.get(1));
        when(chatroomRepository.findByUserId(userId)).thenReturn(userChatrooms);

        List<Chatroom> result = chatroomService.getChatroomsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(room -> room.getUserId().equals(userId)));
        verify(chatroomRepository).findByUserId(userId);
    }

    @Test
    void testGetChatroomsByUserId_NotFound() {
        String userId = "nonexistent";
        when(chatroomRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Chatroom> result = chatroomService.getChatroomsByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatroomRepository).findByUserId(userId);
    }

    @Test
    void testGetChatroomsByUserId_NullUserId() {
        when(chatroomRepository.findByUserId(null)).thenReturn(Collections.emptyList());

        List<Chatroom> result = chatroomService.getChatroomsByUserId(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatroomRepository).findByUserId(null);
    }

    @Test
    void testCreateChatroom_Success() {
        Chatroom newChatroom = createTestChatroom(null, "user1", "New Room");
        Chatroom savedChatroom = createTestChatroom("room4", "user1", "New Room");

        when(chatroomRepository.save(newChatroom)).thenReturn(savedChatroom);

        Chatroom result = chatroomService.createChatroom(newChatroom);

        assertNotNull(result);
        assertEquals("room4", result.getChatroomId());
        assertEquals("New Room", result.getTitle());
        assertEquals("user1", result.getUserId());
        verify(chatroomRepository).save(newChatroom);
    }

    @Test
    void testCreateChatroom_WithExistingId() {
        when(chatroomRepository.save(testChatroom)).thenReturn(testChatroom);

        Chatroom result = chatroomService.createChatroom(testChatroom);

        assertNotNull(result);
        assertEquals(testChatroom, result);
        verify(chatroomRepository).save(testChatroom);
    }

    @Test
    void testCreateChatroom_NullChatroom() {
        when(chatroomRepository.save(null)).thenThrow(new IllegalArgumentException("Chatroom cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            chatroomService.createChatroom(null);
        });
        verify(chatroomRepository).save(null);
    }

    @Test
    void testUpdateChatroom_Success() {
        testChatroom.setTitle("Updated Room Title");

        when(chatroomRepository.save(testChatroom)).thenReturn(testChatroom);

        Chatroom result = chatroomService.updateChatroom(testChatroom);

        assertNotNull(result);
        assertEquals("Updated Room Title", result.getTitle());
        assertEquals(testChatroom, result);
        verify(chatroomRepository).save(testChatroom);
    }

    @Test
    void testUpdateChatroom_NonExistentChatroom() {
        Chatroom nonExistentChatroom = createTestChatroom("nonexistent", "user1", "Non-existent Room");
        when(chatroomRepository.save(nonExistentChatroom)).thenReturn(nonExistentChatroom);

        Chatroom result = chatroomService.updateChatroom(nonExistentChatroom);

        assertNotNull(result);
        assertEquals(nonExistentChatroom, result);
        verify(chatroomRepository).save(nonExistentChatroom);
    }

    @Test
    void testUpdateChatroom_NullChatroom() {
        when(chatroomRepository.save(null)).thenThrow(new IllegalArgumentException("Chatroom cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            chatroomService.updateChatroom(null);
        });
        verify(chatroomRepository).save(null);
    }

    @Test
    void testDeleteChatroom_Success() {
        String chatroomId = "room1";
        doNothing().when(chatMessageRepository).deleteByChatroomId(chatroomId);
        doNothing().when(chatroomRepository).deleteById(chatroomId);

        chatroomService.deleteChatroom(chatroomId);

        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository).deleteById(chatroomId);
    }

    @Test
    void testDeleteChatroom_NullId() {
        doNothing().when(chatMessageRepository).deleteByChatroomId(null);
        doNothing().when(chatroomRepository).deleteById(null);

        chatroomService.deleteChatroom(null);

        verify(chatMessageRepository).deleteByChatroomId(null);
        verify(chatroomRepository).deleteById(null);
    }

    @Test
    void testDeleteChatroom_WithRepositoryException() {
        String chatroomId = "room1";
        doThrow(new RuntimeException("Database error")).when(chatMessageRepository).deleteByChatroomId(chatroomId);

        assertThrows(RuntimeException.class, () -> {
            chatroomService.deleteChatroom(chatroomId);
        });

        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository, never()).deleteById(chatroomId);
    }

    @Test
    void testDeleteChatroom_ChatMessageDeletionSucceedsButChatroomDeletionFails() {
        String chatroomId = "room1";
        doNothing().when(chatMessageRepository).deleteByChatroomId(chatroomId);
        doThrow(new RuntimeException("Failed to delete chatroom")).when(chatroomRepository).deleteById(chatroomId);

        assertThrows(RuntimeException.class, () -> {
            chatroomService.deleteChatroom(chatroomId);
        });

        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository).deleteById(chatroomId);
    }

    @Test
    void testDeleteChatroom_VerifyDeletionOrder() {
        String chatroomId = "room1";
        doNothing().when(chatMessageRepository).deleteByChatroomId(chatroomId);
        doNothing().when(chatroomRepository).deleteById(chatroomId);

        chatroomService.deleteChatroom(chatroomId);

        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatroomRepository).deleteById(chatroomId);

        var inOrder = inOrder(chatMessageRepository, chatroomRepository);
        inOrder.verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        inOrder.verify(chatroomRepository).deleteById(chatroomId);
    }

    @Test
    void testRepositoryInteractions_VerifyNoUnexpectedCalls() {
        chatroomService.getAllChatrooms();

        verify(chatroomRepository).findAll();
        verifyNoMoreInteractions(chatroomRepository);
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void testServiceWorkflow_CreateUpdateDelete() {
        Chatroom newChatroom = createTestChatroom(null, "user1", "Workflow Test Room");
        Chatroom savedChatroom = createTestChatroom("workflow-room", "user1", "Workflow Test Room");

        when(chatroomRepository.save(newChatroom)).thenReturn(savedChatroom);
        when(chatroomRepository.save(savedChatroom)).thenReturn(savedChatroom);
        doNothing().when(chatMessageRepository).deleteByChatroomId("workflow-room");
        doNothing().when(chatroomRepository).deleteById("workflow-room");

        Chatroom created = chatroomService.createChatroom(newChatroom);

        created.setTitle("Updated Workflow Room");
        Chatroom updated = chatroomService.updateChatroom(created);

        chatroomService.deleteChatroom(created.getChatroomId());

        assertEquals("workflow-room", created.getChatroomId());
        assertEquals("Updated Workflow Room", updated.getTitle());

        verify(chatroomRepository, times(2)).save(any(Chatroom.class));
        verify(chatMessageRepository).deleteByChatroomId("workflow-room");
        verify(chatroomRepository).deleteById("workflow-room");
    }
}