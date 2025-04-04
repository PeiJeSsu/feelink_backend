package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

@RestController
public class ChatController {
    private final ChatService drawingAnalysisChatService;
    private final ChatService emotionPredictionChatService;

    @Autowired
    public ChatController(
            @Qualifier("drawingAnalysisChatServiceImpl") ChatService drawingAnalysisChatService,
            @Qualifier("emotionPredictionChatServiceImpl") ChatService emotionPredictionChatService
    ) {
        this.drawingAnalysisChatService = drawingAnalysisChatService;
        this.emotionPredictionChatService = emotionPredictionChatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatMessage> chat(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("userMessage") String userMessage,
            @RequestParam("sessionId") String sessionId) throws IOException {

        String emotionPrediction = emotionPredictionChatService.processChat(null, userMessage);

        String analysisPrompt = userMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        String drawingAnalysisResponse = drawingAnalysisChatService.processChat(file, analysisPrompt);

        ChatMessage aiMsg = createChatMessage(drawingAnalysisResponse, false, sessionId);

        return ResponseEntity.ok(aiMsg);
    }

    private ChatMessage createChatMessage(String content, boolean isUser, String sessionId) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setUser(isUser);
        message.setSessionId(sessionId);
        return message;
    }
}