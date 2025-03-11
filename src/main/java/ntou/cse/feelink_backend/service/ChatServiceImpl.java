package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    // 記憶體儲存（可以替換成數據庫儲存庫）
    private final Map<String, List<ChatMessage>> chatHistory = new HashMap<>();

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        String sessionId = message.getSessionId();
        chatHistory.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        return message;
    }

    @Override
    public List<ChatMessage> getMessagesBySessionId(String sessionId) {
        return chatHistory.getOrDefault(sessionId, new ArrayList<>());
    }
}