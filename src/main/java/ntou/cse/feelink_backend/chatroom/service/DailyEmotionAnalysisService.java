package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.DailyEmotionAnalysis;
import ntou.cse.feelink_backend.chatroom.repository.DailyEmotionAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DailyEmotionAnalysisService {

    @Autowired
    private DailyEmotionAnalysisRepository repository;

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @Autowired
    private GoogleNaturalLanguageService googleNaturalLanguageService;

    // 執行當天完整分析並儲存到資料庫
    public DailyEmotionAnalysis performAndSaveTodayAnalysis(String chatroomId) {
        try {
            String today = emotionAnalysisService.getTaiwanTodayString();

            EmotionAnalysisService.EmotionScores emotionScores = emotionAnalysisService.analyzeTodayEmotions(chatroomId);
            String summary = emotionAnalysisService.generateTodayChatSummary(chatroomId);
            String demand = emotionAnalysisService.analyzeTodayDemandSimple(chatroomId);

            GoogleNaturalLanguageService.SimpleSentimentResult sentimentResult = googleNaturalLanguageService.analyzeTodaySentiment(chatroomId);

            Map<String, Float> emotionsMap = new HashMap<>();
            emotionsMap.put("難過", (float) emotionScores.getSadness());
            emotionsMap.put("喜悅", (float) emotionScores.getJoy());
            emotionsMap.put("興奮", (float) emotionScores.getExcited());
            emotionsMap.put("焦慮", (float) emotionScores.getAnxiety());
            emotionsMap.put("生氣", (float) emotionScores.getAnger());
            emotionsMap.put("平靜", (float) emotionScores.getCalm());
            emotionsMap.put("期待", (float) emotionScores.getAnticipation());
            emotionsMap.put("樂觀", (float) emotionScores.getOptimism());

            return saveDailyAnalysis(chatroomId, today, summary, emotionsMap, demand,
                    sentimentResult.getScore(), sentimentResult.getMagnitude());

        } catch (Exception e) {
            System.err.println("執行每日分析失敗: " + e.getMessage());
            throw e;
        }
    }

    // 儲存每日分析到資料庫
    public DailyEmotionAnalysis saveDailyAnalysis(String chatroomId, String dateString, String summary,
                                                  Map<String, Float> emotions, String demand,
                                                  float score, float magnitude) {

        System.out.println("儲存分析資料:");
        System.out.println("chatroomId: " + chatroomId);
        System.out.println("dateString: " + dateString);

        DailyEmotionAnalysis existing = repository.findByChatroomIdAndDateString(chatroomId, dateString);

        DailyEmotionAnalysis analysis;
        if (existing != null) {
            // 更新現有資料
            analysis = existing;
            System.out.println("更新現有資料");
        } else {
            // 建立新資料
            analysis = new DailyEmotionAnalysis();
            analysis.setChatroomId(chatroomId);
            analysis.setDateString(dateString);
            System.out.println("建立新資料");
        }

        // 設定資料
        analysis.setSummary(summary);
        analysis.setEmotions(emotions);
        analysis.setDemand(demand);
        analysis.setScore(score);
        analysis.setMagnitude(magnitude);

        DailyEmotionAnalysis saved = repository.save(analysis);
        return saved;
    }

    // 查詢今天的分析結果
    public DailyEmotionAnalysis getTodayAnalysis(String chatroomId) {
        String today = emotionAnalysisService.getTaiwanTodayString();
        return repository.findByChatroomIdAndDateString(chatroomId, today);
    }

    // 查詢歷史分析記錄
    public List<DailyEmotionAnalysis> getHistoryAnalysis(String chatroomId) {
        return repository.findByChatroomIdOrderByDateStringDesc(chatroomId);
    }

    // 獲取該聊天室所有的情緒分析記錄（用於管理）
    public List<DailyEmotionAnalysis> getAllAnalysisForChatroom(String chatroomId) {
        return repository.findByChatroomId(chatroomId);
    }

    // 查詢最近30天分析記錄
    public List<DailyEmotionAnalysis> getRecentAnalysis(String chatroomId) {
        return repository.findTop30ByChatroomIdOrderByDateStringDesc(chatroomId);
    }

    // 刪除當天的情緒分析記錄
    public boolean deleteTodayAnalysis(String chatroomId) {
        String today = emotionAnalysisService.getTaiwanTodayString();
        System.out.println("刪除當天情緒分析記錄，日期: " + today);
        return deleteAnalysisByDate(chatroomId, today);
    }

    // 刪除指定聊天室、指定日期的情緒分析記錄
    public boolean deleteAnalysisByDate(String chatroomId, String dateString) {
        try {
            System.out.println("準備刪除情緒分析記錄:");
            System.out.println("聊天室ID: " + chatroomId);
            System.out.println("日期: " + dateString);

            // 先檢查是否存在該記錄
            DailyEmotionAnalysis existing = repository.findByChatroomIdAndDateString(chatroomId, dateString);
            if (existing == null) {
                System.out.println("沒有找到該日期的情緒分析記錄");
                return false;
            }

            System.out.println("找到記錄，ID: " + existing.getId());

            // 執行刪除
            long deletedCount = repository.deleteByChatroomIdAndDateString(chatroomId, dateString);
            System.out.println("成功刪除 " + deletedCount + " 筆情緒分析記錄");

            return deletedCount > 0;

        } catch (Exception e) {
            System.err.println("刪除情緒分析記錄失敗: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
