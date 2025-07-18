package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlainChatServiceImplTest {

    @Mock private ChatSession chatSession;
    @Mock private GenerateContentResponse mockResponse;
    @Mock private ChatSessionRegistry registry;
    @InjectMocks private PlainChatServiceImpl service;
    private static final String SESSION_ID = "1";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 設置 registry mock
        when(registry.getSession(anyString())).thenReturn(chatSession);

        // 設置 GenerateContentResponse mock
        Candidate cand = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn("Mock AI 回應");
        when(content.getPartsList()).thenReturn(java.util.Collections.singletonList(part));
        when(cand.getContent()).thenReturn(content);
        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(cand);
        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
    }

    @Test
    void reply_withAllParameters() throws IOException {
        // Given
        String userMessage = "Hello";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;

        // When
        ChatMessage msg = service.reply(userMessage, SESSION_ID, conversationCount, hasDefaultQuestion);

        // Then
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        // 驗證 chatSession 被調用，但不需要驗證具體的格式化消息內容
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_withNullOptionalParameters() throws IOException {
        // Given
        String userMessage = "Hello";

        // When
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, null);

        // Then
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_withOnlyConversationCount() throws IOException {
        // Given
        String userMessage = "Hello";
        Integer conversationCount = 5;

        // When
        ChatMessage msg = service.reply(userMessage, SESSION_ID, conversationCount, null);

        // Then
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_withOnlyHasDefaultQuestion() throws IOException {
        // Given
        String userMessage = "Hello";
        Boolean hasDefaultQuestion = false;

        // When
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, hasDefaultQuestion);

        // Then
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_whenSendMessageThrows() throws IOException {
        // Given
        String userMessage = "Error message";
        when(chatSession.sendMessage(anyString()))
                .thenThrow(new RuntimeException("AI service failed"));

        // When
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, null);

        // Then
        assertEquals("AI處理錯誤", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_whenChatSessionThrows() throws IOException {
        // Given
        String userMessage = "Error message";
        when(chatSession.sendMessage(anyString()))
                .thenThrow(new RuntimeException("Chat session failed"));

        // When
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, null);

        // Then
        assertEquals("AI處理錯誤", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }
}