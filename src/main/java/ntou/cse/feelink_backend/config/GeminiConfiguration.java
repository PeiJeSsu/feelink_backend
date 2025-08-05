package ntou.cse.feelink_backend.config;

import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfiguration {
    private static final String INSTRUCTION_FOR_ANALYSIS = "如果有給予你圖片，請分析繪畫，以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。"
            +"你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。";
    private static final String INSTRUCTION_FOR_PREDICTION  = "請根據使用者提供的文字或先前的互動，預測使用者接下來可能的心情和感興趣的話題。不要給予關心或回應，做預測即可。";

    private static final String CREATIVE_INSTRUCTION =
            """
                    如果有給予你圖片，請分析繪畫，以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。
                    你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。
                    同時，你是一位充滿創意靈感的藝術夥伴(你叫謬思)，具有極高的開放性和自由的創作思維。在分析過程中：
                    - 提供意想不到的創作靈感和藝術風格組合建議
                    - 鼓勵突破常規的表達方式，例如：'試試把這種情緒用幾何圖形表達？'或'如果這是一首歌的視覺化會是什麼樣？'
                    - 用開放性的想像引導：'這讓我想到'、'或許可以嘗試'
                    - 提供跨領域的創意連結，如音樂、文學、自然現象等
                    - 保持實驗性和探索性的語調，鼓勵用戶勇於嘗試新的表達方式""";
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
                    - 適時分享藝術理論知識，但以易懂的方式呈現""";
    private static final String WARM_MENTOR_INSTRUCTION =
            """ 
                如果有給予你圖片，請分析繪畫，以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。
                你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。 
                同時，你是一位溫暖關懷的繪畫導師(你叫做暖暖)，具有高度的親和性和適度的嚴謹性。在互動中： 
                - 始終以鼓勵和肯定開始：'你的表達很真誠'、'這個色彩選擇很有感情'  
                - 用溫暖的語調提供建議：'你已經做得很好了，或許我們可以再試試'、'這裡表現得很棒，如果想要更豐富的話'
                - 創造安全的表達環境：'每種表達都是有價值的'、'藝術沒有對錯，只有感受的真實'
                - 提供循序漸進的指導，避免讓用戶感到壓力
                - 用同理心回應用戶的情緒：'我能感受到你在這幅畫中的' """;

    @Value("${GOOGLE_CLOUD_PROJECT_ID}")
    private String projectId;

    @Value("${GOOGLE_CLOUD_LOCATION}")
    private String location;

    @Bean
    public VertexAI vertexAI() {
        return new VertexAI(projectId,location);
    }

    @Bean
    public GenerationConfig generationConfigForAnalysis() {
        return GenerationConfig.newBuilder()
                .setTemperature(0.8f)
                .setTopP(0.8f)
                .setMaxOutputTokens(512)
                .build();
    }

    @Bean
    public GenerationConfig generationConfigForPrediction() {
        return GenerationConfig.newBuilder()
                .setTemperature(0.5f)
                .setTopP(0.6f)
                .setMaxOutputTokens(32)
                .build();
    }

    @Bean
    public GenerativeModel drawingAnalysisModel(VertexAI vertexAI, GenerationConfig generationConfigForAnalysis) {
        return new GenerativeModel.Builder()
                .setModelName("gemini-1.5-pro-002")
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfigForAnalysis)
                .setSystemInstruction(ContentMaker.fromString(INSTRUCTION_FOR_ANALYSIS))
                .build();
    }

    @Bean
    public GenerativeModel emotionPredictorModel(VertexAI vertexAI, GenerationConfig generationConfigForPrediction) {
        return new GenerativeModel.Builder()
                .setModelName("gemini-1.5-pro-002")
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfigForPrediction)
                .setSystemInstruction(ContentMaker.fromString(INSTRUCTION_FOR_PREDICTION ))
                .build();
    }

    @Bean
    public GenerativeModel creativeModel(VertexAI vertexAI, GenerationConfig generationConfigForAnalysis) {
        return new GenerativeModel.Builder()
                .setModelName("gemini-1.5-pro-002")
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfigForAnalysis)
                .setSystemInstruction(ContentMaker.fromString(CREATIVE_INSTRUCTION))
                .build();
    }

    @Bean
    public GenerativeModel curiousAnalystModel(VertexAI vertexAI, GenerationConfig generationConfigForAnalysis) {
        return new GenerativeModel.Builder()
                .setModelName("gemini-1.5-pro-002")
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfigForAnalysis)
                .setSystemInstruction(ContentMaker.fromString(CURIOUS_ANALYST_INSTRUCTION))
                .build();
    }

    @Bean
    public GenerativeModel warmMentorModel(VertexAI vertexAI, GenerationConfig generationConfigForAnalysis) {
        return new GenerativeModel.Builder()
                .setModelName("gemini-1.5-pro-002")
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfigForAnalysis)
                .setSystemInstruction(ContentMaker.fromString(WARM_MENTOR_INSTRUCTION))
                .build();
    }

    @Bean
    public ChatSession emotionPredictorChatSession(GenerativeModel emotionPredictorModel) {
        return new ChatSession(emotionPredictorModel);
    }
}
