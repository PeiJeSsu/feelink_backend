package ntou.cse.feelink_backend.chatroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.Canvas;
import ntou.cse.feelink_backend.chatroom.service.CanvasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CanvasControllerTest {

    @Mock
    private CanvasService canvasService;

    @InjectMocks
    private CanvasController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new org.springframework.web.filter.CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8),
                        new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter()
                )
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSaveCanvas_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("chatroomId", "room1");
        request.put("canvasData", "data123");
        request.put("canvasImageUrl", "url123");

        mockMvc.perform(post("/api/canvas/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("畫布儲存成功"));

        verify(canvasService).saveCanvas("room1", "data123", "url123");
    }

    @Test
    void testSaveCanvas_MissingChatroomId() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("canvasData", "data123");

        mockMvc.perform(post("/api/canvas/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("聊天室 ID 不能為空"));
    }

    @Test
    void testSaveCanvas_MissingCanvasData() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("chatroomId", "room1");
        mockMvc.perform(post("/api/canvas/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("畫布資料不能為空"));
    }

    @Test
    void testSaveCanvas_Exception() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("chatroomId", "room1");
        request.put("canvasData", "data");

        doThrow(new RuntimeException("DB Error")).when(canvasService).saveCanvas(anyString(), anyString(), any());

        mockMvc.perform(post("/api/canvas/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("畫布儲存失敗")));
    }

    @Test
    void testGetChatroomCanvas_Found() throws Exception {
        Canvas mockCanvas = new Canvas("room1", "data_content", "url");
        when(canvasService.getCanvasByChatroomId("room1")).thenReturn(Optional.of(mockCanvas));

        mockMvc.perform(get("/api/canvas/chatroom/room1"))
                .andExpect(status().isOk())
                .andExpect(content().string("data_content"));
    }

    @Test
    void testGetChatroomCanvas_NotFound() throws Exception {
        when(canvasService.getCanvasByChatroomId("room1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/canvas/chatroom/room1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testGetChatroomCanvas_EmptyId() throws Exception {
        mockMvc.perform(get("/api/canvas/chatroom/ "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetChatroomCanvas_Exception() throws Exception {
        when(canvasService.getCanvasByChatroomId("room1")).thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/canvas/chatroom/room1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("取得畫布失敗")));
    }

    @Test
    void testGetChatroomCanvasImage_Found() throws Exception {
        Canvas mockCanvas = new Canvas("room1", "data", "url_content");
        when(canvasService.getCanvasByChatroomId("room1")).thenReturn(Optional.of(mockCanvas));

        mockMvc.perform(get("/api/canvas/chatroom/room1/image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatroomId").value("room1"))
                .andExpect(jsonPath("$.canvasImageUrl").value("url_content"));
    }

    @Test
    void testGetChatroomCanvasImage_NotFound() throws Exception {
        when(canvasService.getCanvasByChatroomId("room1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/canvas/chatroom/room1/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetChatroomCanvasImage_Exception() throws Exception {
        when(canvasService.getCanvasByChatroomId("room1")).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/canvas/chatroom/room1/image"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteCanvas_Success() throws Exception {
        doNothing().when(canvasService).deleteCanvasByChatroomId("room1");

        mockMvc.perform(delete("/api/canvas/room1"))
                .andExpect(status().isOk())
                .andExpect(content().string("畫布刪除成功"));

        verify(canvasService).deleteCanvasByChatroomId("room1");
    }

    @Test
    void testDeleteCanvas_Exception() throws Exception {
        doThrow(new RuntimeException("Delete Error")).when(canvasService).deleteCanvasByChatroomId("room1");

        mockMvc.perform(delete("/api/canvas/room1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("刪除畫布失敗")));
    }
}
