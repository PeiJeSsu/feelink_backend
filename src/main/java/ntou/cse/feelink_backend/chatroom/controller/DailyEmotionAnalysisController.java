package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.DailyEmotionAnalysis;
import ntou.cse.feelink_backend.chatroom.service.DailyEmotionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotion-analysis")
public class DailyEmotionAnalysisController {

    @Autowired
    private DailyEmotionAnalysisService analysisService;

    // 執行並獲取今天的完整分析
    @PostMapping("/chatroom/{chatroomId}/analyze-today")
    public ResponseEntity<DailyEmotionAnalysis> analyzeAndSaveToday(@PathVariable String chatroomId) {
        try {
            DailyEmotionAnalysis result = analysisService.performAndSaveTodayAnalysis(chatroomId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 獲取今天的分析結果（從資料庫讀取）
    @GetMapping("/chatroom/{chatroomId}/today")
    public ResponseEntity<DailyEmotionAnalysis> getTodayAnalysis(@PathVariable String chatroomId) {
        DailyEmotionAnalysis analysis = analysisService.getTodayAnalysis(chatroomId);
        if (analysis != null) {
            return ResponseEntity.ok(analysis);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 獲取歷史分析記錄
    @GetMapping("/chatroom/{chatroomId}/history")
    public ResponseEntity<List<DailyEmotionAnalysis>> getHistoryAnalysis(@PathVariable String chatroomId) {
        List<DailyEmotionAnalysis> history = analysisService.getHistoryAnalysis(chatroomId);
        return ResponseEntity.ok(history);
    }
    // 獲取所有分析記錄
    @GetMapping("/chatroom/{chatroomId}/all")
    public ResponseEntity<List<DailyEmotionAnalysis>> getAllAnalysisForChatroom(@PathVariable String chatroomId) {
        try {
            List<DailyEmotionAnalysis> analyses = analysisService.getAllAnalysisForChatroom(chatroomId);
            return ResponseEntity.ok(analyses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 刪除指定日期的情緒分析記錄
    @DeleteMapping("/chatroom/{chatroomId}/date/{dateString}")
    public ResponseEntity<Map<String, Object>> deleteAnalysisByDate(
            @PathVariable String chatroomId,
            @PathVariable String dateString) {
        try {
            System.out.println("收到刪除請求 - chatroomId: " + chatroomId + ", date: " + dateString);

            boolean deleted = analysisService.deleteAnalysisByDate(chatroomId, dateString);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("chatroomId", chatroomId);
            response.put("dateString", dateString);

            if (deleted) {
                response.put("message", "成功刪除 " + dateString + " 的情緒分析記錄");
            } else {
                response.put("message", "沒有找到 " + dateString + " 的情緒分析記錄");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("刪除情緒分析記錄 API 失敗: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("chatroomId", chatroomId);
            errorResponse.put("dateString", dateString);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 刪除當天的情緒分析記錄
    @DeleteMapping("/chatroom/{chatroomId}/today")
    public ResponseEntity<Map<String, Object>> deleteTodayAnalysis(@PathVariable String chatroomId) {
        try {
            boolean deleted = analysisService.deleteTodayAnalysis(chatroomId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "成功刪除當天的情緒分析記錄" : "沒有找到當天的情緒分析記錄");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
