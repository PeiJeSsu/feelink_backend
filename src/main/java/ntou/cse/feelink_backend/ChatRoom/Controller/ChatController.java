package ntou.cse.feelink_backend.ChatRoom.Controller;

import ntou.cse.feelink_backend.ChatRoom.Service.ChatService;
import ntou.cse.feelink_backend.ChatRoom.Model.ChatMessage;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

@RestController
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatMessage> chat(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId") String sessionId) throws IOException {

        String aiResponse = chatService.processChat(file, userMessage);

        ChatMessage aiMsg = createChatMessage(aiResponse, false, sessionId);

        return ResponseEntity.ok(aiMsg);
    }

    private ChatMessage createChatMessage(String content, boolean isUser, String sessionId) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setUser(isUser);
        message.setSessionId(sessionId);
        return message;
    }
}