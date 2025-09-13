package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatSessionRegistry chatSessionRegistry;

    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getSentAt() == null) {
            message.setSentAt(new Date());
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        logger.info("已保存訊息");  // 移除 user-controlled 資料
        return savedMessage;
    }

    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll();
    }

    public Optional<ChatMessage> getMessageById(String messageId) {
        return chatMessageRepository.findById(messageId);
    }

    public ChatMessage createMessage(ChatMessage message) {
        return saveMessage(message);
    }

    public ChatMessage updateMessage(ChatMessage message) {
        ChatMessage updatedMessage = chatMessageRepository.save(message);
        logger.info("已更新訊息");  // 安全日誌
        return updatedMessage;
    }

    public void deleteMessage(String messageId) {
        chatMessageRepository.deleteById(messageId);
        logger.info("已刪除訊息");  // 移除 user-controlled 資料
    }

    public List<ChatMessage> getMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdOrderBySentAtAsc(chatroomId);
    }

    public List<ChatMessage> getTextMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsDrawingDataFalseOrderBySentAtAsc(chatroomId);
    }

    public List<ChatMessage> getDrawingMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsDrawingDataTrueOrderBySentAtAsc(chatroomId);
    }

    public List<ChatMessage> getUserMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsUserTrueOrderBySentAtAsc(chatroomId);
    }

    public List<ChatMessage> getAIMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsUserFalseOrderBySentAtAsc(chatroomId);
    }

    public int deleteAllMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        int count = messages.size();
        chatMessageRepository.deleteByChatroomId(chatroomId);
        chatSessionRegistry.recreateSession(chatroomId);
        logger.info("已刪除訊息");  // 安全日誌
        return count;
    }

    public int deleteUserMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> userMessages = getUserMessagesByChatroomId(chatroomId);
        int count = userMessages.size();

        for (ChatMessage message : userMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除使用者訊息");  // 安全日誌
        return count;
    }

    public int deleteAIMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> aiMessages = getAIMessagesByChatroomId(chatroomId);
        int count = aiMessages.size();

        for (ChatMessage message : aiMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除 AI 訊息");  // 安全日誌
        return count;
    }

    public int deleteTextMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> textMessages = getTextMessagesByChatroomId(chatroomId);
        int count = textMessages.size();

        for (ChatMessage message : textMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除文字訊息");  // 安全日誌
        return count;
    }

    public int deleteDrawingMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> drawingMessages = getDrawingMessagesByChatroomId(chatroomId);
        int count = drawingMessages.size();

        for (ChatMessage message : drawingMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除畫布訊息");  // 安全日誌
        return count;
    }

    public void deleteMessagesByChatroomId(String chatroomId) {
        chatMessageRepository.deleteByChatroomId(chatroomId);
        logger.info("已刪除所有訊息");  // 安全日誌
    }

    public long countMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.countByChatroomId(chatroomId);
    }

    public long countUserMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.countByChatroomIdAndIsUserTrue(chatroomId);
    }

    public long countAIMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.countByChatroomIdAndIsUserFalse(chatroomId);
    }

    public List<ChatMessage> getTextMessagesByDate(String chatroomId, String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date targetDate = sdf.parse(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(targetDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startDate = calendar.getTime();

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = calendar.getTime();

            return chatMessageRepository.findByChatroomIdAndIsDrawingDataFalseAndSentAtBetweenOrderBySentAtAsc(chatroomId, startDate, endDate);

        } catch (ParseException e) {
            logger.error("日期格式錯誤: " + date);
            return new ArrayList<>();
        }
    }
}
