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
import reactor.core.publisher.Mono;

import java.util.Base64;

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
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        Mono<ResponseEntity<AIDrawingResponse>> result = aiDrawingController.generateImage(testRequest);

        ResponseEntity<AIDrawingResponse> response = result.block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testGeminiResponse, response.getBody().getContent());

        verify(geminiService, times(1)).generateImage(eq("測試提示文字"), eq(testImageData), eq(testMode));
        verify(chatMessageService, times(4)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_WithBackgroundRemoval() {
        testRequest.setRemoveBackground(true);
        byte[] mockRemovedBgBytes = "removedBgImageData".getBytes();
        String expectedBase64 = Base64.getEncoder().encodeToString(mockRemovedBgBytes);

        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(removeBgService.removeBackground(any())).thenReturn(Mono.just(mockRemovedBgBytes));
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(expectedBase64, response.getBody().getContent().getImageData());

        verify(removeBgService, times(1)).removeBackground(any());
    }

    @Test
    void testGenerateImageSimple_WithBackgroundRemovalError() {
        testRequest.setRemoveBackground(true);
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(removeBgService.removeBackground(any())).thenReturn(Mono.error(new RuntimeException("去背失敗")));
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode()); // 修正為 206
        assertTrue(response.getBody().isSuccess());
        assertEquals("base64EncodedImageData", response.getBody().getContent().getImageData());

        verify(removeBgService, times(1)).removeBackground(any());
    }

    @Test
    void testGenerateImageSimple_WithEmptyPrompt() {
        testRequest.setText("");
        when(geminiService.generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode)))
                .thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(geminiService, times(1)).generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode));
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_WithNullPrompt() {
        testRequest.setText(null);
        when(geminiService.generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode)))
                .thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(geminiService, times(1)).generateImage(eq("請根據這張圖片生成新的內容"), eq(testImageData), eq(testMode));
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_ServiceException() {
        when(geminiService.generateImage(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Gemini服務異常"));
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getError().contains("Gemini服務異常"));

        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<String> result = aiDrawingController.healthCheck();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("AI Drawing service is running", result.getBody());
    }

    @Test
    void testSaveAIResponse_WithNullChatroomId() {
        testRequest.setChatroomId(null);
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(chatMessageService, never()).createMessage(any(ChatMessage.class));
    }

    @Test
    void testSaveUserMessages_WithNullCanvasData() {
        testRequest.setImageData(null);
        when(geminiService.generateImage(anyString(), isNull(), anyString())).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testChatMessageService_CreateMessageException() {
        when(geminiService.generateImage(anyString(), anyString(), anyString())).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testGeminiResponse, response.getBody().getContent());

        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testGenerateImageSimple_WithMode() {
        testRequest.setMode("edit");
        when(geminiService.generateImage(anyString(), anyString(), eq("edit"))).thenReturn(testGeminiResponse);
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        ResponseEntity<AIDrawingResponse> response = aiDrawingController.generateImage(testRequest).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(geminiService, times(1)).generateImage(anyString(), anyString(), eq("edit"));
    }


}
