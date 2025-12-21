package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlainChatServiceImplTest {

    @Mock
    private ChatSessionRegistry registry;

    @Mock
    private MessageFormattingService messageFormattingService;

    @Mock
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Mock
    private ChatMessageService chatMessageService;

    private Client client;
    private Object spyModels;
    private Object spyChats;

    @Mock
    private Chat chatSession;

    @Mock
    private Chat searchChat;

    @Mock
    private ResponseStream<GenerateContentResponse> responseStream;

    @Mock
    private GenerateContentResponse generateContentResponse;

    @InjectMocks
    private PlainChatServiceImpl plainChatService;

    private static final String TEST_USER_MESSAGE = "我今天很開心";
    private static final String TEST_CHATROOM_ID = "room123";
    private static final String TEST_AI_MESSAGE_ID = "ai_msg_123";
    private static final Integer TEST_CONVERSATION_COUNT = 5;
    private static final Boolean TEST_HAS_DEFAULT_QUESTION = true;

    @BeforeEach
    void setUp() throws Exception {
        client = Client.builder().apiKey("fake-key").build();

        Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        spyModels = spy(modelsField.get(client));

        Field chatsField = Client.class.getDeclaredField("chats");
        chatsField.setAccessible(true);
        spyChats = spy(chatsField.get(client));

        setFinalField(client, "models", spyModels);
        setFinalField(client, "chats", spyChats);

        ReflectionTestUtils.setField(plainChatService, "client", client);

        doReturn(generateContentResponse).when(spyModels).getClass()
                .getMethod("generateContent", String.class, String.class, com.google.genai.types.GenerateContentConfig.class)
                .invoke(spyModels, anyString(), anyString(), any());

        doReturn(searchChat).when(spyChats).getClass()
                .getMethod("create", String.class, com.google.genai.types.GenerateContentConfig.class)
                .invoke(spyChats, anyString(), any());

        when(emotionPredictionChatService.processChat(anyString())).thenReturn("快樂");

        when(messageFormattingService.getFullMessage(anyString(), anyInt(), anyBoolean())).thenReturn("Formatted Message");

        when(registry.getSession(anyString())).thenReturn(chatSession);
        when(registry.getSession(anyString(), anyString())).thenReturn(chatSession);

        when(chatSession.getHistory(true)).thenReturn(ImmutableList.of(Content.fromParts(Part.fromText("history"))));

        when(generateContentResponse.text()).thenReturn("否");

        Iterator<GenerateContentResponse> iterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(iterator);
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);
    }

    private void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSuccess_NoSearch_EmotionSuccess() throws Exception {
        SseEmitter emitter = plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        assertNotNull(emitter);
        verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
    }

    @Test
    void testEmotionPredictionFailure() throws Exception {
        when(emotionPredictionChatService.processChat(anyString())).thenThrow(new RuntimeException("API Error"));

        SseEmitter emitter = plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        assertNotNull(emitter);
        verify(messageFormattingService).getFullMessage(anyString(), anyInt(), anyBoolean());
    }

    @Test
    void testSearchDecision_Yes() throws Exception {
        when(generateContentResponse.text()).thenReturn("是");

        GenerateContentResponse searchResultResponse = mock(GenerateContentResponse.class);
        when(searchResultResponse.text()).thenReturn("搜尋關鍵字");
        when(searchChat.sendMessage(anyString())).thenReturn(searchResultResponse);

        SseEmitter emitter = plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        assertNotNull(emitter);
        verify(searchChat).sendMessage(anyString());
    }

    @Test
    void testCustomPersonality() throws Exception {
        String personality = "MUSE";
        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, personality, TEST_AI_MESSAGE_ID
        );

        verify(registry).getSession(TEST_CHATROOM_ID, personality);
    }

    @Test
    void testLoadHistory() throws Exception {
        when(chatSession.getHistory(true)).thenReturn(ImmutableList.of());

        ChatMessage msg = new ChatMessage();
        msg.setContent("old msg");
        msg.setIsUser(true);
        when(chatMessageService.getTextMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(List.of(msg));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        verify(chatSession).sendMessage(anyList());
    }

    @Test
    void testStreamFailure() throws Exception {
        when(chatSession.sendMessageStream(anyString())).thenThrow(new RuntimeException("Stream Error"));

        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(argThat(msg ->
                msg.getContent().contains("AI處理錯誤")
        ));
    }

    @Test
    void testReplyStreamDelegation() throws Exception {
        PlainChatServiceImpl spyService = spy(plainChatService);
        SseEmitter expectedEmitter = new SseEmitter();

        doReturn(expectedEmitter).when(spyService).processStreamWithAllFeatures(
                anyString(), anyString(), anyInt(), anyBoolean(), anyString(), anyString()
        );

        spyService.replyStreamWithDatabase(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID
        );

        verify(spyService).processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );
    }

    @Test
    void testLoadHistory_WithAiMessage_And_Failure() throws Exception {
        when(chatSession.getHistory(true)).thenReturn(ImmutableList.of());

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("old ai msg");
        aiMsg.setIsUser(false);
        when(chatMessageService.getTextMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(List.of(aiMsg));

        doThrow(new RuntimeException("Load History Error")).when(chatSession).sendMessage(anyList());

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        verify(chatSession).sendMessage(anyList());
    }

    @Test
    void testSseSendFailure() throws Exception {
        when(generateContentResponse.text()).thenReturn("token");
    }

    @Test
    void testUpdateDatabase_Success() throws Exception {
        when(generateContentResponse.text()).thenReturn("token");

        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(argThat(msg -> msg.getContent().equals("token")));
    }

    @Test
    void testUpdateDatabase_Failure() throws Exception {
        when(generateContentResponse.text()).thenReturn("token");
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(new ChatMessage()));

        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenThrow(new RuntimeException("DB Error"));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(any(ChatMessage.class));
    }

    @Test
    void testHandleError_UpdateDbFailure() throws Exception {
        when(chatSession.sendMessageStream(anyString())).thenThrow(new RuntimeException("Main Error"));

        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(new ChatMessage()));

        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenThrow(new RuntimeException("DB Update Error In Handler"));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(any(ChatMessage.class));
    }

    @Test
    void testErrorWithPartialResponse() throws Exception {
        Iterator<GenerateContentResponse> mockIterator = mock(Iterator.class);
        when(responseStream.iterator()).thenReturn(mockIterator);

        when(mockIterator.hasNext()).thenReturn(true, true, false);
        GenerateContentResponse resp1 = mock(GenerateContentResponse.class);
        when(resp1.text()).thenReturn("Part 1");

        when(mockIterator.next())
                .thenReturn(resp1)
                .thenThrow(new RuntimeException("Stream Broken"));

        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(argThat(msg ->
                msg.getContent().contains("Part 1") && msg.getContent().contains("Stream Broken")
        ));
    }

}
