package ntou.cse.feelink_backend.chatroom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import java.io.IOException;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatSession chatSession;

    @Autowired
    public ChatServiceImpl(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    @Override
    public String processChat(MultipartFile file, String userMessage) throws IOException {
        GenerateContentResponse response;

        String processedText = processText(userMessage);
        Object processedImage = (file != null && !file.isEmpty()) ? processImage(file) : null;

        try {
            if (processedImage != null) {
                response = chatSession.sendMessage(
                        ContentMaker.fromMultiModalData(processedImage, processedText)
                );
            } else {
                response = chatSession.sendMessage(processedText);
            }
            return ResponseHandler.getText(response);
        } catch (Exception e) {
            return "AI處理錯誤";
        }
    }

    private String processText(String userMessage){
        if (userMessage == null || userMessage.isBlank()) {return "請分析這張圖片";}
        return userMessage;
    }

    private Object processImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) { return null; }
        return PartMaker.fromMimeTypeAndData(file.getContentType(), file.getBytes());
    }
}
