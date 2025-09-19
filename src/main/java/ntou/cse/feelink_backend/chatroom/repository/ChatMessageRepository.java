package ntou.cse.feelink_backend.chatroom.repository;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 原有方法保持不變
    List<ChatMessage> findByChatroomIdOrderBySentAtAsc(String chatroomId);
    void deleteByChatroomId(String chatroomId);

    // 根據 isUser 查詢的方法
    List<ChatMessage> findByChatroomIdAndIsUserOrderBySentAtAsc(String chatroomId, Boolean isUser);

    // 只取得使用者訊息
    List<ChatMessage> findByChatroomIdAndIsUserTrueOrderBySentAtAsc(String chatroomId);

    // 只取得使用者當天訊息
    List<ChatMessage> findByChatroomIdAndIsUserTrueAndIsDrawingDataFalseAndSentAtBetweenOrderBySentAtAsc(String chatroomId, Date startDate, Date endDate);

    // 只取得AI訊息
    List<ChatMessage> findByChatroomIdAndIsUserFalseOrderBySentAtAsc(String chatroomId);

    // 根據 isDrawingData 查詢的方法
    List<ChatMessage> findByChatroomIdAndIsDrawingDataOrderBySentAtAsc(String chatroomId, Boolean isDrawingData);

    //只取得文字訊息（非畫布資料）
    List<ChatMessage> findByChatroomIdAndIsDrawingDataFalseOrderBySentAtAsc(String chatroomId);

    // 只取得畫布資料
    List<ChatMessage> findByChatroomIdAndIsDrawingDataTrueOrderBySentAtAsc(String chatroomId);

    // 複合查詢 - 使用者的畫布資料
    List<ChatMessage> findByChatroomIdAndIsUserTrueAndIsDrawingDataTrueOrderBySentAtAsc(String chatroomId);

    // 複合查詢 - AI的畫布資料
    List<ChatMessage> findByChatroomIdAndIsUserFalseAndIsDrawingDataTrueOrderBySentAtAsc(String chatroomId);

    // 複合查詢 - 使用者的文字訊息
    List<ChatMessage> findByChatroomIdAndIsUserTrueAndIsDrawingDataFalseOrderBySentAtAsc(String chatroomId);

    // 複合查詢 - AI的文字訊息
    List<ChatMessage> findByChatroomIdAndIsUserFalseAndIsDrawingDataFalseOrderBySentAtAsc(String chatroomId);

    // 查詢某個日期範圍的訊息
    List<ChatMessage> findByChatroomIdAndSentAtBetweenOrderBySentAtAsc(String chatroomId, Date startDate, Date endDate);

    // 查詢某個日期範圍的文字訊息
    List<ChatMessage> findByChatroomIdAndIsDrawingDataFalseAndSentAtBetweenOrderBySentAtAsc(String chatroomId, Date startDate, Date endDate);

    // 統計方法
    long countByChatroomId(String chatroomId);
    long countByChatroomIdAndIsUserTrue(String chatroomId);
    long countByChatroomIdAndIsUserFalse(String chatroomId);

    // 統計畫布資料數量
    long countByChatroomIdAndIsDrawingDataTrue(String chatroomId);

    // 統計文字訊息數量
    long countByChatroomIdAndIsDrawingDataFalse(String chatroomId);

    // 刪除方法 - 根據條件刪除
    void deleteByChatroomIdAndIsUserTrue(String chatroomId);
    void deleteByChatroomIdAndIsUserFalse(String chatroomId);
    void deleteByChatroomIdAndIsDrawingDataTrue(String chatroomId);
    void deleteByChatroomIdAndIsDrawingDataFalse(String chatroomId);
}