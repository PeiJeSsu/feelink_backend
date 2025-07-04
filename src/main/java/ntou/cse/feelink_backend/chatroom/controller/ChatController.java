package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
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
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "conversationCount", required = false) Integer conversationCount,
            @RequestParam(value = "hasDefaultQuestion", required = false) Boolean hasDefaultQuestion) throws IOException {

        ChatMessage aiMsg = chatService.processChat(file, userMessage, sessionId, conversationCount, hasDefaultQuestion);
        return ResponseEntity.ok(aiMsg);
    }
}