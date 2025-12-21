package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.genai.Chat;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceImplTest {

    @Mock
    private DrawingAnalysisChatServiceImpl drawingAnalysisChatService;

    @Mock
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ChatSessionRegistry registry;

    @InjectMocks
    private ChatServiceImpl chatService;

    private MultipartFile mockFile;
    private String testChatroomId;
    private String testUserMessage;
    private List<ChatMessage> mockChatHistory;

    @Mock
    private Chat mockChat;

    @Mock
    private ResponseStream<GenerateContentResponse> mockResponseStream;

    @Mock
    private GenerateContentResponse mockResponse;

    @BeforeEach
    void setUp() throws IOException {
        mockFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test image content".getBytes()
        );
        testChatroomId = "chatroom123";
        testUserMessage = "測試訊息";

        mockChatHistory = new ArrayList<>();
        ChatMessage msg1 = new ChatMessage();
        msg1.setContent("歷史訊息1");
        msg1.setIsUser(true);
        mockChatHistory.add(msg1);

        when(emotionPredictionChatService.processChat(anyString())).thenReturn("快樂");

        when(registry.getSession(anyString())).thenReturn(mockChat);
        when(registry.getSession(anyString(), anyString())).thenReturn(mockChat);

        when(mockChat.getHistory(anyBoolean())).thenReturn(ImmutableList.of(Content.fromParts(Part.fromText("history"))));

        when(chatMessageService.getTextMessagesByChatroomId(anyString())).thenReturn(new ArrayList<>());

        when(mockResponse.text()).thenReturn("token");
        Iterator<GenerateContentResponse> iterator = Arrays.asList(mockResponse).iterator();
        when(mockResponseStream.iterator()).thenReturn(iterator);
        when(mockChat.sendMessageStream(any(Content.class))).thenReturn(mockResponseStream);
        when(mockChat.sendMessageStream(anyString())).thenReturn(mockResponseStream);
    }

    @Test
    void testProcessChatWithValidInput() throws IOException {
        when(chatMessageService.getMessagesByChatroomId(testChatroomId)).thenReturn(mockChatHistory);
        when(drawingAnalysisChatService.processChat(any(), anyString(), eq(testChatroomId))).thenReturn("這是 AI 的回應");

        ChatMessage result = chatService.processChat(mockFile, testUserMessage, testChatroomId);

        assertNotNull(result);
        assertEquals("這是 AI 的回應", result.getContent());
    }

    @Test
    void testProcessChatWithNullFile() throws IOException {
        when(chatMessageService.getMessagesByChatroomId(testChatroomId)).thenReturn(new ArrayList<>());
        when(drawingAnalysisChatService.processChat(isNull(), anyString(), eq(testChatroomId))).thenReturn("無圖片的回應");

        ChatMessage result = chatService.processChat(null, testUserMessage, testChatroomId);

        assertNotNull(result);
    }

    @Test
    void testProcessChatStreamReturnsEmitter() throws IOException {
        SseEmitter emitter = chatService.processChatStream(mockFile, testUserMessage, testChatroomId, "aiMsg123");

        assertNotNull(emitter);
        verify(registry).getSession(testChatroomId);
    }

    @Test
    void testProcessChatStreamWithPersonalityCustomPersonality() throws IOException {
        String personality = "MUSE";

        SseEmitter emitter = chatService.processChatStreamWithPersonality(
                mockFile, testUserMessage, testChatroomId, personality, "aiMsg123"
        );

        assertNotNull(emitter);
        verify(registry).getSession(testChatroomId, personality);
    }

    @Test
    void testProcessChatStreamWithEmotionPredictionFailure() throws IOException {
        when(emotionPredictionChatService.processChat(testUserMessage)).thenThrow(new RuntimeException("情緒預測錯誤"));

        SseEmitter emitter = chatService.processChatStreamWithPersonality(
                mockFile, testUserMessage, testChatroomId, "default", "aiMsg123"
        );

        assertNotNull(emitter);
    }

    @Test
    void testLoadHistory_And_Failure() throws IOException {
        when(mockChat.getHistory(true)).thenReturn(ImmutableList.of());

        ChatMessage msg = new ChatMessage();
        msg.setContent("hi");
        msg.setIsUser(true);
        when(chatMessageService.getTextMessagesByChatroomId(testChatroomId)).thenReturn(List.of(msg));

        doThrow(new RuntimeException("Load History Error")).when(mockChat).sendMessage(anyList());

        chatService.processChatStreamWithPersonality(
                mockFile, testUserMessage, testChatroomId, "default", "aiMsg123"
        );

        verify(mockChat).sendMessage(anyList());
    }

    @Test
    void testStreamExecution_And_ImageFailure() throws Exception {
        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(anyString())).thenReturn(Optional.of(aiMessage));

        chatService.processChatStreamWithPersonality(
                mockFile, testUserMessage, testChatroomId, "default", "aiMsg123"
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(argThat(msg -> msg.getContent().equals("token")));
    }

    @Test
    void testStreamConsumptionError() throws Exception {
        Iterator<GenerateContentResponse> mockIterator = mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true);
        when(mockIterator.next()).thenThrow(new RuntimeException("Stream Broken"));
        when(mockResponseStream.iterator()).thenReturn(mockIterator);

        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(anyString())).thenReturn(Optional.of(aiMessage));

        chatService.processChatStreamWithPersonality(
                null, testUserMessage, testChatroomId, "default", "aiMsg123" // null file 走純文字路徑
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(argThat(msg ->
                msg.getContent().contains("AI處理錯誤") && msg.getContent().contains("Stream Broken")
        ));
    }

    @Test
    void testDbUpdateFailure() throws Exception {
        when(chatMessageService.getMessageById(anyString())).thenReturn(Optional.of(new ChatMessage()));
        when(chatMessageService.updateMessage(any())).thenThrow(new RuntimeException("DB Error"));

        chatService.processChatStreamWithPersonality(
                null, testUserMessage, testChatroomId, "default", "aiMsg123"
        );

        Thread.sleep(200);

        verify(chatMessageService).updateMessage(any());
    }

    @Test
    void testNoImageFlow() throws Exception {
        chatService.processChatStreamWithPersonality(
                null, testUserMessage, testChatroomId, "default", "aiMsg123"
        );

        Thread.sleep(500);

        verify(mockChat).sendMessageStream(anyString());
    }

    @Test
    void testNullAiMessageId() throws Exception {
        chatService.processChatStreamWithPersonality(
                null, testUserMessage, testChatroomId, "default", null
        );

        Thread.sleep(200);

        verify(chatMessageService, never()).getMessageById(any());
    }
}
