package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll();
    }

    public Optional<ChatMessage> getMessageById(String messageId) {
        return chatMessageRepository.findById(messageId);
    }

    public List<ChatMessage> getMessagesByChatroomId(String chatroomId) {
        return chatMessageRepository.findByChatroomIdOrderBySentAtAsc(chatroomId);
    }

    public ChatMessage createMessage(ChatMessage message) {
        message.setSentAt(new Date());
        if (message.getIsDrawingData() == null) {
            message.setIsDrawingData(false);
        }
        return chatMessageRepository.save(message);
    }

    public ChatMessage updateMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    public void deleteMessage(String messageId) {
        chatMessageRepository.deleteById(messageId);
    }
    public int deleteAllMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        int count = messages.size();

        for (ChatMessage message : messages) {
            deleteMessage(message.getMessageId());
        }

        return count;
    }

    /**
     * 刪除聊天室的使用者訊息
     */
    public int deleteUserMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        List<ChatMessage> userMessages = messages.stream()
                .filter(ChatMessage::getIsUser)
                .collect(Collectors.toList());

        int count = userMessages.size();

        for (ChatMessage message : userMessages) {
            deleteMessage(message.getMessageId());
        }

        return count;
    }

    /**
     * 刪除聊天室的AI訊息
     */
    public int deleteAIMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        List<ChatMessage> aiMessages = messages.stream()
                .filter(msg -> !msg.getIsUser())
                .collect(Collectors.toList());

        int count = aiMessages.size();

        for (ChatMessage message : aiMessages) {
            deleteMessage(message.getMessageId());
        }

        return count;
    }

    /**
     * 刪除聊天室的文字訊息（非畫布資料）
     */
    public int deleteTextMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        List<ChatMessage> textMessages = messages.stream()
                .filter(msg -> !msg.getIsDrawingData())
                .collect(Collectors.toList());

        int count = textMessages.size();

        for (ChatMessage message : textMessages) {
            deleteMessage(message.getMessageId());
        }

        return count;
    }

    /**
     * 刪除聊天室的畫布訊息
     */
    public int deleteDrawingMessagesByChatroomId(String chatroomId) {
        List<ChatMessage> messages = getMessagesByChatroomId(chatroomId);
        List<ChatMessage> drawingMessages = messages.stream()
                .filter(ChatMessage::getIsDrawingData)
                .collect(Collectors.toList());

        int count = drawingMessages.size();

        for (ChatMessage message : drawingMessages) {
            deleteMessage(message.getMessageId());
        }

        return count;
    }
}