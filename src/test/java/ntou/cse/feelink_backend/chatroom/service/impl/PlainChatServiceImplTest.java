package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlainChatServiceImplTest {

    @Mock private ChatSession chatSession;
    @Mock private GenerateContentResponse mockResponse;
    @Mock private ResponseStream<GenerateContentResponse> mockResponseStream;
    @Mock private ChatSessionRegistry registry;
    @InjectMocks private PlainChatServiceImpl service;
    private static final String SESSION_ID = "1";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(registry.getSession(anyString())).thenReturn(chatSession);
        Candidate cand = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn("Mock AI 回應");
        when(content.getPartsList()).thenReturn(java.util.Collections.singletonList(part));
        when(cand.getContent()).thenReturn(content);
        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(cand);
        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
        when(chatSession.sendMessageStream(anyString())).thenReturn(mockResponseStream);
    }

    @Test
    void reply_withAllParameters() throws IOException {
        String userMessage = "Hello";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;
        ChatMessage msg = service.reply(userMessage, SESSION_ID, conversationCount, hasDefaultQuestion);
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());
        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_withNullOptionalParameters() throws IOException {
        String userMessage = "Hello";
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, null);
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_withOnlyConversationCount() throws IOException {
        String userMessage = "Hello";
        Integer conversationCount = 5;
        ChatMessage msg = service.reply(userMessage, SESSION_ID, conversationCount, null);
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_withOnlyHasDefaultQuestion() throws IOException {
        String userMessage = "Hello";
        Boolean hasDefaultQuestion = false;
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, hasDefaultQuestion);
        assertEquals("Mock AI 回應", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());

        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_whenSendMessageThrows() throws IOException {
        String userMessage = "Error message";
        when(chatSession.sendMessage(anyString()))
                .thenThrow(new RuntimeException("AI service failed"));
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, null);
        assertEquals("AI處理錯誤", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());
        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }

    @Test
    void reply_whenChatSessionThrows() throws IOException {
        String userMessage = "Error message";
        when(chatSession.sendMessage(anyString()))
                .thenThrow(new RuntimeException("Chat session failed"));
        ChatMessage msg = service.reply(userMessage, SESSION_ID, null, null);
        assertEquals("AI處理錯誤", msg.getContent());
        assertFalse(msg.isUser());
        assertEquals(SESSION_ID, msg.getSessionId());
        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage(anyString());
    }


    @Test
    void replyStream_withAllParameters() throws Exception {
        String userMessage = "Hello";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;
        GenerateContentResponse response1 = createMockResponse("Token1");
        GenerateContentResponse response2 = createMockResponse("Token2");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response1, response2).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, conversationCount, hasDefaultQuestion);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_withNullOptionalParameters() throws Exception {
        String userMessage = "Hello";
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_withOnlyConversationCount() throws Exception {
        String userMessage = "Hello";
        Integer conversationCount = 5;
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, conversationCount, null);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_withOnlyHasDefaultQuestion() throws Exception {
        String userMessage = "Hello";
        Boolean hasDefaultQuestion = false;
        GenerateContentResponse response = createMockResponse("Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, hasDefaultQuestion);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    @Test
    void replyStream_whenSendMessageStreamThrows() throws Exception {
        String userMessage = "Error message";
        when(chatSession.sendMessageStream(anyString()))
                .thenThrow(new RuntimeException("Stream error"));
        SseEmitter emitter = service.replyStream(userMessage, SESSION_ID, null, null);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream(anyString());
    }

    private GenerateContentResponse createMockResponse(String text) {
        GenerateContentResponse response = mock(GenerateContentResponse.class);
        Candidate candidate = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn(text);
        when(content.getPartsList()).thenReturn(Arrays.asList(part));
        when(candidate.getContent()).thenReturn(content);
        when(response.getCandidatesCount()).thenReturn(1);
        when(response.getCandidates(0)).thenReturn(candidate);
        return response;
    }
}