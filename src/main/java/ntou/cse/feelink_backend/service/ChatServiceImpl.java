package ntou.cse.feelink_backend.service;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ChatServiceImpl implements ChatService{
    private final ChatSession chatSession;
    private final ImageProcessor imageProcessor;
    private final TextProcessor textProcessor;

    public ChatServiceImpl(ChatSession chatSession, ImageProcessor imageProcessor, TextProcessor textProcessor) {
        this.chatSession = chatSession;
        this.imageProcessor = imageProcessor;
        this.textProcessor = textProcessor;
    }

    @Override
    public String processChat(MultipartFile file, String userMessage) throws IOException {
        GenerateContentResponse response;

        String processedText = textProcessor.processText(userMessage);
        Object processedImage = imageProcessor.processImage(file);

        if (processedImage != null) {
            response = chatSession.sendMessage(
                    ContentMaker.fromMultiModalData(processedImage, processedText)
            );
        } else {
            response = chatSession.sendMessage(processedText);
        }
        return ResponseHandler.getText(response);
    }
}
