package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmotionPredictionChatServiceImpl{
    private final ChatSession emotionPredictorChatSession;

    @Autowired
    public EmotionPredictionChatServiceImpl(ChatSession emotionPredictorChatSession) {
        this.emotionPredictorChatSession = emotionPredictorChatSession;
    }

    public String processChat(String userMessage) throws IOException {
        GenerateContentResponse response;

        try {
            response = emotionPredictorChatSession.sendMessage(userMessage);
            return ResponseHandler.getText(response);
        } catch (Exception e) {
            return "AI處理情緒預測錯誤";
        }
    }
}