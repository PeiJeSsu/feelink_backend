package ntou.cse.feelink_backend.chatroom.service;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawingAnalysisChatServiceImplTest {
    @Mock private ChatSession chatSession;
    @Mock private GenerateContentResponse mockResponse;
    @Mock ChatSessionRegistry registry;
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
}