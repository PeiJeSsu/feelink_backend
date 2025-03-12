package ntou.cse.feelink_backend.service;


import ntou.cse.feelink_backend.model.ChatMessage;
import java.util.List;

public interface ChatService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesBySessionId(String sessionId);
}