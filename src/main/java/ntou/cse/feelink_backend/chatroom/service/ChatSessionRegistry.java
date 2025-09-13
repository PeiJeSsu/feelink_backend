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

    public Chat getSession(String chatroomId) {
        return getSession(chatroomId, "default");
    }

    public Chat getSession(String chatroomId, String personality) {
        String key = chatroomId + "_" + personality;
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

    public void recreateSession(String chatroomId) {
        String targetKey = null;
        for (String key : pool.keySet()) {
            if (key.startsWith(chatroomId + "_")) {
                targetKey = key;
                break;
            }
        }

        if (targetKey != null) {
            String personality = targetKey.substring((chatroomId + "_").length());

            pool.remove(targetKey);

            GenerateContentConfig config = getConfigByPersonality(personality);
            Chat newSession = client.chats.create("gemini-2.5-flash-lite", config);
            pool.put(targetKey, newSession);

            System.out.println("重新創建 session: " + targetKey + " (personality: " + personality + ")");
        }
    }
}
