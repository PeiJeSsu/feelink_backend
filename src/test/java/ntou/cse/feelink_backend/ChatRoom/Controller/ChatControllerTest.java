package ntou.cse.feelink_backend.ChatRoom.Controller;

import ntou.cse.feelink_backend.ChatRoom.Service.ChatService;
import org.junit.jupiter.api.Disabled;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @InjectMocks
    private ChatController chatController;
    @Mock
    private ChatService chatService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    void testChatWithFileAndText() throws Exception {
        when(chatService.processChat(any(), anyString())).thenReturn("Mock AI response");

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "fake image".getBytes());

        mockMvc.perform(multipart("/chat")
                        .file(file)
                        .param("userMessage", "Hello Image")
                        .param("sessionId", "testSession")
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Mock AI response"))
                .andExpect(jsonPath("$.sessionId").value("testSession"))
                .andExpect(jsonPath("$.user").value(false));
    }

    @Test
    void testChatWithText() throws Exception {
        when(chatService.processChat(null, "Only Text")).thenReturn("Mock AI response");

        mockMvc.perform(multipart("/chat")
                        .param("userMessage", "Only Text")
                        .param("sessionId", "testSession")
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Mock AI response"))
                .andExpect(jsonPath("$.sessionId").value("testSession"))
                .andExpect(jsonPath("$.user").value(false));
    }
}
