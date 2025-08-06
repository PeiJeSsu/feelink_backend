package ntou.cse.feelink_backend.chatroom.controller;


import ntou.cse.feelink_backend.chatroom.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final ChatService chatService;

    public AnalysisController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyse(@RequestPart(value = "file", required = false) MultipartFile file,
                              @RequestParam("userMessage") String userMessage,
                              @RequestParam("sessionId") String sessionId,
                              @RequestParam(value = "personality" , defaultValue = "default") String personality) throws IOException {

        return chatService.processChatStreamWithPersonality(file, userMessage, sessionId,personality);
    }
}
