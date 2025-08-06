package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;

import java.util.List;

@Service
public class PlainChatServiceImpl {

    @Autowired
    private ChatSessionRegistry registry;

    @Autowired
    private ChatMessageService chatMessageService;

    private final MessageFormattingService messageFormattingService = new MessageFormattingService();

    public ChatMessage reply(String userMessage, String chatroomId, Integer conversationCount, Boolean hasDefaultQuestion) {
        // 使用 chatroomId 作為 session 識別符
        ChatSession chatSession = registry.getSession(chatroomId);

        // 取得該聊天室的歷史訊息來提供上下文
        List<ChatMessage> chatHistory = chatMessageService.getMessagesByChatroomId(chatroomId);

        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setIsUser(false);

        try {
            GenerateContentResponse resp = chatSession.sendMessage(formattedMessage);
            aiMsg.setContent(ResponseHandler.getText(resp));
        } catch (Exception e) {
            aiMsg.setContent("AI處理錯誤: " + e.getMessage());
        }

        return aiMsg;
    }
}