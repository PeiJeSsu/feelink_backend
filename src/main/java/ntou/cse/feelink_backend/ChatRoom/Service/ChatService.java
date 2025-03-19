package ntou.cse.feelink_backend.ChatRoom.Service;

import ntou.cse.feelink_backend.ChatRoom.Model.ChatMessage;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ChatService {
    String processChat(MultipartFile file, String userMessage) throws IOException;
}