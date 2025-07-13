package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {
    @Mock
    private DrawingAnalysisChatServiceImpl drawingAnalysisChatService;
    @Mock
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;
    @InjectMocks
    private ChatServiceImpl chatService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testProcessChat_NormalFlow() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String userMessage = "畫了太陽";
        String sessionId = "session-123";

        when(emotionPredictionChatService.processChat(userMessage))
                .thenReturn("開心");

        when(drawingAnalysisChatService.processChat(eq(file), contains("開心"), eq(sessionId)))
                .thenReturn("這幅畫讓人感到溫暖");

        ChatMessage result = chatService.processChat(file, userMessage, sessionId);
        assertNotNull(result);
        assertEquals("這幅畫讓人感到溫暖", result.getContent());
        assertFalse(result.isUser());
        assertEquals(sessionId, result.getSessionId());

        verify(emotionPredictionChatService, times(1)).processChat(userMessage);
        verify(drawingAnalysisChatService, times(1)).processChat(eq(file), contains("開心"), eq(sessionId));
    }
}
