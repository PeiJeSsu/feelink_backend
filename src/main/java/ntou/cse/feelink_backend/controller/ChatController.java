package com.example.chatapp.controller;

import com.example.chatapp.model.ApiResponse;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.ChatService;
import com.example.chatapp.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    private final ChatService chatService;
    private final ApiService apiService;

    @Autowired
    public ChatController(ChatService chatService, ApiService apiService) {
        this.chatService = chatService;
        this.apiService = apiService;
    }

    // 發送使用者訊息
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        // 儲存使用者訊息
        ChatMessage savedMessage = chatService.saveMessage(message);

        // 呼叫外部API
        if (message.isUser()) {
            apiService.callExternalApi(message.getContent(), message.getSessionId());
        }
        savedMessage.setContent("測試用 " + savedMessage.getContent() + " [時間: " + System.currentTimeMillis() + "]");
        //這裡要處理成單純傳送要回傳的訊息
        return ResponseEntity.ok(savedMessage);
    }

    // 獲取聊天歷史
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId);
        return ResponseEntity.ok(messages);
    }

    // 直接呼叫API並返回回應
    @PostMapping("/api-message")
    public ResponseEntity<ApiResponse> sendApiMessage(@RequestParam String message,
                                                      @RequestParam String sessionId) {
        ApiResponse response = apiService.callExternalApi(message, sessionId);
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