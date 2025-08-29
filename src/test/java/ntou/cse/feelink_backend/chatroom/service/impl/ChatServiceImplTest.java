package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.ChatInputProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private DrawingAnalysisChatServiceImpl drawingAnalysisChatService;

    @Mock
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private MessageFormattingService messageFormattingService;

    @Mock
    private ChatSessionRegistry registry;

    @Mock
    private ChatSession chatSession;

    @Mock
    private ResponseStream<GenerateContentResponse> responseStream;

    @Mock
    private GenerateContentResponse generateContentResponse;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private ChatServiceImpl chatService;

    private static final String TEST_CHATROOM_ID = "test-chatroom-123";
    private static final String TEST_USER_MESSAGE = "這是一個測試訊息";
    private static final String TEST_AI_MESSAGE_ID = "ai-msg-456";
    private static final String TEST_PERSONALITY = "friendly";

    @BeforeEach
    void setUp() {
        // 使用 ReflectionTestUtils 設置私有欄位
        ReflectionTestUtils.setField(chatService, "drawingAnalysisChatService", drawingAnalysisChatService);
        ReflectionTestUtils.setField(chatService, "emotionPredictionChatService", emotionPredictionChatService);
        ReflectionTestUtils.setField(chatService, "chatMessageService", chatMessageService);
        ReflectionTestUtils.setField(chatService, "messageFormattingService", messageFormattingService);
        ReflectionTestUtils.setField(chatService, "registry", registry);
    }

    @Test
    void testProcessChat_Success() throws IOException {
        // Arrange
        List<ChatMessage> mockChatHistory = createMockChatHistory();
        String mockEmotionPrediction = "正面情緒";
        String mockDrawingAnalysisResponse = "這是AI的分析結果";

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(mockChatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn(mockEmotionPrediction);
        when(drawingAnalysisChatService.processChat(eq(mockFile), contains(mockEmotionPrediction), eq(TEST_CHATROOM_ID)))
                .thenReturn(mockDrawingAnalysisResponse);

        // Act
        ChatMessage result = chatService.processChat(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID);

        // Assert
        assertNotNull(result);
        assertEquals(mockDrawingAnalysisResponse, result.getContent());
        assertFalse(result.getIsUser());

        // Verify interactions
        verify(chatMessageService).getMessagesByChatroomId(TEST_CHATROOM_ID);
        verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
        verify(drawingAnalysisChatService).processChat(eq(mockFile),
                eq(TEST_USER_MESSAGE + "\n\n先前的情緒預測結果：" + mockEmotionPrediction),
                eq(TEST_CHATROOM_ID));
    }

    @Test
    void testProcessChat_EmotionPredictionFails() throws IOException {
        // Arrange
        List<ChatMessage> mockChatHistory = createMockChatHistory();

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(mockChatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenThrow(new RuntimeException("情緒預測失敗"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                chatService.processChat(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID));

        // Verify that drawingAnalysisChatService is never called when emotion prediction fails
        verify(drawingAnalysisChatService, never()).processChat(any(), any(), any());
    }

    @Test
    void testProcessChat_DrawingAnalysisFails() throws IOException {
        // Arrange
        String mockEmotionPrediction = "正面情緒";

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(Collections.emptyList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn(mockEmotionPrediction);
        when(drawingAnalysisChatService.processChat(any(), any(), any()))
                .thenThrow(new IOException("繪圖分析失敗"));

        // Act & Assert
        assertThrows(IOException.class, () ->
                chatService.processChat(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID));
    }

    @Test
    void testProcessChatStream_Success() throws IOException, InterruptedException {
        // Arrange
        List<ChatMessage> mockChatHistory = createMockChatHistory();
        String mockEmotionPrediction = "正面情緒";
        ChatMessage mockAiMessage = new ChatMessage();
        mockAiMessage.setContent("");
        mockAiMessage.setIsUser(false);

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(mockChatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn(mockEmotionPrediction);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);

        // Mock the response stream
        setupMockResponseStream("Token1", "Token2");
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);

        // Wait for asynchronous processing
        Thread.sleep(500);

        // Verify interactions
        verify(chatMessageService).getMessagesByChatroomId(TEST_CHATROOM_ID);
        verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
        verify(registry, timeout(1000)).getSession(TEST_CHATROOM_ID);
        verify(chatMessageService, timeout(1000)).getMessageById(TEST_AI_MESSAGE_ID);
        verify(chatMessageService, timeout(1000)).updateMessage(mockAiMessage);
    }

    @Test
    void testProcessChatStreamWithPersonality_Success() throws IOException, InterruptedException {
        // Arrange
        List<ChatMessage> mockChatHistory = createMockChatHistory();
        String mockEmotionPrediction = "正面情緒";
        ChatMessage mockAiMessage = new ChatMessage();
        mockAiMessage.setContent("");

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(mockChatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn(mockEmotionPrediction);
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(registry.getSession(TEST_CHATROOM_ID, TEST_PERSONALITY)).thenReturn(chatSession);

        setupMockResponseStream("Hello", " World");
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        // Act
        SseEmitter result = chatService.processChatStreamWithPersonality(
                mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_PERSONALITY, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(500);

        verify(registry, timeout(1000)).getSession(TEST_CHATROOM_ID, TEST_PERSONALITY);
        verify(chatMessageService, timeout(1000)).updateMessage(mockAiMessage);
    }

    @Test
    void testProcessChatStreamWithPersonality_DefaultPersonality() throws IOException, InterruptedException {
        // Arrange
        ChatMessage mockAiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(Collections.emptyList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn("中性");
        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);

        setupMockResponseStream("Default", " response");
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        // Act
        SseEmitter result = chatService.processChatStreamWithPersonality(
                mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, "default", TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(500);

        // Should use default session (without personality parameter)
        verify(registry, timeout(1000)).getSession(TEST_CHATROOM_ID);
        verify(registry, never()).getSession(eq(TEST_CHATROOM_ID), anyString());
    }

    @Test
    void testProcessChatStream_EmotionPredictionFails() throws IOException, InterruptedException {
        // Arrange
        List<ChatMessage> mockChatHistory = createMockChatHistory();
        ChatMessage mockAiMessage = new ChatMessage();
        mockAiMessage.setContent("");

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(mockChatHistory);
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenThrow(new RuntimeException("情緒預測失敗"));
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);

        setupMockResponseStream("處理", "完成");
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(500);

        // Should still proceed with stream processing despite emotion prediction failure
        verify(registry, timeout(1000)).getSession(TEST_CHATROOM_ID);
        verify(chatSession, timeout(1000)).sendMessageStream(TEST_USER_MESSAGE);
    }

    @Test
    void testProcessChatStream_StreamError() throws IOException, InterruptedException {
        // Arrange
        ChatMessage mockAiMessage = new ChatMessage();
        mockAiMessage.setContent("");

        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(Collections.emptyList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn("正面情緒");
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);

        // Mock stream to throw exception
        when(chatSession.sendMessageStream(anyString()))
                .thenThrow(new RuntimeException("串流處理錯誤"));

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(500);

        // Verify error message is updated to database
        verify(chatMessageService, timeout(1000)).updateMessage(argThat(message ->
                message.getContent().contains("AI處理錯誤")));
    }

    @Test
    void testProcessChatStream_EmotionPredictionServiceNull() throws IOException, InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(chatService, "emotionPredictionChatService", null);

        ChatMessage mockAiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(Collections.emptyList());
        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);

        setupMockResponseStream("No", " emotion");
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(500);

        // Should proceed without emotion prediction
        verify(registry, timeout(1000)).getSession(TEST_CHATROOM_ID);
        verify(chatSession, timeout(1000)).sendMessageStream(TEST_USER_MESSAGE);
    }

    @Test
    void testProcessChatStream_ImageProcessingFails() throws IOException, InterruptedException {
        // Arrange
        ChatMessage mockAiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID))
                .thenReturn(Optional.of(mockAiMessage));
        when(chatMessageService.getMessagesByChatroomId(TEST_CHATROOM_ID))
                .thenReturn(Collections.emptyList());
        when(emotionPredictionChatService.processChat(TEST_USER_MESSAGE))
                .thenReturn("正面");
        when(registry.getSession(TEST_CHATROOM_ID)).thenReturn(chatSession);
        when(mockFile.isEmpty()).thenReturn(false);

        setupMockResponseStream("Text", " only");

        try (MockedStatic<ChatInputProcessor> mockedProcessor = Mockito.mockStatic(ChatInputProcessor.class)) {
            mockedProcessor.when(() -> ChatInputProcessor.processImage(mockFile))
                    .thenThrow(new RuntimeException("圖片處理失敗"));
            when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);

            // Act
            SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

            // Assert
            assertNotNull(result);
            Thread.sleep(500);

            // Should proceed with text-only processing when image processing fails
            verify(chatSession, timeout(1000)).sendMessageStream(anyString());
        }
    }

    private void setupMockResponseStream(String... tokens) {
        Iterator<GenerateContentResponse> iterator = Arrays.stream(tokens)
                .map(token -> {
                    GenerateContentResponse response = mock(GenerateContentResponse.class);
                    try (MockedStatic<com.google.cloud.vertexai.generativeai.ResponseHandler> mockedHandler =
                                 Mockito.mockStatic(com.google.cloud.vertexai.generativeai.ResponseHandler.class)) {
                        mockedHandler.when(() -> com.google.cloud.vertexai.generativeai.ResponseHandler.getText(response))
                                .thenReturn(token);
                    }
                    return response;
                })
                .iterator();

        when(responseStream.iterator()).thenReturn(iterator);
    }

    private List<ChatMessage> createMockChatHistory() {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setContent("用戶訊息");
        userMsg.setIsUser(true);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應");
        aiMsg.setIsUser(false);

        return Arrays.asList(userMsg, aiMsg);
    }
}