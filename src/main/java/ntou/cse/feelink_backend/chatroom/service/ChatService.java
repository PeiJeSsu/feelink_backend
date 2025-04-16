package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ChatService {
    ChatMessage processChat(MultipartFile file, String userMessage, String SessionId) throws IOException;
}