package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import java.util.List;

public interface ChatService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesBySessionId(String sessionId);
}