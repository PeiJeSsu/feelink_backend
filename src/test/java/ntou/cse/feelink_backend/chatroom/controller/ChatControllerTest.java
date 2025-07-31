package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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
    void chat_shouldReturnAiMessage_withAllParameters() throws Exception {
        // Given
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應");
        aiMsg.setUser(false);
        aiMsg.setSessionId("SID-123");

        given(plainChatService.reply("hello", "SID-123", 1, true))
                .willReturn(aiMsg);

        // When & Then
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("conversationCount", "1")
                        .param("hasDefaultQuestion", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應"))
                .andExpect(jsonPath("$.user").value(false))
                .andExpect(jsonPath("$.sessionId").value("SID-123"));

        verify(plainChatService).reply("hello", "SID-123", 1, true);
    }

    @Test
    void chat_shouldReturnAiMessage_withOptionalParametersAsNull() throws Exception {
        // Given
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應");
        aiMsg.setUser(false);
        aiMsg.setSessionId("SID-123");

        given(plainChatService.reply("hello", "SID-123", null, null))
                .willReturn(aiMsg);

        // When & Then
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應"))
                .andExpect(jsonPath("$.user").value(false))
                .andExpect(jsonPath("$.sessionId").value("SID-123"));

        verify(plainChatService).reply("hello", "SID-123", null, null);
    }

    @Test
    void chat_shouldReturnAiMessage_withOnlyConversationCount() throws Exception {
        // Given
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應");
        aiMsg.setUser(false);
        aiMsg.setSessionId("SID-123");

        given(plainChatService.reply("hello", "SID-123", 5, null))
                .willReturn(aiMsg);

        // When & Then
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("conversationCount", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應"))
                .andExpect(jsonPath("$.user").value(false))
                .andExpect(jsonPath("$.sessionId").value("SID-123"));

        verify(plainChatService).reply("hello", "SID-123", 5, null);
    }

    @Test
    void chat_shouldReturnAiMessage_withOnlyHasDefaultQuestion() throws Exception {
        // Given
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應");
        aiMsg.setUser(false);
        aiMsg.setSessionId("SID-123");

        given(plainChatService.reply("hello", "SID-123", null, false))
                .willReturn(aiMsg);

        // When & Then
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123")
                        .param("hasDefaultQuestion", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應"))
                .andExpect(jsonPath("$.user").value(false))
                .andExpect(jsonPath("$.sessionId").value("SID-123"));

        verify(plainChatService).reply("hello", "SID-123", null, false);
    }
}