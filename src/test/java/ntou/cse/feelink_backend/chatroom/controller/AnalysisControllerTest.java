package ntou.cse.feelink_backend.chatroom.controller;

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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

    private MockMvc mockMvc;
    @Mock
    private ChatService chatService;
    @InjectMocks
    private AnalysisController analysisController;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(analysisController).build();
    }

    @Test
    void testAnalyseWithoutFile() throws Exception {
        SseEmitter emitter = new SseEmitter();

        given(chatService.processChatStreamWithPersonality(isNull(), eq("hi"), eq("S1"), eq("default")))
                .willReturn(emitter);

        mockMvc.perform(multipart("/analysis")
                        .param("userMessage", "hi")
                        .param("sessionId", "S1"))
                .andExpect(status().isOk());
    }

    @Test
    void testAnalyseWithFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3});

        SseEmitter emitter = new SseEmitter();

        given(chatService.processChatStreamWithPersonality(any(), eq("analyze"), eq("S2"), eq("default")))
                .willReturn(emitter);

        mockMvc.perform(multipart("/analysis")
                        .file(file)
                        .param("userMessage", "analyze")
                        .param("sessionId", "S2"))
                .andExpect(status().isOk());
    }

    @Test
    void testAnalyseWithCustomPersonality() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3});

        SseEmitter emitter = new SseEmitter();

        given(chatService.processChatStreamWithPersonality(any(), eq("analyze"), eq("S3"), eq("creative")))
                .willReturn(emitter);

        mockMvc.perform(multipart("/analysis")
                        .file(file)
                        .param("userMessage", "analyze")
                        .param("sessionId", "S3")
                        .param("personality", "creative"))
                .andExpect(status().isOk());
    }
}
