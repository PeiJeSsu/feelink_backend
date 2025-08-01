package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawingAnalysisChatServiceImplTest {
    @Mock private ChatSession chatSession;
    @Mock private GenerateContentResponse mockResponse;
    @Mock private ResponseStream<GenerateContentResponse> mockResponseStream;
    @Mock
    ChatSessionRegistry registry;
    @InjectMocks private DrawingAnalysisChatServiceImpl service;
    private static final String SESSION_ID  = "1";
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(registry.getSession(anyString())).thenReturn(chatSession);
        when(mockResponse.toString()).thenReturn("Mock AI 回應");
        Candidate cand = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn("Mock AI 回應");
        when(content.getPartsList()).thenReturn(Collections.singletonList(part));
        when(cand.getContent()).thenReturn(content);
        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(cand);
        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
        when(chatSession.sendMessage(any(Content.class))).thenReturn(mockResponse);
        when(chatSession.sendMessageStream(anyString())).thenReturn(mockResponseStream);
        when(chatSession.sendMessageStream(any(Content.class))).thenReturn(mockResponseStream);
    }

    @Test
    void withText() throws IOException {
        String out = service.processChat(null, "Hello", SESSION_ID);
        assertEquals("Mock AI 回應", out);
        verify(registry).getSession(SESSION_ID);
        verify(chatSession).sendMessage("Hello");
    }

    @Test
    void nullText() throws IOException {
        String resp = service.processChat(null, null, SESSION_ID);
        assertEquals("Mock AI 回應", resp);
        verify(chatSession).sendMessage("請分析這張圖片");
        verify(registry).getSession(SESSION_ID);
    }

    @Test
    void emptyText() throws IOException {
        String resp = service.processChat(null, "   ", SESSION_ID);
        assertEquals("Mock AI 回應", resp);
        verify(chatSession).sendMessage("請分析這張圖片");
        verify(registry).getSession(SESSION_ID);
    }

    @Test
    void withImage() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        byte[] bytes = "data".getBytes();
        when(file.getBytes()).thenReturn(bytes);
        Part mockPart = mock(Part.class);
        Content multimodal = mock(Content.class);
        try (var pm = mockStatic(com.google.cloud.vertexai.generativeai.PartMaker.class);
             var cm = mockStatic(ContentMaker.class)) {
            pm.when(() -> com.google.cloud.vertexai.generativeai.PartMaker.fromMimeTypeAndData("image/jpeg", bytes))
                    .thenReturn(mockPart);
            cm.when(() -> ContentMaker.fromMultiModalData(mockPart, "請分析這張圖片"))
                    .thenReturn(multimodal);
            String resp = service.processChat(file, "", SESSION_ID);
            assertEquals("Mock AI 回應", resp);
            verify(chatSession).sendMessage(multimodal);
            verify(registry).getSession(SESSION_ID);
        }
    }

    @Test
    void emptyFile() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        String resp = service.processChat(file, "只有空檔案", SESSION_ID );
        assertEquals("Mock AI 回應", resp);
        verify(chatSession).sendMessage("只有空檔案");
        verify(registry).getSession(SESSION_ID);
    }

    @Test
    void exception_returnsErrorMessage() throws IOException {
        when(chatSession.sendMessage(anyString())).thenThrow(new RuntimeException("fail"));
        String resp = service.processChat(null, "Err", SESSION_ID);
        assertEquals("AI處理錯誤", resp);
        verify(registry).getSession(SESSION_ID);
    }
    @Test
    void processChatStream_withText() throws Exception {
        GenerateContentResponse response = createMockResponse("Stream Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.processChatStream(null, "Hello", SESSION_ID);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream("Hello");
    }

    @Test
    void processChatStream_nullText() throws Exception {
        GenerateContentResponse response = createMockResponse("Stream Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.processChatStream(null, null, SESSION_ID);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream("請分析這張圖片");
    }

    @Test
    void processChatStream_withImage() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        byte[] bytes = "data".getBytes();
        when(file.getBytes()).thenReturn(bytes);
        Part mockPart = mock(Part.class);
        Content multimodal = mock(Content.class);
        GenerateContentResponse response = createMockResponse("Image Analysis");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        try (var pm = mockStatic(com.google.cloud.vertexai.generativeai.PartMaker.class);
             var cm = mockStatic(ContentMaker.class)) {
            pm.when(() -> com.google.cloud.vertexai.generativeai.PartMaker.fromMimeTypeAndData("image/jpeg", bytes))
                    .thenReturn(mockPart);
            cm.when(() -> ContentMaker.fromMultiModalData(mockPart, "請分析這張圖片"))
                    .thenReturn(multimodal);
            SseEmitter emitter = service.processChatStream(file, "", SESSION_ID);
            assertNotNull(emitter);
            verify(registry).getSession(SESSION_ID);
            Thread.sleep(100);
            verify(chatSession).sendMessageStream(multimodal);
        }
    }

    @Test
    void processChatStream_emptyFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        GenerateContentResponse response = createMockResponse("Text Only Response");
        when(mockResponseStream.iterator()).thenReturn(Arrays.asList(response).iterator());
        SseEmitter emitter = service.processChatStream(file, "只有空檔案", SESSION_ID);
        assertNotNull(emitter);
        verify(registry).getSession(SESSION_ID);
        Thread.sleep(100);
        verify(chatSession).sendMessageStream("只有空檔案");
    }

    private GenerateContentResponse createMockResponse(String text) {
        GenerateContentResponse response = mock(GenerateContentResponse.class);
        Candidate candidate = mock(Candidate.class);
        Content content = mock(Content.class);
        Part part = mock(Part.class);
        when(part.getText()).thenReturn(text);
        when(content.getPartsList()).thenReturn(Arrays.asList(part));
        when(candidate.getContent()).thenReturn(content);
        when(response.getCandidatesCount()).thenReturn(1);
        when(response.getCandidates(0)).thenReturn(candidate);
        return response;
    }
}