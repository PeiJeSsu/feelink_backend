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
    private final ConcurrentHashMap<String, ChatSession> pool = new ConcurrentHashMap<>();

    @Autowired
    public ChatSessionRegistry(@Qualifier("drawingAnalysisModel") GenerativeModel drawingAnalysisModel) {
        this.drawingAnalysisModel = drawingAnalysisModel;
    }

    public ChatSession getSession(String sessionId) {
        ChatSession session = pool.get(sessionId);
        if (session == null) {
            ChatSession newSession = new ChatSession(drawingAnalysisModel);
            ChatSession existed     = pool.putIfAbsent(sessionId, newSession);
            session = existed != null ? existed : newSession;
        }
        return session;
    }
}
