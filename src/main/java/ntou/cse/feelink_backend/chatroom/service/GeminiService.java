package ntou.cse.feelink_backend.chatroom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GeminiResponse generateImage(String prompt, String canvasData, String mode) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-preview-image-generation:generateContent?key=" + apiKey;
            String systemInstructions = "";
            if ("drawing".equals(mode)) {
                systemInstructions = String.format("""
                    請詳細分析我上傳圖片的藝術風格（包含線條、色彩、筆觸和整體氛圍等），然後，請完全採用這種分析出來的風格來編輯原圖。
                    如果無法從圖片中看出風格，例如是一張空白的圖片，則生成的圖像風格應為「簡單的線條塗鴉風格」。
                    你的任務如下：
                    - 如果我要求添加元素，你就只畫上我指定的東西。
                    - 如果我要求移除元素，你就只刪除我指定的東西。
                    - 如果我要求更改現有元素的位置、大小或特徵等，你就依照我的描述進行修改。
                    %s
                    除了我明確指示要添加、移除或更改的部分之外，請務必完整保留原圖的所有其他內容。維持一樣的背景。
                    請一定要生成圖片，並且不要在圖片上生成文字，請用繁體中文回答。
                    """, prompt);
            }
            else if ("generateObject".equals(mode)) {
                systemInstructions = String.format("""
                    請詳細分析我上傳圖片的藝術風格（包含線條、色彩、筆觸和整體氛圍等）。然後，請完全採用這種分析出來的風格。
                    如果無法從圖片中看出風格，例如是一張空白的圖片，則生成的圖像風格應為「簡單的線條塗鴉風格」。
                    %s。
                    圖片中只能有我要你畫的東西，背景必須是純白色，不能有任何其他元素。
                    請一定要生成圖片，並且不要在圖片上生成文字，請用繁體中文回答。
                    """, prompt);
            }

            // 構建請求體
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseModalities", Arrays.asList("Text", "Image"));
            requestBody.put("generationConfig", generationConfig);

            // 構建內容部分
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");

            List<Map<String, Object>> parts = new ArrayList<>();

            // 如果有畫布數據，添加圖片部分
            if (canvasData != null && !canvasData.isEmpty()) {
                Map<String, Object> imagePart = new HashMap<>();
                Map<String, Object> inlineData = new HashMap<>();
                inlineData.put("mimeType", "image/png");
                inlineData.put("data", canvasData);
                imagePart.put("inlineData", inlineData);
                parts.add(imagePart);
            }

            // 添加文本部分
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", systemInstructions);
            parts.add(textPart);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 設置請求頭
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 發送請求
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 解析回應
            return parseGeminiResponse(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating image: " + e.getMessage(), e);
        }
    }

    private GeminiResponse parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.get("candidates");

            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode candidate = candidates.get(0);
                JsonNode content = candidate.get("content");
                JsonNode parts = content.get("parts");

                GeminiResponse result = new GeminiResponse();
                result.setSuccess(true);
                StringBuilder messageBuilder = new StringBuilder();

                if (parts != null && parts.isArray()) {
                    for (JsonNode part : parts) {
                        if (part.has("text")) {
                            messageBuilder.append(part.get("text").asText());
                        }
                        if (part.has("inlineData")) {
                            JsonNode inlineData = part.get("inlineData");
                            if (inlineData.has("data")) {
                                result.setImageData(inlineData.get("data").asText());
                            }
                        }
                    }
                }

                result.setMessage(messageBuilder.toString());
                return result;
            }

            throw new RuntimeException("Invalid response format from Gemini API");

        } catch (Exception e) {
            throw new RuntimeException("Error parsing Gemini response: " + e.getMessage(), e);
        }
    }
}