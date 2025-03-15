package ntou.cse.feelink_backend.service;
import ntou.cse.feelink_backend.model.ChatMessage;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
public interface ChatService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesBySessionId(String sessionId);
  String processChat(MultipartFile file, String userMessage) throws IOException;
}
