package ntou.cse.feelink_backend.controller;
import ntou.cse.feelink_backend.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerStandaloneTest {

    @Mock
    private ChatService chatService;

    // 直接建立 Controller，並用 @InjectMocks 或建構子注入
    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Stand-alone 模式：不使用 @WebMvcTest，自行建構 MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    void testChatWithFileAndText() throws Exception {
        // 模擬 Service 回傳
        when(chatService.processChat(any(), anyString())).thenReturn("Mock AI response");

        // 建立模擬檔案
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "fake image".getBytes()
        );

        // 執行測試
        mockMvc.perform(multipart("/chat")
                        .file(file)
                        .param("userMessage", "Hello Image")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Mock AI response"));
    }

    @Test
    void testChatTextOnly() throws Exception {
        when(chatService.processChat(null, "Only Text")).thenReturn("Mock AI response");

        mockMvc.perform(multipart("/chat")
                        .param("userMessage", "Only Text")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Mock AI response"));
    }
}
