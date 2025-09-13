package ntou.cse.feelink_backend.chatroom.service;

import com.google.genai.Client;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EmotionAnalysisService {
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmotionAnalysisService(Client client) {
        this.client = client;
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
            List<String> t = new ArrayList<>();
            t.add("今天真的過得很糟糕...早上被主管罵，說我的報告做得不夠仔細\n" +
                    "感覺整個人都很沮喪，一直想哭\n" +
                    "中午的時候都不想吃飯，覺得什麼都沒有意義\n" +
                    "下午坐在辦公室裡，看著電腦螢幕發呆，完全沒有動力\n" +
                    "但是想想，這樣下去也不是辦法\n" +
                    "我不能因為一次挫折就放棄自己\n" +
                    "剛才去便利商店買了一個便當，雖然沒什麼胃口，但還是逼自己吃完了\n" +
                    "至少要好好照顧身體對吧\n" +
                    "吃飽之後心情好像稍微好了一點\n" +
                    "我決定今晚要早點睡，明天重新開始\n" +
                    "其實仔細想想，主管說的也有道理，我確實可以做得更仔細一些\n" +
                    "這次的經驗雖然讓我很難受，但也讓我學到了很多\n" +
                    "我要把這次的教訓記下來，下次做報告的時候更用心\n" +
                    "現在已經開始規劃明天要怎麼改進工作了\n" +
                    "覺得明天會是嶄新的一天，我會做得更好的\n" +
                    "雖然今天哭了，但我相信自己可以克服這個困難\n" +
                    "明天我要帶著更積極的心態去上班\n" +
                    "感謝今天的挫折，讓我變得更堅強\n" +
                    "期待明天的挑戰，我已經準備好了\n");// t.add("我今天很憤怒"); t.add("想哭了"); t.add("但是我會振作起來面對一切");
            String prompt = buildChatAnalysisPrompt(t);

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash-lite",
                    prompt,
                    config
            );
            System.out.println("Response: " + response.text());

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
            return new EmotionScores(0, 0, 0, 0,0,0,0,0); // 預設值
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

        // 增加對應的 getters
        public double getSadness() { return sadness; }
        public double getJoy() { return joy; }
        public double getExcited() { return excited; }
        public double getAnxiety() { return anxiety; }
        public double getAnger() { return anger; }
        public double getAnticipation() { return anticipation; }
        public double getOptimism() { return optimism; }
        public double getCalm() { return calm; }
    }
}
