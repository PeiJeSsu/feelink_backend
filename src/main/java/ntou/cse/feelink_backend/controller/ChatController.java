package ntou.cse.feelink_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import ntou.cse.feelink_backend.service.ChatService;
import ntou.cse.feelink_backend.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/chat")

public class ChatController {

    private final ChatService chatService;


    @Autowired
    public ChatController(ChatService chatService ) {
        this.chatService = chatService;

    }


    @PostMapping("/chat")
    public ResponseEntity<ChatMessage> chat(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId") String sessionId) throws IOException {
        System.out.println("收到請求，文字訊息: " + userMessage);
        // 儲存使用者訊息
        ChatMessage userMsg = new ChatMessage(userMessage, true, sessionId);
        ChatMessage savedMessage = chatService.saveMessage(userMsg);

        // 處理圖片和文字並呼叫AI
        String aiResponse = chatService.processChat(file, userMessage);

        // 儲存AI回應
        ChatMessage aiMsg = new ChatMessage(aiResponse, false, sessionId);
        chatService.saveMessage(aiMsg);

        return ResponseEntity.ok(aiMsg);
    }

    
}
