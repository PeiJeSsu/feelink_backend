package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.springframework.stereotype.Service;

@Service
public class PlainChatServiceImpl {

    private final ChatSessionRegistry registry;

    public PlainChatServiceImpl(ChatSessionRegistry registry) {
        this.registry = registry;
    }

    public ChatMessage reply(String userMessage, String sessionId) {

        ChatSession chatSession = registry.getSession(sessionId);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setUser(false);
        aiMsg.setSessionId(sessionId);

        try {
            GenerateContentResponse resp = chatSession.sendMessage(userMessage);
            aiMsg.setContent(ResponseHandler.getText(resp));
        } catch (Exception e) {
            aiMsg.setContent("AI處理錯誤");
        }
        return aiMsg;
    }
}


