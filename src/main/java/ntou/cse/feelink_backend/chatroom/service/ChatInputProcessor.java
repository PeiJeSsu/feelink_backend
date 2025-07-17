package ntou.cse.feelink_backend.chatroom.service;


import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.vertexai.generativeai.PartMaker;
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
    public static Object processImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return PartMaker.fromMimeTypeAndData(file.getContentType(), file.getBytes());
    }
}
