package ntou.cse.feelink_backend.chatroom.service.Impl;

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
    private static final String SESSION_ID  = "1";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(registry.getSession(anyString())).thenReturn(chatSession);
        Candidate cand = mock(Candidate.class);
        Content   content = mock(Content.class);
        Part      part = mock(Part.class);
        when(part.getText()).thenReturn("Mock AI 回應");
        when(content.getPartsList()).thenReturn(java.util.Collections.singletonList(part));
        when(cand.getContent()).thenReturn(content);
        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(cand);
        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
    }

    @Test
    void reply_withNormalText() throws IOException {
        ChatMessage msg = service.reply("Hello", SESSION_ID);
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());
        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage("Hello");
    }

    @Test
    void reply_whenSendMessageThrows() throws IOException {
        when(chatSession.sendMessage(anyString()))
                .thenThrow(new RuntimeException("fail"));
        ChatMessage msg = service.reply("Err", SESSION_ID);
        assertEquals("AI處理錯誤", msg.getContent());
        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage("Err");
    }
}
