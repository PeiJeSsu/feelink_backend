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

            // 如果沒有提供 prompt，使用預設訊息
            if (prompt == null || prompt.trim().isEmpty()) {
                prompt = "請根據這張圖片生成新的內容";
            }

            // 調用 Gemini 服務生成圖片
            GeminiResponse geminiResponse = geminiService.generateImage(prompt, canvasData);

            // 如果需要去背且生成成功
            if (removeBackground && geminiResponse.isSuccess() && geminiResponse.getImageData() != null) {
                return processWithBackgroundRemoval(geminiResponse);
            } else {
                // 不需要去背，直接返回結果
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

            // 使用非阻塞的響應式處理
            return removeBgService.removeBackground(imageFile)
                    .map(removedBgBytes -> {
                        // 去背成功
                        String removedBgBase64 = Base64.getEncoder().encodeToString(removedBgBytes);
                        geminiResponse.setImageData(removedBgBase64);

                        // 更新訊息
                        String originalMessage = geminiResponse.getMessage();
                        if (originalMessage != null && !originalMessage.isEmpty()) {
                            geminiResponse.setMessage(originalMessage + " (背景已移除)");
                        } else {
                            geminiResponse.setMessage("圖片生成成功，背景已移除");
                        }

                        logger.info("去背處理成功完成");
                        AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                        return ResponseEntity.ok(response);
                    })
                    .onErrorResume(error -> {
                        // 去背失敗，返回原始圖片
                        logger.error("去背處理失敗: {}", error.getMessage(), error);

                        String originalMessage = geminiResponse.getMessage();
                        if (originalMessage != null && !originalMessage.isEmpty()) {
                            geminiResponse.setMessage(originalMessage + " (去背處理失敗，顯示原始圖片)");
                        } else {
                            geminiResponse.setMessage("圖片生成成功，但去背處理失敗");
                        }

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