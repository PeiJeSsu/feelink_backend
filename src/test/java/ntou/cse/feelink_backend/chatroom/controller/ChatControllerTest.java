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

        testSavedUserMessage = new ChatMessage();
        testSavedUserMessage.setMessageId("user-message-123");
        testSavedUserMessage.setChatroomId(testChatroomId);
        testSavedUserMessage.setIsUser(true);
        testSavedUserMessage.setIsDrawingData(false);
        testSavedUserMessage.setContent(testUserMessage);
        testSavedUserMessage.setSentAt(new Date());

        testSavedAiMessage = new ChatMessage();
        testSavedAiMessage.setMessageId("saved-ai-message-123");
        testSavedAiMessage.setChatroomId(testChatroomId);
        testSavedAiMessage.setIsUser(false);
        testSavedAiMessage.setIsDrawingData(false);
        testSavedAiMessage.setContent("");
        testSavedAiMessage.setSentAt(new Date());
    }

    @Test
    void testChatStream_WithAllParameters() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                testHasDefaultQuestion, testPersonality);

        assertNotNull(result);
        assertEquals(mockEmitter, result);

        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123"));
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));

        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals(testUserMessage) &&
                        message.getSentAt() != null
        ));

        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals("") &&
                        message.getSentAt() != null
        ));
    }

    @Test
    void testChatStream_WithRequiredParametersOnly() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                eq("default"), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, null, null, "default");

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                eq("default"), eq("saved-ai-message-123"));
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testChatStream_WithDefaultPersonality() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq("default"), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                testHasDefaultQuestion, "default");

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq("default"), eq("saved-ai-message-123"));
    }

    @Test
    void testChatStream_WithConversationCountOnly() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                isNull(), eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                null, testPersonality);

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                isNull(), eq(testPersonality), eq("saved-ai-message-123"));
    }

    @Test
    void testChatStream_WithHasDefaultQuestionOnly() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, null,
                testHasDefaultQuestion, testPersonality);

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), isNull(),
                eq(testHasDefaultQuestion), eq(testPersonality), eq("saved-ai-message-123"));
    }

    @Test
    void testChatStream_EmptyUserMessage() throws IOException {
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

        SseEmitter result = chatController.chatStream(
                emptyMessage, testChatroomId, null, null, testPersonality);

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(emptyMessage), eq(testChatroomId), isNull(), isNull(),
                eq(testPersonality), eq("saved-ai-message-123"));

        verify(chatMessageService).createMessage(argThat(message ->
                message.getContent().equals(emptyMessage) && message.getIsUser()
        ));
    }

    @Test
    void testChatStream_PlainChatServiceException() throws IOException {
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString()))
                .thenThrow(new RuntimeException("串流服務異常"));

        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId,
                    testConversationCount, testHasDefaultQuestion, testPersonality);
        });

        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testChatStream_ChatMessageServiceException() throws IOException {
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId,
                    testConversationCount, testHasDefaultQuestion, testPersonality);
        });

        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class));
        verify(plainChatService, never()).processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testChatStream_PartialChatMessageServiceException() throws IOException {
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenThrow(new RuntimeException("第二次資料庫連接失敗"));

        assertThrows(RuntimeException.class, () -> {
            chatController.chatStream(testUserMessage, testChatroomId,
                    testConversationCount, testHasDefaultQuestion, testPersonality);
        });

        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(plainChatService, never()).processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testChatStream_WithDifferentPersonalities() throws IOException {
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

            SseEmitter result = chatController.chatStream(
                    testUserMessage, testChatroomId, null, null, personality);

            assertNotNull(result);
            verify(plainChatService).processStreamWithAllFeatures(
                    eq(testUserMessage), eq(testChatroomId), isNull(), isNull(),
                    eq(personality), eq("saved-ai-message-123"));
        }
    }

    @Test
    void testMessagePropertiesSetCorrectly() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedUserMessage)
                .thenReturn(testSavedAiMessage);
        when(plainChatService.processStreamWithAllFeatures(
                anyString(), anyString(), any(), any(), anyString(), anyString()))
                .thenReturn(mockEmitter);

        chatController.chatStream(testUserMessage, testChatroomId,
                testConversationCount, testHasDefaultQuestion, testPersonality);

        verify(chatMessageService).createMessage(argThat(userMessage ->
                userMessage.getChatroomId().equals(testChatroomId) &&
                        userMessage.getIsUser() &&
                        !userMessage.getIsDrawingData() &&
                        userMessage.getContent().equals(testUserMessage) &&
                        userMessage.getSentAt() != null
        ));

        verify(chatMessageService).createMessage(argThat(aiMessage ->
                aiMessage.getChatroomId().equals(testChatroomId) &&
                        !aiMessage.getIsUser() &&
                        !aiMessage.getIsDrawingData() &&
                        aiMessage.getContent().equals("") &&
                        aiMessage.getSentAt() != null
        ));
    }

    @Test
    void testChatStream_NullUserMessage() throws IOException {
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

        SseEmitter result = chatController.chatStream(
                null, testChatroomId, null, null, testPersonality);

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                isNull(), eq(testChatroomId), isNull(), isNull(),
                eq(testPersonality), eq("saved-ai-message-123"));

        verify(chatMessageService).createMessage(argThat(message ->
                message.getIsUser() && message.getContent() == null
        ));
    }

    @Test
    void testChatStream_MessageIdCorrectlyPassed() throws IOException {
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

        SseEmitter result = chatController.chatStream(
                testUserMessage, testChatroomId, testConversationCount,
                testHasDefaultQuestion, testPersonality);

        assertNotNull(result);
        verify(plainChatService, times(1)).processStreamWithAllFeatures(
                eq(testUserMessage), eq(testChatroomId), eq(testConversationCount),
                eq(testHasDefaultQuestion), eq(testPersonality), eq(expectedMessageId));
    }
}