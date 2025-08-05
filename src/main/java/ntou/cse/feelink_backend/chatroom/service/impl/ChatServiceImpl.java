package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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
        String emotionPrediction = emotionPredictionChatService.processChat(userMessage);

        String analysisPrompt = userMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        String drawingAnalysisResponse = drawingAnalysisChatService.processChat(file, analysisPrompt,sessionId);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent(drawingAnalysisResponse);
        aiMsg.setUser(false);
        aiMsg.setSessionId(sessionId);

        return aiMsg;
    }

    @Override
    public SseEmitter processChatStream(MultipartFile file, String userMessage, String sessionId) throws IOException {
        String emotionPrediction = emotionPredictionChatService.processChat(userMessage);
        String analysisPrompt = userMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        return drawingAnalysisChatService.processChatStream(file, analysisPrompt, sessionId);
    }

    @Override
    public SseEmitter processChatStreamWithPersonality(MultipartFile file, String userMessage, String sessionId, String personality) throws IOException {
        String emotionPrediction = emotionPredictionChatService.processChat(userMessage);
        String analysisPrompt = userMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        return drawingAnalysisChatService.processChatStreamWithPersonality(file, analysisPrompt, sessionId, personality);
    }
}

