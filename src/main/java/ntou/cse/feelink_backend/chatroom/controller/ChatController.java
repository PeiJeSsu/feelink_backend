package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/chat")
public class ChatController {

    private final PlainChatServiceImpl service;

    public ChatController(PlainChatServiceImpl service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChatMessage> chat(
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId")   String sessionId) {

        return ResponseEntity.ok(service.reply(userMessage, sessionId));
    }
}
