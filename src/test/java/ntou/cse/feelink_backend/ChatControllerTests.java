package ntou.cse.feelink_backend.controller;

import ntou.cse.feelink_backend.model.ChatMessage;
import ntou.cse.feelink_backend.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChatControllerTests {

    @Mock
    private ChatService chatService;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendMessage() {
        // Arrange
        ChatMessage inputMessage = new ChatMessage("Hello", true, "session123");
        ChatMessage savedMessage = new ChatMessage("Hello", true, "session123");
        savedMessage.setId(1L);

        when(chatService.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        // Act
        ResponseEntity<ChatMessage> response = chatController.sendMessage(inputMessage);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getContent().startsWith("測試用 Hello"));
        verify(chatService, times(1)).saveMessage(any(ChatMessage.class));
        verify(aiService, times(1)).callExternalApi(eq("Hello"), eq("session123"));
    }

    @Test
    public void testGetChatHistory() {
        // Arrange
        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(new ChatMessage("User message", true, "session123"));
        messageList.add(new ChatMessage("AI response", false, "session123"));

        when(chatService.getMessagesBySessionId("session123")).thenReturn(messageList);

        // Act
        ResponseEntity<List<ChatMessage>> response = chatController.getChatHistory("session123");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(chatService, times(1)).getMessagesBySessionId("session123");
    }
    @Test
    public void testGetLatestResponse_WithResponse() {
        // Arrange
        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(new ChatMessage("User message", true, "session123"));
        messageList.add(new ChatMessage("AI response 1", false, "session123"));
        messageList.add(new ChatMessage("User message 2", true, "session123"));
        messageList.add(new ChatMessage("AI response 2", false, "session123"));

        when(chatService.getMessagesBySessionId("session123")).thenReturn(messageList);

        // Act
        ResponseEntity<ChatMessage> response = chatController.getLatestResponse("session123");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("AI response 2", response.getBody().getContent());
        assertFalse(response.getBody().isUser());
        verify(chatService, times(1)).getMessagesBySessionId("session123");
    }

    @Test
    public void testGetLatestResponse_NoResponse() {
        // Arrange
        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(new ChatMessage("User message", true, "session123"));

        when(chatService.getMessagesBySessionId("session123")).thenReturn(messageList);

        // Act
        ResponseEntity<ChatMessage> response = chatController.getLatestResponse("session123");

        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(chatService, times(1)).getMessagesBySessionId("session123");
    }
}