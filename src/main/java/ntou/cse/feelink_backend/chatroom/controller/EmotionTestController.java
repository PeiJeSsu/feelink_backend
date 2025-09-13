package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.service.EmotionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class EmotionTestController {

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @PostMapping("/emotion")
    public EmotionAnalysisService.EmotionScores testEmotion(@RequestBody TestRequest request) {
        return emotionAnalysisService.analyzeEmotionsFromChatHistory(
                request.getChatroomId(),
                request.getMessages()
        );
    }

    @GetMapping("/emotion/quick")
    public EmotionAnalysisService.EmotionScores quickTest() {
        List<String> testMessages = Arrays.asList(
                "我今天很累",
                "工作壓力很大",
                "但是完成項目後很有成就感",
                "希望明天會更好"
        );

        return emotionAnalysisService.analyzeEmotionsFromChatHistory("test-room", testMessages);
    }

    public static class TestRequest {
        private String chatroomId;
        private List<String> messages;

        public String getChatroomId() { return chatroomId; }
        public void setChatroomId(String chatroomId) { this.chatroomId = chatroomId; }
        public List<String> getMessages() { return messages; }
        public void setMessages(List<String> messages) { this.messages = messages; }
    }
}
