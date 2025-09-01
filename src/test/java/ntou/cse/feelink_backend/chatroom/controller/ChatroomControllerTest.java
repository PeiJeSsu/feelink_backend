package ntou.cse.feelink_backend.chatroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.dto.ChatroomDTO;
import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.service.ChatroomService;
import ntou.cse.feelink_backend.chatroom.util.MapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatroomControllerTest {

    @Mock
    private ChatroomService chatroomService;

    @InjectMocks
    private ChatroomController chatroomController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Chatroom testChatroom;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatroomController).build();
        objectMapper = new ObjectMapper();

        testChatroom = new Chatroom();
        testChatroom.setChatroomId("chatroom123");
        testChatroom.setUserId("user123");
        testChatroom.setTitle("Test Chatroom");
    }

    @Test
    void getAllChatrooms_ShouldReturnAllChatrooms() throws Exception {
        List<Chatroom> chatrooms = Arrays.asList(testChatroom);
        when(chatroomService.getAllChatrooms()).thenReturn(chatrooms);

        mockMvc.perform(get("/api/chatrooms"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].chatroomId").value("chatroom123"))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].title").value("Test Chatroom"));

        verify(chatroomService, times(1)).getAllChatrooms();
    }

    @Test
    void getChatroomById_WhenChatroomExists_ShouldReturnChatroomDTO() throws Exception {
        when(chatroomService.getChatroomById("chatroom123")).thenReturn(Optional.of(testChatroom));

        mockMvc.perform(get("/api/chatrooms/chatroom123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.chatroomId").value("chatroom123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.title").value("Test Chatroom"));

        verify(chatroomService, times(1)).getChatroomById("chatroom123");
    }

    @Test
    void getChatroomById_WhenChatroomNotExists_ShouldReturnNotFound() throws Exception {
        when(chatroomService.getChatroomById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/chatrooms/nonexistent"))
                .andExpect(status().isNotFound());

        verify(chatroomService, times(1)).getChatroomById("nonexistent");
    }

    @Test
    void getUserChatrooms_WhenSuccessful_ShouldReturnChatroomDTOList() throws Exception {
        List<Chatroom> userChatrooms = Arrays.asList(testChatroom);
        when(chatroomService.getChatroomsByUserId("user123")).thenReturn(userChatrooms);

        mockMvc.perform(get("/api/chatrooms/user/user123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].chatroomId").value("chatroom123"))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].title").value("Test Chatroom"));

        verify(chatroomService, times(1)).getChatroomsByUserId("user123");
    }

    @Test
    void getUserChatrooms_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(chatroomService.getChatroomsByUserId("user123"))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/chatrooms/user/user123"))
                .andExpect(status().isInternalServerError());

        verify(chatroomService, times(1)).getChatroomsByUserId("user123");
    }

    @Test
    void createChatroom_WhenSuccessful_ShouldReturnCreatedChatroomDTO() throws Exception {
        ChatroomDTO inputDTO = MapperUtil.toDTO(testChatroom);
        when(chatroomService.createChatroom(any(Chatroom.class))).thenReturn(testChatroom);

        mockMvc.perform(post("/api/chatrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.chatroomId").value("chatroom123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.title").value("Test Chatroom"));

        verify(chatroomService, times(1)).createChatroom(any(Chatroom.class));
    }

    @Test
    void createChatroom_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        ChatroomDTO inputDTO = MapperUtil.toDTO(testChatroom);
        when(chatroomService.createChatroom(any(Chatroom.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/chatrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isInternalServerError());

        verify(chatroomService, times(1)).createChatroom(any(Chatroom.class));
    }

    @Test
    void updateChatroom_WhenSuccessful_ShouldReturnUpdatedChatroomDTO() throws Exception {
        Chatroom updatedChatroom = new Chatroom();
        updatedChatroom.setChatroomId("chatroom123");
        updatedChatroom.setUserId("user123");
        updatedChatroom.setTitle("Updated Chatroom");

        ChatroomDTO updatedDTO = MapperUtil.toDTO(updatedChatroom);
        when(chatroomService.updateChatroom(any(Chatroom.class))).thenReturn(updatedChatroom);

        mockMvc.perform(put("/api/chatrooms/chatroom123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatroomId").value("chatroom123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.title").value("Updated Chatroom"));

        verify(chatroomService, times(1)).updateChatroom(any(Chatroom.class));
    }

    @Test
    void updateChatroom_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        ChatroomDTO updateDTO = MapperUtil.toDTO(testChatroom);
        when(chatroomService.updateChatroom(any(Chatroom.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/api/chatrooms/chatroom123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isInternalServerError());

        verify(chatroomService, times(1)).updateChatroom(any(Chatroom.class));
    }

    @Test
    void deleteChatroom_WhenSuccessful_ShouldReturnNoContent() throws Exception {
        doNothing().when(chatroomService).deleteChatroom("chatroom123");

        mockMvc.perform(delete("/api/chatrooms/chatroom123"))
                .andExpect(status().isNoContent());

        verify(chatroomService, times(1)).deleteChatroom("chatroom123");
    }

    @Test
    void deleteChatroom_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Service error")).when(chatroomService).deleteChatroom("chatroom123");

        mockMvc.perform(delete("/api/chatrooms/chatroom123"))
                .andExpect(status().isInternalServerError());

        verify(chatroomService, times(1)).deleteChatroom("chatroom123");
    }
}
