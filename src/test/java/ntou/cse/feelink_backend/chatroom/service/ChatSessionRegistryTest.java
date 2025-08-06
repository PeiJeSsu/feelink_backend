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

    @Mock GenerativeModel drawingAnalysisModel;
    @Mock GenerativeModel creativeModel;
    @Mock GenerativeModel curiousAnalystModel;
    @Mock GenerativeModel warmMentorModel;

    private ChatSessionRegistry registry;
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
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
}