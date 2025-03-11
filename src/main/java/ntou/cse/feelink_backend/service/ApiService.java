package com.example.chatapp.service;

import com.example.chatapp.model.ApiResponse;
import com.example.chatapp.model.ChatMessage;

public interface ApiService {
    ApiResponse callExternalApi(String message, String sessionId);
}