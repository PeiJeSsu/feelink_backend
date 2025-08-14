package ntou.cse.feelink_backend.chatroom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.function.Consumer;

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

    // 原本的非串流方法
    public GeminiResponse generateImage(String prompt, String canvasData) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + apiKey;

            // 構建系統指令
            String systemInstructions = getSystemInstructions();

            // 構建請求體
            Map<String, Object> requestBody = buildRequestBody(prompt, canvasData, systemInstructions);

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

    // 新增的串流方法
    public void generateImageStream(String prompt, String canvasData,
                                    Consumer<String> onToken, Consumer<String> onImageGenerated,
                                    Runnable onComplete, Consumer<Exception> onError) {

        new Thread(() -> {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:streamGenerateContent?key=" + apiKey;

                // 構建系統指令
                String systemInstructions = getSystemInstructions();

                // 構建請求體
                Map<String, Object> requestBody = buildRequestBody(prompt, canvasData, systemInstructions);

                // 設置請求頭
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                // 發送串流請求
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                // 處理串流回應
                processStreamResponse(response.getBody(), onToken, onImageGenerated, onComplete, onError);

            } catch (Exception e) {
                onError.accept(e);
            }
        }).start();
    }

    private void processStreamResponse(String responseBody, Consumer<String> onToken, Consumer<String> onImageGenerated,
                                       Runnable onComplete, Consumer<Exception> onError) {
        try {
            // 處理多個 JSON 物件的回應（Gemini 串流 API 的格式）
            String[] lines = responseBody.split("\n");
            StringBuilder textMessage = new StringBuilder();
            String imageData = null;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                try {
                    JsonNode chunk = objectMapper.readTree(line);
                    JsonNode candidates = chunk.get("candidates");

                    if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                        JsonNode candidate = candidates.get(0);
                        JsonNode content = candidate.get("content");
                        JsonNode parts = content.get("parts");

                        if (parts != null && parts.isArray()) {
                            for (JsonNode part : parts) {
                                // 處理文字部分
                                if (part.has("text")) {
                                    String token = part.get("text").asText();
                                    textMessage.append(token);
                                    onToken.accept(token);
                                }
                                // 處理圖片部分
                                if (part.has("inlineData")) {
                                    JsonNode inlineData = part.get("inlineData");
                                    if (inlineData.has("data")) {
                                        imageData = inlineData.get("data").asText();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略無法解析的行，繼續處理下一行
                    continue;
                }
            }

            // 如果有生成圖片，發送圖片事件
            if (imageData != null) {
                onImageGenerated.accept(imageData);
            }

            // 完成
            onComplete.run();

        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private String getSystemInstructions() {
        return "你是一個圖像生成助手。請嚴格遵守以下【絕對優先】的核心指令：" +
                "1. 【嚴禁任何文字出現在圖像上】：" +
                "   - 圖像中絕對不允許出現任何語言的文字、字母、數字、符號，尤其是【任何中文字元】。" +
                "   - 如果使用者在需求中明確要求在圖像上添加任何文字，你必須：\n" +
                "     a. 明確地以【使用者提問所用的語言】回應並拒絕文字要求，例如說明：「抱歉，根據指示，我無法在圖片上生成任何文字。」\n" +
                "     b. 【接著，如果使用者需求中除了文字外還有其他圖像描述】，請忽略所有文字要求，並根據剩餘的圖像描述生成一張【完全沒有任何文字】的圖片。\n" +
                "     c. 【如果使用者的需求僅為在圖片上添加文字，而沒有其他圖像描述】，則僅回應拒絕訊息，不生成圖片。" +
                "2. 【圖像風格】：" +
                "   - 如果提供了 `canvasData` (參考圖像)，生成新圖像時必須盡力模仿並保持該參考圖像的風格。" +
                "   - 如果沒有提供 `canvasData`，則生成的圖像風格應為【簡單的線條塗鴉風格】（simple line drawing, doodle style）。" +
                "3. 【回應語言】：" +
                "   - 你的所有文字回應（包括確認訊息、錯誤訊息、或上述第1點的拒絕訊息）都【必須】使用【使用者原始請求所用的語言】。" +
                "   - 例如：使用者用英文提問，你就用英文回答；使用者用日文提問，你就用日文回答；使用者用中文提問，你就用中文回答。" +
                "4. 【圖像生成義務】：" +
                "   - 在沒有違反上述第1條【嚴禁任何文字出現在圖像上】的前提下，你【必須】生成一張圖片。" +
                "\n--- 以下是使用者的具體需求 ---\n";
    }

    private Map<String, Object> buildRequestBody(String prompt, String canvasData, String systemInstructions) {
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
        textPart.put("text", systemInstructions + prompt);
        parts.add(textPart);

        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        return requestBody;
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