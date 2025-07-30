package ntou.cse.feelink_backend.chatroom.service;

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
            // Removed logging of API key to prevent security risks.

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
                                        System.err.println("API錯誤響應: " + errorBody);
                                        return Mono.error(new RuntimeException("API調用失敗: " + response.statusCode() + " - " + errorBody));
                                    }))
                    .bodyToMono(DataBuffer.class)
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return bytes;
                    })
                    .doOnError(error -> {
                        System.err.println("RemoveBG API調用失敗: " + error.getMessage());
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
                                    System.err.println("API錯誤響應: " + errorBody);
                                    return Mono.error(new RuntimeException("API調用失敗: " + response.statusCode() + " - " + errorBody));
                                }))
                .bodyToMono(DataBuffer.class)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .doOnError(error -> {
                    System.err.println("RemoveBG API調用失敗: " + error.getMessage());
                });
    }

    public Mono<String> testApiKey() {
        System.out.println("測試API密鑰中...");
        System.out.println("API密鑰長度: " + apiKey.length());

        return webClient.get()
                .uri("https://api.remove.bg/v1.0/account")
                .header("X-Api-Key", apiKey)  // 使用 X-Api-Key
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("API密鑰測試失敗: " + errorBody);
                                    return Mono.error(new RuntimeException("API密鑰無效: " + response.statusCode()));
                                }))
                .bodyToMono(String.class)
                .doOnSuccess(result -> System.out.println("API密鑰測試成功: " + result));
    }
}