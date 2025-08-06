package ntou.cse.feelink_backend.chatroom.repository;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 原有方法保持不變
    List<ChatMessage> findByChatroomIdOrderBySentAtAsc(String chatroomId);
    void deleteByChatroomId(String chatroomId);

    // 新增：根據 isUser 查詢的方法
    List<ChatMessage> findByChatroomIdAndIsUserOrderBySentAtAsc(String chatroomId, Boolean isUser);

    // 新增：只取得使用者訊息
    List<ChatMessage> findByChatroomIdAndIsUserTrueOrderBySentAtAsc(String chatroomId);

    // 新增：只取得AI訊息
    List<ChatMessage> findByChatroomIdAndIsUserFalseOrderBySentAtAsc(String chatroomId);

    // 新增：統計使用者訊息數量
    long countByChatroomIdAndIsUserTrue(String chatroomId);

    // 新增：統計AI訊息數量
    long countByChatroomIdAndIsUserFalse(String chatroomId);
}
