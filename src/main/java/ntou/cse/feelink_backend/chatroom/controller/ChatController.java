package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final PlainChatServiceImpl service;

    public ChatController(PlainChatServiceImpl service) {
        this.service = service;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "conversationCount", required = false) Integer conversationCount,
            @RequestParam(value = "hasDefaultQuestion", required = false) Boolean hasDefaultQuestion,
            @RequestParam(value = "personality", defaultValue = "default") String personality) throws IOException {

        return service.replyStream(userMessage, sessionId, conversationCount, hasDefaultQuestion, personality);
    }
}
