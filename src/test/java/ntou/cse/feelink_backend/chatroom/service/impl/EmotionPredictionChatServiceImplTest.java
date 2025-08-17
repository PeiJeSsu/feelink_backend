package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.cloud.vertexai.api.Part;
import java.io.IOException;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmotionPredictionChatServiceImplTest {
    @Mock private ChatSession chatSession;
    @Mock private GenerateContentResponse mockResponse;
    @InjectMocks private EmotionPredictionChatServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockResponse.toString()).thenReturn("Mock AI 回應");
        Candidate cand = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn("Mock AI 回應");
        when(content.getPartsList()).thenReturn(Collections.singletonList(part));
        when(cand.getContent()).thenReturn(content);
        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(cand);
        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
    }

    @Test
    void normalText() throws IOException {
        String resp = service.processChat("今天天氣很好");
        assertEquals("Mock AI 回應", resp);
        verify(chatSession).sendMessage("今天天氣很好");
    }

    @Test
    void exception_returnsErrorMessage() throws IOException {
        when(chatSession.sendMessage("Err")).thenThrow(new RuntimeException("fail"));
        String resp = service.processChat( "Err");
        assertEquals("AI處理情緒預測錯誤", resp);
    }
}