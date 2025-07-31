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
@Service
public class PlainChatServiceImpl {

    private final ChatSessionRegistry registry;
    private final MessageFormattingService messageFormattingService = new MessageFormattingService();
    public PlainChatServiceImpl(ChatSessionRegistry registry) {
        this.registry = registry;
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
    public SseEmitter replyStream(String userMessage, String sessionId) {
        ChatSession chatSession = registry.getSession(sessionId);

        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(userMessage);
                for (GenerateContentResponse response : responseStream) {
                    String token = ResponseHandler.getText(response);
                    if (token != null && !token.isEmpty()) {
                        emitter.send(SseEmitter.event().data(token));
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}


