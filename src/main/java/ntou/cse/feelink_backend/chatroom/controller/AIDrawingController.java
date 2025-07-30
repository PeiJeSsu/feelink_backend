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
    public ResponseEntity<AIDrawingResponse> generateImage(@RequestBody AIDrawingRequest request) {
        try {
            String prompt = request.getText();
            String canvasData = request.getImageData();
            boolean removeBackground = request.isRemoveBackground();

            if (prompt == null || prompt.trim().isEmpty()) {
                prompt = "請根據這張圖片生成新的內容";
            }

            GeminiResponse geminiResponse = geminiService.generateImage(prompt, canvasData);

            if (removeBackground && geminiResponse.isSuccess() && geminiResponse.getImageData() != null) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(geminiResponse.getImageData());

                    MockMultipartFile imageFile = new MockMultipartFile(
                            "image",
                            "generated_image.png",
                            "image/png",
                            imageBytes
                    );

                    byte[] removedBgBytes = removeBgService.removeBackground(imageFile).block();

                    if (removedBgBytes != null) {
                        String removedBgBase64 = Base64.getEncoder().encodeToString(removedBgBytes);
                        geminiResponse.setImageData(removedBgBase64);
                        geminiResponse.setMessage(geminiResponse.getMessage() );
                    }
                } catch (Exception bgError) {
                    logger.error("去背處理失敗: {}", bgError.getMessage(), bgError);
                    geminiResponse.setMessage(geminiResponse.getMessage() );
                }
            }
            AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            AIDrawingResponse errorResponse = new AIDrawingResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Drawing service is running");
    }
}