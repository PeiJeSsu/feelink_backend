package ntou.cse.feelink_backend.chatroom.service;


import org.springframework.web.multipart.MultipartFile;
import com.google.genai.types.Part;
import java.io.IOException;

public class ChatInputProcessor {

    private ChatInputProcessor() {
    }
    public static String processText(String userMessage, String defaultHint) {
        if (userMessage == null || userMessage.isBlank()) {
            return defaultHint;
        }
        return userMessage;
    }
    public static Part processImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return Part.fromBytes(file.getBytes(), file.getContentType());
    }
}
