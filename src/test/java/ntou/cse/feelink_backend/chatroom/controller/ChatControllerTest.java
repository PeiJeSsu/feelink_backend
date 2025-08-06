package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;
    @Mock
    private PlainChatServiceImpl plainChatService;
    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(chatController).build();
    }

    @Test
    void chatStream_shouldReturnEventStream_withAllParameters() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        doAnswer(invocation -> {
            emitter.send("data: AI 回應1\n\n");
            emitter.complete();
            return emitter;
        }).when(plainChatService).replyStream("hello", "SID-123", 1, true, "default");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("conversationCount", "1")
                        .param("hasDefaultQuestion", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(plainChatService).replyStream("hello", "SID-123", 1, true, "default");
    }

    @Test
    void chatStream_shouldReturnEventStream_withOptionalParametersAsNull() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        doAnswer(invocation -> {
            emitter.send("data: AI 回應2\n\n");
            emitter.complete();
            return emitter;
        }).when(plainChatService).replyStream("hello", "SID-123", null, null, "default");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(plainChatService).replyStream("hello", "SID-123", null, null, "default");
    }

    @Test
    void chatStream_shouldReturnEventStream_withOnlyConversationCount() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        doAnswer(invocation -> {
            emitter.send("data: AI 回應3\n\n");
            emitter.complete();
            return emitter;
        }).when(plainChatService).replyStream("hello", "SID-123", 5, null, "default");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("conversationCount", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(plainChatService).replyStream("hello", "SID-123", 5, null, "default");
    }

    @Test
    void chatStream_shouldReturnEventStream_withOnlyHasDefaultQuestion() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        doAnswer(invocation -> {
            emitter.send("data: AI 回應4\n\n");
            emitter.complete();
            return emitter;
        }).when(plainChatService).replyStream("hello", "SID-123", null, false, "default");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("hasDefaultQuestion", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(plainChatService).replyStream("hello", "SID-123", null, false, "default");
    }

    @Test
    void chatStream_shouldReturnEventStream_withCustomPersonality() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        doAnswer(invocation -> {
            emitter.send("data: AI 回應5\n\n");
            emitter.complete();
            return emitter;
        }).when(plainChatService).replyStream("hello", "SID-123", null, null, "creative");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("personality", "creative"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(plainChatService).replyStream("hello", "SID-123", null, null, "creative");
    }
}
