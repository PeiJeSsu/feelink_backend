package ntou.cse.feelink_backend.chatroom.service;

import com.google.genai.Client;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EmotionAnalysisService {
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String aiModel = "gemini-2.5-flash-lite";
    @Autowired
    private ChatMessageService chatMessageService;

    public EmotionAnalysisService(Client client) {
        this.client = client;
    }
    public String getTaiwanTodayString() {
        ZoneId taiwanZone = ZoneId.of("Asia/Taipei");
        ZonedDateTime now = ZonedDateTime.now(taiwanZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return now.format(formatter);
    }

    public List<String> getTodayChatMessages(String chatroomId) {
        String today = getTaiwanTodayString();

        List<ChatMessage> todayMessages = chatMessageService.getTextMessagesByDate(chatroomId, today);

        List<String> chatContents = new ArrayList<>();
        for (ChatMessage msg : todayMessages) {
            if (msg.getIsUser()) {
                chatContents.add("使用者: " + msg.getContent());
            } else {
                chatContents.add("AI: " + msg.getContent());
            }
        }
        return chatContents;
    }

    public EmotionScores analyzeTodayEmotions(String chatroomId) {
        List<String> todayMessages = getTodayChatMessages(chatroomId);
        return analyzeEmotionsFromChatHistory(chatroomId, todayMessages);
    }

    public EmotionScores analyzeEmotionsFromChatHistory(String chatroomId, List<String> chatMessages) {
        try {
            Schema schema = Schema.builder()
                    .type("object")
                    .properties(ImmutableMap.<String, Schema>builder()
                            .put("難過", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("悲傷、沮喪的程度，0-10分")
                                    .build())
                            .put("喜悅", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("純粹快樂、愉悅的程度，0-10分")
                                    .build())
                            .put("興奮", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("驚喜、新鮮感的程度，0-10分")
                                    .build())
                            .put("焦慮", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("恐懼、不安、擔憂的程度，0-10分")
                                    .build())
                            .put("生氣", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("憤怒、暴躁的程度，0-10分")
                                    .build())
                            .put("平靜", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("平靜、冷靜的程度，0-10分")
                                    .build())
                            .put("期待", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("對未來期望、盼望的程度，0-10分")
                                    .build())
                            .put("樂觀", Schema.builder()
                                    .type(Type.Known.NUMBER)
                                    .description("積極態度、正面思考的程度，0-10分")
                                    .build())
                            .build())
                    .required(Arrays.asList("難過", "喜悅", "興奮", "焦慮", "生氣", "平靜", "期待", "樂觀"))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .responseSchema(schema)
                    .temperature(0.0f)
                    .topP(0.2f)
                    .candidateCount(1)
                    .maxOutputTokens(100)
                    .systemInstruction(Content.fromParts(Part.fromText("""
                            根據所有對話內容，為以下九種情緒給出 0-10 的數值評分：
                                - 難過：悲傷、失落、沮喪的程度
                                - 喜悅：純粹的快樂、愉悅、滿足感
                                - 興奮：意外、驚奇、新鮮感的程度
                                - 焦慮：恐懼、不安、擔憂的程度
                                - 生氣：憤怒、暴躁的程度
                                - 平靜：平靜、冷靜的程度
                                - 期待: 對未來的盼望、期望
                                - 樂觀: 積極態度、正面思考、希望
                                注意：
                                1. 綜合考慮整個對話的脈絡和情緒變化
                                2. 數值越高表示該情緒越強烈
                                3. 客觀分析，不要只看最後幾句話
                                4. 必須以JSON格式回應
                                5. 明確區分 積極態度 和 開心情緒""")))
                    .build();
            String prompt = buildChatAnalysisPrompt(chatMessages);

            GenerateContentResponse response = client.models.generateContent(
                    aiModel,
                    prompt,
                    config
            );

            JsonNode jsonNode = objectMapper.readTree(response.text());

            return new EmotionScores(
                    jsonNode.get("難過").asDouble(),
                    jsonNode.get("喜悅").asDouble(),
                    jsonNode.get("興奮").asDouble(),
                    jsonNode.get("焦慮").asDouble(),
                    jsonNode.get("生氣").asDouble(),
                    jsonNode.get("平靜").asDouble(),
                    jsonNode.get("期待").asDouble(),
                    jsonNode.get("樂觀").asDouble()
            );

        } catch (Exception e) {
            System.err.println("結構化情緒分析失敗: " + e.getMessage());
            return new EmotionScores(-1, -1,-1,-1,-1,-1,-1,-1); // 預設值
        }
    }
    private String buildChatAnalysisPrompt(List<String> chatMessages) {
        String conversationHistory = String.join("\n", chatMessages);

        return String.format("""
            分析以下完整的聊天記錄，評估使用者整體的情緒狀態：
            === 聊天記錄開始 ===
            %s
            === 聊天記錄結束 ===
            """, conversationHistory);
    }

    public static class EmotionScores {
        private final double sadness;
        private final double joy;
        private final double excited;
        private final double anxiety;
        private final double anger;
        private final double calm;
        private final double anticipation;
        private final double optimism;
        public EmotionScores(double sadness, double joy, double excited, double anxiety,
                             double anger, double calm, double anticipation, double optimism ) {
            this.sadness = sadness;
            this.joy = joy;
            this.excited = excited;
            this.anxiety = anxiety;
            this.anger = anger;
            this.calm = calm;
            this.anticipation = anticipation;
            this.optimism = optimism;
        }

        public double getSadness() { return sadness; }
        public double getJoy() { return joy; }
        public double getExcited() { return excited; }
        public double getAnxiety() { return anxiety; }
        public double getAnger() { return anger; }
        public double getAnticipation() { return anticipation; }
        public double getOptimism() { return optimism; }
        public double getCalm() { return calm; }
    }

    public String generateTodayChatSummary(String chatroomId) {
        String today = getTaiwanTodayString();
        return generateChatSummary(chatroomId, today);
    }

    public String generateChatSummary(String chatroomId, String date) {
        try {
            List<ChatMessage> chatMessages = chatMessageService.getTextMessagesByDate(chatroomId, date);

            if (chatMessages.isEmpty()) {
                return "該日期沒有聊天記錄";
            }

            String prompt = buildSummaryPrompt(chatMessages, date);

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .topP(0.9f)
                    .candidateCount(1)
                    .maxOutputTokens(200)
                    .systemInstruction(Content.fromParts(Part.fromText("""
                    你是對話摘要分析師。根據提供的聊天記錄生成簡潔有條理的摘要。
                    要求：
                    1. 重點整理主要話題和討論內容重點
                    2. 概述情緒變化和互動品質
                    3. 提及 AI 重要的建議或見解
                    4. 保持客觀
                    5. 摘要長度約50字
                    6. 使用繁體中文回應
                    """)))
                    .build();
            GenerateContentResponse response = client.models.generateContent(
                    aiModel,
                    prompt,
                    config
            );

            return response.text();

        } catch (Exception e) {
            System.err.println("產生聊天摘要失敗: " + e.getMessage());
            return "抱歉，無法產生該日期的聊天摘要，請稍後再試。";
        }
    }

    private String buildSummaryPrompt(List<ChatMessage> chatMessages, String date) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(String.format("為 %s 這一天的聊天記錄生成摘要：\n\n", date));
        promptBuilder.append("=== 聊天記錄開始 ===\n");

        for (ChatMessage msg : chatMessages) {
            String role = msg.getIsUser() ? "使用者" : "AI助手";
            promptBuilder.append(role).append(": ").append(msg.getContent()).append("\n");
        }

        promptBuilder.append("=== 聊天記錄結束 ===\n\n");
        promptBuilder.append("生成這次對話的摘要，包含主要話題、情緒變化和重點建議，只能用你來稱呼使用者，只能用夥伴來稱呼 AI。");

        return promptBuilder.toString();
    }

    public String analyzeTodayDemandSimple(String chatroomId) {
        try {
            List<String> todayMessages = getTodayChatMessages(chatroomId);

            if (todayMessages.isEmpty()) {
                return "需要被理解和傾聽";
            }

            return generateSimpleDemandText(todayMessages);

        } catch (Exception e) {
            System.err.println("需求分析失敗: " + e.getMessage());
            return "需要被理解和傾聽";
        }
    }

    private String generateSimpleDemandText(List<String> chatMessages) {
        try {
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("你是心理分析師，根據聊天記錄簡短描述使用者心理需求，給情緒背後的主要需求與渴望分析，猜出使用者想要甚麼。\n ");

            promptBuilder.append("= 聊天記錄 =\n");
            for (String message : chatMessages) {
                promptBuilder.append(message).append("\n");
            }
            promptBuilder.append("\n回傳需求描述。用繁體中文，只能文字，不要用Markdown格式，不可用 * ，不超過200字，只能用你來稱呼使用者，AI只能用夥伴來稱呼。");

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.2f)
                    .topP(0.2f)
                    .candidateCount(1)
                    .maxOutputTokens(500)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    aiModel,
                    promptBuilder.toString(),
                    config
            );

            String result = response.text();
            return result;

        } catch (Exception e) {
            System.err.println("生成需求文字失敗: " + e.getMessage());
            return "需要被理解和傾聽";
        }
    }
}
