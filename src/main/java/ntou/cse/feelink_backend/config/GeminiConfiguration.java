package ntou.cse.feelink_backend.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfiguration {

    @Bean
    public VertexAI vertexAI() {
        return new VertexAI("graduate-project-450802","asia-southeast1");
    }

    @Bean
    public GenerativeModel generativeModel(VertexAI vertexAI) {
        return new GenerativeModel("gemini-1.5-pro-002",vertexAI);
    }
    @Bean
    public ChatSession chatSession( GenerativeModel generativeModel) {
        return new ChatSession(generativeModel);
    }
}
