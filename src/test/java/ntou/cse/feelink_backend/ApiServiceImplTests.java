package ntou.cse.feelink_backend.service;

import ntou.cse.feelink_backend.model.AiResponse;
import ntou.cse.feelink_backend.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ApiServiceImplTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ApiServiceImpl apiService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCallExternalApi_Success() {
        // Arrange
        String message = "Test message";
        String sessionId = "session123";

        when(chatService.saveMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        AiResponse response = apiService.callExternalApi(message, sessionId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("API回應: Test message", response.getMessage());

        // Verify chatService.saveMessage was called with correct parameters
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatService, times(1)).saveMessage(messageCaptor.capture());

        ChatMessage savedMessage = messageCaptor.getValue();
        assertEquals("API回應: Test message", savedMessage.getContent());
        assertEquals("session123", savedMessage.getSessionId());
        assertFalse(savedMessage.isUser());
    }

    @Test
    public void testCallExternalApi_Exception() {
        // Arrange
        String message = "Test message";
        String sessionId = "session123";

        when(chatService.saveMessage(any(ChatMessage.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        AiResponse response = apiService.callExternalApi(message, sessionId);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().startsWith("呼叫API錯誤:"));
    }
}