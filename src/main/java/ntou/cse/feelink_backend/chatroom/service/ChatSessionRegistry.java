package ntou.cse.feelink_backend.chatroom.service;

import com.google.genai.Client;
import com.google.genai.Chat;
import com.google.genai.types.GenerateContentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionRegistry {
    private final Client client;
    private final GenerateContentConfig drawingAnalysisConfig;
    private final GenerateContentConfig creativeConfig;
    private final GenerateContentConfig curiousAnalystConfig;
    private final GenerateContentConfig warmMentorConfig;
    private final ConcurrentHashMap<String, Chat> pool = new ConcurrentHashMap<>();

    @Autowired
    public ChatSessionRegistry(
            Client client,
            @Qualifier("drawingAnalysisConfig") GenerateContentConfig drawingAnalysisConfig,
            @Qualifier("creativeConfig") GenerateContentConfig creativeConfig,
            @Qualifier("curiousAnalystConfig") GenerateContentConfig curiousAnalystConfig,
            @Qualifier("warmMentorConfig") GenerateContentConfig warmMentorConfig) {
        this.client = client;
        this.drawingAnalysisConfig = drawingAnalysisConfig;
        this.creativeConfig = creativeConfig;
        this.curiousAnalystConfig = curiousAnalystConfig;
        this.warmMentorConfig = warmMentorConfig;
    }

    public Chat getSession(String sessionId) {
        return getSession(sessionId, "default");
    }

    public Chat getSession(String sessionId, String personality) {
        String key = sessionId + "_" + personality;
        Chat session = pool.get(key);
        if (session == null) {
            GenerateContentConfig config = getConfigByPersonality(personality);
            Chat newSession = client.chats.create("gemini-2.5-flash-lite", config);
            Chat existed = pool.putIfAbsent(key, newSession);
            if (existed != null) {
                session = existed;
            } else {
                session = newSession;
            }
        }
        return session;
    }

    private GenerateContentConfig getConfigByPersonality(String personality) {
        switch (personality.toLowerCase()) {
            case "creative":
                return creativeConfig;
            case "curious":
                return curiousAnalystConfig;
            case "warm":
                return warmMentorConfig;
            default:
                return drawingAnalysisConfig;
        }
    }
}
