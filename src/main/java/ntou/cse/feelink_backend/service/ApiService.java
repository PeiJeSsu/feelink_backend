package ntou.cse.feelink_backend.service;

import ntou.cse.feelink_backend.model.ApiResponse;
import ntou.cse.feelink_backend.model.ChatMessage;

public interface ApiService {
    ApiResponse callExternalApi(String message, String sessionId);
}