package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.PlainChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;
    @Mock
    private PlainChatService plainChatService;
    @InjectMocks
    private ChatController chatController;
    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(chatController).build();
    }

    @Test
    void chat_shouldReturnAiMessage() throws Exception {

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應");
        aiMsg.setUser(false);
        aiMsg.setSessionId("SID-123");

        given(plainChatService.reply(eq("hello"), eq("SID-123")))
                .willReturn(aiMsg);

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hello")
                        .param("sessionId", "SID-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應"))
                .andExpect(jsonPath("$.user").value(false))
                .andExpect(jsonPath("$.sessionId").value("SID-123"));

        verify(plainChatService).reply("hello", "SID-123");
    }
}
