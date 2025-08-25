package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.AIDrawingRequest;
import ntou.cse.feelink_backend.chatroom.model.AIDrawingResponse;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.GeminiService;
import ntou.cse.feelink_backend.chatroom.service.RemoveBgService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.mock.web.MockMultipartFile;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@RestController
public class AIDrawingController {

    private static final Logger logger = LoggerFactory.getLogger(AIDrawingController.class);

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RemoveBgService removeBgService;

    @Autowired
    private ChatMessageService chatMessageService;

    // 保留原本的非串流 API
    @PostMapping("/generate/simple")
    public Mono<ResponseEntity<AIDrawingResponse>> generateImage(@RequestBody AIDrawingRequest request) {
        try {
            String prompt = request.getText();
            String canvasData = request.getImageData();
            String chatroomId = request.getChatroomId(); // 從請求中獲取 chatroomId
            boolean removeBackground = request.isRemoveBackground();
            String mode = request.getMode();
            
            

            // 保存使用者的繪圖輸入 (畫布資料)
            if (canvasData != null && !canvasData.isEmpty() && chatroomId != null) {
                ChatMessage userDrawingMessage = new ChatMessage();
                userDrawingMessage.setChatroomId(chatroomId);
                userDrawingMessage.setIsUser(true);
                userDrawingMessage.setIsDrawingData(true);
                userDrawingMessage.setContent(canvasData);
                userDrawingMessage.setSentAt(new Date());

                chatMessageService.createMessage(userDrawingMessage);
                logger.info("已保存使用者畫布資料到 chatroomId: {}", chatroomId);
            }

            // 如果有文字提示，也保存為文字訊息
            if (prompt != null && !prompt.trim().isEmpty() && chatroomId != null &&
                    !prompt.equals("請根據這張圖片生成新的內容")) {
                ChatMessage userTextMessage = new ChatMessage();
                userTextMessage.setChatroomId(chatroomId);
                userTextMessage.setIsUser(true);
                userTextMessage.setIsDrawingData(false);
                userTextMessage.setContent(prompt);
                userTextMessage.setSentAt(new Date());

                chatMessageService.createMessage(userTextMessage);
                logger.info("已保存使用者文字訊息到 chatroomId: {}", chatroomId);
            }
            if (prompt == null || prompt.trim().isEmpty()) {
                prompt = "請根據這張圖片生成新的內容";
            }

            GeminiResponse geminiResponse = geminiService.generateImage(prompt, canvasData, mode);

            if (removeBackground && geminiResponse.isSuccess() && geminiResponse.getImageData() != null) {
                return processWithBackgroundRemoval(geminiResponse, chatroomId);
            } else {
                // 保存AI回應
                saveAIResponse(geminiResponse, chatroomId);
                AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                return Mono.just(ResponseEntity.ok(response));
            }

        } catch (Exception e) {
            logger.error("圖片生成失敗: {}", e.getMessage(), e);
            AIDrawingResponse errorResponse = new AIDrawingResponse(false, e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        }
    }

    // 新增串流 API
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateImageStream(@RequestBody AIDrawingRequest request) {
        final String prompt = request.getText() == null || request.getText().trim().isEmpty() ?
                "請根據這張圖片生成新的內容" : request.getText();
        final String canvasData = request.getImageData();
        final String chatroomId = request.getChatroomId();
        final boolean removeBackground = request.isRemoveBackground();

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        new Thread(() -> {
            StringBuilder fullTextResponse = new StringBuilder();
            String generatedImageData = null;

            try {
                // 保存使用者的繪圖輸入 (畫布資料)
                if (canvasData != null && !canvasData.isEmpty() && chatroomId != null) {
                    ChatMessage userDrawingMessage = new ChatMessage();
                    userDrawingMessage.setChatroomId(chatroomId);
                    userDrawingMessage.setIsUser(true);
                    userDrawingMessage.setIsDrawingData(true);
                    userDrawingMessage.setContent(canvasData);
                    userDrawingMessage.setSentAt(new Date());

                    chatMessageService.createMessage(userDrawingMessage);
                    logger.info("已保存使用者畫布資料到 chatroomId: {}", chatroomId);
                }

                // 如果有文字提示，也保存為文字訊息
                if (prompt != null && !prompt.trim().isEmpty() && chatroomId != null &&
                        !prompt.equals("請根據這張圖片生成新的內容")) {
                    ChatMessage userTextMessage = new ChatMessage();
                    userTextMessage.setChatroomId(chatroomId);
                    userTextMessage.setIsUser(true);
                    userTextMessage.setIsDrawingData(false);
                    userTextMessage.setContent(prompt);
                    userTextMessage.setSentAt(new Date());

                    chatMessageService.createMessage(userTextMessage);
                    logger.info("已保存使用者文字訊息到 chatroomId: {}", chatroomId);
                }

                // 建立 AI 文字回應的訊息記錄
                ChatMessage aiTextMsg = new ChatMessage();
                aiTextMsg.setChatroomId(chatroomId);
                aiTextMsg.setIsUser(false);
                aiTextMsg.setIsDrawingData(false);
                aiTextMsg.setContent("");
                aiTextMsg.setSentAt(new Date());
                ChatMessage savedAiTextMsg = chatMessageService.createMessage(aiTextMsg);

                // 調用串流生成圖片的服務
                geminiService.generateImageStream(prompt, canvasData,
                        // onToken callback - 處理文字回應
                        (token) -> {
                            fullTextResponse.append(token);
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (Exception e) {
                                logger.error("發送 SSE 文字資料失敗: {}", e.getMessage());
                            }
                        },
                        // onImageGenerated callback - 處理圖片生成
                        (imageData) -> {
                            try {
                                // 發送圖片生成完成的事件
                                emitter.send(SseEmitter.event().name("image").data(imageData));

                                // 保存AI生成的圖片
                                if (imageData != null && !imageData.trim().isEmpty() && chatroomId != null) {
                                    ChatMessage aiImageMessage = new ChatMessage();
                                    aiImageMessage.setChatroomId(chatroomId);
                                    aiImageMessage.setIsUser(false);
                                    aiImageMessage.setIsDrawingData(true);
                                    aiImageMessage.setContent(imageData);
                                    aiImageMessage.setSentAt(new Date());
                                    chatMessageService.createMessage(aiImageMessage);
                                    logger.info("已保存AI圖片回應到 chatroomId: {}", chatroomId);
                                }
                            } catch (Exception e) {
                                logger.error("處理圖片生成事件失敗: {}", e.getMessage());
                            }
                        },
                        // onComplete callback
                        () -> {
                            try {
                                // 更新文字回應到資料庫
                                Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(savedAiTextMsg.getMessageId());
                                if (aiMessageOpt.isPresent()) {
                                    ChatMessage aiMessage = aiMessageOpt.get();
                                    aiMessage.setContent(fullTextResponse.toString());
                                    chatMessageService.updateMessage(aiMessage);
                                }

                                // 發送完成事件
                                emitter.send(SseEmitter.event().name("complete").data("Generation completed"));
                                emitter.complete();
                            } catch (Exception e) {
                                logger.error("完成串流處理失敗: {}", e.getMessage());
                                emitter.completeWithError(e);
                            }
                        },
                        // onError callback
                        (error) -> {
                            logger.error("圖片生成串流錯誤: {}", error.getMessage());

                            // 更新錯誤訊息到資料庫
                            try {
                                Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(savedAiTextMsg.getMessageId());
                                if (aiMessageOpt.isPresent()) {
                                    ChatMessage aiMessage = aiMessageOpt.get();
                                    String errorContent = fullTextResponse.length() > 0 ?
                                            fullTextResponse.toString() + "\n[錯誤: " + error.getMessage() + "]" :
                                            "AI處理錯誤: " + error.getMessage();
                                    aiMessage.setContent(errorContent);
                                    chatMessageService.updateMessage(aiMessage);
                                }
                            } catch (Exception dbError) {
                                logger.error("更新錯誤訊息到資料庫失敗: {}", dbError.getMessage());
                            }

                            emitter.completeWithError(error);
                        }
                );

            } catch (Exception e) {
                logger.error("圖片生成串流初始化錯誤: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    private Mono<ResponseEntity<AIDrawingResponse>> processWithBackgroundRemoval(GeminiResponse geminiResponse, String chatroomId) {
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

                        // 保存處理後的AI回應
                        saveAIResponse(geminiResponse, chatroomId);

                        AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                        return ResponseEntity.ok(response);
                    })
                    .onErrorResume(error -> {
                        logger.error("去背處理失敗: {}", error.getMessage(), error);

                        // 即使去背失敗，也保存原始AI回應
                        saveAIResponse(geminiResponse, chatroomId);

                        AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
                        return Mono.just(ResponseEntity.ok(response));
                    });

        } catch (Exception e) {
            logger.error("去背前處理失敗: {}", e.getMessage(), e);

            // 保存原始AI回應
            saveAIResponse(geminiResponse, chatroomId);

            AIDrawingResponse response = new AIDrawingResponse(true, geminiResponse);
            return Mono.just(ResponseEntity.ok(response));
        }
    }

    /**
     * 保存AI的回應訊息
     */
    private void saveAIResponse(GeminiResponse geminiResponse, String chatroomId) {
        if (chatroomId == null) {
            logger.warn("chatroomId 為空，無法保存AI回應");
            return;
        }

        try {
            // 保存AI的文字回應（如果有）
            if (geminiResponse.getMessage() != null && !geminiResponse.getMessage().trim().isEmpty()) {
                ChatMessage aiTextMessage = new ChatMessage();
                aiTextMessage.setChatroomId(chatroomId);
                aiTextMessage.setIsUser(false);
                aiTextMessage.setIsDrawingData(false);
                aiTextMessage.setContent(geminiResponse.getMessage());
                aiTextMessage.setSentAt(new Date());

                chatMessageService.createMessage(aiTextMessage);
                logger.info("已保存AI文字回應到 chatroomId: {}", chatroomId);
            }

            // 保存AI生成的圖片（如果有）
            if (geminiResponse.getImageData() != null && !geminiResponse.getImageData().trim().isEmpty()) {
                ChatMessage aiImageMessage = new ChatMessage();
                aiImageMessage.setChatroomId(chatroomId);
                aiImageMessage.setIsUser(false);
                aiImageMessage.setIsDrawingData(true);
                aiImageMessage.setContent(geminiResponse.getImageData());
                aiImageMessage.setSentAt(new Date());

                chatMessageService.createMessage(aiImageMessage);
                logger.info("已保存AI圖片回應到 chatroomId: {}", chatroomId);
            }
        } catch (Exception e) {
            logger.error("保存AI回應失敗: {}", e.getMessage(), e);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Drawing service is running");
    }
}