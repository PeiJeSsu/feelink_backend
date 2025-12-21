package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.genai.Chat;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmotionPredictionChatServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class EmotionPredictionChatServiceImplTest {

    @Mock
    private Chat emotionPredictorChatSession;

    @Mock
    private GenerateContentResponse mockResponse;

    private EmotionPredictionChatServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmotionPredictionChatServiceImpl(emotionPredictorChatSession);
    }

    @Test
    void testProcessChatWithValidMessage() throws IOException {
        String userMessage = "我今天很開心";
        when(emotionPredictorChatSession.sendMessage(userMessage)).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("快樂");

        String result = service.processChat(userMessage);

        assertEquals("快樂", result);
        verify(emotionPredictorChatSession).sendMessage(userMessage);
        verify(mockResponse).text();
    }

    @Test
    void testProcessChatWithSadMessage() throws IOException {
        String userMessage = "我感到很難過";
        when(emotionPredictorChatSession.sendMessage(userMessage)).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("悲傷");

        String result = service.processChat(userMessage);

        assertEquals("悲傷", result);
    }

    @Test
    void testProcessChatWithException() throws IOException {
        String userMessage = "測試訊息";
        when(emotionPredictorChatSession.sendMessage(userMessage))
                .thenThrow(new RuntimeException("API 錯誤"));

        String result = service.processChat(userMessage);

        assertEquals("AI處理情緒預測錯誤", result);
        verify(emotionPredictorChatSession).sendMessage(userMessage);
    }

    @Test
    void testProcessChatWithEmptyMessage() throws IOException {
        String userMessage = "";
        when(emotionPredictorChatSession.sendMessage(userMessage)).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("中性");

        String result = service.processChat(userMessage);

        assertEquals("中性", result);
        verify(emotionPredictorChatSession).sendMessage(userMessage);
    }



    @Test
    void testProcessChatWithLongMessage() throws IOException {
        String longMessage = "這是很長的訊息".repeat(50);
        when(emotionPredictorChatSession.sendMessage(longMessage)).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("焦慮");

        String result = service.processChat(longMessage);

        assertEquals("焦慮", result);
        verify(emotionPredictorChatSession).sendMessage(longMessage);
    }

    @Test
    void testProcessChatWithSpecialCharacters() throws IOException {
        String specialMessage = "我@#$%感覺!!!???";
        when(emotionPredictorChatSession.sendMessage(specialMessage)).thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("困惑");

        String result = service.processChat(specialMessage);

        assertEquals("困惑", result);
    }
}
