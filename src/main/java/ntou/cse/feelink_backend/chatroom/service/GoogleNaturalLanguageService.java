package ntou.cse.feelink_backend.chatroom.service;

import com.google.cloud.language.v1.*;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleNaturalLanguageService {

    private LanguageServiceClient languageServiceClient;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostConstruct
    public void initialize() throws IOException {
        this.languageServiceClient = LanguageServiceClient.create();
    }

    public String getTaiwanTodayString() {
        ZoneId taiwanZone = ZoneId.of("Asia/Taipei");
        ZonedDateTime now = ZonedDateTime.now(taiwanZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return now.format(formatter);
    }

    public List<String> getTodayChatMessages(String chatroomId) {
        String today = getTaiwanTodayString();
        List<ChatMessage> todayMessages = chatMessageService.getUserTextMessagesByDate(chatroomId, today);

        List<String> chatContents = new ArrayList<>();
        if (todayMessages == null) {
            return chatContents;
        }

        for (ChatMessage msg : todayMessages) {
            if (msg.getIsUser()) {
                chatContents.add("使用者: " + msg.getContent());
            }
        }
        return chatContents;
    }

    public SimpleSentimentResult analyzeTodaySentiment(String chatroomId) {
        List<String> todayMessages = getTodayChatMessages(chatroomId);
        String combinedText = String.join(" ", todayMessages);
        return analyzeSentiment(combinedText);
    }

    public SimpleSentimentResult analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SimpleSentimentResult(0.0f, 0.0f);
        }

        try {
            Document document = Document.newBuilder()
                    .setContent(text)
                    .setType(Document.Type.PLAIN_TEXT)
                    .setLanguage("zh-Hant")
                    .build();

            AnalyzeSentimentResponse response = languageServiceClient.analyzeSentiment(document);
            Sentiment documentSentiment = response.getDocumentSentiment();

            return new SimpleSentimentResult(
                    documentSentiment.getScore(),
                    documentSentiment.getMagnitude()
            );
        } catch (Exception e) {
            System.err.println("Google Natural Language API 情感分析失敗: " + e.getMessage());
            e.printStackTrace();
            return new SimpleSentimentResult(0.0f, 0.0f);
        }
    }

    @PreDestroy
    public void destroy() {
        if (languageServiceClient != null) {
            languageServiceClient.close();
        }
    }

    public static class SimpleSentimentResult {
        private final float score;
        private final float magnitude;

        public SimpleSentimentResult(float score, float magnitude) {
            this.score = score;
            this.magnitude = magnitude;
        }

        public float getScore() { return score; }
        public float getMagnitude() { return magnitude; }

        @Override
        public String toString() {
            return "SimpleSentimentResult{" +
                    "score=" + score +
                    ", magnitude=" + magnitude +
                    '}';
        }
    }

}