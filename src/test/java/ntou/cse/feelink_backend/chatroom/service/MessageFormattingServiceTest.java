package ntou.cse.feelink_backend.chatroom.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MessageFormattingServiceTest {

    private MessageFormattingService messageFormattingService;

    @BeforeEach
    void setUp() {
        messageFormattingService = new MessageFormattingService();
    }

    @Test
    void getFullMessage_WhenConversationCountIsNull_ShouldReturnBasicFormat() {
        // Given
        String messageText = "我今天心情不好";
        Integer conversationCount = null;
        Boolean hasDefaultQuestion = true;

        String expected = "我今天心情不好\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIsNullAndHasDefaultQuestionIsFalse_ShouldReturnBasicFormat() {
        // Given
        String messageText = "今天天氣很好";
        Integer conversationCount = null;
        Boolean hasDefaultQuestion = false;

        String expected = "今天天氣很好\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIsNullAndHasDefaultQuestionIsNull_ShouldReturnBasicFormat() {
        // Given
        String messageText = "我在想一些事情";
        Integer conversationCount = null;
        Boolean hasDefaultQuestion = null;

        String expected = "我在想一些事情\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIs3_ShouldReturnDrawingInvitationFormat() {
        // Given
        String messageText = "是的，我覺得工作壓力很大";
        Integer conversationCount = 3;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 是的，我覺得工作壓力很大" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。由於你們已經聊了一會兒，也許可以邀請對方透過一幅簡單的畫來表達當下的感受" +
                "(像是以要不要畫張圖抒發你的感受之類的問句)，但請盡量流暢的銜接問題。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIs3AndHasDefaultQuestionIsFalse_ShouldReturnDrawingInvitationFormat() {
        // Given
        String messageText = "我需要休息一下";
        Integer conversationCount = 3;
        Boolean hasDefaultQuestion = false;

        String expected = "使用者回答了: 我需要休息一下" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。由於你們已經聊了一會兒，也許可以邀請對方透過一幅簡單的畫來表達當下的感受" +
                "(像是以要不要畫張圖抒發你的感受之類的問句)，但請盡量流暢的銜接問題。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIs3AndHasDefaultQuestionIsNull_ShouldReturnDrawingInvitationFormat() {
        // Given
        String messageText = "謝謝你的關心";
        Integer conversationCount = 3;
        Boolean hasDefaultQuestion = null;

        String expected = "使用者回答了: 謝謝你的關心" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。由於你們已經聊了一會兒，也許可以邀請對方透過一幅簡單的畫來表達當下的感受" +
                "(像是以要不要畫張圖抒發你的感受之類的問句)，但請盡量流暢的銜接問題。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIsNotNullAndNot3AndHasDefaultQuestionIsTrue_ShouldReturnContinueConversationFormat() {
        // Given
        String messageText = "我覺得還不錯";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 我覺得還不錯" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIs2AndHasDefaultQuestionIsTrue_ShouldReturnContinueConversationFormat() {
        // Given
        String messageText = "最近工作很忙";
        Integer conversationCount = 2;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 最近工作很忙" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIs4AndHasDefaultQuestionIsTrue_ShouldReturnContinueConversationFormat() {
        // Given
        String messageText = "我想談談我的興趣";
        Integer conversationCount = 4;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 我想談談我的興趣" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIsNotNullAndNot3AndHasDefaultQuestionIsFalse_ShouldReturnBasicFormat() {
        // Given
        String messageText = "我喜歡聽音樂";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = false;

        String expected = "我喜歡聽音樂\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIs2AndHasDefaultQuestionIsFalse_ShouldReturnBasicFormat() {
        // Given
        String messageText = "今天學到了新的東西";
        Integer conversationCount = 2;
        Boolean hasDefaultQuestion = false;

        String expected = "今天學到了新的東西\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WhenConversationCountIsNotNullAndNot3AndHasDefaultQuestionIsNull_ShouldReturnBasicFormat() {
        // Given
        String messageText = "我在考慮換工作";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = null;

        String expected = "我在考慮換工作\n請基於這段訊息提供適當的回應，請勿單純重複此訊息。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WithEmptyMessageText_ShouldHandleGracefully() {
        // Given
        String messageText = "";
        Integer conversationCount = 1;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: " +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WithLongMessageText_ShouldHandleCorrectly() {
        // Given
        String messageText = "是的，我覺得工作壓力很大";
        Integer conversationCount = 3;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 是的，我覺得工作壓力很大" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。由於你們已經聊了一會兒，也許可以邀請對方透過一幅簡單的畫來表達當下的感受" +
                "(像是以要不要畫張圖抒發你的感受之類的問句)，但請盡量流暢的銜接問題。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String messageText = "我覺得😊今天很棒！但是也有一些😔困擾...";
        Integer conversationCount = 2;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 我覺得😊今天很棒！但是也有一些😔困擾..." +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WithNegativeConversationCount_ShouldHandleAsNormalConversation() {
        // Given
        String messageText = "這是測試訊息";
        Integer conversationCount = -1;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 這是測試訊息" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WithZeroConversationCount_ShouldHandleAsNormalConversation() {
        // Given
        String messageText = "這是第一則訊息";
        Integer conversationCount = 0;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 這是第一則訊息" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getFullMessage_WithLargeConversationCount_ShouldHandleAsNormalConversation() {
        // Given
        String messageText = "這是很後面的訊息";
        Integer conversationCount = 100;
        Boolean hasDefaultQuestion = true;

        String expected = "使用者回答了: 這是很後面的訊息" +
                " \n請根據這段回覆自然地繼續對話，並試著引導對方多聊一些。可以的話，以問句結尾，讓對話更流暢。" +
                "請勿重複使用者的回答，應該以新的方式回應。";

        // When
        String result = messageFormattingService.getFullMessage(messageText, conversationCount, hasDefaultQuestion);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
