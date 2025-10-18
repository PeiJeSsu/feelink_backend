package ntou.cse.feelink_backend.chatroom.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final Client client;

    public GeminiService(Client client) {
        this.client = client;
    }

    public GeminiResponse generateImage(String prompt, String canvasData, String mode) {
        try {
            String systemInstructions = buildPromptByMode(prompt, mode);
            byte[] imageBytes= null;
            if (canvasData != null && !canvasData.isEmpty()) {
                String base64Data = canvasData;
                if (base64Data.startsWith("data:image/")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                imageBytes = Base64.getDecoder().decode(base64Data);
            }

            Content content = Content.fromParts(
                    Part.fromText(systemInstructions+"\n一定要回應圖片，並且訴說原先給的圖片畫得如何，正向回應。"),
                    Part.fromBytes(imageBytes,"image/jpeg")
            );
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseModalities("TEXT", "IMAGE")
                    .candidateCount(1)
                    .systemInstruction(Content.fromParts(Part.fromText(systemInstructions)))
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash-image",
                    content,
                    config
            );
            return convertToGeminiResponse(response);

        } catch (Exception e) {
            logger.error("圖片生成失敗: {}", e.getMessage(), e);

            GeminiResponse result = new GeminiResponse();
            result.setSuccess(false);
            result.setError("圖片生成失敗: " + e.getMessage());
            return result;
        }
    }

    private String buildPromptByMode(String prompt, String mode) {
        if ("drawing".equals(mode)) {
            return String.format("""
                    你要 %s。
                    我明確指示要添加、移除或更改的部分之外，請務必完整保留原圖的所有其他內容，其餘部分保持不變。
                    一定要回應圖片，不可生成文字，用繁體中文回答。
                    """, prompt);
        }
        else if ("generateObject".equals(mode)) {
            return String.format("""
                    參考圖片中的元素,生成一個獨立的%s物件。
                            要求:
                            - 只生成物件本身,不包含任何背景或其他元素
                            - 一定要回應圖片，且不要在圖片上生成文字，用繁體中文回答。
                    """, prompt);
        } else {
            return prompt + "，生成圖片並用繁體中文回應";
        }
    }

    private GeminiResponse convertToGeminiResponse(GenerateContentResponse response) {
        GeminiResponse result = new GeminiResponse();
        try {
            StringBuilder textBuilder = new StringBuilder();
            boolean hasImage = false;
            if (response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                var candidate = response.candidates().get().get(0);

                if (candidate.content().isPresent()) {
                    var content = candidate.content().get();

                    if (content.parts().isPresent()) {
                        var parts = content.parts().get();
                        for (var part : parts) {
                            if (part.text().isPresent()) {
                                textBuilder.append(part.text().get());
                            }
                            if (part.inlineData().isPresent()) {
                                var inlineData = part.inlineData().get();

                                if (inlineData.data().isPresent()) {
                                    byte[] imageData = inlineData.data().get();

                                    if (imageData != null && imageData.length > 0) {
                                        String base64Data = Base64.getEncoder().encodeToString(imageData);
                                        result.setImageData(base64Data);
                                        hasImage = true;
                                        logger.info(" 成功取得圖片，大小: {} bytes", imageData.length);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String textContent = textBuilder.toString();
            result.setMessage(textContent.isEmpty() ? "圖片處理完成" : textContent);
            result.setSuccess(true);
            logger.info("轉換完成 - 文字: {}, 圖片: {}", !textContent.isEmpty(), hasImage);
        } catch (Exception e) {
            logger.error("處理回應失敗: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setError("處理回應失敗: " + e.getMessage());
        }

        return result;
    }
}
