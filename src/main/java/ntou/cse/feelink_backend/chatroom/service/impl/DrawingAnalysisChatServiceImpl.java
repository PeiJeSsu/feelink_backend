package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.service.ChatInputProcessor;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;

import java.io.IOException;
import java.util.function.Consumer;

@Service
public class DrawingAnalysisChatServiceImpl {
    private final ChatSessionRegistry registry;

    public DrawingAnalysisChatServiceImpl(ChatSessionRegistry registry) {
        this.registry = registry;
    }

    // 原本的非串流方法
    public String processChat(MultipartFile file, String userMessage, String sessionId) throws IOException {
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

    // 新增的串流方法
    public void processChatStream(MultipartFile file, String userMessage, String sessionId,
                                  Consumer<String> onToken, Runnable onComplete, Consumer<Exception> onError) throws IOException {

        ChatSession drawingAnalysisChatSession = registry.getSession(sessionId);
        String processedText = ChatInputProcessor.processText(userMessage, "請分析這張圖片");
        Object processedImage = ChatInputProcessor.processImage(file);

        try {
            ResponseStream<GenerateContentResponse> responseStream;

            if (processedImage != null) {
                responseStream = drawingAnalysisChatSession.sendMessageStream(
                        ContentMaker.fromMultiModalData(processedImage, processedText)
                );
            } else {
                responseStream = drawingAnalysisChatSession.sendMessageStream(processedText);
            }

            // 處理串流回應
            for (GenerateContentResponse response : responseStream) {
                String token = ResponseHandler.getText(response);
                if (token != null && !token.isEmpty()) {
                    onToken.accept(token);
                }
            }

            // 串流完成
            onComplete.run();

        } catch (Exception e) {
            onError.accept(e);
        }
    }
}