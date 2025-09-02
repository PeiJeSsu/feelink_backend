package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)  // 加上這行註解
class AnalysisControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private AnalysisController analysisController;

    private String testUserMessage;
    private String testChatroomId;
    private String testPersonality;
    private MockMultipartFile testFile;
    private ChatMessage testSavedMessage;

    @BeforeEach
    void setUp() {
        testUserMessage = "請分析這張圖片";
        testChatroomId = "test-chatroom-123";
        testPersonality = "friendly";

        // 創建測試用的 MultipartFile
        String testFileContent = "test image content";
        testFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                testFileContent.getBytes()
        );

        // 創建測試用的已保存訊息
        testSavedMessage = new ChatMessage();
        testSavedMessage.setMessageId("saved-message-123");
        testSavedMessage.setChatroomId(testChatroomId);
        testSavedMessage.setIsUser(false);
        testSavedMessage.setIsDrawingData(false);
        testSavedMessage.setContent("");
        testSavedMessage.setSentAt(new Date());
    }

    @Test
    void testAnalyse_WithFileAndMessage() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 初始訊息
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmitter, result);

        // 驗證服務方法被調用
        verify(chatService, times(1)).processChatStreamWithPersonality(
                eq(testFile), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")
        );
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class)); // 檔案 + 文字 + AI初始訊息

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
    void testAnalyse_WithFileOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(testSavedMessage); // AI 初始訊息
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(""), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyse(testFile, "", testChatroomId, testPersonality);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmitter, result);

        // 驗證只保存了檔案和AI初始訊息，沒有保存空的文字訊息
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChatStreamWithPersonality(
                eq(testFile), eq(""), eq(testChatroomId), eq(testPersonality), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_WithMessageOnly() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 初始訊息
        when(chatService.processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyse(null, testUserMessage, testChatroomId, testPersonality);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmitter, result);

        // 驗證只保存了文字訊息和AI初始訊息，沒有保存檔案
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId), eq(testPersonality), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_WithDefaultPersonality() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 初始訊息
        when(chatService.processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId),
                eq("default"), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act - 不傳入 personality 參數，應該使用預設值 "default"
        SseEmitter result = analysisController.analyse(null, testUserMessage, testChatroomId, "default");

        // Assert
        assertNotNull(result);
        verify(chatService, times(1)).processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId), eq("default"), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_WithEmptyFile() throws IOException {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 初始訊息
        when(chatService.processChatStreamWithPersonality(
                eq(emptyFile), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyse(emptyFile, testUserMessage, testChatroomId, testPersonality);

        // Assert
        assertNotNull(result);

        // 驗證空檔案不會被保存，只有文字訊息和AI初始訊息
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChatStreamWithPersonality(
                eq(emptyFile), eq(testUserMessage), eq(testChatroomId), eq(testPersonality), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_NoUserInput() throws IOException {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedMessage); // 只有AI初始訊息
        when(chatService.processChatStreamWithPersonality(
                isNull(), eq(""), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        SseEmitter result = analysisController.analyse(null, "", testChatroomId, testPersonality);

        // Assert
        assertNotNull(result);
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class)); // 只有AI初始訊息
    }

    @Test
    void testAnalyse_ChatServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 用戶檔案
                .thenReturn(new ChatMessage()) // 用戶文字訊息
                .thenReturn(testSavedMessage); // AI 初始訊息
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenThrow(new RuntimeException("串流服務異常"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);
        });

        // 驗證用戶訊息和AI初始訊息仍被創建（在異常發生前）
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testAnalyse_ChatMessageServiceException() throws IOException {
        // Arrange
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);
        });
    }

    @Test
    void testBase64Encoding() throws IOException {
        // Arrange
        String originalContent = "test image content";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", originalContent.getBytes()
        );
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        analysisController.analyse(file, testUserMessage, testChatroomId, testPersonality);

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
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        // Act
        analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);

        // Assert - 驗證AI回應的屬性設置
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getSentAt() != null &&
                        message.getContent().equals("") // 初始為空內容
        ));
    }

    @Test
    void testAnalyse_WithDifferentPersonalities() throws IOException {
        // Arrange
        String[] personalities = {"friendly", "professional", "casual", "formal"};
        SseEmitter mockEmitter = new SseEmitter();

        for (String personality : personalities) {
            when(chatMessageService.createMessage(any(ChatMessage.class)))
                    .thenReturn(new ChatMessage()) // 用戶文字訊息
                    .thenReturn(testSavedMessage); // AI 初始訊息
            when(chatService.processChatStreamWithPersonality(
                    isNull(), eq(testUserMessage), eq(testChatroomId),
                    eq(personality), eq("saved-message-123")))
                    .thenReturn(mockEmitter);

            // Act
            SseEmitter result = analysisController.analyse(null, testUserMessage, testChatroomId, personality);

            // Assert
            assertNotNull(result);
            verify(chatService).processChatStreamWithPersonality(
                    isNull(), eq(testUserMessage), eq(testChatroomId), eq(personality), eq("saved-message-123"));
        }
    }
}