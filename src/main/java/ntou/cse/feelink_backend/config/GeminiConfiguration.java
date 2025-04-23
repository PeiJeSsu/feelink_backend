package ntou.cse.feelink_backend.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfiguration {
    private static final String InstructionForAnalysis = "請分析這張繪畫，並以繁體中文說明它可能反映的使用者情緒。從心理學的角度，解釋繪畫中的哪些特徵可能與特定的情緒相關聯。你的回應應該引導使用者思考這些可能的聯繫，鼓勵他們探索自己的內心感受，而不是直接給出情緒標籤。請避免使用過於強烈的斷言，並保持解釋性的語氣，解釋為何推論出，並給予使用者陪伴感。也輪流擔任聆聽者與引導者。不要透漏有先前的情緒預測結果。";
    private static final String InstructionForPrediction = "請根據使用者提供的文字或先前的互動，預測使用者接下來可能的心情和感興趣的話題。不要給予關心或回應，做預測即可。";

    @Bean
    public VertexAI vertexAI() {
        return new VertexAI("graduate-project-450802","asia-southeast1");
    }

    @Bean
    public GenerationConfig generationConfigForAnalysis() {
        return GenerationConfig.newBuilder()
                .setTemperature(0.7f)
                .setTopP(0.8f)
                .setMaxOutputTokens(256)
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
                .setSystemInstruction(ContentMaker.fromString(InstructionForAnalysis))
                .build();
    }

    @Bean
    public GenerativeModel emotionPredictorModel(VertexAI vertexAI, GenerationConfig generationConfigForPrediction) {
        return new GenerativeModel.Builder()
                .setModelName("gemini-1.5-pro-002")
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfigForPrediction)
                .setSystemInstruction(ContentMaker.fromString(InstructionForPrediction))
                .build();
    }

    @Bean
    public ChatSession drawingAnalysisChatSession(GenerativeModel drawingAnalysisModel) {
        return new ChatSession(drawingAnalysisModel);
    }

    @Bean
    public ChatSession emotionPredictorChatSession(GenerativeModel emotionPredictorModel) {
        return new ChatSession(emotionPredictorModel);
    }
}
