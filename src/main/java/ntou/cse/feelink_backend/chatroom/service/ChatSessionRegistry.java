package ntou.cse.feelink_backend.chatroom.service;

import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionRegistry {
    private final GenerativeModel drawingAnalysisModel;
    private final GenerativeModel creativeModel;
    private final GenerativeModel curiousAnalystModel;
    private final GenerativeModel warmMentorModel;
    private final ConcurrentHashMap<String, ChatSession> pool = new ConcurrentHashMap<>();

    @Autowired
    public ChatSessionRegistry(
            @Qualifier("drawingAnalysisModel") GenerativeModel drawingAnalysisModel,
            @Qualifier("creativeModel") GenerativeModel creativeModel,
            @Qualifier("curiousAnalystModel") GenerativeModel curiousAnalystModel,
            @Qualifier("warmMentorModel") GenerativeModel warmMentorModel) {
        this.drawingAnalysisModel = drawingAnalysisModel;
        this.creativeModel = creativeModel;
        this.curiousAnalystModel = curiousAnalystModel;
        this.warmMentorModel = warmMentorModel;
    }

    public ChatSession getSession(String sessionId) {
        return getSession(sessionId, "default");
    }
    public ChatSession getSession(String sessionId, String personality) {
        String key = sessionId + "_" + personality;
        ChatSession session = pool.get(key);
        if (session == null) {
            GenerativeModel model = getModelByPersonality(personality);
            ChatSession newSession = new ChatSession(model);
            ChatSession existed = pool.putIfAbsent(key, newSession);
            if (existed != null) {
                session = existed;
            } else {
                session = newSession;
            }
        }
        return session;
    }

    private GenerativeModel getModelByPersonality(String personality) {
        switch (personality.toLowerCase()) {
            case "creative":
                return creativeModel;
            case "curious":
                return curiousAnalystModel;
            case "warm":
                return warmMentorModel;
            default:
                return drawingAnalysisModel;
        }
    }
}
