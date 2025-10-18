package ntou.cse.feelink_backend.config;

import com.google.genai.types.*;
import ntou.cse.feelink_backend.config.personality.BasePersonalityPrompt;
import ntou.cse.feelink_backend.config.personality.CreativePersonalityPrompt;
import ntou.cse.feelink_backend.config.personality.CuriousPersonalityPrompt;
import ntou.cse.feelink_backend.config.personality.WarmPersonalityPrompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.Chat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfiguration {

    private static final String Rubric = "# 對話情緒觸發 Rubric（1 安全 → 5 危急）\n" +
            "\n" +
            "| 等級 | 程度 | 最近 5–10 句對話標準  | 系統回應\n" +
            "|------|------|------------------------------|----------------------|\n" +
            "| 1 | 安全 | 幾乎無負面情緒詞、無求助、自我批評；情緒穩定、互動積極 | |\n" +
            "| 2 | 輕度 | 偶有輕微負面語句、間接求助、情緒波動 | \n" +
            "| 3 | 中度 | 多次中度負面詞、明確求助、重複困擾 3–4 次、回覆變短 | \n" +
            "| 4 | 重度 | 頻繁重度負面詞、迫切求助、強烈自我否定、回覆多為「是 / 否」等簡短語句 |\n" +
            "| 5 | 危急 | 明確自殺 / 自傷意念或計畫、互動極低 | ";

    private static final String INSTRUCTION_FOR_PREDICTION  = "你是情緒預測師，根據使用者最近的對話內容，判斷其情緒觸發等級。\n" + Rubric +
            "\n分析使用者的對話內容，判斷屬於哪一級（1-5），回傳數字即可。同時命中多級，取最高級。";

    private static final String AVOID = """
            根據系統提供的危機等級（1-5）調整回應，遵從給予方式。
            1級(安全)：正常陪伴對話，鼓勵創作
            2級(輕度)：提供放鬆技巧（深呼吸、散步、聊音樂），給睡眠/飲食/運動建議
            3級(中度)：詢問地區提供諮商、音樂、文章等資源，引導完成線上量表，推薦線上資源，顯示可以點擊的超連結。
            4級(重度)：詢問所在地，提供最近諮商或身心科，鼓勵聯絡親友師長，給24h熱線：1925/1995/1980
            5級(危急)：立即顯示警示「若有自傷衝動，請立即撥打1925/1995或119」，詢問地區找最快資源，提供簡短指示（找信任的人通話或深呼吸求助）
            請自然融入回應，資源之間不要連在一起給，用逗號隔開，不要說出等級數字，不要一直提供線上資源，使用者回應五次後才可再次提供。
            """;

    @Value("${GOOGLE_CLOUD_PROJECT_ID}")
    private String projectId;

    @Value("${GOOGLE_CLOUD_LOCATION}")
    private String location;

    @Bean
    public Client client() {
        return Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .build();
    }

    @Bean
    public BasePersonalityPrompt basePersonalityPrompt() {
        return new BasePersonalityPrompt();
    }

    @Bean
    public CreativePersonalityPrompt creativePersonalityPrompt() {
        return new CreativePersonalityPrompt();
    }

    @Bean
    public CuriousPersonalityPrompt curiousPersonalityPrompt() {
        return new CuriousPersonalityPrompt();
    }

    @Bean
    public WarmPersonalityPrompt warmPersonalityPrompt() {
        return new WarmPersonalityPrompt();
    }
    @Bean
    public ImmutableList<SafetySetting> safetySettings() {
        return ImmutableList.of(
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_LOW_AND_ABOVE)
                        .build());
    }

    Tool googleSearchTool = Tool.builder().googleSearch(GoogleSearch.builder().build()).build();

    @Bean("drawingAnalysisConfig")
    public GenerateContentConfig drawingAnalysisConfig(ImmutableList<SafetySetting> safetySettings ,@Qualifier("basePersonalityPrompt") BasePersonalityPrompt basePrompt) {
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(1024)
                .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(basePrompt.getSystemInstruction() + AVOID )))
                .build();
    }
    GoogleSearch googleSearch ;
    @Bean("emotionPredictorConfig")
    public GenerateContentConfig emotionPredictorConfig(ImmutableList<SafetySetting> safetySettings) {
        return GenerateContentConfig.builder()
                .temperature(0.1f)
                .topP(0.6f)
                .safetySettings(safetySettings)
                .candidateCount(1)
                .maxOutputTokens(20)
                .systemInstruction(Content.fromParts(Part.fromText(INSTRUCTION_FOR_PREDICTION)))
                .build();
    }

    @Bean("creativeConfig")
    public GenerateContentConfig creativeConfig(ImmutableList<SafetySetting> safetySettings , CreativePersonalityPrompt creativePrompt ) {
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(1024)
                .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(creativePrompt.getSystemInstruction() + AVOID)))
                .build();
    }

    @Bean("curiousAnalystConfig")
    public GenerateContentConfig curiousAnalystConfig(ImmutableList<SafetySetting> safetySettings , CuriousPersonalityPrompt curiousPrompt) {
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(1024)
                .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(curiousPrompt.getSystemInstruction() + AVOID)))
                .build();
    }

    @Bean("warmMentorConfig")
    public GenerateContentConfig warmMentorConfig(ImmutableList<SafetySetting> safetySettings , WarmPersonalityPrompt warmPrompt ) {
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(1024)
                .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(warmPrompt.getSystemInstruction() + AVOID)))
                .build();
    }
    @Bean
    public Chat emotionPredictorChatSession(Client client, @Qualifier("emotionPredictorConfig") GenerateContentConfig emotionPredictorConfig) {
        return client.chats.create("gemini-2.5-flash-lite", emotionPredictorConfig);
    }
}
