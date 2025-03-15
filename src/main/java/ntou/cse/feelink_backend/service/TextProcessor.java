package ntou.cse.feelink_backend.service;

import org.springframework.stereotype.Component;

@Component
public class TextProcessor {

    public String processText(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "請分析這張圖片";
        }
        return userMessage;
    }
}
