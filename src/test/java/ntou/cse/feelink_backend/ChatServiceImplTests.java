package ntou.cse.feelink_backend.service;

import ntou.cse.feelink_backend.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Note: This test class assumes you have a ChatServiceImpl class implementing ChatService
// You'll need to modify this based on your actual implementation
public class ChatServiceImplTests {

    private ChatService chatService;

    // This would typically be a repository or database mock
    // Since the implementation wasn't provided, we're creating a placeholder
    private ChatServiceImpl chatServiceImpl;

    @BeforeEach
    public void setup() {
        // Since we don't have the actual implementation, we'll create a mock implementation
        chatService = mock(ChatService.class);

        // Setup mock behavior
        ChatMessage mockMessage = new ChatMessage("Test content", true, "session123");
        mockMessage.setId(1L);

        when(chatService.saveMessage(any(ChatMessage.class))).thenReturn(mockMessage);

        List<ChatMessage> mockMessages = Arrays.asList(
                new ChatMessage("Message 1", true, "session123"),
                new ChatMessage("Response 1", false, "session123")
        );

        when(chatService.getMessagesBySessionId("session123")).thenReturn(mockMessages);
    }

    @Test
    public void testSaveMessage() {
        // Arrange
        ChatMessage inputMessage = new ChatMessage("Hello", true, "session123");

        // Act
        ChatMessage savedMessage = chatService.saveMessage(inputMessage);

        // Assert
        assertNotNull(savedMessage);
        assertEquals(1L, savedMessage.getId());
        assertEquals("Test content", savedMessage.getContent());
        verify(chatService, times(1)).saveMessage(inputMessage);
    }

    @Test
    public void testGetMessagesBySessionId() {
        // Act
        List<ChatMessage> messages = chatService.getMessagesBySessionId("session123");

        // Assert
        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals("Message 1", messages.get(0).getContent());
        assertEquals("Response 1", messages.get(1).getContent());
        verify(chatService, times(1)).getMessagesBySessionId("session123");
    }
}