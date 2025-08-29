package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.AIDrawingRequest;
import ntou.cse.feelink_backend.chatroom.model.AIDrawingResponse;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.GeminiService;
import ntou.cse.feelink_backend.chatroom.service.RemoveBgService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIDrawingControllerTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private RemoveBgService removeBgService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private AIDrawingController aiDrawingController;

    private AIDrawingRequest testRequest;
    private GeminiResponse testGeminiResponse;
    private String testImageData;
    private String testChatroomId;
    private String testMode;

    @BeforeEach
    void setUp() {
        testChatroomId = "test-chatroom-123";
        testImageData = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        testMode = "create";

        testRequest = new AIDrawingRequest();
        testRequest.setText("測試提示文字");
        testRequest.setImageData(testImageData);
        testRequest.setChatroomId(testChatroomId);
        testRequest.setRemoveBackground(false);
        testRequest.setMode(testMode);

        testGeminiResponse = new GeminiResponse();
        testGeminiResponse.setSuccess(true);
        testGeminiResponse.setMessage("AI生成的文字回應");
        testGeminiResponse.setImageData("base64EncodedImageData");
    }

    @Test
    void testGenerateImageSimple_Success() {
        // Arrange
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testGeminiResponse, response.getBody().getContent());

        // 驗證服務方法被調用，現在包含 mode 參數
        verify(geminiService, times(1)).generateImage(eq("測試提示文字"), eq(testImageData), eq(testMode));
        verify(chatMessageService, times(4)).createMessage(any(ChatMessage.class)); // 用戶畫布+用戶文字+AI文字+AI圖片
    }

    @Test
    void testGenerateImageSimple_WithBackgroundRemoval() {
        // Arrange
        testRequest.setRemoveBackground(true);
        byte[] mockRemovedBgBytes = "removedBgImageData".getBytes();
        String expectedBase64 = Base64.getEncoder().encodeToString(mockRemovedBgBytes);

        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(removeBgService.removeBackground(any())).thenReturn(Mono.just(mockRemovedBgBytes));
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(expectedBase64, response.getBody().getContent().getImageData());

        verify(removeBgService, times(1)).removeBackground(any());
        verify(geminiService, times(1)).generateImage(anyString(), anyString(), eq(testMode));
    }

    @Test
    void testGenerateImageSimple_WithBackgroundRemovalError() {
        // Arrange
        testRequest.setRemoveBackground(true);
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(removeBgService.removeBackground(any())).thenReturn(Mono.error(new RuntimeException("去背失敗")));
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act & Assert - 去背錯誤應該不影響主要功能，仍然返回成功
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        // 即使去背失敗，也應該返回原始圖片
        assertEquals("base64EncodedImageData", response.getBody().getContent().getImageData());

        // 驗證去背服務被調用但失敗
        verify(removeBgService, times(1)).removeBackground(any());
    }

    @Test
    void testGenerateImageSimple_WithEmptyPrompt() {
        // Arrange
        testRequest.setText(""); // 空的提示文字
        when(geminiService.generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode)))
                .thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // 驗證使用默認提示文字
        verify(geminiService, times(1)).generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode));
        // 應該保存畫布數據+AI文字回應+AI圖片回應，不保存空的文字提示
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_WithNullPrompt() {
        // Arrange
        testRequest.setText(null); // null 的提示文字
        when(geminiService.generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode)))
                .thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // 驗證使用默認提示文字
        verify(geminiService, times(1)).generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode));
        // 應該保存畫布數據+AI文字回應+AI圖片回應，不保存null的文字提示
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_ServiceException() {
        // Arrange
        when(geminiService.generateImage(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Gemini服務異常"));
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert - 服務異常應該返回錯誤狀態
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getError().contains("Gemini服務異常"));

        // 驗證 Gemini 服務被調用但失敗
        verify(geminiService, times(1)).generateImage(anyString(), anyString(), anyString());
        // 用戶訊息仍應被保存，但AI回應不會被保存（因為失敗了）
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageStream() throws InterruptedException {
        // Arrange
        ChatMessage mockSavedMessage = new ChatMessage();
        mockSavedMessage.setMessageId("test-message-id");

        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);
        when(chatMessageService.getMessageById(eq("test-message-id"))).thenReturn(Optional.of(mockSavedMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);

        // Mock geminiService.generateImageStream 的行為
        doAnswer(invocation -> {
            Consumer<String> onToken = invocation.getArgument(2);
            Consumer<String> onImageGenerated = invocation.getArgument(3);
            Runnable onComplete = invocation.getArgument(4);

            // 模擬串流回應
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    onToken.accept("測試");
                    Thread.sleep(50);
                    onToken.accept("文字");
                    Thread.sleep(50);
                    onImageGenerated.accept("mockImageData");
                    Thread.sleep(50);
                    onComplete.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            return null;
        }).when(geminiService).generateImageStream(anyString(), anyString(), any(), any(), any(), any());

        // Act
        SseEmitter emitter = aiDrawingController.generateImageStream(testRequest);

        // Assert
        assertNotNull(emitter);

        // 等待串流處理完成
        Thread.sleep(500);

        // 驗證服務方法被調用
        verify(geminiService, times(1)).generateImageStream(anyString(), anyString(), any(), any(), any(), any());
        verify(chatMessageService, atLeast(2)).createMessage(any(ChatMessage.class));
        verify(chatMessageService, atLeast(1)).updateMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageStream_WithError() throws InterruptedException {
        // Arrange
        ChatMessage mockSavedMessage = new ChatMessage();
        mockSavedMessage.setMessageId("test-message-id");

        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);
        when(chatMessageService.getMessageById(eq("test-message-id"))).thenReturn(Optional.of(mockSavedMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);

        // Mock geminiService.generateImageStream 發生錯誤
        doAnswer(invocation -> {
            Consumer<Exception> onError = invocation.getArgument(5);

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    onError.accept(new RuntimeException("測試錯誤"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            return null;
        }).when(geminiService).generateImageStream(anyString(), anyString(), any(), any(), any(), any());

        // Act
        SseEmitter emitter = aiDrawingController.generateImageStream(testRequest);

        // Assert
        assertNotNull(emitter);

        // 等待錯誤處理完成
        Thread.sleep(300);

        // 驗證錯誤處理
        verify(chatMessageService, atLeast(2)).createMessage(any(ChatMessage.class)); // 用戶訊息
        verify(chatMessageService, times(1)).updateMessage(any(ChatMessage.class)); // 錯誤更新
    }

    @Test
    void testGenerateImageStream_WithEmptyPrompt() throws InterruptedException {
        // Arrange
        testRequest.setText("");
        ChatMessage mockSavedMessage = new ChatMessage();
        mockSavedMessage.setMessageId("test-message-id");

        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);
        when(chatMessageService.getMessageById(eq("test-message-id"))).thenReturn(Optional.of(mockSavedMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);

        // Mock geminiService.generateImageStream 的行為
        doAnswer(invocation -> {
            String usedPrompt = invocation.getArgument(0);
            assertEquals("請根據這張圖片生成新的內容", usedPrompt);

            Consumer<String> onToken = invocation.getArgument(2);
            Runnable onComplete = invocation.getArgument(4);

            // 模擬串流回應
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    onToken.accept("回應");
                    Thread.sleep(50);
                    onComplete.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            return null;
        }).when(geminiService).generateImageStream(anyString(), anyString(), any(), any(), any(), any());

        // Act
        SseEmitter emitter = aiDrawingController.generateImageStream(testRequest);

        // Assert
        assertNotNull(emitter);

        // 等待串流處理完成
        Thread.sleep(300);

        // 驗證使用默認提示文字調用串流服務
        verify(geminiService, times(1)).generateImageStream(eq("請根據這張圖片生成新的內容"), anyString(), any(), any(), any(), any());
        // 應該只保存畫布數據和AI文字回應，不保存空的文字提示
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testHealthCheck() {
        // Act
        ResponseEntity<String> result = aiDrawingController.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("AI Drawing service is running", result.getBody());
    }

    @Test
    void testSaveAIResponse_WithNullChatroomId() {
        // Arrange
        testRequest.setChatroomId(null);
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 驗證沒有嘗試保存訊息（因為chatroomId為null）
        verify(chatMessageService, never()).createMessage(any(ChatMessage.class));
    }

    @Test
    void testSaveUserMessages_WithNullCanvasData() {
        // Arrange
        testRequest.setImageData(null);
        when(geminiService.generateImage(anyString(), isNull(), anyString())).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 驗證保存了用戶文字訊息+AI文字回應+AI圖片回應，沒有保存畫布數據（因為為null）
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testChatMessageService_CreateMessageException() {
        // Arrange - 設定第一個 createMessage 調用成功，第二個失敗（模擬保存用戶訊息成功但AI回應失敗的情況）
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage()) // 第一次成功（用戶畫布）
                .thenReturn(new ChatMessage()) // 第二次成功（用戶文字）
                .thenThrow(new RuntimeException("資料庫連接失敗")); // 第三次失敗（AI回應）

        // Act - 即使保存AI回應失敗，API仍應正常運作
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        ResponseEntity<AIDrawingResponse> response = result.block();

        // Assert - 主要功能正常，返回成功狀態
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testGeminiResponse, response.getBody().getContent());

        // 驗證服務調用次數
        verify(geminiService, times(1)).generateImage(anyString(), anyString(), anyString());
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_WithMode() {
        // Arrange
        testRequest.setMode("edit");
        when(geminiService.generateImage(anyString(), anyString(), eq("edit"))).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        // Assert
        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // 驗證 mode 參數被正確傳遞
        verify(geminiService, times(1)).generateImage(anyString(), anyString(), eq("edit"));
    }

    @Test
    void testGenerateImageStream_WithNullCanvasData() throws InterruptedException {
        // Arrange
        testRequest.setImageData(null);
        ChatMessage mockSavedMessage = new ChatMessage();
        mockSavedMessage.setMessageId("test-message-id");

        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);
        when(chatMessageService.getMessageById(eq("test-message-id"))).thenReturn(Optional.of(mockSavedMessage));
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(mockSavedMessage);

        // Mock geminiService.generateImageStream 的行為
        doAnswer(invocation -> {
            Consumer<String> onToken = invocation.getArgument(2);
            Runnable onComplete = invocation.getArgument(4);

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    onToken.accept("回應");
                    Thread.sleep(50);
                    onComplete.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            return null;
        }).when(geminiService).generateImageStream(anyString(), isNull(), any(), any(), any(), any());

        // Act
        SseEmitter emitter = aiDrawingController.generateImageStream(testRequest);

        // Assert
        assertNotNull(emitter);

        // 等待串流處理完成
        Thread.sleep(300);

        // 驗證沒有保存畫布數據（因為為null），只保存文字訊息和AI回應
        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(geminiService, times(1)).generateImageStream(anyString(), isNull(), any(), any(), any(), any());
    }
}