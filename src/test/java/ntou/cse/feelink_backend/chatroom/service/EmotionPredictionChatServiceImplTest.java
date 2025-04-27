package ntou.cse.feelink_backend.chatroom.service;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class EmotionPredictionChatServiceImplTest {
    @Mock
    private ChatSession chatSession;
    @Mock
    private GenerateContentResponse mockResponse;
    @InjectMocks
    private EmotionPredictionChatServiceImpl emotionChatService;
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        Candidate mockCandidate = mock(Candidate.class);
        Content mockContent = mock(Content.class);
        Part mockPart = mock(Part.class);

        when(mockPart.toString()).thenReturn("Mock AI Response Part");
        when(mockPart.getText()).thenReturn("Mock AI 回應");

        when(mockContent.getPartsList()).thenReturn(Collections.singletonList(mockPart));
        when(mockCandidate.getContent()).thenReturn(mockContent);

        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(mockCandidate);
        when(mockResponse.getCandidatesList()).thenReturn(Collections.singletonList(mockCandidate));
        when(mockResponse.toString()).thenReturn("Mock AI 回應");

        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
        when(chatSession.sendMessage(any(Content.class))).thenReturn(mockResponse);
    }

    @Test
    void testEmotionPredictorService_ProcessChat_NormalText() throws IOException {
        String userMessage = "今天天氣很好";

        String response = emotionChatService.processChat(null, userMessage);

        assertNotNull(response);
        assertEquals("Mock AI 回應", response);
        verify(chatSession, times(1)).sendMessage(userMessage);
    }

    @Test
    void testProcessChat_NullInput() throws IOException {
        String userMessage = null;

        String response = emotionChatService.processChat(null, userMessage);

        assertNotNull(response);
        assertEquals("Mock AI 回應", response);

        verify(chatSession, times(1)).sendMessage("請預測我的心情和感興趣的話題");
    }

    @Test
    void testProcessChat_BlankInput() throws IOException {
        String userMessage = "    ";

        String response = emotionChatService.processChat(null, userMessage);

        assertNotNull(response);
        assertEquals("Mock AI 回應", response);

        verify(chatSession, times(1)).sendMessage("請預測我的心情和感興趣的話題");
    }

    @Test
    void testEmotionPredictorService_ProcessChat_ExceptionThrown() throws IOException {
        String userMessage = "出錯的測試";

        when(chatSession.sendMessage(anyString())).thenThrow(new RuntimeException("AI故障"));

        String response = emotionChatService.processChat(null, userMessage);

        assertNotNull(response);
        assertEquals("AI處理情緒預測錯誤", response);
    }
}

