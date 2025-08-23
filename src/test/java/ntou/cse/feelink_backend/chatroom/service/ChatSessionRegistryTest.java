package ntou.cse.feelink_backend.chatroom.service;

import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatSessionRegistryTest {



    private ChatSessionRegistry registry;
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(drawingAnalysisModel.startChat()).thenReturn(mock(ChatSession.class));
        when(creativeModel.startChat()).thenReturn(mock(ChatSession.class));
        when(curiousAnalystModel.startChat()).thenReturn(mock(ChatSession.class));
        when(warmMentorModel.startChat()).thenReturn(mock(ChatSession.class));
        registry = new ChatSessionRegistry(
                drawingAnalysisModel,
                creativeModel,
                curiousAnalystModel,
                warmMentorModel
        );
    }
    @Test
    void firstCall_createsNewSession() {
        String id = "SID-A";
        ChatSession first = registry.getSession(id);
        assertNotNull(first);
        ChatSession again = registry.getSession(id);
        assertSame(first, again);
    }

    @Test
    void mapAlreadyContains_returnsExisting() {
        String id = "SID-B";
        ChatSession preset = mock(ChatSession.class);

        ConcurrentHashMap<String, ChatSession> fake =
                new ConcurrentHashMap<>() {
                    @Override
                    public ChatSession get(Object key) { return null; }
                    @Override
                    public ChatSession putIfAbsent(String k, ChatSession v) {
                        return preset;
                    }
                };
        try {
            var f = ChatSessionRegistry.class.getDeclaredField("pool");
            f.setAccessible(true);
            f.set(registry, fake);
        } catch (Exception e) { fail(e); }
        ChatSession result = registry.getSession(id);
        assertSame(preset, result);
    }
    @Test
    void getSession_withCreativePersonality() {
        String sessionId = "SID-C";
        String personality = "creative";
        when(creativeModel.startChat()).thenReturn(mock(ChatSession.class));
        ChatSession session = registry.getSession(sessionId, personality);
        assertNotNull(session);
    }

    @Test
    void getSession_withCuriousPersonality() {
        String sessionId = "SID-D";
        String personality = "curious";
        when(curiousAnalystModel.startChat()).thenReturn(mock(ChatSession.class));
        ChatSession session = registry.getSession(sessionId, personality);
        assertNotNull(session);

    }

    @Test
    void getSession_withWarmPersonality() {
        String sessionId = "SID-E";
        String personality = "warm";
        when(warmMentorModel.startChat()).thenReturn(mock(ChatSession.class));
        ChatSession session = registry.getSession(sessionId, personality);
        assertNotNull(session);
    }
}