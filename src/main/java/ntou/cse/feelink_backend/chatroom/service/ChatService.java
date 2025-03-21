package ntou.cse.feelink_backend.chatroom.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ChatService {
    String processChat(MultipartFile file, String userMessage) throws IOException;
}