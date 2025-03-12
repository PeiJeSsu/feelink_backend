package ntou.cse.feelink_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import ntou.cse.feelink_backend.service.ChatService;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String chat(
            @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam("userMessage") String userMessage) throws IOException {
        return chatService.processChat(file, userMessage);
    }
}
