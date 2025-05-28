package ntou.cse.feelink_backend.chatroom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import java.io.IOException;

@Service
public class DrawingAnalysisChatServiceImpl {
    private final ChatSession drawingAnalysisChatSession;

    @Autowired
    public DrawingAnalysisChatServiceImpl(ChatSession drawingAnalysisChatSession) {
        this.drawingAnalysisChatSession = drawingAnalysisChatSession;
    }

    public String processChat(MultipartFile file, String userMessage) throws IOException {
        GenerateContentResponse response;

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
}
