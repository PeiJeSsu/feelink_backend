package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final ChatService chatService;

    public AnalysisController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessage> analyse(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId")   String sessionId) throws IOException {

        return ResponseEntity.ok(chatService.processChat(file, userMessage, sessionId));
    }
}
