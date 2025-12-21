package ntou.cse.feelink_backend.chatroom.service;

import com.google.genai.Chat;
import com.google.genai.Chats;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSessionRegistryTest {

    @Mock
    private Client client;

    @Mock
    private Chats chats;

    @Mock
    private GenerateContentConfig drawingAnalysisConfig;
    @Mock
    private GenerateContentConfig creativeConfig;
    @Mock
    private GenerateContentConfig curiousAnalystConfig;
    @Mock
    private GenerateContentConfig warmMentorConfig;

    @Mock
    private Chat mockChatSession;

    private ChatSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ChatSessionRegistry(
                client,
                drawingAnalysisConfig,
                creativeConfig,
                curiousAnalystConfig,
                warmMentorConfig
        );
        ReflectionTestUtils.setField(client, "chats", chats);
    }


    @Test
    void testGetSession_DefaultPersonality_CreatesNewSession() {
        when(chats.create(anyString(), any(GenerateContentConfig.class))).thenReturn(mockChatSession);
        Chat session = registry.getSession("room1");
        assertNotNull(session);
        assertEquals(mockChatSession, session);
        verify(chats).create(eq("gemini-2.5-flash-lite"), eq(drawingAnalysisConfig));
    }

    @Test
    void testGetSession_CreativePersonality() {
        when(chats.create(anyString(), any(GenerateContentConfig.class))).thenReturn(mockChatSession);

        Chat session = registry.getSession("room2", "creative");

        assertNotNull(session);
        verify(chats).create(eq("gemini-2.5-flash-lite"), eq(creativeConfig));
    }

    @Test
    void testGetSession_CuriousPersonality() {
        when(chats.create(anyString(), any(GenerateContentConfig.class))).thenReturn(mockChatSession);

        registry.getSession("room3", "curious");

        verify(chats).create(eq("gemini-2.5-flash-lite"), eq(curiousAnalystConfig));
    }

    @Test
    void testGetSession_WarmPersonality() {
        when(chats.create(anyString(), any(GenerateContentConfig.class))).thenReturn(mockChatSession);

        registry.getSession("room4", "warm");

        verify(chats).create(eq("gemini-2.5-flash-lite"), eq(warmMentorConfig));
    }

    @Test
    void testGetSession_CaseInsensitivePersonality() {
        when(chats.create(anyString(), any(GenerateContentConfig.class))).thenReturn(mockChatSession);

        registry.getSession("room5", "CREATIVE");

        verify(chats).create(eq("gemini-2.5-flash-lite"), eq(creativeConfig));
    }


    @Test
    void testGetSession_ReturnsCachedSession() {
        when(chats.create(anyString(), any(GenerateContentConfig.class))).thenReturn(mockChatSession);
        Chat session1 = registry.getSession("room_cache");
        Chat session2 = registry.getSession("room_cache");
        assertSame(session1, session2);
        verify(chats, times(1)).create(anyString(), any(GenerateContentConfig.class));
    }

    @Test
    void testGetSession_DifferentRoomsHaveDifferentSessions() {
        Chat chat1 = mock(Chat.class);
        Chat chat2 = mock(Chat.class);

        when(chats.create(eq("gemini-2.5-flash-lite"), any()))
                .thenReturn(chat1)
                .thenReturn(chat2);

        Chat session1 = registry.getSession("roomA");
        Chat session2 = registry.getSession("roomB");

        assertNotSame(session1, session2);
        verify(chats, times(2)).create(anyString(), any());
    }


    @Test
    void testRecreateSession_Success() {
        // Arrange
        Chat oldChat = mock(Chat.class);
        Chat newChat = mock(Chat.class);
        when(chats.create(anyString(), any())).thenReturn(oldChat);
        registry.getSession("room_recreate", "warm");
        when(chats.create(anyString(), any())).thenReturn(newChat);
        registry.recreateSession("room_recreate");
        verify(chats, times(2)).create(eq("gemini-2.5-flash-lite"), eq(warmMentorConfig));
    }

    @Test
    void testRecreateSession_NotFound_DoesNothing() {
        registry.recreateSession("non_existent_room");
        verify(chats, never()).create(anyString(), any());
    }

    @Test
    void testRecreateSession_MultipleKeys_PicksCorrectOne() {
        when(chats.create(anyString(), any())).thenReturn(mockChatSession);
        registry.getSession("room_multi", "creative");

        clearInvocations(chats);
        when(chats.create(anyString(), any())).thenReturn(mockChatSession);

        registry.recreateSession("room_multi");

        verify(chats).create(eq("gemini-2.5-flash-lite"), eq(creativeConfig));
    }


    @Test
    void testGetSession_RaceCondition_PutIfAbsent() {
        when(chats.create(anyString(), any())).thenReturn(mockChatSession);

        registry.getSession("room_race");
        registry.getSession("room_race");

        verify(chats, times(1)).create(anyString(), any());
    }
}
