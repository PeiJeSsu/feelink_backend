package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlainChatServiceImplTest {

    @Mock
    private ChatSessionRegistry registry;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Mock
    private MessageFormattingService messageFormattingService;

    @Mock
    private ChatSession chatSession;

    @Mock
    private ResponseStream<GenerateContentResponse> responseStream;

    @Mock
    private GenerateContentResponse generateContentResponse;

    @InjectMocks
    private PlainChatServiceImpl plainChatService;

    private static final String TEST_USER_MESSAGE = "Hello, how are you?";
    private static final String TEST_CHATROOM_ID = "room123";
    private static final String TEST_AI_MESSAGE_ID = "ai_msg_123";
    private static final Integer TEST_CONVERSATION_COUNT = 5;
    private static final Boolean TEST_HAS_DEFAULT_QUESTION = true;

    @BeforeEach
    void setUp() {
        // 使用反射設置 MessageFormattingService
        ReflectionTestUtils.setField(plainChatService, "messageFormattingService", messageFormattingService);
    }

    private ChatMessage createTestChatMessage(String id, String content, boolean isUser) {
        ChatMessage message = new ChatMessage();
        message.setMessageId(id);
        message.setContent(content);
        message.setIsUser(isUser);
        message.setChatroomId(TEST_CHATROOM_ID);
        return message;
    }

    @Test
    void testReply_Success() {
        // Arrange
        List<ChatMessage> chatHistory = Arrays.asList(
                createTestChatMessage("msg1", "Hello", true),
                createTestChatMessage("msg2", "Hi there!", false)
        );
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        String aiResponse = "I'm doing well, thank you!";

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(chatHistory);
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            when(chatSession.sendMessage(formattedMessage)).thenReturn(generateContentResponse);
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn(aiResponse);

            // Act
            ChatMessage result = plainChatService.reply(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);

            // Assert
            assertNotNull(result);
            assertFalse(result.getIsUser());
            assertEquals(aiResponse, result.getContent());

            verify(registry).getSession(TEST_CHATROOM_ID);
            verify(chatMessageService).getMessagesByChatroomId(TEST_CHATROOM_ID);
            verify(messageFormattingService).getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testReply_WithException() {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        RuntimeException testException = new RuntimeException("AI service error");

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(Arrays.asList());
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);

        try {
            when(chatSession.sendMessage(formattedMessage)).thenThrow(testException);

            // Act
            ChatMessage result = plainChatService.reply(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);

            // Assert
            assertNotNull(result);
            assertFalse(result.getIsUser());
            assertTrue(result.getContent().contains("AI處理錯誤"));
            assertTrue(result.getContent().contains("AI service error"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testReplyStreamWithDatabase_Success() throws Exception {
        // Arrange
        List<ChatMessage> chatHistory = Arrays.asList(createTestChatMessage("msg1", "Hello", true));
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        String emotionPrediction = "情緒：積極";
        ChatMessage aiMessage = createTestChatMessage(TEST_AI_MESSAGE_ID, "", false);

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(chatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn(emotionPrediction);
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(aiMessage);

        // Mock ResponseStream
        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("AI token");

            // Act
            SseEmitter result = plainChatService.replyStreamWithDatabase(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID);

            // Assert
            assertNotNull(result);

            // Wait for async processing to complete
            Thread.sleep(1000);

            verify(registry).getSession(TEST_CHATROOM_ID);
            verify(chatMessageService).getMessagesByChatroomId(TEST_CHATROOM_ID);
            verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
            verify(messageFormattingService).getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);
        }
    }

    @Test
    void testReplyStreamWithDatabase_WithoutEmotionPrediction() throws Exception {
        // Arrange
        List<ChatMessage> chatHistory = Arrays.asList();
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        ChatMessage aiMessage = createTestChatMessage(TEST_AI_MESSAGE_ID, "", false);

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(chatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn("");
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(formattedMessage)).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("Response");

            // Act
            SseEmitter result = plainChatService.replyStreamWithDatabase(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID);

            // Assert
            assertNotNull(result);
            Thread.sleep(1000);

            verify(chatSession).sendMessageStream(formattedMessage); // 應該使用原始 formattedMessage，沒有情緒預測
        }
    }

    @Test
    void testReplyStreamWithDatabase_EmotionPredictionException() throws Exception {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        ChatMessage aiMessage = createTestChatMessage(TEST_AI_MESSAGE_ID, "", false);

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(Arrays.asList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenThrow(new RuntimeException("Emotion prediction failed"));
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(formattedMessage)).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("Response");

            // Act
            SseEmitter result = plainChatService.replyStreamWithDatabase(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID);

            // Assert
            assertNotNull(result);
            Thread.sleep(1000);

            // 應該仍然繼續處理，不受情緒預測失敗影響
            verify(chatSession).sendMessageStream(formattedMessage);
        }
    }

    @Test
    void testReplyStreamWithDatabase_StreamException() throws Exception {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        ChatMessage aiMessage = createTestChatMessage(TEST_AI_MESSAGE_ID, "", false);
        RuntimeException streamException = new RuntimeException("Stream error");

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(Arrays.asList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn("");
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(aiMessage);
        when(chatSession.sendMessageStream(formattedMessage)).thenThrow(streamException);

        // Act
        SseEmitter result = plainChatService.replyStreamWithDatabase(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(1000);

        // 驗證錯誤處理
        verify(chatMessageService).updateMessage(argThat(msg ->
                msg.getContent().contains("AI處理錯誤") &&
                        msg.getContent().contains("Stream error")));
    }

    @Test
    void testReplyStreamWithDatabase_DatabaseUpdateException() throws Exception {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        ChatMessage aiMessage = createTestChatMessage(TEST_AI_MESSAGE_ID, "", false);

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(Arrays.asList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn("");
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("Database update failed"));

        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(formattedMessage)).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("Response");

            // Act
            SseEmitter result = plainChatService.replyStreamWithDatabase(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID);

            // Assert
            assertNotNull(result);
            Thread.sleep(1000);

            // 應該嘗試更新資料庫，即使失敗也不影響SSE
            verify(chatMessageService).updateMessage(any(ChatMessage.class));
        }
    }

    @Test
    void testReplyStream_Success() throws Exception {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        String emotionPrediction = "情緒：中性";

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn(emotionPrediction);
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);

        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("Token");

            // Act
            SseEmitter result = plainChatService.replyStream(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);

            // Assert
            assertNotNull(result);
            Thread.sleep(1000);

            verify(registry).getSession(TEST_CHATROOM_ID);
            verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
            verify(messageFormattingService).getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);
        }
    }

    @Test
    void testReplyStream_WithException() throws Exception {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;
        RuntimeException streamException = new RuntimeException("Stream failed");

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn("");
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatSession.sendMessageStream(formattedMessage)).thenThrow(streamException);

        // Act
        SseEmitter result = plainChatService.replyStream(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);

        // Assert
        assertNotNull(result);
        Thread.sleep(1000);

        // 驗證異常處理（無法直接驗證 emitter.completeWithError，但可以驗證調用）
        verify(chatSession).sendMessageStream(formattedMessage);
    }

    @Test
    void testReplyStream_NullEmotionPredictionService() throws Exception {
        // Arrange - 設置 emotionPredictionChatService 為 null
        ReflectionTestUtils.setField(plainChatService, "emotionPredictionChatService", null);

        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);

        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(formattedMessage)).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("Token");

            // Act
            SseEmitter result = plainChatService.replyStream(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);

            // Assert
            assertNotNull(result);
            Thread.sleep(1000);

            verify(registry).getSession(TEST_CHATROOM_ID);
            verify(messageFormattingService).getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION);
            // 不應該嘗試調用情緒預測服務
            verify(chatSession).sendMessageStream(formattedMessage);
        }
    }

    @Test
    void testReplyStreamWithDatabase_MessageNotFound() throws Exception {
        // Arrange
        String formattedMessage = "Formatted: " + TEST_USER_MESSAGE;

        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(Arrays.asList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE)).thenReturn("");
        when(messageFormattingService.getFullMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION))
                .thenReturn(formattedMessage);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.empty());

        Iterator<GenerateContentResponse> mockIterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(mockIterator);
        when(chatSession.sendMessageStream(formattedMessage)).thenReturn(responseStream);

        try (MockedStatic<ResponseHandler> responseHandlerMock = mockStatic(ResponseHandler.class)) {
            responseHandlerMock.when(() -> ResponseHandler.getText(generateContentResponse)).thenReturn("Response");

            // Act
            SseEmitter result = plainChatService.replyStreamWithDatabase(TEST_USER_MESSAGE, TEST_CHATROOM_ID,
                    TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID);

            // Assert
            assertNotNull(result);
            Thread.sleep(1000);

            verify(chatMessageService).getMessageById(TEST_AI_MESSAGE_ID);
            // 不應該調用 updateMessage，因為找不到訊息
            verify(chatMessageService, never()).updateMessage(any(ChatMessage.class));
        }
    }


    @Test
    void replyStream_withAllParameters() throws Exception {
        String userMessage = "Hello";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;
        GenerateContentResponse response1 = createMockResponse("Token1");
        GenerateContentResponse response2 = createMockResponse("Token2");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response1, response2).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, conversationCount, hasDefaultQuestion);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_withNullOptionalParameters() throws Exception {
        String userMessage = "Hello";
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_withOnlyConversationCount() throws Exception {
        String userMessage = "Hello";
        Integer conversationCount = 5;
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, conversationCount, null);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_withOnlyHasDefaultQuestion() throws Exception {
        String userMessage = "Hello";
        Boolean hasDefaultQuestion = false;
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, hasDefaultQuestion);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_whenSendMessageStreamThrows() throws Exception {
        String userMessage = "Error message";
        when(chatSession.sendMessageStream(anyString()))
                .thenThrow(new RuntimeException("Stream error"));
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    private GenerateContentResponse createMockResponse(String text) {
        GenerateContentResponse response = mock(GenerateContentResponse.class);
        Candidate candidate = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn(text);
        when(content.getPartsList()).thenReturn(Arrays.asList(part));
        when(candidate.getContent()).thenReturn(content);
        when(response.getCandidatesCount()).thenReturn(1);
        when(response.getCandidates(0)).thenReturn(candidate);
        return response;
    }

    @Test
    void replyStreamWithPersonality_withAllParameters() throws Exception {
        String userMessage = "Hello";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;
        GenerateContentResponse response1 = createMockResponse("Token1");
        GenerateContentResponse response2 = createMockResponse("Token2");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response1, response2).iterator());

        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, conversationCount, hasDefaultQuestion, PERSONALITY);

        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStreamWithPersonality_withNullOptionalParameters() throws Exception {
        String userMessage = "Hello";
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());

        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null, PERSONALITY);

        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, null, null);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStreamWithPersonality_withOnlyConversationCount() throws Exception {
        String userMessage = "Hello";
        Integer conversationCount = 5;
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());

        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, conversationCount, null, PERSONALITY);

        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, conversationCount, null);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStreamWithPersonality_withOnlyHasDefaultQuestion() throws Exception {
        String userMessage = "Hello";
        Boolean hasDefaultQuestion = false;
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, hasDefaultQuestion, PERSONALITY);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, null, hasDefaultQuestion);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStreamWithPersonality_whenEmotionPredictionThrows() throws Exception {
        String userMessage = "Error message";
        when(emotionPredictionChatService.processChat(anyString()))
                .thenThrow(new RuntimeException("Emotion prediction failed"));
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null, PERSONALITY);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        Thread.sleep(100);
    }

    @Test
    void replyStreamWithPersonality_whenMessageFormattingThrows() throws Exception {
        String userMessage = "Error message";
        when(messageFormattingService.getFullMessage(anyString(), any(), any())).thenThrow(new RuntimeException("Message formatting failed"));
        assertThrows(RuntimeException.class, () -> {
            service.replyStream(userMessage, SESSION_ID, null, null, PERSONALITY);
        });
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, null, null);
    }

    @Test
    void replyStreamWithPersonality_whenSendMessageStreamThrows() throws Exception {
        String userMessage = "Error message";
        when(chatSession.sendMessageStream(anyString())).thenThrow(new RuntimeException("Stream error"));
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null, PERSONALITY);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, null, null);
        Thread.sleep(200);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStreamWithPersonality_streamIteratorException() throws Exception {
        String userMessage = "Hello";
        when(mockResponseStream.iterator()).thenThrow(new RuntimeException("Stream iteration failed"));
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null, PERSONALITY);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID, PERSONALITY);
        verify(emotionPredictionChatService).processChat(userMessage);
        verify(messageFormattingService).getFullMessage(userMessage, null, null);
        Thread.sleep(300);
        verify(chatSession).sendMessageStream(anyString());
    }
}