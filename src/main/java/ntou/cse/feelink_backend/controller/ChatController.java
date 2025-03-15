package ntou.cse.feelink_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import ntou.cse.feelink_backend.service.ChatService;
import ntou.cse.feelink_backend.model.AiResponse;
import ntou.cse.feelink_backend.model.ChatMessage;
import ntou.cse.feelink_backend.service.ChatService;
import ntou.cse.feelink_backend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    private final ChatService chatService;
    private final AiService aiService;

    @Autowired
    public ChatController(ChatService chatService, AiService aiService) {
        this.chatService = chatService;
        this.aiService = aiService;
    }

    // 發送使用者訊息
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        // 儲存使用者訊息
        ChatMessage savedMessage = chatService.saveMessage(message);

        // 呼叫外部API
        if (message.isUser()) {
            aiService.callExternalApi(message.getContent(), message.getSessionId());
        }
        savedMessage.setContent("測試用 " + savedMessage.getContent() + " [時間: " + System.currentTimeMillis() + "]");
        //這裡要處理成單純傳送要回傳的訊息
        return ResponseEntity.ok(savedMessage);
    }
  
    @PostMapping
    public String chat(
            @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam("userMessage") String userMessage) throws IOException {
        return chatService.processChat(file, userMessage);
    }
  
    // 獲取聊天歷史
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId);
        return ResponseEntity.ok(messages);
    }

    // 直接呼叫API並返回回應
    @PostMapping("/api-message")
    public ResponseEntity<AiResponse> sendApiMessage(@RequestParam String message,
                                                      @RequestParam String sessionId) {
        AiResponse response = aiService.callExternalApi(message, sessionId);
        //這裡要接分析的訊息
        return ResponseEntity.ok(response);
    }

    // 獲取最新的API回應（一般為系統訊息）
    @GetMapping("/latest-response/{sessionId}")
    public ResponseEntity<ChatMessage> getLatestResponse(@PathVariable String sessionId) {
        List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId);

        // 找出最後一條非使用者訊息
        ChatMessage latestResponse = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (!messages.get(i).isUser()) {
                latestResponse = messages.get(i);
                break;
            }
        }

        if (latestResponse != null) {
            return ResponseEntity.ok(latestResponse);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
