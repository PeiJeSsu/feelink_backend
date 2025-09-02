package ntou.cse.feelink_backend.chatroom.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveBgServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private RemoveBgService removeBgService;
    private final String testApiKey = "test-api-key-12345";
    private final String testApiUrl = "https://api.remove.bg/v1/removebg";

    @BeforeEach
    void setUp() {
        removeBgService = new RemoveBgService();

        // 使用反射設置私有字段
        ReflectionTestUtils.setField(removeBgService, "apiKey", testApiKey);
        ReflectionTestUtils.setField(removeBgService, "apiUrl", testApiUrl);
        ReflectionTestUtils.setField(removeBgService, "webClient", webClient);
    }

    @Test
    void removeBackground_WhenSuccessful_ShouldReturnProcessedImageBytes() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        byte[] originalImageData = "original image data".getBytes(StandardCharsets.UTF_8);
        byte[] processedImageData = "processed image data".getBytes(StandardCharsets.UTF_8);

        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test-image.jpg",
                "image/jpeg",
                originalImageData
        );

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(processedImageData);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));

        // When
        Mono<byte[]> result = removeBgService.removeBackground(mockFile);

        // Then - Convert Mono to CompletableFuture for testing
        CompletableFuture<byte[]> future = result.toFuture();
        byte[] actualResult = future.get(5, TimeUnit.SECONDS);

        assertThat(actualResult).isEqualTo(processedImageData);

        // 驗證調用
        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri(testApiUrl);
        verify(requestBodySpec, times(1)).header("X-Api-Key", testApiKey);
    }

    @Test
    void removeBackground_WhenApiReturns4xxError_ShouldReturnError() {
        // Given
        byte[] imageData = "test image data".getBytes(StandardCharsets.UTF_8);
        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test-image.jpg",
                "image/jpeg",
                imageData
        );

        String errorMessage = "Invalid API key";

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class))
                .thenReturn(Mono.error(new RuntimeException("API調用失敗: 401 - " + errorMessage)));

        // When & Then
        Mono<byte[]> result = removeBgService.removeBackground(mockFile);

        assertThatThrownBy(() -> {
            CompletableFuture<byte[]> future = result.toFuture();
            future.get(5, TimeUnit.SECONDS);
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("API調用失敗")
                .hasMessageContaining("401");
    }

    @Test
    void removeBackground_WhenApiReturns5xxError_ShouldReturnError() {
        // Given
        byte[] imageData = "test image data".getBytes(StandardCharsets.UTF_8);
        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test-image.jpg",
                "image/jpeg",
                imageData
        );

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class))
                .thenReturn(Mono.error(new RuntimeException("API調用失敗: 500 - Internal server error")));

        // When & Then
        Mono<byte[]> result = removeBgService.removeBackground(mockFile);

        assertThatThrownBy(() -> {
            CompletableFuture<byte[]> future = result.toFuture();
            future.get(5, TimeUnit.SECONDS);
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("API調用失敗")
                .hasMessageContaining("500");
    }

    @Test
    void removeBackground_WhenFileReadFails_ShouldReturnError() {
        // Given
        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test-image.jpg",
                "image/jpeg",
                "test data".getBytes()
        ) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("File read error");
            }
        };

        // When & Then
        Mono<byte[]> result = removeBgService.removeBackground(mockFile);

        assertThatThrownBy(() -> {
            CompletableFuture<byte[]> future = result.toFuture();
            future.get(5, TimeUnit.SECONDS);
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("文件讀取失敗");
    }

    @Test
    void removeBackground_WithDifferentImageTypes_ShouldWork() throws ExecutionException, InterruptedException, TimeoutException {
        // Given - PNG file
        byte[] pngImageData = "PNG image data".getBytes(StandardCharsets.UTF_8);
        byte[] processedImageData = "processed PNG data".getBytes(StandardCharsets.UTF_8);

        MultipartFile pngFile = new MockMultipartFile(
                "image_file",
                "test-image.png",
                "image/png",
                pngImageData
        );

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(processedImageData);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));

        // When
        Mono<byte[]> result = removeBgService.removeBackground(pngFile);

        // Then
        CompletableFuture<byte[]> future = result.toFuture();
        byte[] actualResult = future.get(5, TimeUnit.SECONDS);

        assertThat(actualResult).isEqualTo(processedImageData);
    }

    @Test
    void removeBackground_WithLargeFile_ShouldWork() throws ExecutionException, InterruptedException, TimeoutException {
        // Given - 大文件 (模擬1KB，避免測試太慢)
        byte[] largeImageData = new byte[1024]; // 1KB
        java.util.Arrays.fill(largeImageData, (byte) 1);

        byte[] processedImageData = "processed large image".getBytes(StandardCharsets.UTF_8);

        MultipartFile largeFile = new MockMultipartFile(
                "image_file",
                "large-image.jpg",
                "image/jpeg",
                largeImageData
        );

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(processedImageData);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));

        // When
        Mono<byte[]> result = removeBgService.removeBackground(largeFile);

        // Then
        CompletableFuture<byte[]> future = result.toFuture();
        byte[] actualResult = future.get(5, TimeUnit.SECONDS);

        assertThat(actualResult).isEqualTo(processedImageData);
    }

    @Test
    void removeBackground_WithSpecialCharactersInFilename_ShouldWork() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        byte[] imageData = "test image data".getBytes(StandardCharsets.UTF_8);
        byte[] processedImageData = "processed image".getBytes(StandardCharsets.UTF_8);

        MultipartFile fileWithSpecialName = new MockMultipartFile(
                "image_file",
                "測試圖片 (1).jpg", // 包含中文和特殊字符
                "image/jpeg",
                imageData
        );

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(processedImageData);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));

        // When
        Mono<byte[]> result = removeBgService.removeBackground(fileWithSpecialName);

        // Then
        CompletableFuture<byte[]> future = result.toFuture();
        byte[] actualResult = future.get(5, TimeUnit.SECONDS);

        assertThat(actualResult).isEqualTo(processedImageData);
    }

    @Test
    void removeBackground_WithEmptyFile_ShouldWork() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "image_file",
                "empty.jpg",
                "image/jpeg",
                new byte[0] // 空文件
        );

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(new byte[0]);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));

        // When
        Mono<byte[]> result = removeBgService.removeBackground(emptyFile);

        // Then
        CompletableFuture<byte[]> future = result.toFuture();
        byte[] actualResult = future.get(5, TimeUnit.SECONDS);

        assertThat(actualResult).isEqualTo(new byte[0]);
    }

    @Test
    void removeBackground_WhenWebClientThrowsException_ShouldReturnError() {
        // Given
        byte[] imageData = "test image data".getBytes(StandardCharsets.UTF_8);
        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test-image.jpg",
                "image/jpeg",
                imageData
        );

        // Mock WebClient to throw exception
        when(webClient.post()).thenThrow(new RuntimeException("WebClient error"));

        // When & Then - 直接測試方法調用會拋出異常
        assertThatThrownBy(() -> removeBgService.removeBackground(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("WebClient error");
    }

    @Test
    void removeBackground_WhenDataBufferProcessingFails_ShouldHandleGracefully() {
        // Given
        byte[] imageData = "test image data".getBytes(StandardCharsets.UTF_8);
        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test-image.jpg",
                "image/jpeg",
                imageData
        );

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class))
                .thenReturn(Mono.error(new RuntimeException("DataBuffer processing error")));

        // When & Then
        Mono<byte[]> result = removeBgService.removeBackground(mockFile);

        assertThatThrownBy(() -> {
            CompletableFuture<byte[]> future = result.toFuture();
            future.get(5, TimeUnit.SECONDS);
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("DataBuffer processing error");
    }

    @Test
    void removeBackground_ShouldSetCorrectHeaders() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        byte[] imageData = "test image data".getBytes(StandardCharsets.UTF_8);
        byte[] processedImageData = "processed image".getBytes(StandardCharsets.UTF_8);

        MultipartFile mockFile = new MockMultipartFile(
                "image_file",
                "test.jpg",
                "image/jpeg",
                imageData
        );

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(processedImageData);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DataBuffer.class)).thenReturn(Mono.just(dataBuffer));

        // When
        Mono<byte[]> result = removeBgService.removeBackground(mockFile);
        CompletableFuture<byte[]> future = result.toFuture();
        future.get(5, TimeUnit.SECONDS);

        // Then - 驗證正確的 headers 和 URL 被調用
        verify(requestBodyUriSpec, times(1)).uri(testApiUrl);
        verify(requestBodySpec, times(1)).header("X-Api-Key", testApiKey);
        verify(requestBodySpec, times(1)).contentType(any());
    }
}