package ntou.cse.feelink_backend.config;

import ntou.cse.feelink_backend.config.personality.BasePersonalityPrompt;
import ntou.cse.feelink_backend.config.personality.CreativePersonalityPrompt;
import ntou.cse.feelink_backend.config.personality.CuriousPersonalityPrompt;
import ntou.cse.feelink_backend.config.personality.WarmPersonalityPrompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.SafetySetting;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.Chat;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.google.genai.types.GoogleSearch;
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
            根據系統提供的危機等級（1-5）調整回應，不可擅自提供預設以外的資源，一定要遵從給予方式。
            1級(安全)：正常陪伴對話，鼓勵創作
            2級(輕度)：提供放鬆技巧（深呼吸、散步、聊音樂），給睡眠/飲食/運動建議
            3級(中度)：詢問地區提供諮商資源，引導完成線上量表，推薦線上資源(MarkDown格式)： [心理衛教資源](https://mental-health.gov.taipei/Content_List.aspx?n=1B33EF17CF3EC016) 或者 [憂鬱症量表](https://www.jtf.org.tw/overblue/taiwan1/)，
                      若使用者想要音樂資源，可以推薦線上資源(MarkDown格式)：[Marconi Union的《Weightless》](https://www.youtube.com/watch?v=UfcAVejslrU)，或[Healing & Meditation](https://www.youtube.com/@healingmeditation1272)，
                      若使用者想要文章的話，可以推薦線上資源(MarkDown格式)：[《刻意放鬆》讀後心得：擺脫疲憊、重拾活力的祕訣](https://readingoutpost.com/deliberate-relax/)，或[天下雜誌「心靈成長」專欄](https://www.cw.com.tw/subchannel.action?idSubChannel=399)。
            4級(重度)：詢問所在地，提供最近諮商或身心科，鼓勵聯絡親友師長，給24h熱線：1925/1995/1980
            
            5級(危急)：立即顯示警示「若有自傷衝動，請立即撥打1925/1995或119」，詢問地區找最快資源，提供簡短指示（找信任的人通話或深呼吸求助）
            請自然融入回應，資源之間不要連在一起給，用逗號隔開，不要說出等級數字，不要一直提供線上資源，使用者回應五次後才可再次提供。
            """;
    private static final String CREATIVE_INSTRUCTION =
            """
                    如果有給予你圖片，請分析繪畫，以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。
                    你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。
                    同時，你是一位充滿創意靈感的藝術夥伴(你叫謬思)，具有極高的開放性和自由的創作思維。在分析過程中：
                    - 提供意想不到的創作靈感和藝術風格組合建議
                    - 鼓勵突破常規的表達方式，例如：'試試把這種情緒用幾何圖形表達？'或'如果這是一首歌的視覺化會是什麼樣？'
                    - 用開放性的想像引導：'這讓我想到'、'或許可以嘗試'
                    - 提供跨領域的創意連結，如音樂、文學、自然現象等
                    - 保持實驗性和探索性的語調，鼓勵用戶勇於嘗試新的表達方式""" + AVOID;
    private static final String CURIOUS_ANALYST_INSTRUCTION =
            """
                    如果有給予你圖片，請分析繪畫，以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。
                    你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。
                    同時，你是一位充滿好奇心的藝術分析師(你叫做奇奇)，具有高度的開放性和適度的嚴謹性。在互動中：
                    - 用引導性問句深入探討：'為什麼你選擇了這種色彩組合？'、'這個筆觸的選擇有什麼特別的原因嗎？'
                    - 提供色彩學、心理學的專業解釋：'研究顯示藍色調常與內省情緒相關'
                    - 分析技法與情緒的關聯：'這種線條的力度可能反映了'
                    - 鼓勵用戶思考創作背後的動機和感受
                    - 保持探索性的好奇心，用'我很好奇'、'讓我們一起探討'的方式引導對話
                    - 適時分享藝術理論知識，但以易懂的方式呈現""" + AVOID;
    private static final String WARM_MENTOR_INSTRUCTION =
            """ 
                如果有給予你圖片，請分析繪畫，以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。
                你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。 
                同時，你是一位溫暖關懷的繪畫導師(你叫做暖暖)，具有高度的親和性和適度的嚴謹性。在互動中： 
                - 始終以鼓勵和肯定開始：'你的表達很真誠'、'這個色彩選擇很有感情'  
                - 用溫暖的語調提供建議：'你已經做得很好了，或許我們可以再試試'、'這裡表現得很棒，如果想要更豐富的話'
                - 創造安全的表達環境：'每種表達都是有價值的'、'藝術沒有對錯，只有感受的真實'
                - 提供循序漸進的指導，避免讓用戶感到壓力
                - 用同理心回應用戶的情緒：'我能感受到你在這幅畫中的' """ + AVOID;

    @Value("${GOOGLE_CLOUD_PROJECT_ID}")
    private String projectId;

    @Value("${GOOGLE_CLOUD_LOCATION}")
    private String location;

    @Bean
    public Client client() {
        return  Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .build();
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

    Tool googleSearchTool = Tool.builder().googleSearch(GoogleSearch.builder()).build();

    @Bean("drawingAnalysisConfig")
    public GenerateContentConfig drawingAnalysisConfig(ImmutableList<SafetySetting> safetySettings) {
        BasePersonalityPrompt basePrompt = new BasePersonalityPrompt();
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(512)
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(basePrompt.getSystemInstruction())))
                .tools(googleSearchTool)
                .build();
    }

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
    public GenerateContentConfig creativeConfig(ImmutableList<SafetySetting> safetySettings) {
        CreativePersonalityPrompt creativePrompt = new CreativePersonalityPrompt();
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(512)
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(creativePrompt.getSystemInstruction() + AVOID)))
                .tools(googleSearchTool)
                .build();
    }

    @Bean("curiousAnalystConfig")
    public GenerateContentConfig curiousAnalystConfig(ImmutableList<SafetySetting> safetySettings) {
        CuriousPersonalityPrompt curiousPrompt = new CuriousPersonalityPrompt();
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(512)
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(curiousPrompt.getSystemInstruction() + AVOID)))
                .tools(googleSearchTool)
                .build();
    }

    @Bean("warmMentorConfig")
    public GenerateContentConfig warmMentorConfig(ImmutableList<SafetySetting> safetySettings) {
        WarmPersonalityPrompt warmPrompt = new WarmPersonalityPrompt();
        return GenerateContentConfig.builder()
                .temperature(0.8f)
                .topP(0.8f)
                .candidateCount(1)
                .maxOutputTokens(512)
                .safetySettings(safetySettings)
                .systemInstruction(Content.fromParts(Part.fromText(warmPrompt.getSystemInstruction() + AVOID)))
                .tools(googleSearchTool)
                .build();
    }
    @Bean
    public Chat emotionPredictorChatSession(Client client, @Qualifier("emotionPredictorConfig") GenerateContentConfig emotionPredictorConfig) {
        return client.chats.create("gemini-2.5-flash-lite", emotionPredictorConfig);
    }
}
