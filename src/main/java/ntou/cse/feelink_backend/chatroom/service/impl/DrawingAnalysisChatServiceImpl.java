package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.service.ChatInputProcessor;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.google.genai.Chat;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.io.IOException;

@Service
public class DrawingAnalysisChatServiceImpl {
    private final ChatSessionRegistry registry;

    public DrawingAnalysisChatServiceImpl(ChatSessionRegistry registry) {
        this.registry = registry;
    }

    // 原本的非串流方法
    public String processChat(MultipartFile file, String userMessage, String sessionId) throws IOException {
        GenerateContentResponse response;
        Chat drawingAnalysisChatSession = registry.getSession(sessionId);

        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Part processedImage = ChatInputProcessor.processImage(file);

        try {
            if (processedImage != null) {
                Content content = Content.fromParts(
                        Part.fromText(processedText),
                        processedImage
                );
                response = drawingAnalysisChatSession.sendMessage(content);
            } else {
                response = drawingAnalysisChatSession.sendMessage(processedText);
            }
            return response.text();
        } catch (Exception e) {
            return "AI處理錯誤";
        }
    }
    public SseEmitter processChatStream(MultipartFile file, String userMessage, String sessionId) throws IOException {
        Chat chatSession = registry.getSession(sessionId);
        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Part processedImage = ChatInputProcessor.processImage(file);
        ResponseStream<GenerateContentResponse> rs;
        if (processedImage != null) {
            Content content = Content.fromParts(Part.fromText(processedText), processedImage);
            rs = chatSession.sendMessageStream(content);
        } else {
            rs = chatSession.sendMessageStream(processedText);
        }
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                for (GenerateContentResponse r : rs) {
                    String token = r.text();
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
        Chat chatSession = registry.getSession(sessionId, personality);
        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Part processedImage = ChatInputProcessor.processImage(file);

        ResponseStream<GenerateContentResponse> rs;
        if (processedImage != null) {
            Content content = Content.fromParts(Part.fromText(processedText), processedImage);
            rs = chatSession.sendMessageStream(content);
        } else {
            rs = chatSession.sendMessageStream(processedText);
        }

        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                for (GenerateContentResponse r : rs) {
                    String token = r.text();
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
