package com.example.chatapp.service;

import com.example.chatapp.model.ApiResponse;
import com.example.chatapp.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiServiceImpl implements ApiService {

    private final RestTemplate restTemplate;
    private final ChatService chatService;
    private final String apiUrl = "https://api.example.com/chat"; // 替換為您的API URL

    @Autowired
    public ApiServiceImpl(RestTemplate restTemplate, ChatService chatService) {
        this.restTemplate = restTemplate;
        this.chatService = chatService;
    }

    @Override
    public ApiResponse callExternalApi(String message, String sessionId) {
        try {
            // 要接API的部分
            // ApiResponse response = restTemplate.postForObject(apiUrl, message, ApiResponse.class);

            // 模擬API回應
            ApiResponse response = new ApiResponse("API回應: " + message, true);

            // 處理回應
            if (response.isSuccess()) {
                // 建立一個系統訊息（非使用者）
                ChatMessage apiMessage = new ChatMessage(response.getMessage(), false, sessionId);

                // 使用聊天服務儲存訊息
                chatService.saveMessage(apiMessage);
            }

            return response;
        } catch (Exception e) {
            return new ApiResponse("呼叫API錯誤: " + e.getMessage(), false);
        }
    }
}