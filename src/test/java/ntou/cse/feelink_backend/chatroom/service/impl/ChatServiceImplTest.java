package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private MultipartFile mockFile;

    @InjectMocks
    private ChatServiceImpl chatService;

    private static final String TEST_CHATROOM_ID = "test-chatroom-123";
    private static final String TEST_USER_MESSAGE = "這是一個測試訊息";
    private static final String TEST_AI_MESSAGE_ID = "ai-msg-456";

    @BeforeEach
    void setUp() {
        // 使用 ReflectionTestUtils 設置私有欄位
        ReflectionTestUtils.setField(chatService, "drawingAnalysisChatService", drawingAnalysisChatService);
        ReflectionTestUtils.setField(chatService, "emotionPredictionChatService", emotionPredictionChatService);
        ReflectionTestUtils.setField(chatService, "chatMessageService", chatMessageService);
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
        // 移除不必要的 drawingAnalysisChatService stubbing，因為當情緒預測失敗時不會被調用

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

        // 模擬 drawingAnalysisChatService.processChatStream 的行為
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> onToken = (java.util.function.Consumer<String>) args[3];
            Runnable onComplete = (Runnable) args[4];

            // 模擬發送一些 token
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    onToken.accept("Token1");
                    Thread.sleep(50);
                    onToken.accept("Token2");
                    Thread.sleep(50);
                    onComplete.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return null;
        }).when(drawingAnalysisChatService).processChatStream(
                eq(mockFile),
                contains(mockEmotionPrediction),
                eq(TEST_CHATROOM_ID),
                any(), any(), any()
        );

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);

        // 等待一段時間讓非同步處理完成
        Thread.sleep(500);

        // Verify interactions
        verify(chatMessageService).getMessagesByChatroomId(TEST_CHATROOM_ID);
        verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
        verify(chatMessageService, timeout(1000)).getMessageById(TEST_AI_MESSAGE_ID);
        verify(chatMessageService, timeout(1000)).updateMessage(mockAiMessage);

        // 驗證 AI 訊息的內容被更新
        assertEquals("Token1Token2", mockAiMessage.getContent());
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

        // 模擬正常的串流處理
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> onToken = (java.util.function.Consumer<String>) args[3];
            Runnable onComplete = (Runnable) args[4];

            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    onToken.accept("處理完成");
                    Thread.sleep(50);
                    onComplete.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return null;
        }).when(drawingAnalysisChatService).processChatStream(
                eq(mockFile),
                eq(TEST_USER_MESSAGE), // 沒有情緒預測結果的情況
                eq(TEST_CHATROOM_ID),
                any(), any(), any()
        );

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);

        // 等待處理完成
        Thread.sleep(500);

        // 驗證即使情緒預測失敗，串流處理仍然進行
        verify(drawingAnalysisChatService, timeout(1000)).processChatStream(
                eq(mockFile),
                eq(TEST_USER_MESSAGE),
                eq(TEST_CHATROOM_ID),
                any(), any(), any()
        );
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

        // 模擬串流處理錯誤
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Exception> onError = (java.util.function.Consumer<Exception>) args[5];

            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    onError.accept(new RuntimeException("串流處理錯誤"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return null;
        }).when(drawingAnalysisChatService).processChatStream(
                any(), any(), any(), any(), any(), any()
        );

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);

        // 等待錯誤處理完成
        Thread.sleep(500);

        // 驗證錯誤訊息被更新到資料庫
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

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Runnable onComplete = (Runnable) args[4];
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    onComplete.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(drawingAnalysisChatService).processChatStream(
                eq(mockFile), eq(TEST_USER_MESSAGE), eq(TEST_CHATROOM_ID),
                any(), any(), any()
        );

        // Act
        SseEmitter result = chatService.processChatStream(mockFile, TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_AI_MESSAGE_ID);

        // Assert
        assertNotNull(result);
        Thread.sleep(300);

        // 驗證當 emotionPredictionChatService 為 null 時不會呼叫情緒預測
        verify(drawingAnalysisChatService, timeout(1000)).processChatStream(
                eq(mockFile), eq(TEST_USER_MESSAGE), eq(TEST_CHATROOM_ID),
                any(), any(), any()
        );
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

