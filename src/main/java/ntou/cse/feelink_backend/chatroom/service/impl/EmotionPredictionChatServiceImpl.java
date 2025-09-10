package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.genai.Chat;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmotionPredictionChatServiceImpl{
    private final Chat emotionPredictorChatSession;

    @Autowired
    public EmotionPredictionChatServiceImpl(Chat emotionPredictorChatSession) {
        this.emotionPredictorChatSession = emotionPredictorChatSession;
    }

    public String processChat(String userMessage) throws IOException {
        GenerateContentResponse response;
        try {
            response = emotionPredictorChatSession.sendMessage(userMessage);
            return response.text();
        } catch (Exception e) {
            return "AI處理情緒預測錯誤";
        }
    }
}