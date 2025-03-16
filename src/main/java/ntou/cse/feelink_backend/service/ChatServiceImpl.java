package ntou.cse.feelink_backend.service;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import ntou.cse.feelink_backend.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatSession chatSession;
    private final ImageProcessor imageProcessor;
    private final TextProcessor textProcessor;

    public ChatServiceImpl(ChatSession chatSession, ImageProcessor imageProcessor, TextProcessor textProcessor) {
        this.chatSession = chatSession;
        this.imageProcessor = imageProcessor;
        this.textProcessor = textProcessor;
    }
  
    // 記憶體儲存（可以替換成數據庫儲存庫）
    private final Map<String, List<ChatMessage>> chatHistory = new HashMap<>();

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        String sessionId = message.getSessionId();
        chatHistory.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        return message;
    }

    @Override
    public List<ChatMessage> getMessagesBySessionId(String sessionId) {
        return chatHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    @Override
    public String processChat(MultipartFile file, String userMessage) throws IOException {
        GenerateContentResponse response;

        String processedText = textProcessor.processText(userMessage);
        Object processedImage = null;

        if (file != null && !file.isEmpty()) {
            processedImage = imageProcessor.processImage(file);
        }

        try {
            if (processedImage != null) {
                // 處理圖片和文字
                response = chatSession.sendMessage(
                        ContentMaker.fromMultiModalData(processedImage, processedText)
                );
            } else {
                // 只處理文字
                response = chatSession.sendMessage(processedText);
            }
            return ResponseHandler.getText(response);
        } catch (Exception e) {
            return "AI處理錯誤: " + e.getMessage();
        }
    }
}
