package ntou.cse.feelink_backend.chatroom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmotionAnalysisServiceTest {

    @Mock
    private Client client;

    @Mock
    private Models models;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private EmotionAnalysisService emotionAnalysisService;

    @Mock
    private GenerateContentResponse mockResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "models", models);

        ReflectionTestUtils.setField(emotionAnalysisService, "chatMessageService", chatMessageService);
    }


    @Test
    void testGetTodayChatMessages() {
        String chatroomId = "room123";
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("msg1", chatroomId, true, false, "Hi", new Date()));
        messages.add(new ChatMessage("msg2", chatroomId, false, false, "Hello", new Date()));

        when(chatMessageService.getTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(messages);

        List<String> result = emotionAnalysisService.getTodayChatMessages(chatroomId);

        assertEquals(2, result.size());
        assertEquals("使用者: Hi", result.get(0));
        assertEquals("AI: Hello", result.get(1));
    }


    @Test
    void testAnalyzeTodayEmotions_Success() throws Exception {
        String chatroomId = "room123";
        List<ChatMessage> messages = Collections.singletonList(
                new ChatMessage("msg1", chatroomId, true, false, "I am sad", new Date())
        );
        when(chatMessageService.getTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(messages);

        String jsonResponse = """
            {
                "難過": 8.5,
                "喜悅": 1.0,
                "興奮": 0.0,
                "焦慮": 5.2,
                "生氣": 2.0,
                "平靜": 3.0,
                "期待": 1.5,
                "樂觀": 2.0
            }
            """;

        when(models.generateContent(eq("gemini-2.5-flash-lite"), anyString(), any(GenerateContentConfig.class)))
                .thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn(jsonResponse);

        EmotionAnalysisService.EmotionScores scores = emotionAnalysisService.analyzeTodayEmotions(chatroomId);

        assertEquals(8.5, scores.getSadness());
        assertEquals(1.0, scores.getJoy());
    }

    @Test
    void testAnalyzeTodayEmotions_Exception_ReturnsDefault() throws Exception {
        String chatroomId = "room123";
        when(chatMessageService.getTextMessagesByDate(eq(chatroomId), anyString()))
                .thenReturn(Collections.emptyList());

        lenient().when(models.generateContent(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        EmotionAnalysisService.EmotionScores scores = emotionAnalysisService.analyzeTodayEmotions(chatroomId);

        assertEquals(-1, scores.getSadness());
    }


    @Test
    void testGenerateChatSummary_Success() throws Exception {
        String chatroomId = "room123";
        String date = "2025-10-27";
        List<ChatMessage> messages = Collections.singletonList(
                new ChatMessage("msg1", chatroomId, true, false, "Content", new Date())
        );

        when(chatMessageService.getTextMessagesByDate(chatroomId, date)).thenReturn(messages);

        when(models.generateContent(eq("gemini-2.5-flash-lite"), anyString(), any(GenerateContentConfig.class)))
                .thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("這是摘要內容");

        String summary = emotionAnalysisService.generateChatSummary(chatroomId, date);

        assertEquals("這是摘要內容", summary);

        ArgumentMatcher<String> promptMatcher = prompt -> prompt.contains("Content") && prompt.contains("使用者: Content");

        verify(models).generateContent(
                eq("gemini-2.5-flash-lite"),
                argThat(promptMatcher),
                any(GenerateContentConfig.class)
        );
    }

    @Test
    void testGenerateChatSummary_NoMessages() {
        String chatroomId = "room123";
        String date = "2025-10-27";
        when(chatMessageService.getTextMessagesByDate(chatroomId, date)).thenReturn(Collections.emptyList());

        String summary = emotionAnalysisService.generateChatSummary(chatroomId, date);

        assertEquals("該日期沒有聊天記錄", summary);
        verifyNoInteractions(models);
    }

    @Test
    void testGenerateChatSummary_Exception() throws Exception {
        String chatroomId = "room123";
        String date = "2025-10-27";
        List<ChatMessage> messages = Collections.singletonList(new ChatMessage());
        when(chatMessageService.getTextMessagesByDate(chatroomId, date)).thenReturn(messages);
        lenient().when(models.generateContent(
                anyString(),
                anyString(),
                any(GenerateContentConfig.class)
        )).thenThrow(new RuntimeException("API Fail"));

        String summary = emotionAnalysisService.generateChatSummary(chatroomId, date);

        assertTrue(summary.contains("無法產生該日期的聊天摘要"));
    }


    @Test
    void testAnalyzeTodayDemandSimple_Success() throws Exception {
        String chatroomId = "room123";
        List<ChatMessage> messages = Collections.singletonList(
                new ChatMessage("msg1", chatroomId, true, false, "I feel lonely", new Date())
        );
        when(chatMessageService.getTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(messages);

        when(models.generateContent(eq("gemini-2.5-flash-lite"), anyString(), any(GenerateContentConfig.class)))
                .thenReturn(mockResponse);
        when(mockResponse.text()).thenReturn("使用者渴望陪伴");

        String demand = emotionAnalysisService.analyzeTodayDemandSimple(chatroomId);

        assertEquals("使用者渴望陪伴", demand);

        ArgumentMatcher<String> promptMatcher = prompt -> prompt.contains("I feel lonely") && prompt.contains("心理分析師");

        verify(models).generateContent(
                eq("gemini-2.5-flash-lite"),
                argThat(promptMatcher),
                any(GenerateContentConfig.class)
        );
    }

    @Test
    void testAnalyzeTodayDemandSimple_NoMessages() {
        String chatroomId = "room123";
        when(chatMessageService.getTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(Collections.emptyList());

        String demand = emotionAnalysisService.analyzeTodayDemandSimple(chatroomId);

        assertEquals("需要被理解和傾聽", demand);
        verifyNoInteractions(models);
    }

    @Test
    void testAnalyzeTodayDemandSimple_Exception() throws Exception {
        String chatroomId = "room123";
        List<ChatMessage> messages = Collections.singletonList(new ChatMessage());
        when(chatMessageService.getTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(messages);
        lenient().when(models.generateContent(
                anyString(),
                anyString(),
                any(GenerateContentConfig.class)
        )).thenThrow(new RuntimeException("API Fail"));

        String demand = emotionAnalysisService.analyzeTodayDemandSimple(chatroomId);

        assertEquals("需要被理解和傾聽", demand);
    }
}
