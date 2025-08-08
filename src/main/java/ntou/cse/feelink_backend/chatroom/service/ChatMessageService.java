package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Date;
import java.util.Optional;

@Service
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * 保存聊天訊息
     */
    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getSentAt() == null) {
            message.setSentAt(new Date());
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        logger.info("已保存訊息: chatroomId={}, isUser={}, isDrawingData={}",
                message.getChatroomId(), message.getIsUser(), message.getIsDrawingData());
        return savedMessage;
    }

    /**
     * 🎯 新增：取得所有訊息 (for Controller)
     */
    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll();
    }

    /**
     * 🎯 新增：根據ID取得訊息 (for Controller)
     */
    public Optional<ChatMessage> getMessageById(String messageId) {
        return chatMessageRepository.findById(messageId);
    }

    /**
     * 🎯 新增：創建訊息 (for Controller)
     */
    public ChatMessage createMessage(ChatMessage message) {
        return saveMessage(message);
    }

    /**
     * 🎯 新增：更新訊息 (for Controller)
     */
    public ChatMessage updateMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    /**
     * 🎯 新增：刪除單一訊息 (for Controller)
     */
    public void deleteMessage(String messageId) {
        chatMessageRepository.deleteById(messageId);
        logger.info("已刪除訊息: messageId={}", messageId);
    }

    /**
     * 根據聊天室ID載入所有訊息
     */
    public List<ChatMessage> getMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdOrderBySentAtAsc(chatroomId);
    }

    /**
     * 根據聊天室ID載入文字訊息（非畫布資料）
     */
    public List<ChatMessage> getTextMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsDrawingDataFalseOrderBySentAtAsc(chatroomId);
    }

    /**
     * 根據聊天室ID載入畫布資料
     */
    public List<ChatMessage> getDrawingMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsDrawingDataTrueOrderBySentAtAsc(chatroomId);
    }

    /**
     * 根據聊天室ID載入使用者訊息
     */
    public List<ChatMessage> getUserMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsUserTrueOrderBySentAtAsc(chatroomId);
    }

    /**
     * 根據聊天室ID載入AI訊息
     */
    public List<ChatMessage> getAIMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdAndIsUserFalseOrderBySentAtAsc(chatroomId);
    }

    /**
     * 🎯 新增：刪除聊天室的所有訊息並回傳刪除數量 (for Controller)
     */
    public int deleteAllMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        int count = messages.size();
        chatMessageRepository.deleteByChatroomId(chatroomId);
        logger.info("已刪除聊天室 {} 的 {} 條訊息", chatroomId, count);
        return count;
    }

    /**
     * 🎯 新增：刪除聊天室的使用者訊息並回傳刪除數量
     */
    public int deleteUserMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> userMessages = getUserMessagesByChatroomId(chatroomId);
        int count = userMessages.size();

        // 刪除使用者訊息
        for (ChatMessage message : userMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除聊天室 {} 的 {} 條使用者訊息", chatroomId, count);
        return count;
    }

    /**
     * 🎯 新增：刪除聊天室的AI訊息並回傳刪除數量
     */
    public int deleteAIMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> aiMessages = getAIMessagesByChatroomId(chatroomId);
        int count = aiMessages.size();

        // 刪除AI訊息
        for (ChatMessage message : aiMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除聊天室 {} 的 {} 條AI訊息", chatroomId, count);
        return count;
    }

    /**
     * 🎯 新增：刪除聊天室的文字訊息並回傳刪除數量
     */
    public int deleteTextMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> textMessages = getTextMessagesByChatroomId(chatroomId);
        int count = textMessages.size();

        // 刪除文字訊息
        for (ChatMessage message : textMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除聊天室 {} 的 {} 條文字訊息", chatroomId, count);
        return count;
    }

    /**
     * 🎯 新增：刪除聊天室的畫布訊息並回傳刪除數量
     */
    public int deleteDrawingMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> drawingMessages = getDrawingMessagesByChatroomId(chatroomId);
        int count = drawingMessages.size();

        // 刪除畫布訊息
        for (ChatMessage message : drawingMessages) {
            chatMessageRepository.deleteById(message.getMessageId());
        }

        logger.info("已刪除聊天室 {} 的 {} 條畫布訊息", chatroomId, count);
        return count;
    }

    /**
     * 刪除聊天室的所有訊息（原方法保持不變）
     */
    public void deleteMessagesByChatroomId(String chatroomId) {
        chatMessageRepository.deleteByChatroomId(chatroomId);
        logger.info("已刪除聊天室 {} 的所有訊息", chatroomId);
    }

    /**
     * 統計聊天室訊息數量
     */
    public long countMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.countByChatroomId(chatroomId);
    }

    /**
     * 統計使用者訊息數量
     */
    public long countUserMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.countByChatroomIdAndIsUserTrue(chatroomId);
    }

    /**
     * 統計AI訊息數量
     */
    public long countAIMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.countByChatroomIdAndIsUserFalse(chatroomId);
    }
}