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


    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getSentAt() == null) {
            message.setSentAt(new Date());
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        logger.info("已保存訊息: chatroomId={}, isUser={}, isDrawingData={}",
                message.getChatroomId(), message.getIsUser(), message.getIsDrawingData());
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
        return chatMessageRepository.save(message);
    }


    public void deleteMessage(String messageId) {
        chatMessageRepository.deleteById(messageId);
        logger.info("已刪除訊息: messageId={}", messageId);
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
        logger.info("已刪除聊天室 {} 的 {} 條訊息", chatroomId, count);
        return count;
    }

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

    public void deleteMessagesByChatroomId(String chatroomId) {
        chatMessageRepository.deleteByChatroomId(chatroomId);
        logger.info("已刪除聊天室 {} 的所有訊息", chatroomId);
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
}