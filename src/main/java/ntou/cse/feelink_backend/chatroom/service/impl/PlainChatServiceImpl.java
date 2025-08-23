package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.genai.Chat;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentResponse;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import java.io.IOException;
@Service
public class PlainChatServiceImpl {

    private final ChatSessionRegistry registry;
    private final MessageFormattingService messageFormattingService;
    private final EmotionPredictionChatServiceImpl emotionPredictionChatService;
    public PlainChatServiceImpl(ChatSessionRegistry registry, EmotionPredictionChatServiceImpl emotionPredictionChatService, MessageFormattingService messageFormattingService) {
        this.registry = registry;
        this.emotionPredictionChatService = emotionPredictionChatService;
        this.messageFormattingService = messageFormattingService;
    }

    public ChatMessage reply(String userMessage, String sessionId, Integer conversationCount, Boolean hasDefaultQuestion) {
        System.out.println("開始處理請求: " + userMessage + ", sessionId: " + sessionId);

        Chat chatSession = registry.getSession(sessionId);
        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        System.out.println("格式化後的訊息: " + formattedMessage);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setUser(false);
        aiMsg.setSessionId(sessionId);

        try {
            GenerateContentResponse resp = chatSession.sendMessage(formattedMessage);
            System.out.println("收到回應物件: " + resp.text());

            String responseText = resp.text();
            System.out.println("回應文字: " + responseText);

            if (responseText != null && !responseText.trim().isEmpty()) {
                aiMsg.setContent(responseText);
            } else {
                System.out.println("回應為空，可能被安全過濾器阻擋");
                aiMsg.setContent("抱歉，我無法回應這個問題");
            }

        } catch (Exception e) {
            System.err.println("AI處理錯誤: " + e.getMessage());
            e.printStackTrace();
            aiMsg.setContent("AI處理錯誤: " + e.getMessage());
        }

        return aiMsg;
    }

    public SseEmitter replyStream(String userMessage, String sessionId,Integer conversationCount, Boolean hasDefaultQuestion) {
        Chat chatSession = registry.getSession(sessionId);
        String formattedMessage =messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(formattedMessage);
                for (GenerateContentResponse response : responseStream) {
                    String token = response.text();
                    emitter.send(SseEmitter.event().data(token));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    public SseEmitter replyStream(String userMessage, String sessionId, Integer conversationCount, Boolean hasDefaultQuestion, String personality) throws IOException {
        Chat chatSession = registry.getSession(sessionId, personality);
        String emotionPrediction;
        try {
            emotionPrediction = emotionPredictionChatService.processChat(userMessage);
        } catch (Exception e) {
            emotionPrediction = "AI處理情緒預測錯誤";
        }
        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        String analysisPrompt = formattedMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(analysisPrompt);
                for (GenerateContentResponse response : responseStream) {
                    String token = response.text();
                    emitter.send(SseEmitter.event().data(token));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}


