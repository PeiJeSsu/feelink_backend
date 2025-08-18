package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.generativeai.ResponseStream;
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

    public ChatMessage reply(String userMessage, String sessionId,Integer conversationCount, Boolean hasDefaultQuestion) {
        ChatSession chatSession = registry.getSession(sessionId);
        String formattedMessage =messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setUser(false);
        aiMsg.setSessionId(sessionId);

        try {
            GenerateContentResponse resp = chatSession.sendMessage(formattedMessage);
            aiMsg.setContent(ResponseHandler.getText(resp));
        } catch (Exception e) {
            aiMsg.setContent("AI處理錯誤");
        }
        return aiMsg;
    }

    public SseEmitter replyStream(String userMessage, String sessionId,Integer conversationCount, Boolean hasDefaultQuestion) {
        ChatSession chatSession = registry.getSession(sessionId);
        String formattedMessage =messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(formattedMessage);
                for (GenerateContentResponse response : responseStream) {
                    String token = ResponseHandler.getText(response);
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
        ChatSession chatSession = registry.getSession(sessionId, personality);
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
                    String token = ResponseHandler.getText(response);
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


