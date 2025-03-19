package ntou.cse.feelink_backend.chatroom.service;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {
    @Mock
    private ChatSession chatSession;
    @Mock
    private GenerateContentResponse mockResponse;
    @InjectMocks
    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        Candidate mockCandidate = mock(Candidate.class);
        Content mockContent = mock(Content.class);
        Part mockPart = mock(Part.class);

        when(mockPart.toString()).thenReturn("Mock AI Response Part");
        when(mockPart.getText()).thenReturn("Mock AI 回應");

        when(mockContent.getPartsList()).thenReturn(Collections.singletonList(mockPart));
        when(mockCandidate.getContent()).thenReturn(mockContent);

        when(mockResponse.getCandidatesCount()).thenReturn(1);
        when(mockResponse.getCandidates(0)).thenReturn(mockCandidate);
        when(mockResponse.getCandidatesList()).thenReturn(Collections.singletonList(mockCandidate));
        when(mockResponse.toString()).thenReturn("Mock AI 回應");

        when(chatSession.sendMessage(anyString())).thenReturn(mockResponse);
        when(chatSession.sendMessage(any(Content.class))).thenReturn(mockResponse);
    }

    @Test
    void testProcessChat_WithText() throws IOException {
        MultipartFile file = null;
        String userMessage = "Hello";

        String response = chatService.processChat(file, userMessage);

        assertNotNull(response);
        assertEquals("Mock AI 回應", response);
        verify(chatSession, times(1)).sendMessage(userMessage);
    }

    @Test
    void testProcessChat_WithImage() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String userMessage = "";
        byte[] imageBytes = "test image".getBytes();

        Part mockImagePart = mock(Part.class);
        Content mockMultiModalContent = mock(Content.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenReturn(imageBytes);

        try (MockedStatic<PartMaker> partMakerMock = Mockito.mockStatic(PartMaker.class);
                MockedStatic<ContentMaker> contentMakerMock = Mockito.mockStatic(ContentMaker.class)) {

            partMakerMock.when(() -> PartMaker.fromMimeTypeAndData(anyString(), any(byte[].class)))
                    .thenReturn(mockImagePart);

            contentMakerMock.when(() -> ContentMaker.fromMultiModalData(any(), anyString()))
                    .thenReturn(mockMultiModalContent);

            String response = chatService.processChat(file, userMessage);

            assertNotNull(response);
            assertEquals("Mock AI 回應", response);
            verify(chatSession, times(1)).sendMessage(mockMultiModalContent);
            partMakerMock.verify(() -> PartMaker.fromMimeTypeAndData("image/jpeg", imageBytes), times(1));
            contentMakerMock.verify(() -> ContentMaker.fromMultiModalData(mockImagePart, "請分析這張圖片"), times(1));
        }
    }

    @Test
    void testProcessChat_WithTextAndImage() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String userMessage = "Hello Image";
        byte[] imageBytes = "test image".getBytes();

        Part mockImagePart = mock(Part.class);
        Content mockMultiModalContent = mock(Content.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenReturn(imageBytes);

        try (MockedStatic<PartMaker> partMakerMock = Mockito.mockStatic(PartMaker.class);
                MockedStatic<ContentMaker> contentMakerMock = Mockito.mockStatic(ContentMaker.class)) {

            partMakerMock.when(() -> PartMaker.fromMimeTypeAndData(anyString(), any(byte[].class)))
                    .thenReturn(mockImagePart);

            contentMakerMock.when(() -> ContentMaker.fromMultiModalData(any(), anyString()))
                    .thenReturn(mockMultiModalContent);

            String response = chatService.processChat(file, userMessage);

            assertNotNull(response);
            assertEquals("Mock AI 回應", response);
            verify(chatSession, times(1)).sendMessage(mockMultiModalContent);
            partMakerMock.verify(() -> PartMaker.fromMimeTypeAndData("image/jpeg", imageBytes), times(1));
            contentMakerMock.verify(() -> ContentMaker.fromMultiModalData(mockImagePart, userMessage), times(1));
        }
    }
}