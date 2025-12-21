package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatSessionRegistry chatSessionRegistry;

    @InjectMocks
    private ChatMessageService chatMessageService;

    // === 基本 CRUD ===

    @Test
    void testSaveMessage() {
        ChatMessage message = new ChatMessage("msg1", "room1", true, false, "Hello", null);
        when(chatMessageRepository.save(message)).thenAnswer(inv -> {
            ChatMessage msg = inv.getArgument(0);
            if (msg.getSentAt() == null) msg.setSentAt(new Date());
            return msg;
        });

        ChatMessage saved = chatMessageService.saveMessage(message);

        assertNotNull(saved.getSentAt());
        verify(chatMessageRepository).save(message);
    }

    @Test
    void testGetAllMessages() {
        when(chatMessageRepository.findAll()).thenReturn(Collections.singletonList(new ChatMessage()));
        List<ChatMessage> result = chatMessageService.getAllMessages();
        assertEquals(1, result.size());
    }

    @Test
    void testGetMessageById() {
        when(chatMessageRepository.findById("msg1")).thenReturn(Optional.of(new ChatMessage()));
        assertTrue(chatMessageService.getMessageById("msg1").isPresent());
    }

    @Test
    void testCreateMessage() {
        ChatMessage message = new ChatMessage("msg1", "room1", true, false, "Hello", new Date());
        when(chatMessageRepository.save(message)).thenReturn(message);
        ChatMessage result = chatMessageService.createMessage(message);
        assertNotNull(result);
    }

    @Test
    void testUpdateMessage() {
        ChatMessage message = new ChatMessage("msg1", "room1", true, false, "Updated", new Date());
        when(chatMessageRepository.save(message)).thenReturn(message);
        ChatMessage result = chatMessageService.updateMessage(message);
        assertEquals("Updated", result.getContent());
    }

    @Test
    void testDeleteMessage() {
        chatMessageService.deleteMessage("msg1");
        verify(chatMessageRepository).deleteById("msg1");
    }

    // === 條件查詢 ===

    @Test
    void testGetMessagesByChatroomId() {
        when(chatMessageRepository.findByChatroomIdOrderBySentAtAsc("room1"))
                .thenReturn(Collections.singletonList(new ChatMessage()));
        List<ChatMessage> result = chatMessageService.getMessagesByChatroomId("room1");
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetTextMessagesByChatroomId() {
        when(chatMessageRepository.findByChatroomIdAndIsDrawingDataFalseOrderBySentAtAsc("room1"))
                .thenReturn(Collections.emptyList());
        assertTrue(chatMessageService.getTextMessagesByChatroomId("room1").isEmpty());
    }

    @Test
    void testGetDrawingMessagesByChatroomId() {
        when(chatMessageRepository.findByChatroomIdAndIsDrawingDataTrueOrderBySentAtAsc("room1"))
                .thenReturn(Collections.emptyList());
        assertTrue(chatMessageService.getDrawingMessagesByChatroomId("room1").isEmpty());
    }

    @Test
    void testGetUserMessagesByChatroomId() {
        when(chatMessageRepository.findByChatroomIdAndIsUserTrueOrderBySentAtAsc("room1"))
                .thenReturn(Collections.emptyList());
        assertTrue(chatMessageService.getUserMessagesByChatroomId("room1").isEmpty());
    }

    @Test
    void testGetAIMessagesByChatroomId() {
        when(chatMessageRepository.findByChatroomIdAndIsUserFalseOrderBySentAtAsc("room1"))
                .thenReturn(Collections.emptyList());
        assertTrue(chatMessageService.getAIMessagesByChatroomId("room1").isEmpty());
    }

    // === 批量刪除 ===

    @Test
    void testDeleteAllMessagesByChatroomId() {
        // Arrange
        String chatroomId = "room1";
        List<ChatMessage> messages = Arrays.asList(new ChatMessage(), new ChatMessage());
        when(chatMessageRepository.findByChatroomIdOrderBySentAtAsc(chatroomId)).thenReturn(messages);

        // Act
        int count = chatMessageService.deleteAllMessagesByChatroomId(chatroomId);

        // Assert
        assertEquals(2, count);
        verify(chatMessageRepository).deleteByChatroomId(chatroomId);
        verify(chatSessionRegistry).recreateSession(chatroomId); // 驗證 Session 重建
    }

    @Test
    void testDeleteUserMessagesByChatroomId() {
        String chatroomId = "room1";
        ChatMessage msg1 = new ChatMessage("msg1", chatroomId, true, false, "Hi", new Date());
        List<ChatMessage> messages = Collections.singletonList(msg1);

        when(chatMessageRepository.findByChatroomIdAndIsUserTrueOrderBySentAtAsc(chatroomId)).thenReturn(messages);

        int count = chatMessageService.deleteUserMessagesByChatroomId(chatroomId);

        assertEquals(1, count);
        verify(chatMessageRepository).deleteById("msg1");
    }

    @Test
    void testDeleteAIMessagesByChatroomId() {
        String chatroomId = "room1";
        ChatMessage msg1 = new ChatMessage("msg1", chatroomId, false, false, "Hi", new Date());
        List<ChatMessage> messages = Collections.singletonList(msg1);

        when(chatMessageRepository.findByChatroomIdAndIsUserFalseOrderBySentAtAsc(chatroomId)).thenReturn(messages);

        int count = chatMessageService.deleteAIMessagesByChatroomId(chatroomId);

        assertEquals(1, count);
        verify(chatMessageRepository).deleteById("msg1");
    }

    @Test
    void testDeleteTextMessagesByChatroomId() {
        String chatroomId = "room1";
        ChatMessage msg1 = new ChatMessage("msg1", chatroomId, true, false, "Text", new Date());
        List<ChatMessage> messages = Collections.singletonList(msg1);

        when(chatMessageRepository.findByChatroomIdAndIsDrawingDataFalseOrderBySentAtAsc(chatroomId)).thenReturn(messages);

        int count = chatMessageService.deleteTextMessagesByChatroomId(chatroomId);

        assertEquals(1, count);
        verify(chatMessageRepository).deleteById("msg1");
    }

    @Test
    void testDeleteDrawingMessagesByChatroomId() {
        String chatroomId = "room1";
        ChatMessage msg1 = new ChatMessage("msg1", chatroomId, true, true, "Draw", new Date());
        List<ChatMessage> messages = Collections.singletonList(msg1);

        when(chatMessageRepository.findByChatroomIdAndIsDrawingDataTrueOrderBySentAtAsc(chatroomId)).thenReturn(messages);

        int count = chatMessageService.deleteDrawingMessagesByChatroomId(chatroomId);

        assertEquals(1, count);
        verify(chatMessageRepository).deleteById("msg1");
    }

    @Test
    void testDeleteMessagesByChatroomId() {
        chatMessageService.deleteMessagesByChatroomId("room1");
        verify(chatMessageRepository).deleteByChatroomId("room1");
    }

    // === 統計 ===

    @Test
    void testCountMessages() {
        when(chatMessageRepository.countByChatroomId("room1")).thenReturn(10L);
        assertEquals(10L, chatMessageService.countMessagesByChatroomId("room1"));
    }

    @Test
    void testCountUserMessages() {
        when(chatMessageRepository.countByChatroomIdAndIsUserTrue("room1")).thenReturn(5L);
        assertEquals(5L, chatMessageService.countUserMessagesByChatroomId("room1"));
    }

    @Test
    void testCountAIMessages() {
        when(chatMessageRepository.countByChatroomIdAndIsUserFalse("room1")).thenReturn(5L);
        assertEquals(5L, chatMessageService.countAIMessagesByChatroomId("room1"));
    }

    // === 日期範圍查詢 (複雜邏輯) ===

    @Test
    void testGetTextMessagesByDate_Success() throws Exception {
        String date = "2025-10-27";
        List<ChatMessage> messages = Collections.singletonList(new ChatMessage());

        when(chatMessageRepository.findByChatroomIdAndIsDrawingDataFalseAndSentAtBetweenOrderBySentAtAsc(
                eq("room1"), any(Date.class), any(Date.class)))
                .thenReturn(messages);

        List<ChatMessage> result = chatMessageService.getTextMessagesByDate("room1", date);

        assertFalse(result.isEmpty());
        // 驗證是否有正確呼叫到帶有日期範圍的 Repository 方法
        verify(chatMessageRepository).findByChatroomIdAndIsDrawingDataFalseAndSentAtBetweenOrderBySentAtAsc(
                eq("room1"), any(Date.class), any(Date.class));
    }

    @Test
    void testGetTextMessagesByDate_InvalidFormat() {
        List<ChatMessage> result = chatMessageService.getTextMessagesByDate("room1", "invalid-date");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserTextMessagesByDate_Success() {
        String date = "2025-10-27";
        when(chatMessageRepository.findByChatroomIdAndIsUserTrueAndIsDrawingDataFalseAndSentAtBetweenOrderBySentAtAsc(
                eq("room1"), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        List<ChatMessage> result = chatMessageService.getUserTextMessagesByDate("room1", date);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserTextMessagesByDate_InvalidFormat() {
        List<ChatMessage> result = chatMessageService.getUserTextMessagesByDate("room1", "bad");
        assertTrue(result.isEmpty());
    }

    @Test
    void testCountUserTextMessagesToday() {
        // 這個測試依賴系統時間 (new Date())，為了穩定性，我們只驗證流程有走到 Repository
        when(chatMessageRepository.countByChatroomIdAndIsUserTrueAndIsDrawingDataFalseAndSentAtBetween(
                eq("room1"), any(Date.class), any(Date.class)))
                .thenReturn(5);

        int count = chatMessageService.countUserTextMessagesToday("room1");
        assertEquals(5, count);
    }

    // === 其他 ===

    @Test
    void testSavePreQuestionForNewChatroom() {
        String chatroomId = "room1";
        String question = "How are you?";

        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatMessage result = chatMessageService.savePreQuestionForNewChatroom(chatroomId, question);

        assertNotNull(result);
        assertEquals(chatroomId, result.getChatroomId());
        assertEquals(question, result.getContent());
        assertFalse(result.getIsUser());
        assertFalse(result.getIsDrawingData());
        assertNotNull(result.getSentAt());
    }
}
