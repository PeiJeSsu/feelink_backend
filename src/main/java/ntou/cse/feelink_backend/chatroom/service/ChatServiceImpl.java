package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ChatServiceImpl implements ChatService {

    private final DrawingAnalysisChatServiceImpl drawingAnalysisChatService;
    private final EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Autowired
    public ChatServiceImpl(
            DrawingAnalysisChatServiceImpl drawingAnalysisChatService,
            EmotionPredictionChatServiceImpl emotionPredictionChatService) {
        this.drawingAnalysisChatService = drawingAnalysisChatService;
        this.emotionPredictionChatService = emotionPredictionChatService;
    }

    @Override
    public ChatMessage processChat(MultipartFile file, String userMessage, String sessionId) throws IOException {
        String emotionPrediction = emotionPredictionChatService.processChat(null, userMessage);

        String analysisPrompt = userMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        String drawingAnalysisResponse = drawingAnalysisChatService.processChat(file, analysisPrompt);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent(drawingAnalysisResponse);
        aiMsg.setUser(false);
        aiMsg.setSessionId(sessionId);

        return aiMsg;
    }
}

