package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/chat")
public class ChatController {

    private final PlainChatServiceImpl service;

    public ChatController(PlainChatServiceImpl service) {
        this.service = service;
    }

    @PostMapping( produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId")   String sessionId) {

        return service.replyStream(userMessage, sessionId);
    }
}
