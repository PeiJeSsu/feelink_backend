package ntou.cse.feelink_backend.chatroom.service;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.*;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private Client client;

    @Mock
    private Models models;

    @InjectMocks
    private GeminiService geminiService;

    @Mock private GenerateContentResponse mockResponse;
    @Mock private Candidate mockCandidate;
    @Mock private Content mockContent;
    @Mock private Part mockTextPart;
    @Mock private Part mockImagePart;
    @Mock private Blob mockBlob;

    private final String VALID_BASE64_IMAGE = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    private final String VALID_BASE64_WITH_PREFIX = "data:image/png;base64," + VALID_BASE64_IMAGE;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "models", models);
    }

    private void setupMockResponse(String text, byte[] imageBytes) throws Exception {
        when(models.generateContent(anyString(), any(Content.class), any(GenerateContentConfig.class)))
                .thenReturn(mockResponse);

        when(mockResponse.candidates()).thenReturn(Optional.of(List.of(mockCandidate)));
        when(mockCandidate.content()).thenReturn(Optional.of(mockContent));

        List<Part> parts;
        if (imageBytes != null) {
            when(mockImagePart.text()).thenReturn(Optional.empty());
            when(mockImagePart.inlineData()).thenReturn(Optional.of(mockBlob));
            when(mockBlob.data()).thenReturn(Optional.of(imageBytes));

            when(mockTextPart.text()).thenReturn(Optional.ofNullable(text));
            when(mockTextPart.inlineData()).thenReturn(Optional.empty());

            parts = List.of(mockTextPart, mockImagePart);
        } else {
            when(mockTextPart.text()).thenReturn(Optional.ofNullable(text));
            when(mockTextPart.inlineData()).thenReturn(Optional.empty());
            parts = List.of(mockTextPart);
        }

        when(mockContent.parts()).thenReturn(Optional.of(parts));
    }


    @Test
    void testGenerateImage_ModeDrawing() throws Exception {
        setupMockResponse("Drawing Result", null);

        GeminiResponse result = geminiService.generateImage("draw a cat", VALID_BASE64_IMAGE, "drawing");

        assertTrue(result.isSuccess());
        assertEquals("Drawing Result", result.getMessage());
        ArgumentMatcher<Content> contentMatcher = content -> {
            if (content.parts().isEmpty() || content.parts().get().isEmpty()) return false;

            String text = content.parts().get().get(0).text().orElse("");
            return text.contains("指示要添加、移除或更改的部分之外") && text.contains("draw a cat");
        };

        verify(models).generateContent(
                eq("gemini-2.5-flash-image"),
                argThat(contentMatcher),
                any(GenerateContentConfig.class)
        );
    }

    @Test
    void testGenerateImage_ModeGenerateObject() throws Exception {
        setupMockResponse("Object Result", null);

        GeminiResponse result = geminiService.generateImage("make a cup", VALID_BASE64_IMAGE, "generateObject");

        assertTrue(result.isSuccess());

        ArgumentMatcher<Content> contentMatcher = content -> {
            if (content.parts().isEmpty() || content.parts().get().isEmpty()) return false;
            String text = content.parts().get().get(0).text().orElse("");
            return text.contains("生成一個獨立的") && text.contains("make a cup");
        };

        verify(models).generateContent(
                eq("gemini-2.5-flash-image"),
                argThat(contentMatcher),
                any(GenerateContentConfig.class)
        );
    }

    @Test
    void testGenerateImage_ModeDefault() throws Exception {
        setupMockResponse("Default Result", null);

        GeminiResponse result = geminiService.generateImage("test", VALID_BASE64_IMAGE, "unknown_mode");

        assertTrue(result.isSuccess());

        ArgumentMatcher<Content> contentMatcher = content -> {
            if (content.parts().isEmpty() || content.parts().get().isEmpty()) return false;
            String text = content.parts().get().get(0).text().orElse("");
            return text.contains("生成圖片並用繁體中文回應");
        };

        verify(models).generateContent(
                eq("gemini-2.5-flash-image"),
                argThat(contentMatcher),
                any(GenerateContentConfig.class)
        );
    }

    @Test
    void testGenerateImage_CanvasDataWithPrefix() throws Exception {
        setupMockResponse("Success", null);

        geminiService.generateImage("prompt", VALID_BASE64_WITH_PREFIX, "default");

        ArgumentMatcher<Content> contentMatcher = content ->
                content.parts().isPresent() && content.parts().get().size() == 2;

        verify(models).generateContent(
                anyString(),
                argThat(contentMatcher),
                any(GenerateContentConfig.class)
        );
    }


    @Test
    void testGenerateImage_WithImageResponse() throws Exception {
        byte[] fakeImage = new byte[]{1, 2, 3};
        setupMockResponse("Here is your image", fakeImage);

        GeminiResponse result = geminiService.generateImage("prompt", VALID_BASE64_IMAGE, "default");

        assertTrue(result.isSuccess());
        assertEquals("Here is your image", result.getMessage());
        assertNotNull(result.getImageData());
        assertEquals(Base64.getEncoder().encodeToString(fakeImage), result.getImageData());
    }

    @Test
    void testGenerateImage_EmptyTextResponse() throws Exception {
        setupMockResponse("", new byte[]{1});

        GeminiResponse result = geminiService.generateImage("prompt", VALID_BASE64_IMAGE, "default");

        assertTrue(result.isSuccess());
        assertEquals("圖片處理完成", result.getMessage());
    }


    @Test
    void testGenerateImage_ClientException() throws Exception {
        when(models.generateContent(anyString(), any(Content.class), any(GenerateContentConfig.class)))
                .thenThrow(new RuntimeException("API Connection Failed"));

        GeminiResponse result = geminiService.generateImage("prompt", VALID_BASE64_IMAGE, "default");

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("圖片生成失敗"));
    }

    @Test
    void testConvertToGeminiResponse_ParsingException() throws Exception {
        when(models.generateContent(anyString(), any(Content.class), any(GenerateContentConfig.class))).thenReturn(mockResponse);
        when(mockResponse.candidates()).thenThrow(new RuntimeException("Parsing Error"));

        GeminiResponse result = geminiService.generateImage("prompt", VALID_BASE64_IMAGE, "default");

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("處理回應失敗"));
    }
}
