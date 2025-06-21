package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;
    @Mock
    private ChatService chatService;
    @InjectMocks
    private ChatController chatController;
    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(chatController).build();
    }

    @Test
    void testChatWithoutFile() throws Exception {
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應（無檔案）");
        aiMsg.setUser(false);
        aiMsg.setSessionId("S1");

        given(chatService.processChat(isNull(), eq("hi"), eq("S1")))
                .willReturn(aiMsg);

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userMessage", "hi")
                        .param("sessionId", "S1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應（無檔案）"))
                .andExpect(jsonPath("$.user").value(false))
                .andExpect(jsonPath("$.sessionId").value("S1"));
    }

    @Test
    void testChatWithFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{/* ... */});

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("AI 回應（有檔案）");
        aiMsg.setUser(false);
        aiMsg.setSessionId("S2");

        given(chatService.processChat(any(), eq("analyze"), eq("S2")))
                .willReturn(aiMsg);

        mockMvc.perform(multipart("/chat")
                        .file(file)
                        .param("userMessage", "analyze")
                        .param("sessionId", "S2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("AI 回應（有檔案）"))
                .andExpect(jsonPath("$.sessionId").value("S2"));
    }
}
