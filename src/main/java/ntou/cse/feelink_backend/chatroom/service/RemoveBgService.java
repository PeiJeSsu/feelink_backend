package ntou.cse.feelink_backend.chatroom.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class RemoveBgService {

    private static final Logger logger = LoggerFactory.getLogger(RemoveBgService.class);

    @Value("${removebg.api.key}")
    private String apiKey;

    @Value("${removebg.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public RemoveBgService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB
                .build();
    }

    public Mono<byte[]> removeBackground(MultipartFile file) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("image_file", file.getBytes())
                    .header("Content-Disposition", "form-data; name=image_file; filename=" + file.getOriginalFilename());
            builder.part("size", "auto");

            return webClient.post()
                    .uri(apiUrl)
                    .header("X-Api-Key", apiKey)  // 使用 X-Api-Key 而不是 Authorization
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        logger.error("API錯誤響應: {}", errorBody);
                                        return Mono.error(new RuntimeException("API調用失敗: " + response.statusCode() + " - " + errorBody));
                                    }))
                    .bodyToMono(DataBuffer.class)
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return bytes;
                    })
                    .doOnError(error -> {
                        logger.error("RemoveBG API調用失敗: {}", error.getMessage(), error);
                    });

        } catch (IOException e) {
            return Mono.error(new RuntimeException("文件讀取失敗", e));
        }
    }

    public Mono<byte[]> removeBackgroundFromUrl(String imageUrl) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image_url", imageUrl);
        builder.part("size", "auto");

        return webClient.post()
                .uri(apiUrl)
                .header("X-Api-Key", apiKey)  // 使用 X-Api-Key
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("API錯誤響應: {}", errorBody);
                                    return Mono.error(new RuntimeException("API調用失敗: " + response.statusCode() + " - " + errorBody));
                                }))
                .bodyToMono(DataBuffer.class)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .doOnError(error -> {
                    logger.error("RemoveBG API調用失敗: {}", error.getMessage(), error);
                });
    }

    public Mono<String> testApiKey() {

        return webClient.get()
                .uri(apiUrl)
                .header("X-Api-Key", apiKey)  // 使用 X-Api-Key
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("API密鑰測試失敗: {}", errorBody);
                                    return Mono.error(new RuntimeException("API密鑰無效: " + response.statusCode()));
                                }))
                .bodyToMono(String.class)
                .doOnSuccess(result -> logger.info("API密鑰測試成功: {}", result));
    }
}