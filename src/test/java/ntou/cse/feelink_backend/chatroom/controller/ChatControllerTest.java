package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private PlainChatServiceImpl plainChatService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatController chatController;

    private String testUserMessage;
    private String testChatroomId;
    private Integer testConversationCount;
    private Boolean testHasDefaultQuestion;
    private ChatMessage testAiResponse;
    private ChatMessage testSavedUserMessage;
    private ChatMessage testSavedAiMessage;

    @BeforeEach
    void setUp() {
        testUserMessage = "你好，我需要幫助";
        testChatroomId = "test-chatroom-123";
        testConversationCount = 5;
        testHasDefaultQuestion = true;

        // 創建測試用的使用者訊息
        testSavedUserMessage = new ChatMessage();
        testSavedUserMessage.setMessageId("user-message-123");
        testSavedUserMessage.setChatroomId(testChatroomId);
        testSavedUserMessage.setIsUser(true);
        testSavedUserMessage.setIsDrawingData(false);
        testSavedUserMessage.setContent(testUserMessage);
        testSavedUserMessage.setSentAt(new Date());

        // 創建測試用的 AI 回應
        testAiResponse = new ChatMessage();
        testAiResponse.setMessageId("ai-message-123");
        testAiResponse.setChatroomId(testChatroomId);
        testAiResponse.setIsUser(false);
        testAiResponse.setIsDrawingData(false);
        testAiResponse.setContent("AI回應：我很樂意幫助您");
        testAiResponse.setSentAt(new Date());

        // 創建測試用的已保存 AI 訊息
        testSavedAiMessage = new ChatMessage();
        testSavedAiMessage.setMessageId("saved-ai-message-123");
        testSavedAiMessage.setChatroomId(testChatroomId);
        testSavedAiMessage.setIsUser(false);
        testSavedAiMessage.setIsDrawingData(false);
        testSavedAiMessage.setContent("AI回應：我很樂意幫助您");
        testSavedAiMessage.setSentAt(new Date());
    }

    @Test
    void testChatSimple_WithAllParameters() throws IOException {
        // Arrange
        when(plainChatService.reply(eq(testUserMessage), eq(testChatroomId),
                eq(testConversationCount), eq(testHasDefaultQuestion)))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage) // 用戶訊息
                .thenReturn(testSavedAiMessage);  // AI 回應

        // Act
        ResponseEntity<ChatMessage> result = chatController.chat(
                testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSavedAiMessage, result.getBody());

        // 驗證服務方法被調用
        verify(plainChatService, times(1)).reply(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount), eq(testHasDefaultQuestion));
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));

        // 驗證使用者訊息被正確保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals(testUserMessage) &&
                        message.getSentAt() != null
        ));

        // 驗證AI回應被正確處理和保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getSentAt() != null
        ));
    }

    @Test
    void testChatSimple_WithRequiredParametersOnly() throws IOException {
        // Arrange
        when(plainChatService.reply(eq(testUserMessage), eq(testChatroomId), isNull(), isNull()))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);

        // Act
        ResponseEntity<ChatMessage> result = chatController.chat(
                testUserMessage, testChatroomId, null, null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSavedAiMessage, result.getBody());

        // 驗證服務方法被調用，可選參數為null
        verify(plainChatService, times(1)).reply(
                eq(testUserMessage), eq(testChatroomId), isNull(), isNull());
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testChatSimple_WithPartialParameters() throws IOException {
        // Arrange
        when(plainChatService.reply(eq(testUserMessage), eq(testChatroomId),
                eq(testConversationCount), isNull()))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);

        // Act
        ResponseEntity<ChatMessage> result = chatController.chat(
                testUserMessage, testChatroomId, testConversationCount, null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(plainChatService, times(1)).reply(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount), isNull());
    }

    @Test
    void testChatSimple_EmptyUserMessage() throws IOException {
        // Arrange
        String emptyMessage = "";
        when(plainChatService.reply(eq(emptyMessage), eq(testChatroomId), isNull(), isNull()))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);

        // Act
        ResponseEntity<ChatMessage> result = chatController.chat(
                emptyMessage, testChatroomId, null, null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(plainChatService, times(1)).reply(eq(emptyMessage), eq(testChatroomId), isNull(), isNull());

        // 驗證空訊息也會被保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getContent().equals(emptyMessage) && message.getIsUser()
        ));
    }

    @Test
    void testChatSimple_PlainChatServiceException() throws IOException {
        // Arrange
        when(plainChatService.reply(anyString(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("PlainChatService異常"));
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chat(testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);
        });

        // 驗證使用者訊息仍被保存（在異常發生前）
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class));
        verify(plainChatService, times(1)).reply(anyString(), anyString(), any(), any());
    }

    @Test
    void testChatSimple_ChatMessageServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chat(testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);
        });

        // 驗證服務被調用
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class));
        // PlainChatService 不應該被調用，因為在保存使用者訊息時就異常了
        verify(plainChatService, never()).reply(anyString(), anyString(), any(), any());
    }

    @Test
    void testChatStream_WithAllParameters() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedUserMsg = new ChatMessage();
        savedUserMsg.setMessageId("stream-user-msg-123");

        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-123");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(savedUserMsg)  // 用戶訊息
                .thenReturn(savedAiMsg);   // AI 初始訊息
        when(plainChatService.replyStreamWithDatabase(eq(testUserMessage), eq(testChatroomId),
                eq(testConversationCount), eq(testHasDefaultQuestion), eq("stream-ai-msg-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmitter, result);

        // 驗證服務方法被調用
        verify(plainChatService, times(1)).replyStreamWithDatabase(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq("stream-ai-msg-123"));
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));

        // 驗證使用者訊息被正確保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals(testUserMessage)
        ));

        // 驗證AI初始訊息被正確創建
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals("") // 初始為空
        ));
    }

    @Test
    void testChatStream_WithRequiredParametersOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedUserMsg = new ChatMessage();
        savedUserMsg.setMessageId("stream-user-msg-456");

        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-456");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(savedUserMsg)
                .thenReturn(savedAiMsg);
        when(plainChatService.replyStreamWithDatabase(eq(testUserMessage), eq(testChatroomId),
                isNull(), isNull(), eq("stream-ai-msg-456")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(testUserMessage, testChatroomId, null, null);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).replyStreamWithDatabase(
                eq(testUserMessage), eq(testChatroomId), isNull(), isNull(), eq("stream-ai-msg-456"));
    }

    @Test
    void testChatStream_WithConversationCountOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedUserMsg = new ChatMessage();
        savedUserMsg.setMessageId("stream-user-msg-789");

        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-789");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(savedUserMsg)
                .thenReturn(savedAiMsg);
        when(plainChatService.replyStreamWithDatabase(eq(testUserMessage), eq(testChatroomId),
                eq(testConversationCount), isNull(), eq("stream-ai-msg-789")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount, null);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).replyStreamWithDatabase(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                isNull(), eq("stream-ai-msg-789"));
    }

    @Test
    void testChatStream_PlainChatServiceException() throws IOException {
        // Arrange
        ChatMessage savedUserMsg = new ChatMessage();
        savedUserMsg.setMessageId("stream-user-error");

        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-error");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(savedUserMsg)
                .thenReturn(savedAiMsg);
        when(plainChatService.replyStreamWithDatabase(anyString(), anyString(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("串流服務異常"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);
        });

        // 驗證使用者訊息和AI初始訊息仍被創建（在異常發生前）
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(plainChatService, times(1)).replyStreamWithDatabase(anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void testChatStream_ChatMessageServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);
        });

        // 驗證服務被調用
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class));
        // PlainChatService 不應該被調用，因為在保存訊息時就異常了
        verify(plainChatService, never()).replyStreamWithDatabase(anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void testChatStream_PartialChatMessageServiceException() throws IOException {
        // Arrange - 第一次createMessage成功，第二次失敗
        ChatMessage savedUserMsg = new ChatMessage();
        savedUserMsg.setMessageId("partial-success-user");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(savedUserMsg) // 第一次成功
                .thenThrow(new RuntimeException("第二次資料庫連接失敗")); // 第二次失敗

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);
        });

        // 驗證服務被調用兩次
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        // PlainChatService 不應該被調用，因為在保存AI初始訊息時異常了
        verify(plainChatService, never()).replyStreamWithDatabase(anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void testMessagePropertiesSetCorrectly() throws IOException {
        // Arrange
        when(plainChatService.reply(anyString(), anyString(), any(), any()))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);

        // Act
        chatController.chat(testUserMessage, testChatroomId, testConversationCount, testHasDefaultQuestion);

        // Assert - 驗證AI回應的屬性被正確設置
        // 注意：testAiResponse 是從 plainChatService.reply 返回的，controller 會設置其屬性
        assertEquals(testChatroomId, testAiResponse.getChatroomId());
        assertFalse(testAiResponse.getIsDrawingData());
        assertNotNull(testAiResponse.getSentAt());
    }
}