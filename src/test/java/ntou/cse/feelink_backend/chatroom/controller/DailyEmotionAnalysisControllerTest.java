package ntou.cse.feelink_backend.chatroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.DailyEmotionAnalysis;
import ntou.cse.feelink_backend.chatroom.service.DailyEmotionAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DailyEmotionAnalysisControllerTest {

    @Mock
    private DailyEmotionAnalysisService analysisService;

    @InjectMocks
    private DailyEmotionAnalysisController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DailyEmotionAnalysis mockAnalysis;
    private String testChatroomId = "room123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        mockAnalysis = new DailyEmotionAnalysis();
        mockAnalysis.setId("analysis1");
        mockAnalysis.setChatroomId(testChatroomId);
        mockAnalysis.setDateString("2025-10-27");
        mockAnalysis.setSummary("Test summary");
    }

    @Test
    void testAnalyzeAndSaveToday_Success() throws Exception {
        when(analysisService.performAndSaveTodayAnalysis(testChatroomId)).thenReturn(mockAnalysis);

        mockMvc.perform(post("/api/emotion-analysis/chatroom/{chatroomId}/analyze-today", testChatroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("analysis1"))
                .andExpect(jsonPath("$.summary").value("Test summary"));
    }

    @Test
    void testAnalyzeAndSaveToday_Exception() throws Exception {
        when(analysisService.performAndSaveTodayAnalysis(testChatroomId))
                .thenThrow(new RuntimeException("Analysis failed"));

        mockMvc.perform(post("/api/emotion-analysis/chatroom/{chatroomId}/analyze-today", testChatroomId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetTodayAnalysis_Found() throws Exception {
        when(analysisService.getTodayAnalysis(testChatroomId)).thenReturn(mockAnalysis);

        mockMvc.perform(get("/api/emotion-analysis/chatroom/{chatroomId}/today", testChatroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("analysis1"));
    }

    @Test
    void testGetTodayAnalysis_NotFound() throws Exception {
        when(analysisService.getTodayAnalysis(testChatroomId)).thenReturn(null);

        mockMvc.perform(get("/api/emotion-analysis/chatroom/{chatroomId}/today", testChatroomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetHistoryAnalysis_Success() throws Exception {
        List<DailyEmotionAnalysis> history = Arrays.asList(mockAnalysis);
        when(analysisService.getHistoryAnalysis(testChatroomId)).thenReturn(history);

        mockMvc.perform(get("/api/emotion-analysis/chatroom/{chatroomId}/history", testChatroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("analysis1"));
    }

    @Test
    void testGetAllAnalysisForChatroom_Success() throws Exception {
        List<DailyEmotionAnalysis> all = Arrays.asList(mockAnalysis);
        when(analysisService.getAllAnalysisForChatroom(testChatroomId)).thenReturn(all);

        mockMvc.perform(get("/api/emotion-analysis/chatroom/{chatroomId}/all", testChatroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetAllAnalysisForChatroom_Exception() throws Exception {
        when(analysisService.getAllAnalysisForChatroom(testChatroomId))
                .thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/api/emotion-analysis/chatroom/{chatroomId}/all", testChatroomId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteAnalysisByDate_Success_Deleted() throws Exception {
        String dateString = "2025-10-27";
        when(analysisService.deleteAnalysisByDate(testChatroomId, dateString)).thenReturn(true);

        mockMvc.perform(delete("/api/emotion-analysis/chatroom/{chatroomId}/date/{dateString}", testChatroomId, dateString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("成功刪除")));
    }

    @Test
    void testDeleteAnalysisByDate_Success_NotFound() throws Exception {
        String dateString = "2025-10-27";
        when(analysisService.deleteAnalysisByDate(testChatroomId, dateString)).thenReturn(false);

        mockMvc.perform(delete("/api/emotion-analysis/chatroom/{chatroomId}/date/{dateString}", testChatroomId, dateString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("沒有找到")));
    }

    @Test
    void testDeleteAnalysisByDate_Exception() throws Exception {
        String dateString = "2025-10-27";
        when(analysisService.deleteAnalysisByDate(testChatroomId, dateString))
                .thenThrow(new RuntimeException("Delete Error"));

        mockMvc.perform(delete("/api/emotion-analysis/chatroom/{chatroomId}/date/{dateString}", testChatroomId, dateString))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Delete Error"));
    }

    @Test
    void testDeleteTodayAnalysis_Success_Deleted() throws Exception {
        when(analysisService.deleteTodayAnalysis(testChatroomId)).thenReturn(true);

        mockMvc.perform(delete("/api/emotion-analysis/chatroom/{chatroomId}/today", testChatroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成功刪除當天的情緒分析記錄"));
    }

    @Test
    void testDeleteTodayAnalysis_Success_NotFound() throws Exception {
        when(analysisService.deleteTodayAnalysis(testChatroomId)).thenReturn(false);

        mockMvc.perform(delete("/api/emotion-analysis/chatroom/{chatroomId}/today", testChatroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("沒有找到當天的情緒分析記錄"));
    }

    @Test
    void testDeleteTodayAnalysis_Exception() throws Exception {
        when(analysisService.deleteTodayAnalysis(testChatroomId))
                .thenThrow(new RuntimeException("Delete Error"));

        mockMvc.perform(delete("/api/emotion-analysis/chatroom/{chatroomId}/today", testChatroomId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Delete Error"));
    }

    @Test
    void testGetLatestAnalysisForChatrooms_Success() throws Exception {
        Map<String, List<String>> request = new HashMap<>();
        request.put("chatroomIds", Arrays.asList("room1", "room2"));

        List<DailyEmotionAnalysis> analyses = Arrays.asList(mockAnalysis);
        when(analysisService.getLatestAnalysisForChatrooms(anyList())).thenReturn(analyses);

        mockMvc.perform(post("/api/emotion-analysis/chatrooms/latest-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetLatestAnalysisForChatrooms_BadRequest_NullList() throws Exception {
        Map<String, List<String>> request = new HashMap<>();
        mockMvc.perform(post("/api/emotion-analysis/chatrooms/latest-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetLatestAnalysisForChatrooms_BadRequest_EmptyList() throws Exception {
        Map<String, List<String>> request = new HashMap<>();
        request.put("chatroomIds", Collections.emptyList());

        mockMvc.perform(post("/api/emotion-analysis/chatrooms/latest-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetLatestAnalysisForChatrooms_Exception() throws Exception {
        Map<String, List<String>> request = new HashMap<>();
        request.put("chatroomIds", Arrays.asList("room1"));

        when(analysisService.getLatestAnalysisForChatrooms(anyList()))
                .thenThrow(new RuntimeException("Query Error"));

        mockMvc.perform(post("/api/emotion-analysis/chatrooms/latest-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
