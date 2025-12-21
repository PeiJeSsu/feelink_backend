package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.DailyEmotionAnalysis;
import ntou.cse.feelink_backend.chatroom.repository.DailyEmotionAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyEmotionAnalysisServiceTest {

    @Mock
    private DailyEmotionAnalysisRepository repository;

    @Mock
    private EmotionAnalysisService emotionAnalysisService;

    @Mock
    private GoogleNaturalLanguageService googleNaturalLanguageService;

    @InjectMocks
    private DailyEmotionAnalysisService dailyEmotionAnalysisService;

    private final String CHATROOM_ID = "room2025";
    private final String TODAY_2025 = "2025-05-20";


    @Test
    void testPerformAndSaveTodayAnalysis_Success() {
        when(emotionAnalysisService.getTaiwanTodayString()).thenReturn(TODAY_2025);
        EmotionAnalysisService.EmotionScores mockScores = new EmotionAnalysisService.EmotionScores(
                1.0, 8.0, 5.0, 2.0, 0.0, 6.0, 7.0, 9.0 // sadness, joy, excited, etc.
        );
        when(emotionAnalysisService.analyzeTodayEmotions(CHATROOM_ID)).thenReturn(mockScores);
        when(emotionAnalysisService.generateTodayChatSummary(CHATROOM_ID)).thenReturn("2025 Summary");
        when(emotionAnalysisService.analyzeTodayDemandSimple(CHATROOM_ID)).thenReturn("2025 Demand");
        GoogleNaturalLanguageService.SimpleSentimentResult mockSentiment =
                new GoogleNaturalLanguageService.SimpleSentimentResult(0.8f, 0.9f);
        when(googleNaturalLanguageService.analyzeTodaySentiment(CHATROOM_ID)).thenReturn(mockSentiment);
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(null);
        when(repository.save(any(DailyEmotionAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));
        DailyEmotionAnalysis result = dailyEmotionAnalysisService.performAndSaveTodayAnalysis(CHATROOM_ID);
        assertNotNull(result);
        assertEquals(CHATROOM_ID, result.getChatroomId());
        assertEquals(TODAY_2025, result.getDateString());
        assertEquals("2025 Summary", result.getSummary());
        assertEquals("2025 Demand", result.getDemand());
        assertEquals(0.8f, result.getScore());
        assertEquals(8.0f, result.getEmotions().get("喜悅"));

        verify(repository).save(any(DailyEmotionAnalysis.class));
    }

    @Test
    void testPerformAndSaveTodayAnalysis_Exception() {
        when(emotionAnalysisService.getTaiwanTodayString()).thenReturn(TODAY_2025);
        when(emotionAnalysisService.analyzeTodayEmotions(anyString()))
                .thenThrow(new RuntimeException("Analysis Failed"));

        assertThrows(RuntimeException.class, () ->
                dailyEmotionAnalysisService.performAndSaveTodayAnalysis(CHATROOM_ID)
        );
    }

    @Test
    void testSaveDailyAnalysis_CreateNew() {
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(null);
        when(repository.save(any(DailyEmotionAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyEmotionAnalysis result = dailyEmotionAnalysisService.saveDailyAnalysis(
                CHATROOM_ID, TODAY_2025, "Summary", new HashMap<>(), "Demand", 0.5f, 0.5f);
        assertEquals(TODAY_2025, result.getDateString());
        verify(repository).findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025);
        verify(repository).save(any(DailyEmotionAnalysis.class));
    }

    @Test
    void testSaveDailyAnalysis_UpdateExisting() {
        DailyEmotionAnalysis existing = new DailyEmotionAnalysis();
        existing.setId("old_id");
        existing.setSummary("Old Summary");

        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(existing);
        when(repository.save(any(DailyEmotionAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyEmotionAnalysis result = dailyEmotionAnalysisService.saveDailyAnalysis(
                CHATROOM_ID, TODAY_2025, "New Summary", new HashMap<>(), "New Demand", 0.9f, 0.9f);

        assertEquals("old_id", result.getId());
        assertEquals("New Summary", result.getSummary());
        verify(repository).save(existing);
    }

    @Test
    void testGetTodayAnalysis() {
        when(emotionAnalysisService.getTaiwanTodayString()).thenReturn(TODAY_2025);
        DailyEmotionAnalysis mockData = new DailyEmotionAnalysis();
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(mockData);

        DailyEmotionAnalysis result = dailyEmotionAnalysisService.getTodayAnalysis(CHATROOM_ID);

        assertNotNull(result);
        assertEquals(mockData, result);
    }

    @Test
    void testGetHistoryAnalysis() {
        when(repository.findByChatroomIdOrderByDateStringDesc(CHATROOM_ID))
                .thenReturn(Collections.singletonList(new DailyEmotionAnalysis()));

        List<DailyEmotionAnalysis> result = dailyEmotionAnalysisService.getHistoryAnalysis(CHATROOM_ID);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllAnalysisForChatroom() {
        when(repository.findByChatroomId(CHATROOM_ID))
                .thenReturn(Collections.singletonList(new DailyEmotionAnalysis()));

        List<DailyEmotionAnalysis> result = dailyEmotionAnalysisService.getAllAnalysisForChatroom(CHATROOM_ID);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetLatestAnalysisForChatrooms() {
        String roomA = "roomA";
        String roomB = "roomB";
        List<String> roomIds = Arrays.asList(roomA, roomB);

        DailyEmotionAnalysis a1 = new DailyEmotionAnalysis();
        a1.setChatroomId(roomA); a1.setDateString("2025-01-01");

        DailyEmotionAnalysis a2 = new DailyEmotionAnalysis();
        a2.setChatroomId(roomA); a2.setDateString("2025-01-02");

        DailyEmotionAnalysis b1 = new DailyEmotionAnalysis();
        b1.setChatroomId(roomB); b1.setDateString("2025-01-01");

        when(repository.findByChatroomIdIn(roomIds)).thenReturn(Arrays.asList(a1, a2, b1));

        List<DailyEmotionAnalysis> result = dailyEmotionAnalysisService.getLatestAnalysisForChatrooms(roomIds);

        assertEquals(2, result.size());

        DailyEmotionAnalysis resultA = result.stream().filter(r -> r.getChatroomId().equals(roomA)).findFirst().get();
        assertEquals("2025-01-02", resultA.getDateString());

        DailyEmotionAnalysis resultB = result.stream().filter(r -> r.getChatroomId().equals(roomB)).findFirst().get();
        assertEquals("2025-01-01", resultB.getDateString());
    }

    @Test
    void testDeleteTodayAnalysis() {
        when(emotionAnalysisService.getTaiwanTodayString()).thenReturn(TODAY_2025);

        DailyEmotionAnalysis existing = new DailyEmotionAnalysis();
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(existing);
        when(repository.deleteByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(1L);

        boolean deleted = dailyEmotionAnalysisService.deleteTodayAnalysis(CHATROOM_ID);

        assertTrue(deleted);
    }

    @Test
    void testDeleteAnalysisByDate_Success() {
        DailyEmotionAnalysis existing = new DailyEmotionAnalysis();
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(existing);
        when(repository.deleteByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(1L);

        boolean result = dailyEmotionAnalysisService.deleteAnalysisByDate(CHATROOM_ID, TODAY_2025);

        assertTrue(result);
        verify(repository).deleteByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025);
    }

    @Test
    void testDeleteAnalysisByDate_NotFound() {
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025)).thenReturn(null);

        boolean result = dailyEmotionAnalysisService.deleteAnalysisByDate(CHATROOM_ID, TODAY_2025);

        assertFalse(result);
        verify(repository, never()).deleteByChatroomIdAndDateString(anyString(), anyString());
    }

    @Test
    void testDeleteAnalysisByDate_Exception() {
        when(repository.findByChatroomIdAndDateString(CHATROOM_ID, TODAY_2025))
                .thenThrow(new RuntimeException("DB Error"));

        boolean result = dailyEmotionAnalysisService.deleteAnalysisByDate(CHATROOM_ID, TODAY_2025);

        assertFalse(result);
    }
}
