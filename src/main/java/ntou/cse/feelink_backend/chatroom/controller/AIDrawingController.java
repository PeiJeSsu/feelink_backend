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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@RestController
public class AIDrawingController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RemoveBgService removeBgService;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/generate/simple")
    public Mono<ResponseEntity<AIDrawingResponse>> generateImage(@RequestBody AIDrawingRequest request) {
        try {
            String prompt = request.getText();
            String canvasData = request.getImageData();
            String chatroomId = request.getChatroomId();
            boolean removeBackground = request.isRemoveBackground();
            String mode = request.getMode();

            if (canvasData != null && !canvasData.isEmpty() && chatroomId != null) {
                ChatMessage userDrawingMessage = new ChatMessage();
                userDrawingMessage.setChatroomId(chatroomId);
                userDrawingMessage.setIsUser(true);
                userDrawingMessage.setIsDrawingData(true);
                userDrawingMessage.setContent(canvasData);
                userDrawingMessage.setSentAt(new Date());
                chatMessageService.createMessage(userDrawingMessage);
            }

            if (prompt != null && !prompt.trim().isEmpty() && chatroomId != null &&
                    !prompt.equals("請根據這張圖片生成新的內容")) {
                ChatMessage userTextMessage = new ChatMessage();
                userTextMessage.setChatroomId(chatroomId);
                userTextMessage.setIsUser(true);
                userTextMessage.setIsDrawingData(false);
                userTextMessage.setContent(prompt);
                userTextMessage.setSentAt(new Date());
                chatMessageService.createMessage(userTextMessage);
            }

            if (prompt == null || prompt.trim().isEmpty()) {
                prompt = "請根據這張圖片生成新的內容";
            }

            GeminiResponse geminiResponse = geminiService.generateImage(prompt, canvasData, mode);

            if (removeBackground && geminiResponse.isSuccess() && geminiResponse.getImageData() != null) {
                return processWithBackgroundRemoval(geminiResponse, chatroomId);
            } else {
                saveAIResponse(geminiResponse, chatroomId);
                return Mono.just(ResponseEntity.ok(new AIDrawingResponse(true, geminiResponse)));
            }

        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AIDrawingResponse(false, e.getMessage())));
        }
    }

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
            try {
                if (canvasData != null && !canvasData.isEmpty() && chatroomId != null) {
                    ChatMessage userDrawingMessage = new ChatMessage();
                    userDrawingMessage.setChatroomId(chatroomId);
                    userDrawingMessage.setIsUser(true);
                    userDrawingMessage.setIsDrawingData(true);
                    userDrawingMessage.setContent(canvasData);
                    userDrawingMessage.setSentAt(new Date());
                    chatMessageService.createMessage(userDrawingMessage);
                }

                if (prompt != null && !prompt.trim().isEmpty() && chatroomId != null &&
                        !prompt.equals("請根據這張圖片生成新的內容")) {
                    ChatMessage userTextMessage = new ChatMessage();
                    userTextMessage.setChatroomId(chatroomId);
                    userTextMessage.setIsUser(true);
                    userTextMessage.setIsDrawingData(false);
                    userTextMessage.setContent(prompt);
                    userTextMessage.setSentAt(new Date());
                    chatMessageService.createMessage(userTextMessage);
                }

                ChatMessage aiTextMsg = new ChatMessage();
                aiTextMsg.setChatroomId(chatroomId);
                aiTextMsg.setIsUser(false);
                aiTextMsg.setIsDrawingData(false);
                aiTextMsg.setContent("");
                aiTextMsg.setSentAt(new Date());
                ChatMessage savedAiTextMsg = chatMessageService.createMessage(aiTextMsg);

                geminiService.generateImageStream(prompt, canvasData,
                        token -> {
                            fullTextResponse.append(token);
                            try { emitter.send(SseEmitter.event().data(token)); } catch (Exception ignored) {}
                        },
                        imageData -> {
                            try {
                                emitter.send(SseEmitter.event().name("image").data(imageData));
                                if (imageData != null && !imageData.trim().isEmpty() && chatroomId != null) {
                                    ChatMessage aiImageMessage = new ChatMessage();
                                    aiImageMessage.setChatroomId(chatroomId);
                                    aiImageMessage.setIsUser(false);
                                    aiImageMessage.setIsDrawingData(true);
                                    aiImageMessage.setContent(imageData);
                                    aiImageMessage.setSentAt(new Date());
                                    chatMessageService.createMessage(aiImageMessage);
                                }
                            } catch (Exception ignored) {}
                        },
                        () -> {
                            try {
                                Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(savedAiTextMsg.getMessageId());
                                aiMessageOpt.ifPresent(aiMessage -> {
                                    aiMessage.setContent(fullTextResponse.toString());
                                    chatMessageService.updateMessage(aiMessage);
                                });
                                emitter.send(SseEmitter.event().name("complete").data("Generation completed"));
                                emitter.complete();
                            } catch (Exception e) { emitter.completeWithError(e); }
                        },
                        error -> {
                            try {
                                Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(savedAiTextMsg.getMessageId());
                                aiMessageOpt.ifPresent(aiMessage -> {
                                    String errorContent = fullTextResponse.length() > 0 ?
                                            fullTextResponse.toString() + "\n[錯誤: " + error.getMessage() + "]" :
                                            "AI處理錯誤: " + error.getMessage();
                                    aiMessage.setContent(errorContent);
                                    chatMessageService.updateMessage(aiMessage);
                                });
                            } catch (Exception ignored) {}
                            emitter.completeWithError(error);
                        });

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    private Mono<ResponseEntity<AIDrawingResponse>> processWithBackgroundRemoval(GeminiResponse geminiResponse, String chatroomId) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(geminiResponse.getImageData());

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image", "generated_image.png", "image/png", imageBytes
            );

            return removeBgService.removeBackground(imageFile)
                    .map(removedBgBytes -> {
                        String removedBgBase64 = Base64.getEncoder().encodeToString(removedBgBytes);
                        geminiResponse.setImageData(removedBgBase64);
                        saveAIResponse(geminiResponse, chatroomId);
                        return ResponseEntity.ok(new AIDrawingResponse(true, geminiResponse));
                    })
                    .onErrorResume(error -> {
                        saveAIResponse(geminiResponse, chatroomId);
                        return Mono.just(ResponseEntity.ok(new AIDrawingResponse(true, geminiResponse)));
                    });

        } catch (Exception e) {
            saveAIResponse(geminiResponse, chatroomId);
            return Mono.just(ResponseEntity.ok(new AIDrawingResponse(true, geminiResponse)));
        }
    }

    private void saveAIResponse(GeminiResponse geminiResponse, String chatroomId) {
        if (chatroomId == null) return;

        try {
            if (geminiResponse.getMessage() != null && !geminiResponse.getMessage().trim().isEmpty()) {
                ChatMessage aiTextMessage = new ChatMessage();
                aiTextMessage.setChatroomId(chatroomId);
                aiTextMessage.setIsUser(false);
                aiTextMessage.setIsDrawingData(false);
                aiTextMessage.setContent(geminiResponse.getMessage());
                aiTextMessage.setSentAt(new Date());
                chatMessageService.createMessage(aiTextMessage);
            }

            if (geminiResponse.getImageData() != null && !geminiResponse.getImageData().trim().isEmpty()) {
                ChatMessage aiImageMessage = new ChatMessage();
                aiImageMessage.setChatroomId(chatroomId);
                aiImageMessage.setIsUser(false);
                aiImageMessage.setIsDrawingData(true);
                aiImageMessage.setContent(geminiResponse.getImageData());
                aiImageMessage.setSentAt(new Date());
                chatMessageService.createMessage(aiImageMessage);
            }
        } catch (Exception ignored) {}
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Drawing service is running");
    }
}
