package ntou.cse.feelink_backend.chatroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.dto.ChatMessageDTO;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.EmotionAnalysisService;
import ntou.cse.feelink_backend.chatroom.service.GoogleNaturalLanguageService;
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
    @Mock
    private EmotionAnalysisService emotionAnalysisService;

    @Mock
    private GoogleNaturalLanguageService googleNaturalLanguageService;
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

    @Test
    void testGetMessagesByChatroomId_Success() throws Exception {
        List<ChatMessage> messages = Arrays.asList(userTextMessage, aiTextMessage, userDrawingMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(messages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

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

    @Test
    void testCreateMessage_Success() throws Exception {
        when(chatMessageService.createMessage(any(ChatMessage.class))).thenReturn(userTextMessage);

        ChatMessageDTO userTextDTO = MapperUtil.toDTO(userTextMessage);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTextDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("msg1"))
                .andExpect(jsonPath("$.content").value("Hello"));

        verify(chatMessageService).createMessage(any(ChatMessage.class));
    }

    @Test
    void testUpdateMessage_Success() throws Exception {
        ChatMessage updatedMessage = createChatMessage("msg1", "chatroom1", "Updated content", true, false);
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenReturn(updatedMessage);

        ChatMessageDTO updatedDTO = MapperUtil.toDTO(updatedMessage);

        mockMvc.perform(put("/api/messages/msg1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));

        verify(chatMessageService).updateMessage(any(ChatMessage.class));
    }

    @Test
    void testDeleteMessage_Success() throws Exception {
        doNothing().when(chatMessageService).deleteMessage("msg1");

        mockMvc.perform(delete("/api/messages/msg1"))
                .andExpect(status().isNoContent());

        verify(chatMessageService).deleteMessage("msg1");
    }

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
    @Test
    void testGetUserTextMessageCount_Success() throws Exception {
        List<ChatMessage> messages = Arrays.asList(userTextMessage, userDrawingMessage, aiTextMessage);
        when(chatMessageService.getMessagesByChatroomId("chatroom1")).thenReturn(messages);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/user/text/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testGetUserTextMessageCountToday_Success() throws Exception {
        when(chatMessageService.countUserTextMessagesToday("chatroom1")).thenReturn(5);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/user/today/text/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void testGetUserTextMessageCountToday_Exception() throws Exception {
        when(chatMessageService.countUserTextMessagesToday("chatroom1"))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/user/today/text/count"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void testGetTodayEmotionAnalysis_Success() throws Exception {
        EmotionAnalysisService.EmotionScores scores = new EmotionAnalysisService.EmotionScores(0.5, 0.5, 0.1, 0.1, 0.0, 0.0,0.0,0.0);
        when(emotionAnalysisService.analyzeTodayEmotions("chatroom1")).thenReturn(scores);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/emotion/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joy").value(0.5));
    }

    @Test
    void testGetTodayEmotionAnalysis_Exception() throws Exception {
        when(emotionAnalysisService.analyzeTodayEmotions("chatroom1"))
                .thenThrow(new RuntimeException("Analysis error"));

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/emotion/today"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetTodayChatSummary_Success() throws Exception {
        when(emotionAnalysisService.generateTodayChatSummary("chatroom1")).thenReturn("Summary text");
        when(emotionAnalysisService.getTaiwanTodayString()).thenReturn("2023-10-27");

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/summary/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Summary text"))
                .andExpect(jsonPath("$.date").value("2023-10-27"));
    }

    @Test
    void testGetTodayChatSummary_Exception() throws Exception {
        when(emotionAnalysisService.generateTodayChatSummary("chatroom1"))
                .thenThrow(new RuntimeException("Summary error"));

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/summary/today"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetTodaySimpleDemand_Success() throws Exception {
        when(emotionAnalysisService.analyzeTodayDemandSimple("chatroom1")).thenReturn("Support");

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/demands/today"))
                .andExpect(status().isOk())
                .andExpect(content().string("Support"));
    }

    @Test
    void testGetTodaySimpleDemand_Exception() throws Exception {
        when(emotionAnalysisService.analyzeTodayDemandSimple("chatroom1"))
                .thenThrow(new RuntimeException("Demand error"));

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/demands/today"))
                .andExpect(status().isOk());
    }
    @Test
    void testGetTodaySentiment_Success() throws Exception {
        GoogleNaturalLanguageService.SimpleSentimentResult result =
                new GoogleNaturalLanguageService.SimpleSentimentResult(0.8f, 0.9f);
        when(googleNaturalLanguageService.analyzeTodaySentiment("chatroom1")).thenReturn(result);

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/score/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0.8))
                .andExpect(jsonPath("$.magnitude").value(0.9));
    }

    @Test
    void testGetTodaySentiment_Exception() throws Exception {
        when(googleNaturalLanguageService.analyzeTodaySentiment("chatroom1"))
                .thenThrow(new RuntimeException("Sentiment error"));

        mockMvc.perform(get("/api/messages/chatroom/chatroom1/score/today"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.score").value(0.0))
                .andExpect(jsonPath("$.magnitude").value(0.0));
    }

    @Test
    void testSavePreQuestionForChatroom_Success() throws Exception {
        String preQuestion = "How are you?";
        ChatMessage savedMessage = createChatMessage("msg5", "chatroom1", preQuestion, true, false);
        when(chatMessageService.savePreQuestionForNewChatroom("chatroom1", preQuestion))
                .thenReturn(savedMessage);

        mockMvc.perform(post("/api/messages/chatroom/chatroom1/prequestion")
                        .content(preQuestion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(preQuestion));
    }

    @Test
    void testSavePreQuestionForChatroom_Exception() throws Exception {
        when(chatMessageService.savePreQuestionForNewChatroom(anyString(), anyString()))
                .thenThrow(new RuntimeException("Save error"));

        mockMvc.perform(post("/api/messages/chatroom/chatroom1/prequestion")
                        .content("test"))
                .andExpect(status().isInternalServerError());
    }
}
