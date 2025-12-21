package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.genai.Chat;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawingAnalysisChatServiceImplTest {

    @Mock
    private ChatSessionRegistry registry;

    @Mock
    private Chat mockChat;

    @Mock
    private GenerateContentResponse mockResponse;

    private DrawingAnalysisChatServiceImpl service;
    private MultipartFile mockFile;
    private String testSessionId;
    private String testUserMessage;

    @BeforeEach
    void setUp() {
        service = new DrawingAnalysisChatServiceImpl(registry);

        mockFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test image content".getBytes()
        );

        testSessionId = "session123";
        testUserMessage = "請分析這張圖";
    }

    @Test
    void testProcessChatWithValidImageFile() throws IOException {
        when(registry.getSession(testSessionId)).thenReturn(mockChat);
        when(mockChat.sendMessage(any(Content.class))).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("這是圖片分析結果");

        String result = service.processChat(mockFile, testUserMessage, testSessionId);

        assertEquals("這是圖片分析結果", result);
        verify(mockChat).sendMessage(any(Content.class));
    }

    @Test
    void testProcessChatWithNullFile() throws IOException {
        when(registry.getSession(testSessionId)).thenReturn(mockChat);
        when(mockChat.sendMessage(anyString())).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("純文字回應");

        String result = service.processChat(null, testUserMessage, testSessionId);

        assertEquals("純文字回應", result);
    }


    @Test
    void testProcessChatStreamReturnsEmitter() throws IOException {
        when(registry.getSession(testSessionId)).thenReturn(mockChat);

        SseEmitter emitter = service.processChatStream(mockFile, testUserMessage, testSessionId);

        assertNotNull(emitter);
        verify(registry).getSession(testSessionId);
    }

    @Test
    void testProcessChatStreamWithNullFile() throws IOException {
        when(registry.getSession(testSessionId)).thenReturn(mockChat);

        SseEmitter emitter = service.processChatStream(null, testUserMessage, testSessionId);

        assertNotNull(emitter);
    }

    @Test
    void testProcessChatStreamWithPersonalityReturnsEmitter() throws IOException {
        String personality = "MUSE";
        when(registry.getSession(testSessionId, personality)).thenReturn(mockChat);

        SseEmitter emitter = service.processChatStreamWithPersonality(
                mockFile, testUserMessage, testSessionId, personality
        );

        assertNotNull(emitter);
        verify(registry).getSession(testSessionId, personality);
    }

    @Test
    void testProcessChatStreamWithPersonalityNullFile() throws IOException {
        String personality = "QIQI";
        when(registry.getSession(testSessionId, personality)).thenReturn(mockChat);

        SseEmitter emitter = service.processChatStreamWithPersonality(
                null, testUserMessage, testSessionId, personality
        );

        assertNotNull(emitter);
    }
}
