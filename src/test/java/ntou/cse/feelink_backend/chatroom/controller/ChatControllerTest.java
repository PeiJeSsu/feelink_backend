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
    private String testPersonality;
    private Integer testConversationCount;
    private Boolean testHasDefaultQuestion;
    private ChatMessage testSavedUserMessage;
    private ChatMessage testSavedAiMessage;

    @BeforeEach
    void setUp() {
        testUserMessage = "你好，我需要幫助";
        testChatroomId = "test-chatroom-123";
        testPersonality = "friendly";
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

        // 創建測試用的已保存 AI 訊息
        testSavedAiMessage = new ChatMessage();
        testSavedAiMessage.setMessageId("saved-ai-message-123");
        testSavedAiMessage.setChatroomId(testChatroomId);
        testSavedAiMessage.setIsUser(false);
        testSavedAiMessage.setIsDrawingData(false);
        testSavedAiMessage.setContent(""); // 初始為空內容
        testSavedAiMessage.setSentAt(new Date());
    }

    @Test
    void testChatStream_WithAllParameters() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)  // 用戶訊息
                .thenReturn(testSavedAiMessage);   // AI 初始訊息
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                testHasDefaultQuestion, testPersonality);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmitter, result);

        // 驗證服務方法被調用
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123"));
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));

        // 驗證使用者訊息被正確保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals(testUserMessage) &&
                        message.getSentAt() != null
        ));

        // 驗證AI初始訊息被正確創建
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals("") && // 初始為空
                        message.getSentAt() != null
        ));
    }

    @Test
    void testChatStream_WithRequiredParametersOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                eq("default"), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, null, null, "default");

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                eq("default"), eq("saved-ai-message-123"));
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testChatStream_WithDefaultPersonality() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq("default"), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act - 不傳入 personality 參數，應該使用預設值 "default"
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                testHasDefaultQuestion, "default");

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq("default"), eq("saved-ai-message-123"));
    }

    @Test
    void testChatStream_WithConversationCountOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                isNull(), eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                null, testPersonality);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                isNull(), eq(testPersonality), eq("saved-ai-message-123"));
    }

    @Test
    void testChatStream_WithHasDefaultQuestionOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, null,
                testHasDefaultQuestion, testPersonality);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123"));
    }

    @Test
    void testChatStream_EmptyUserMessage() throws IOException {
        // Arrange
        String emptyMessage = "";
        SseEmitter mockEmitter = new SseEmitter();

        ChatMessage emptyUserMessage = new ChatMessage();
        emptyUserMessage.setMessageId("empty-user-message");
        emptyUserMessage.setContent("");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(emptyUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(emptyMessage), eq(testChatroomId), isNull(), isNull(),
                eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                emptyMessage, testChatroomId, null, null, testPersonality);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(emptyMessage), eq(testChatroomId), isNull(), isNull(),
                eq(testPersonality), eq("saved-ai-message-123"));

        // 驗證空訊息也會被保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getContent().equals(emptyMessage) && message.getIsUser()
        ));
    }

    @Test
    void testChatStream_PlainChatServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString()))
                .thenThrow(new RuntimeException("串流服務異常"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId,
                    testConversationCount, testHasDefaultQuestion, testPersonality);
        });

        // 驗證使用者訊息和AI初始訊息仍被創建（在異常發生前）
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testChatStream_ChatMessageServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId,
                    testConversationCount, testHasDefaultQuestion, testPersonality);
        });

        // 驗證服務被調用
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class));
        // PlainChatService 不應該被調用，因為在保存使用者訊息時就異常了
        verify(plainChatService, never()).processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testChatStream_PartialChatMessageServiceException() throws IOException {
        // Arrange - 第一次createMessage成功，第二次失敗
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage) // 第一次成功
                .thenThrow(new RuntimeException("第二次資料庫連接失敗")); // 第二次失敗

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId,
                    testConversationCount, testHasDefaultQuestion, testPersonality);
        });

        // 驗證服務被調用兩次
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        // PlainChatService 不應該被調用，因為在保存AI初始訊息時異常了
        verify(plainChatService, never()).processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testChatStream_WithDifferentPersonalities() throws IOException {
        // Arrange
        String[] personalities = {"friendly", "professional", "casual", "formal"};
        SseEmitter mockEmitter = new SseEmitter();

        for (String personality : personalities) {
            when(chatMessageService.createMessage(any(ChatMessage.class)))
                    .thenReturn(testSavedUserMessage)
                    .thenReturn(testSavedAiMessage);
            when(plainChatService.processStreamWithAllFeatures(
                    eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                    eq(personality), eq("saved-ai-message-123")))
                    .thenReturn(mockEmitter);

            // Act
            SseEmitter result = chatController.chatStream(
                    testUserMessage, testChatroomId, null, null, personality);

            // Assert
            assertNotNull(result);
            verify(plainChatService).processStreamWithAllFeatures(
                    eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                    eq(personality), eq("saved-ai-message-123"));
        }
    }

    @Test
    void testMessagePropertiesSetCorrectly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString()))
                .thenReturn(mockEmitter);

        // Act
        chatController.chatStream(testUserMessage, testChatroomId,
                testConversationCount, testHasDefaultQuestion, testPersonality);

        // Assert - 驗證用戶訊息的屬性被正確設置
        verify(chatMessageService).createMessage(argThat(userMessage ->
                userMessage.getChatroomId().equals(testChatroomId) &&
                        userMessage.getIsUser() &&
                        !userMessage.getIsDrawingData() &&
                        userMessage.getContent().equals(testUserMessage) &&
                        userMessage.getSentAt() != null
        ));

        // 驗證AI訊息的屬性被正確設置
        verify(chatMessageService).createMessage(argThat(aiMessage ->
                aiMessage.getChatroomId().equals(testChatroomId) &&
                        !aiMessage.getIsUser() &&
                        !aiMessage.getIsDrawingData() &&
                        aiMessage.getContent().equals("") && // 初始為空
                        aiMessage.getSentAt() != null
        ));
    }

    @Test
    void testChatStream_NullUserMessage() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        ChatMessage nullUserMessage = new ChatMessage();
        nullUserMessage.setMessageId("null-user-message");
        nullUserMessage.setContent(null);

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(nullUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                isNull(), eq(testChatroomId), isNull(), isNull(),
                eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                null, testChatroomId, null, null, testPersonality);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                isNull(), eq(testChatroomId), isNull(), isNull(),
                eq(testPersonality), eq("saved-ai-message-123"));

        // 驗證null訊息也會被保存
        verify(chatMessageService).createMessage(argThat(message ->
                message.getIsUser() && message.getContent() == null
        ));
    }

    @Test
    void testChatStream_MessageIdCorrectlyPassed() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        String expectedMessageId = "specific-ai-message-id";

        ChatMessage specificAiMessage = new ChatMessage();
        specificAiMessage.setMessageId(expectedMessageId);

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(specificAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq(expectedMessageId)))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                testHasDefaultQuestion, testPersonality);

        // Assert
        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq(expectedMessageId));
    }
}