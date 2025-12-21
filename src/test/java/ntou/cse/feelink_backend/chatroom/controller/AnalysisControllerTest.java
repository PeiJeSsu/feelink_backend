package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalysisControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private AnalysisController analysisController;

    private String testUserMessage;
    private String testChatroomId;
    private String testPersonality;
    private MockMultipartFile testFile;
    private ChatMessage testSavedMessage;

    @BeforeEach
    void setUp() {
        testUserMessage = "請分析這張圖片";
        testChatroomId = "test-chatroom-123";
        testPersonality = "friendly";

        String testFileContent = "test image content";
        testFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                testFileContent.getBytes()
        );

        testSavedMessage = new ChatMessage();
        testSavedMessage.setMessageId("saved-message-123");
        testSavedMessage.setChatroomId(testChatroomId);
        testSavedMessage.setIsUser(false);
        testSavedMessage.setIsDrawingData(false);
        testSavedMessage.setContent("");
        testSavedMessage.setSentAt(new Date());
    }

    @Test
    void testAnalyse_WithFileAndMessage() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);

        assertNotNull(result);
        assertEquals(mockEmitter, result);

        verify(chatService, times(1)).processChatStreamWithPersonality(
                eq(testFile), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")
        );
        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
        String expectedBase64 = Base64.getEncoder().encodeToString(testFile.getBytes());
        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        message.getIsDrawingData() &&
                        message.getContent().equals(expectedBase64)
        ));

        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getContent().equals(testUserMessage)
        ));
    }

    @Test
    void testAnalyse_WithFileOnly() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(""), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = analysisController.analyse(testFile, "", testChatroomId, testPersonality);

        assertNotNull(result);
        assertEquals(mockEmitter, result);

        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChatStreamWithPersonality(
                eq(testFile), eq(""), eq(testChatroomId), eq(testPersonality), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_WithMessageOnly() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = analysisController.analyse(null, testUserMessage, testChatroomId, testPersonality);

        assertNotNull(result);
        assertEquals(mockEmitter, result);

        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId), eq(testPersonality), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_WithDefaultPersonality() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId),
                eq("default"), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = analysisController.analyse(null, testUserMessage, testChatroomId, "default");

        assertNotNull(result);
        verify(chatService, times(1)).processChatStreamWithPersonality(
                isNull(), eq(testUserMessage), eq(testChatroomId), eq("default"), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_WithEmptyFile() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                eq(emptyFile), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = analysisController.analyse(emptyFile, testUserMessage, testChatroomId, testPersonality);

        assertNotNull(result);

        verify(chatMessageService, times(2)).createMessage(any(ChatMessage.class));
        verify(chatService, times(1)).processChatStreamWithPersonality(
                eq(emptyFile), eq(testUserMessage), eq(testChatroomId), eq(testPersonality), eq("saved-message-123"));
    }

    @Test
    void testAnalyse_NoUserInput() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                isNull(), eq(""), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        SseEmitter result = analysisController.analyse(null, "", testChatroomId, testPersonality);

        assertNotNull(result);
        verify(chatMessageService, times(1)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testAnalyse_ChatServiceException() throws IOException {
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenThrow(new RuntimeException("串流服務異常"));

        assertThrows(RuntimeException.class, () -> {
            analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);
        });

        verify(chatMessageService, times(3)).createMessage(any(ChatMessage.class));
    }

    @Test
    void testAnalyse_ChatMessageServiceException() throws IOException {
        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("資料庫連接失敗"));

        assertThrows(RuntimeException.class, () -> {
            analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);
        });
    }

    @Test
    void testBase64Encoding() throws IOException {
        String originalContent = "test image content";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", originalContent.getBytes()
        );
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        analysisController.analyse(file, testUserMessage, testChatroomId, testPersonality);

        String expectedBase64 = Base64.getEncoder().encodeToString(originalContent.getBytes());
        verify(chatMessageService).createMessage(argThat(message ->
                message.getIsDrawingData() &&
                        message.getContent().equals(expectedBase64)
        ));
    }

    @Test
    void testAIResponseProperties() throws IOException {
        SseEmitter mockEmitter = new SseEmitter();

        when(chatMessageService.createMessage(any(ChatMessage.class)))
                .thenReturn(new ChatMessage())
                .thenReturn(new ChatMessage())
                .thenReturn(testSavedMessage);
        when(chatService.processChatStreamWithPersonality(
                any(MultipartFile.class), eq(testUserMessage), eq(testChatroomId),
                eq(testPersonality), eq("saved-message-123")))
                .thenReturn(mockEmitter);

        analysisController.analyse(testFile, testUserMessage, testChatroomId, testPersonality);

        verify(chatMessageService).createMessage(argThat(message ->
                message.getChatroomId().equals(testChatroomId) &&
                        !message.getIsUser() &&
                        !message.getIsDrawingData() &&
                        message.getSentAt() != null &&
                        message.getContent().equals("")
        ));
    }

    @Test
    void testAnalyse_WithDifferentPersonalities() throws IOException {
        String[] personalities = {"friendly", "professional", "casual", "formal"};
        SseEmitter mockEmitter = new SseEmitter();

        for (String personality : personalities) {
            when(chatMessageService.createMessage(any(ChatMessage.class)))
                    .thenReturn(new ChatMessage())
                    .thenReturn(testSavedMessage);
            when(chatService.processChatStreamWithPersonality(
                    isNull(), eq(testUserMessage), eq(testChatroomId),
                    eq(personality), eq("saved-message-123")))
                    .thenReturn(mockEmitter);

            SseEmitter result = analysisController.analyse(null, testUserMessage, testChatroomId, personality);

            assertNotNull(result);
            verify(chatService).processChatStreamWithPersonality(
                    isNull(), eq(testUserMessage), eq(testChatroomId), eq(personality), eq("saved-message-123"));
        }
    }
}