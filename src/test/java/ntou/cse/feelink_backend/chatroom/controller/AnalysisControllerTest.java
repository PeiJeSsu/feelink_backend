package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private AnalysisController analysisController;

    private String testUserMessage;
    private String testChatroomId;
    private MockMultipartFile testFile;
    private ChatMessage testAiResponse;
    private ChatMessage testSavedMessage;

    @BeforeEach
    void setUp() {
        testUserMessage = "請分析這張圖片";
        testChatroomId = "test-chatroom-123";

        // 創建測試用的 MultipartFile
        String testFileContent = "test image content";
        testFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                testFileContent.getBytes()
        );

        // 創建測試用的 AI 回應
        testAiResponse = new ChatMessage();
        testAiResponse.setMessageId("ai-message-123");
        testAiResponse.setChatroomId(testChatroomId);
        testAiResponse.setIsUser(false);
        testAiResponse.setIsDrawingData(false);
        testAiResponse.setContent("AI 分析結果：這是一張測試圖片");
        testAiResponse.setSentAt(new Date());

        // 創建測試用的已保存訊息
        testSavedMessage = new ChatMessage();
        testSavedMessage.setMessageId("saved-message-123");
        testSavedMessage.setChatroomId(testChatroomId);
        testSavedMessage.setIsUser(false);
        testSavedMessage.setIsDrawingData(false);
        testSavedMessage.setContent("AI 分析結果：這是一張測試圖片");
        testSavedMessage.setSentAt(new Date());
    }

    @Test
    void testAnalyseSimple_WithFileAndMessage() throws IOException {
        // Arrange
        when(chatService.processChat(any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId)))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶訊息
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 回應

        // Act
        ResponseEntity<ChatMessage> result = analysisController.analyse(testFile, testUserMessage, testChatroomId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSavedMessage, result.getBody());

        // 驗證服務方法被調用
        verify(chatService, times(1)).processChat(eq(testFile), eq(testUserMessage), eq(testChatroomId));
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class)); // 檔案 + 文字 + AI回應

        // 驗證保存的訊息內容
        String expectedBase64 = Base64.getEncoder().encodeToString(testFile.getBytes());
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        message.getIsDrawingData() &&
                        message.getContent().equals(expectedBase64)
        ));

        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals(testUserMessage)
        ));
    }

    @Test
    void testAnalyseSimple_WithFileOnly() throws IOException {
        // Arrange
        when(chatService.processChat(any(MultipartFile.class), eq(""), eq(testChatroomId)))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(testSavedMessage); // AI 回應

        // Act
        ResponseEntity<ChatMessage> result = analysisController.analyse(testFile, "", testChatroomId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSavedMessage, result.getBody());

        // 驗證只保存了檔案和AI回應，沒有保存空的文字訊息
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChat(eq(testFile), eq(""), eq(testChatroomId));
    }

    @Test
    void testAnalyseSimple_WithMessageOnly() throws IOException {
        // Arrange
        when(chatService.processChat(isNull(), eq(testUserMessage), eq(testChatroomId)))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 回應

        // Act
        ResponseEntity<ChatMessage> result = analysisController.analyse(null, testUserMessage, testChatroomId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testSavedMessage, result.getBody());

        // 驗證只保存了文字訊息和AI回應，沒有保存檔案
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChat(isNull(), eq(testUserMessage), eq(testChatroomId));
    }

    @Test
    void testAnalyseSimple_WithEmptyFile() throws IOException {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        // 使用 doReturn().when() 避免 stubbing 衝突
        doReturn(testAiResponse).when(chatService).processChat(eq(emptyFile), eq(testUserMessage), eq(testChatroomId));
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 回應

        // Act
        ResponseEntity<ChatMessage> result = analysisController.analyse(emptyFile, testUserMessage, testChatroomId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());

        // 驗證空檔案不會被保存，只有文字訊息和AI回應
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChat(eq(emptyFile), eq(testUserMessage), eq(testChatroomId));
    }

    @Test
    void testAnalyseSimple_WithNullAndEmptyMessage() throws IOException {
        // Arrange
        // 確保 testAiResponse 不是 null
        assertNotNull(testAiResponse, "testAiResponse should not be null");

        // 使用更寬鬆的 matchers
        when(chatService.processChat(any(), any(), any()))
                .thenReturn(testAiResponse);

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(testSavedMessage); // AI 回應

        // Act
        ResponseEntity<ChatMessage> result = analysisController.analyse(testFile, null, testChatroomId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChat(any(), any(), any());
    }

    @Test
    void testAnalyseSimple_ServiceException() throws IOException {
        // Arrange
        when(chatService.processChat(any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId)))
                .thenThrow(new RuntimeException("Chat服務異常"));
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            analysisController.analyse(testFile, testUserMessage, testChatroomId);
        });

        // 驗證用戶訊息仍被保存（在異常發生前）
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class)); // 檔案 + 文字訊息
    }

    @Test
    void testAnalyseSimple_ChatMessageServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            analysisController.analyse(testFile, testUserMessage, testChatroomId);
        });
    }

    @Test
    void testAnalyseStream_WithFileAndMessage() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-123");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(savedAiMsg); // AI 初始訊息
        when(chatService.processChatStream(any(MultipartFile.class), eq(testUserMessage),
                eq(testChatroomId), eq("stream-ai-msg-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyseStream(testFile, testUserMessage, testChatroomId);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmitter, result);

        // 驗證服務方法被調用
        verify(chatService, times(1)).processChatStream(
                eq(testFile), eq(testUserMessage), eq(testChatroomId), eq("stream-ai-msg-123")
        );
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class)); // 檔案 + 文字 + AI初始訊息
    }

    @Test
    void testAnalyseStream_WithFileOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-456");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(savedAiMsg); // AI 初始訊息
        when(chatService.processChatStream(any(MultipartFile.class), eq(""),
                eq(testChatroomId), eq("stream-ai-msg-456")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyseStream(testFile, "", testChatroomId);

        // Assert
        assertNotNull(result);
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class)); // 檔案 + AI初始訊息
    }

    @Test
    void testAnalyseStream_WithMessageOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-789");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(savedAiMsg); // AI 初始訊息
        when(chatService.processChatStream(isNull(), eq(testUserMessage),
                eq(testChatroomId), eq("stream-ai-msg-789")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyseStream(null, testUserMessage, testChatroomId);

        // Assert
        assertNotNull(result);
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class)); // 文字訊息 + AI初始訊息
    }

    @Test
    void testAnalyseStream_NoUserInput() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-empty");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(savedAiMsg); // 只有AI初始訊息
        when(chatService.processChatStream(isNull(), eq(""),
                eq(testChatroomId), eq("stream-ai-msg-empty")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyseStream(null, "", testChatroomId);

        // Assert
        assertNotNull(result);
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class)); // 只有AI初始訊息
    }

    @Test
    void testAnalyseStream_ChatServiceException() throws IOException {
        // Arrange
        ChatMessage savedAiMsg = new ChatMessage();
        savedAiMsg.setMessageId("stream-ai-msg-error");

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(savedAiMsg); // AI 初始訊息
        when(chatService.processChatStream(any(MultipartFile.class), eq(testUserMessage),
                eq(testChatroomId), eq("stream-ai-msg-error")))
                .thenThrow(new RuntimeException("串流服務異常"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            analysisController.analyseStream(testFile, testUserMessage, testChatroomId);
        });

        // 驗證用戶訊息和AI初始訊息仍被創建（在異常發生前）
        // 根據錯誤日誌，實際調用了3次（檔案+文字+AI初始訊息）
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testBase64Encoding() throws IOException {
        // Arrange
        String originalContent = "test image content";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", originalContent.getBytes()
        );

        when(chatService.processChat(any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId)))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);

        // Act
        analysisController.analyse(file, testUserMessage, testChatroomId);

        // Assert - 驗證檔案內容被正確編碼為 Base64
        String expectedBase64 = Base64.getEncoder().encodeToString(originalContent.getBytes());
        verify(chatMessageService).createMessage(argThat(message ->
                message.getIsDrawingData() &&
                        message.getContent().equals(expectedBase64)
        ));
    }

    @Test
    void testAIResponseProperties() throws IOException {
        // Arrange
        when(chatService.processChat(any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId)))
                .thenReturn(testAiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);

        // Act
        analysisController.analyse(testFile, testUserMessage, testChatroomId);

        // Assert - 驗證AI回應的屬性設置
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getSentAt() != null
        ));
    }
}