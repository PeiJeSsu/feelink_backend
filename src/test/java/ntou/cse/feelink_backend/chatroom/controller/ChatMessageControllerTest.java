package ntou.cse.feelink_backend.chatroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ChatMessage userTextMessage;
    private ChatMessage aiTextMessage;
    private ChatMessage userDrawingMessage;
    private ChatMessage aiDrawingMessage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatMessageController).build();
        objectMapper = new ObjectMapper();

        // 初始化測試數據
        userTextMessage = createChatMessage("msg1", "chatroom1", "Hello", true, false);
        aiTextMessage = createChatMessage("msg2", "chatroom1", "Hi there!", false, false);
        userDrawingMessage = createChatMessage("msg3", "chatroom1", "drawing-data", true, true);
        aiDrawingMessage = createChatMessage("msg4", "chatroom1", "ai-drawing", false, true);
    }

    private ChatMessage createChatMessage(String messageId, String chatroomId, String content,
                                          boolean isUser, boolean isDrawingData) {
        ChatMessage message = new ChatMessage();
        message.setMessageId(messageId);
        message.setChatroomId(chatroomId);
        message.setContent(content);
        message.setIsUser(isUser);
        message.setIsDrawingData(isDrawingData);
        message.setSentAt(new Date());
        return message;
    }

    // ===== GET 所有訊息 =====
    @Test
    void testGetAllMessages_Success() throws Exception {
        List<ChatMessage> messages = Arrays.asList(userTextMessage, aiTextMessage);
        when(chatMessageService.getAllMessages()).thenReturn(messages);

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].messageId").value("msg1"))
                .andExpect(jsonPath("$[1].messageId").value("msg2"));
    }

    @Test
    void testGetAllMessages_EmptyList() throws Exception {
        when(chatMessageService.getAllMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ===== GET 單一訊息 =====
    @Test
    void testGetMessageById_Found() throws Exception {
        when(chatMessageService.getMessageById("msg1")).thenReturn(Optional.of(userTextMessage));

        mockMvc.perform(get("/api/messages/msg1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("msg1"))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void testGetMessageById_NotFound() throws Exception {
        when(chatMessageService.getMessageById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/messages/nonexistent"))
                .andExpect(status().isNotFound());
    }

    // ===== GET 聊天室所有訊息 =====
    @Test
    void testGetMessagesByChatroomId_Success() throws Exception {
        List<ChatMessage> messages = Arrays.asList(userTextMessage, aiTextMessage, userDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(messages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ===== GET 使用者訊息 =====
    @Test
    void testGetUserMessages_Success() throws Exception {
        List<ChatMessage> allMessages = Arrays.asList(userTextMessage, aiTextMessage, userDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(allMessages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isUser").value(true))
                .andExpect(jsonPath("$[1].isUser").value(true));
    }

    // ===== GET AI訊息 =====
    @Test
    void testGetAIMessages_Success() throws Exception {
        List<ChatMessage> allMessages = Arrays.asList(userTextMessage, aiTextMessage, aiDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(allMessages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isUser").value(false))
                .andExpect(jsonPath("$[1].isUser").value(false));
    }

    // ===== GET 文字訊息 =====
    @Test
    void testGetTextMessages_Success() throws Exception {
        List<ChatMessage> allMessages = Arrays.asList(userTextMessage, aiTextMessage, userDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(allMessages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isDrawingData").value(false))
                .andExpect(jsonPath("$[1].isDrawingData").value(false));
    }

    // ===== GET 畫布訊息 =====
    @Test
    void testGetDrawingMessages_Success() throws Exception {
        List<ChatMessage> allMessages = Arrays.asList(userTextMessage, userDrawingMessage, aiDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(allMessages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/drawing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isDrawingData").value(true))
                .andExpect(jsonPath("$[1].isDrawingData").value(true));
    }

    // ===== GET 使用者畫布訊息 =====
    @Test
    void testGetUserDrawingMessages_Success() throws Exception {
        List<ChatMessage> allMessages = Arrays.asList(userTextMessage, userDrawingMessage, aiDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(allMessages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/user/drawing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isUser").value(true))
                .andExpect(jsonPath("$[0].isDrawingData").value(true));
    }

    // ===== GET 使用者文字訊息 =====
    @Test
    void testGetUserTextMessages_Success() throws Exception {
        List<ChatMessage> allMessages = Arrays.asList(userTextMessage, userDrawingMessage, aiTextMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(allMessages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/user/text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isUser").value(true))
                .andExpect(jsonPath("$[0].isDrawingData").value(false));
    }

    // ===== POST 創建訊息 =====
    @Test
    void testCreateMessage_Success() throws Exception {
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(userTextMessage);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTextMessage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("msg1"))
                .andExpect(jsonPath("$.content").value("Hello"));

        verify(chatMessageService).createMessage(any(ChatMessage.class));
    }

    // ===== PUT 更新訊息 =====
    @Test
    void testUpdateMessage_Success() throws Exception {
        ChatMessage updatedMessage = createChatMessage("msg1", "chatroom1", "Updated content", true, false);
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(updatedMessage);

        mockMvc.perform(put("/api/messages/msg1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));

        verify(chatMessageService).updateMessage(any(ChatMessage.class));
    }

    // ===== DELETE 單一訊息 =====
    @Test
    void testDeleteMessage_Success() throws Exception {
        doNothing().when(chatMessageService).deleteMessage("msg1");

        mockMvc.perform(delete("/api/messages/msg1"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteMessage("msg1");
    }

    // ===== DELETE 聊天室所有訊息 =====
    @Test
    void testDeleteAllMessagesInChatroom_Success() throws Exception {
        when(chatMessageService.deleteAllMessagesByChatroomId("chatroom1")).thenReturn(5);

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteAllMessagesByChatroomId("chatroom1");
    }

    @Test
    void testDeleteAllMessagesInChatroom_Exception() throws Exception {
        when(chatMessageService.deleteAllMessagesByChatroomId("chatroom1"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1"))
                .andExpect(status().isInternalServerError());
    }

    // ===== DELETE 使用者訊息 =====
    @Test
    void testDeleteUserMessagesInChatroom_Success() throws Exception {
        when(chatMessageService.deleteUserMessagesByChatroomId("chatroom1")).thenReturn(3);

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/user"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteUserMessagesByChatroomId("chatroom1");
    }

    @Test
    void testDeleteUserMessagesInChatroom_Exception() throws Exception {
        when(chatMessageService.deleteUserMessagesByChatroomId("chatroom1"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/user"))
                .andExpect(status().isInternalServerError());
    }

    // ===== DELETE AI訊息 =====
    @Test
    void testDeleteAIMessagesInChatroom_Success() throws Exception {
        when(chatMessageService.deleteAIMessagesByChatroomId("chatroom1")).thenReturn(2);

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/ai"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteAIMessagesByChatroomId("chatroom1");
    }

    @Test
    void testDeleteAIMessagesInChatroom_Exception() throws Exception {
        when(chatMessageService.deleteAIMessagesByChatroomId("chatroom1"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/ai"))
                .andExpect(status().isInternalServerError());
    }

    // ===== DELETE 文字訊息 =====
    @Test
    void testDeleteTextMessagesInChatroom_Success() throws Exception {
        when(chatMessageService.deleteTextMessagesByChatroomId("chatroom1")).thenReturn(4);

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/text"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteTextMessagesByChatroomId("chatroom1");
    }

    @Test
    void testDeleteTextMessagesInChatroom_Exception() throws Exception {
        when(chatMessageService.deleteTextMessagesByChatroomId("chatroom1"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/text"))
                .andExpect(status().isInternalServerError());
    }

    // ===== DELETE 畫布訊息 =====
    @Test
    void testDeleteDrawingMessagesInChatroom_Success() throws Exception {
        when(chatMessageService.deleteDrawingMessagesByChatroomId("chatroom1")).thenReturn(1);

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/drawing"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteDrawingMessagesByChatroomId("chatroom1");
    }

    @Test
    void testDeleteDrawingMessagesInChatroom_Exception() throws Exception {
        when(chatMessageService.deleteDrawingMessagesByChatroomId("chatroom1"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/messages/chatroom/chatroom1/drawing"))
                .andExpect(status().isInternalServerError());
    }

    // ===== 邊界情況測試 =====
    @Test
    void testGetMessagesWithEmptyResult() throws Exception {
        when(chatMessageService.getMessagesByChatroomId("empty-chatroom"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/messages/chatroom/empty-chatroom/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testDeleteWithZeroAffectedRows() throws Exception {
        when(chatMessageService.deleteAllMessagesByChatroomId("empty-chatroom")).thenReturn(0);

        mockMvc.perform(delete("/api/messages/chatroom/empty-chatroom"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteAllMessagesByChatroomId("empty-chatroom");
    }
}