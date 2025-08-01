package ntou.cse.feelink_backend.chatroom.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageFormattingServiceTest {

    private final MessageFormattingService service = new MessageFormattingService();

    @Test
    void getFullMessage_whenConversationCountIsNull() {
        String message = "你好";
        Integer conversationCount = null;
        Boolean hasDefaultQuestion = null;

        String expected = "你好\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";
        String actual = service.getFullMessage(message, conversationCount, hasDefaultQuestion);

        assertEquals(expected, actual);
    }

    @Test
    void getFullMessage_whenConversationCountIsThree() {
        String message = "我今天心情不錯";
        Integer conversationCount = 3;
        Boolean hasDefaultQuestion = null;

        String actual = service.getFullMessage(message, conversationCount, hasDefaultQuestion);

        assertTrue(actual.startsWith("使用者回答了: 我今天心情不錯"));
        assertTrue(actual.contains("請根據這段回覆自然地繼續對話"));
        assertTrue(actual.contains("也許可以邀請對方透過一幅簡單的畫來表達當下的感受"));
    }

    @Test
    void getFullMessage_whenHasDefaultQuestionIsTrue() {
        String message = "是的";
        Integer conversationCount = 2;
        Boolean hasDefaultQuestion = true;

        String actual = service.getFullMessage(message, conversationCount, hasDefaultQuestion);

        assertTrue(actual.startsWith("使用者回答了: 是的"));
        assertTrue(actual.contains("請根據這段回覆自然地繼續對話"));
        assertFalse(actual.contains("也許可以邀請對方透過一幅簡單的畫來表達當下的感受"));
    }

    @Test
    void getFullMessage_whenOnlyHasDefaultQuestionFalse() {
        String message = "沒有";
        Integer conversationCount = null;
        Boolean hasDefaultQuestion = false;
        String expected = "沒有\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";
        String actual = service.getFullMessage(message, conversationCount, hasDefaultQuestion);
        assertEquals(expected, actual);
    }

    @Test
    void getFullMessage_whenOnlyConversationCountIsNotSpecial() {
        String message = "Hello";
        Integer conversationCount = 5;
        Boolean hasDefaultQuestion = null;
        String expected = "Hello\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";
        String actual = service.getFullMessage(message, conversationCount, hasDefaultQuestion);
        assertEquals(expected, actual);
    }

    @Test
    void getFullMessage_whenConversationCountNotThreeAndHasDefaultQuestionFalse() {
        String message = "不是";
        Integer conversationCount = 5;
        Boolean hasDefaultQuestion = false; // false
        String expected = "不是\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";
        String actual = service.getFullMessage(message, conversationCount, hasDefaultQuestion);
        assertEquals(expected, actual);
    }

}
