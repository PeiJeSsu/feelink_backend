package ntou.cse.feelink_backend.chatroom.service;

import com.google.cloud.language.v1.*;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleNaturalLanguageServiceTest {

    @Mock
    private LanguageServiceClient languageServiceClient;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private GoogleNaturalLanguageService googleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleService, "languageServiceClient", languageServiceClient);
    }

    @Test
    void testAnalyzeSentiment_Success() {
        String text = "我今天很開心";
        float expectedScore = 0.9f;
        float expectedMagnitude = 0.9f;

        Sentiment sentiment = Sentiment.newBuilder()
                .setScore(expectedScore)
                .setMagnitude(expectedMagnitude)
                .build();
        AnalyzeSentimentResponse response = AnalyzeSentimentResponse.newBuilder()
                .setDocumentSentiment(sentiment)
                .build();

        when(languageServiceClient.analyzeSentiment(any(Document.class))).thenReturn(response);

        GoogleNaturalLanguageService.SimpleSentimentResult result = googleService.analyzeSentiment(text);

        assertEquals(expectedScore, result.getScore());
        assertEquals(expectedMagnitude, result.getMagnitude());

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(languageServiceClient).analyzeSentiment(docCaptor.capture());
        Document capturedDoc = docCaptor.getValue();
        assertEquals(text, capturedDoc.getContent());
        assertEquals("zh-Hant", capturedDoc.getLanguage());
        assertEquals(Document.Type.PLAIN_TEXT, capturedDoc.getType());
    }

    @Test
    void testAnalyzeSentiment_EmptyText() {
        GoogleNaturalLanguageService.SimpleSentimentResult result = googleService.analyzeSentiment("");

        assertEquals(0.0f, result.getScore());
        assertEquals(0.0f, result.getMagnitude());
        verifyNoInteractions(languageServiceClient);
    }

    @Test
    void testAnalyzeSentiment_NullText() {
        GoogleNaturalLanguageService.SimpleSentimentResult result = googleService.analyzeSentiment(null);
        assertEquals(0.0f, result.getScore());
        verifyNoInteractions(languageServiceClient);
    }

    @Test
    void testAnalyzeSentiment_Exception() {
        when(languageServiceClient.analyzeSentiment(any(Document.class)))
                .thenThrow(new RuntimeException("Google API Error"));

        GoogleNaturalLanguageService.SimpleSentimentResult result = googleService.analyzeSentiment("text");

        assertEquals(0.0f, result.getScore());
        assertEquals(0.0f, result.getMagnitude());
    }

    @Test
    void testAnalyzeTodaySentiment_HasMessages() {
        String chatroomId = "room1";
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("msg1", chatroomId, true, false, "很高興", new Date()));
        messages.add(new ChatMessage("msg2", chatroomId, true, false, "見到你", new Date()));

        when(chatMessageService.getUserTextMessagesByDate(eq(chatroomId), anyString()))
                .thenReturn(messages);

        Sentiment sentiment = Sentiment.newBuilder().setScore(0.5f).setMagnitude(0.5f).build();
        AnalyzeSentimentResponse response = AnalyzeSentimentResponse.newBuilder().setDocumentSentiment(sentiment).build();
        when(languageServiceClient.analyzeSentiment(any(Document.class))).thenReturn(response);

        GoogleNaturalLanguageService.SimpleSentimentResult result = googleService.analyzeTodaySentiment(chatroomId);

        assertEquals(0.5f, result.getScore());

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(languageServiceClient).analyzeSentiment(docCaptor.capture());
        String content = docCaptor.getValue().getContent();
        assertTrue(content.contains("使用者: 很高興"));
        assertTrue(content.contains("使用者: 見到你"));
    }

    @Test
    void testAnalyzeTodaySentiment_NoMessages() {
        String chatroomId = "room1";
        when(chatMessageService.getUserTextMessagesByDate(eq(chatroomId), anyString()))
                .thenReturn(Collections.emptyList());

        GoogleNaturalLanguageService.SimpleSentimentResult result = googleService.analyzeTodaySentiment(chatroomId);

        assertEquals(0.0f, result.getScore());
        verifyNoInteractions(languageServiceClient);
    }


    @Test
    void testGetTodayChatMessages_FiltersNonUserMessages() {
        String chatroomId = "room1";
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("msg1", chatroomId, true, false, "User Msg", new Date()));
        messages.add(new ChatMessage("msg2", chatroomId, false, false, "AI Msg", new Date()));

        when(chatMessageService.getUserTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(messages);

        List<String> result = googleService.getTodayChatMessages(chatroomId);

        assertEquals(1, result.size());
        assertEquals("使用者: User Msg", result.get(0));
    }

    @Test
    void testGetTodayChatMessages_NullResult() {
        String chatroomId = "room1";
        when(chatMessageService.getUserTextMessagesByDate(eq(chatroomId), anyString())).thenReturn(null);

        List<String> result = googleService.getTodayChatMessages(chatroomId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDestroy() {
        googleService.destroy();

        verify(languageServiceClient).close();
    }

    @Test
    void testSimpleSentimentResultToString() {
        GoogleNaturalLanguageService.SimpleSentimentResult res = new GoogleNaturalLanguageService.SimpleSentimentResult(0.5f, 0.8f);
        String str = res.toString();
        assertTrue(str.contains("score=0.5"));
        assertTrue(str.contains("magnitude=0.8"));
    }
}
