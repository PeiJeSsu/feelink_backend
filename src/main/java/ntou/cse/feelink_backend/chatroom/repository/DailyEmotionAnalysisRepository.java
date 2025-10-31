package ntou.cse.feelink_backend.chatroom.repository;

import ntou.cse.feelink_backend.chatroom.model.DailyEmotionAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DailyEmotionAnalysisRepository extends MongoRepository<DailyEmotionAnalysis, String> {

    // 改用字串查詢
    DailyEmotionAnalysis findByChatroomIdAndDateString(String chatroomId, String dateString);

    // 歷史查詢也改用字串排序
    List<DailyEmotionAnalysis> findByChatroomIdOrderByDateStringDesc(String chatroomId);

    //根據聊天室ID和日期字串刪除
    long deleteByChatroomIdAndDateString(String chatroomId, String dateString);

    // 查詢特定聊天室的所有分析記錄(用於管理)
    List<DailyEmotionAnalysis> findByChatroomId(String chatroomId);

    // 查詢多個聊天室的所有分析記錄
    List<DailyEmotionAnalysis> findByChatroomIdIn(List<String> chatroomIds);
}
