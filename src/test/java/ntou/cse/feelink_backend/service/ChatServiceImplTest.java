package ntou.cse.feelink_backend.service;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
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
    private ImageProcessor imageProcessor;

    @Mock
    private TextProcessor textProcessor;

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
    void testProcessChat_WithTextOnly() throws IOException {
        String userMessage = "Hello";
        when(textProcessor.processText(userMessage)).thenReturn("Processed Text");

        String response = chatService.processChat(null, userMessage);

        assertNotNull(response);
        verify(textProcessor, times(1)).processText(userMessage);
        verify(chatSession, times(1)).sendMessage("Processed Text");
    }

    @Test
    void shouldProcessImageOnly() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String userMessage = "";
        String processedText = "請分析這張圖片";
        Part dummyImage = mock(Part.class);

        when(textProcessor.processText(userMessage)).thenReturn(processedText);
        when(imageProcessor.processImage(file)).thenReturn(dummyImage);

        Content dummyContent = mock(Content.class);

        try (MockedStatic<ContentMaker> contentMakerMock = Mockito.mockStatic(ContentMaker.class)) {
            contentMakerMock.when(() -> ContentMaker.fromMultiModalData(dummyImage, processedText))
                    .thenReturn(dummyContent);
            when(chatSession.sendMessage(dummyContent)).thenReturn(mockResponse);

            String response = chatService.processChat(file, userMessage);
            assertNotNull(response);
            assertEquals("Mock AI 回應", response);

            verify(textProcessor, times(1)).processText(userMessage);
            verify(imageProcessor, times(1)).processImage(file);
            contentMakerMock.verify(() -> ContentMaker.fromMultiModalData(dummyImage, processedText), times(1));
        }
    }
    @Test
    void testProcessChat_WithImageAndText() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String userMessage = "Hello Image";
        String processedText = "Hello Image";
        Part dummyImage = mock(Part.class);

        when(textProcessor.processText(userMessage)).thenReturn(processedText);
        when(imageProcessor.processImage(file)).thenReturn(dummyImage);

        Content dummyContent = mock(Content.class);

        try (MockedStatic<ContentMaker> contentMakerMock = Mockito.mockStatic(ContentMaker.class)) {
            contentMakerMock.when(() -> ContentMaker.fromMultiModalData(dummyImage, processedText))
                    .thenReturn(dummyContent);
            when(chatSession.sendMessage(dummyContent)).thenReturn(mockResponse);

            String response = chatService.processChat(file, userMessage);

            assertNotNull(response);
            assertEquals("Mock AI 回應", response);

            verify(textProcessor, times(1)).processText(userMessage);
            verify(imageProcessor, times(1)).processImage(file);
            contentMakerMock.verify(() -> ContentMaker.fromMultiModalData(dummyImage, processedText), times(1));
        }
    }
}
