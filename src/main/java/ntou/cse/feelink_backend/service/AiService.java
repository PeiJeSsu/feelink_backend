package ntou.cse.feelink_backend.service;

import ntou.cse.feelink_backend.model.AiResponse;

public interface AiService {
    AiResponse callExternalApi(String message, String sessionId);
}