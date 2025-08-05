package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.service.ChatInputProcessor;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class DrawingAnalysisChatServiceImpl {
    private final ChatSessionRegistry registry;

    public DrawingAnalysisChatServiceImpl(ChatSessionRegistry registry) {
        this.registry = registry;
    }

    public String processChat(MultipartFile file, String userMessage,String sessionId) throws IOException {
        GenerateContentResponse response;
        ChatSession drawingAnalysisChatSession = registry.getSession(sessionId);

        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Object processedImage = ChatInputProcessor.processImage(file);

        try {
            if (processedImage != null) {
                response = drawingAnalysisChatSession.sendMessage(
                        ContentMaker.fromMultiModalData(processedImage, processedText)
                );
            } else {
                response = drawingAnalysisChatSession.sendMessage(processedText);
            }
            return ResponseHandler.getText(response);
        } catch (Exception e) {
            return "AI處理錯誤";
        }
    }

    public SseEmitter processChatStream(MultipartFile file, String userMessage, String sessionId) throws IOException {
        ChatSession chatSession = registry.getSession(sessionId);
        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Object processedImage = ChatInputProcessor.processImage(file);
        ResponseStream<GenerateContentResponse> rs;
        if (processedImage != null) {
            rs = chatSession.sendMessageStream(
                    ContentMaker.fromMultiModalData(processedImage, processedText));
        } else {
            rs = chatSession.sendMessageStream(processedText);
        }
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                for (GenerateContentResponse r : rs) {
                    String token = ResponseHandler.getText(r);
                    emitter.send(SseEmitter.event().data(token));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    public SseEmitter processChatStreamWithPersonality(MultipartFile file, String userMessage, String sessionId, String personality) throws IOException {
        ChatSession chatSession = registry.getSession(sessionId, personality);
        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Object processedImage = ChatInputProcessor.processImage(file);

        ResponseStream<GenerateContentResponse> rs;
        if (processedImage != null) {
            rs = chatSession.sendMessageStream(
                    ContentMaker.fromMultiModalData(processedImage, processedText));
        } else {
            rs = chatSession.sendMessageStream(processedText);
        }

        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                for (GenerateContentResponse r : rs) {
                    String token = ResponseHandler.getText(r);
                    emitter.send(SseEmitter.event().data(token));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}
