package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.AIDrawingRequest;
import ntou.cse.feelink_backend.chatroom.model.AIDrawingResponse;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import ntou.cse.feelink_backend.chatroom.service.GeminiService;
import ntou.cse.feelink_backend.chatroom.service.RemoveBgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mock.web.MockMultipartFile;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;

@RestController
public class AIDrawingController {

    private static final Logger logger = LoggerFactory.getLogger(AIDrawingController.class);

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RemoveBgService removeBgService;

    @PostMapping("/generate")
    public Mono<ResponseEntity<AIDrawingResponse>> generateImage(@RequestBody AIDrawingRequest request) {
        try {
            String prompt = request.getText();
            String canvasData = request.getImageData();
            boolean removeBackground = request.isRemoveBackground();
            
            if (prompt == null || prompt.trim().isEmpty()) {
                prompt = "請根據這張圖片生成新的內容";
            }

            GeminiResponse geminiResponse = geminiService.generateImage(prompt, canvasData);

            if (removeBackground && geminiResponse.isSuccess() && geminiResponse.getImageData() != null) {
                return processWithBackgroundRemoval(geminiResponse);
            } else {
                AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                return Mono.just(ResponseEntity.ok(response));
            }

        } catch (Exception e) {
            logger.error("圖片生成失敗: {}", e.getMessage(), e);
            AIDrawingResponse errorResponse = new AIDrawingResponse(false, e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        }
    }

    private Mono<ResponseEntity<AIDrawingResponse>> processWithBackgroundRemoval(GeminiResponse geminiResponse) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(geminiResponse.getImageData());

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "generated_image.png",
                    "image/png",
                    imageBytes
            );

            return removeBgService.removeBackground(imageFile)
                    .map(removedBgBytes -> {
                        String removedBgBase64 = Base64.getEncoder().encodeToString(removedBgBytes);
                        geminiResponse.setImageData(removedBgBase64);

                        logger.info("去背處理成功完成");
                        AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                        return ResponseEntity.ok(response);
                    })
                    .onErrorResume(error -> {
                        logger.error("去背處理失敗: {}", error.getMessage(), error);

                        AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                        return Mono.just(ResponseEntity.ok(response));
                    });

        } catch (Exception e) {
            logger.error("去背前處理失敗: {}", e.getMessage(), e);
            AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
            return Mono.just(ResponseEntity.ok(response));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Drawing service is running");
    }
}