package ntou.cse.feelink_backend.chatroom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GeminiService geminiService;
    private ObjectMapper objectMapper;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_PROMPT = "Generate a simple drawing";
    private static final String TEST_CANVAS_DATA = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService();
        objectMapper = new ObjectMapper();

        // 使用反射設置私有字段
        ReflectionTestUtils.setField(geminiService, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(geminiService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(geminiService, "objectMapper", objectMapper);
    }

    @Test
    void testGenerateImage_Success() {
        // Arrange
        String mockResponse = """
            {
                "candidates": [{
                    "content": {
                        "parts": [
                            {
                                "text": "Generated image description"
                            },
                            {
                                "inlineData": {
                                    "data": "base64ImageData"
                                }
                            }
                        ]
                    }
                }]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        GeminiResponse result = geminiService.generateImage(TEST_PROMPT, TEST_CANVAS_DATA);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Generated image description", result.getMessage());
        assertEquals("base64ImageData", result.getImageData());

        // Verify API call
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST),
                entityCaptor.capture(), eq(String.class));

        assertTrue(urlCaptor.getValue().contains("generateContent"));
        assertTrue(urlCaptor.getValue().contains(TEST_API_KEY));
    }

    @Test
    void testGenerateImage_WithoutCanvasData() {
        // Arrange
        String mockResponse = """
            {
                "candidates": [{
                    "content": {
                        "parts": [
                            {
                                "text": "Generated without canvas"
                            }
                        ]
                    }
                }]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        GeminiResponse result = geminiService.generateImage(TEST_PROMPT, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Generated without canvas", result.getMessage());
        assertNull(result.getImageData());
    }

    @Test
    void testGenerateImage_EmptyCanvasData() {
        // Arrange
        String mockResponse = """
            {
                "candidates": [{
                    "content": {
                        "parts": [
                            {
                                "text": "Generated with empty canvas"
                            }
                        ]
                    }
                }]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        GeminiResponse result = geminiService.generateImage(TEST_PROMPT, "");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Generated with empty canvas", result.getMessage());
    }

    @Test
    void testGenerateImage_InvalidResponse() {
        // Arrange
        String invalidResponse = "{ \"invalid\": \"response\" }";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> geminiService.generateImage(TEST_PROMPT, TEST_CANVAS_DATA));

        assertTrue(exception.getMessage().contains("Invalid response format from Gemini API"));
    }

    @Test
    void testGenerateImage_RestTemplateException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> geminiService.generateImage(TEST_PROMPT, TEST_CANVAS_DATA));

        assertTrue(exception.getMessage().contains("Error generating image"));
        assertTrue(exception.getCause().getMessage().contains("Network error"));
    }

    @Test
    void testGenerateImageStream_Success() throws InterruptedException {
        // Arrange
        String mockStreamResponse = """
            {"candidates": [{"content": {"parts": [{"text": "First token"}]}}]}
            {"candidates": [{"content": {"parts": [{"text": " Second token"}]}}]}
            {"candidates": [{"content": {"parts": [{"inlineData": {"data": "streamImageData"}}]}}]}
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockStreamResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Test data collectors
        AtomicReference<String> tokenResult = new AtomicReference<>("");
        AtomicReference<String> imageResult = new AtomicReference<>();
        AtomicReference<Boolean> completed = new AtomicReference<>(false);
        AtomicReference<Exception> errorResult = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Act
        geminiService.generateImageStream(
                TEST_PROMPT,
                TEST_CANVAS_DATA,
                token -> tokenResult.set(tokenResult.get() + token),
                image -> imageResult.set(image),
                () -> {
                    completed.set(true);
                    latch.countDown();
                },
                error -> {
                    errorResult.set(error);
                    latch.countDown();
                }
        );

        // Wait for completion
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Assert
        assertTrue(completed.get());
        assertNull(errorResult.get());
        assertEquals("First token Second token", tokenResult.get());
        assertEquals("streamImageData", imageResult.get());

        // Verify stream API call
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, timeout(1000)).exchange(urlCaptor.capture(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class));

        assertTrue(urlCaptor.getValue().contains("streamGenerateContent"));
    }

    @Test
    void testGenerateImageStream_WithError() throws InterruptedException {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Stream error"));

        AtomicReference<Exception> errorResult = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Act
        geminiService.generateImageStream(
                TEST_PROMPT,
                TEST_CANVAS_DATA,
                token -> {},
                image -> {},
                () -> latch.countDown(),
                error -> {
                    errorResult.set(error);
                    latch.countDown();
                }
        );

        // Wait for completion
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Assert
        assertNotNull(errorResult.get());
        assertTrue(errorResult.get().getMessage().contains("Stream error"));
    }

    @Test
    void testGenerateImageStream_InvalidStreamResponse() throws InterruptedException {
        // Arrange
        String invalidStreamResponse = "invalid json line\n{\"valid\": \"but incomplete\"}\n";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidStreamResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        AtomicReference<Boolean> completed = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);

        // Act
        geminiService.generateImageStream(
                TEST_PROMPT,
                null,
                token -> {},
                image -> {},
                () -> {
                    completed.set(true);
                    latch.countDown();
                },
                error -> latch.countDown()
        );

        // Wait for completion
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Assert - should complete successfully despite invalid lines (they are ignored)
        assertTrue(completed.get());
    }

    @Test
    void testSystemInstructions_Content() {
        // Use reflection to access private method for testing
        String systemInstructions = (String) ReflectionTestUtils.invokeMethod(
                geminiService, "getSystemInstructions");

        assertNotNull(systemInstructions);
        assertTrue(systemInstructions.contains("圖像生成助手"));
        assertTrue(systemInstructions.contains("嚴禁任何文字出現在圖像上"));
        assertTrue(systemInstructions.contains("簡單的線條塗鴉風格"));
        assertTrue(systemInstructions.contains("使用者原始請求所用的語言"));
    }

    @Test
    void testBuildRequestBody_WithCanvasData() {
        // Use reflection to test private method
        String systemInstructions = "test instructions";
        Object requestBody = ReflectionTestUtils.invokeMethod(
                geminiService, "buildRequestBody", TEST_PROMPT, TEST_CANVAS_DATA, systemInstructions);

        assertNotNull(requestBody);
        // Additional assertions would require casting to Map and checking structure
    }

    @Test
    void testBuildRequestBody_WithoutCanvasData() {
        // Use reflection to test private method
        String systemInstructions = "test instructions";
        Object requestBody = ReflectionTestUtils.invokeMethod(
                geminiService, "buildRequestBody", TEST_PROMPT, null, systemInstructions);

        assertNotNull(requestBody);
        // Additional assertions would require casting to Map and checking structure
    }

    @Test
    void testParseGeminiResponse_OnlyText() {
        // Arrange
        String responseWithOnlyText = """
            {
                "candidates": [{
                    "content": {
                        "parts": [
                            {
                                "text": "Only text response"
                            }
                        ]
                    }
                }]
            }
            """;

        // Act
        GeminiResponse result = (GeminiResponse) ReflectionTestUtils.invokeMethod(
                geminiService, "parseGeminiResponse", responseWithOnlyText);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Only text response", result.getMessage());
        assertNull(result.getImageData());
    }

    @Test
    void testParseGeminiResponse_OnlyImage() {
        // Arrange
        String responseWithOnlyImage = """
            {
                "candidates": [{
                    "content": {
                        "parts": [
                            {
                                "inlineData": {
                                    "data": "onlyImageData"
                                }
                            }
                        ]
                    }
                }]
            }
            """;

        // Act
        GeminiResponse result = (GeminiResponse) ReflectionTestUtils.invokeMethod(
                geminiService, "parseGeminiResponse", responseWithOnlyImage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("", result.getMessage());
        assertEquals("onlyImageData", result.getImageData());
    }
}