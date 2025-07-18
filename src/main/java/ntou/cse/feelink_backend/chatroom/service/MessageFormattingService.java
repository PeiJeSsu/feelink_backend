package ntou.cse.feelink_backend.chatroom.service;

import org.springframework.stereotype.Service;

@Service
public class MessageFormattingService {

    public String getFullMessage(String messageText, Integer conversationCount, Boolean hasDefaultQuestion) {
        if (conversationCount == null) {
            return messageText + "\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";
        }

        if (conversationCount == 3) {
            return "使用者回答了: " + messageText +
                    " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                    "請勿重複使用者的回答，應該以新的方式回應。由於你們已經聊了一會兒，也許可以邀請對方透過一幅簡單的畫來表達當下的感受" +
                    "(像是以要不要話張圖抒發你的感受之類的問句)，但請盡量流暢的銜接問題。";
        } else if (hasDefaultQuestion != null && hasDefaultQuestion) {
            return "使用者回答了: " + messageText +
                    " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                    "請勿重複使用者的回答，應該以新的方式回應。";
        } else {
            return messageText + "\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";
        }
    }
}