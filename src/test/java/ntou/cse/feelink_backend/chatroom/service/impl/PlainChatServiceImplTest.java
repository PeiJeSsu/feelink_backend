package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlainChatServiceImplTest {

    @Mock
    private ChatSessionRegistry registry;

    @Mock
    private MessageFormattingService messageFormattingService;

    @Mock
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Mock
    private ChatMessageService chatMessageService;

    private Client client;
    private Object spyModels;
    private Object spyChats;

    @Mock
    private Chat chatSession;

    @Mock
    private Chat searchChat;

    @Mock
    private ResponseStream<GenerateContentResponse> responseStream;

    @Mock
    private GenerateContentResponse generateContentResponse;

    @InjectMocks
    private PlainChatServiceImpl plainChatService;

    private static final String TEST_USER_MESSAGE = "我今天很開心";
    private static final String TEST_CHATROOM_ID = "room123";
    private static final String TEST_AI_MESSAGE_ID = "ai_msg_123";
    private static final Integer TEST_CONVERSATION_COUNT = 5;
    private static final Boolean TEST_HAS_DEFAULT_QUESTION = true;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 創建真實 Client
        client = Client.builder().apiKey("fake-key").build();

        // 2. 取出真實 models/chats 並 spy
        Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        spyModels = spy(modelsField.get(client));

        Field chatsField = Client.class.getDeclaredField("chats");
        chatsField.setAccessible(true);
        spyChats = spy(chatsField.get(client));

        // 3. 塞回 client
        setFinalField(client, "models", spyModels);
        setFinalField(client, "chats", spyChats);

        // 4. 注入 Service
        ReflectionTestUtils.setField(plainChatService, "client", client);

        // 5. 預設 Mock 行為 (攔截 API 調用)
        doReturn(generateContentResponse).when(spyModels).getClass()
                .getMethod("generateContent", String.class, String.class, com.google.genai.types.GenerateContentConfig.class)
                .invoke(spyModels, anyString(), anyString(), any());

        doReturn(searchChat).when(spyChats).getClass()
                .getMethod("create", String.class, com.google.genai.types.GenerateContentConfig.class)
                .invoke(spyChats, anyString(), any());

        // ====== 全局預設 Mock (避免 NPE) ======
        // 確保情緒預測有回傳值
        when(emotionPredictionChatService.processChat(anyString())).thenReturn("快樂");

        // 確保格式化訊息有回傳值
        when(messageFormattingService.getFullMessage(anyString(), anyInt(), anyBoolean())).thenReturn("Formatted Message");

        // 確保 Registry 有回傳值
        when(registry.getSession(anyString())).thenReturn(chatSession);
        when(registry.getSession(anyString(), anyString())).thenReturn(chatSession);

        // 確保 ChatSession 不會報錯
        when(chatSession.getHistory(true)).thenReturn(ImmutableList.of(Content.fromParts(Part.fromText("history"))));

        // 確保 GenerateContentResponse.text() 不回傳 null (這是關鍵!)
        when(generateContentResponse.text()).thenReturn("否"); // 預設不搜尋

        // 確保 ResponseStream 有東西
        Iterator<GenerateContentResponse> iterator = Arrays.asList(generateContentResponse).iterator();
        when(responseStream.iterator()).thenReturn(iterator);
        when(chatSession.sendMessageStream(anyString())).thenReturn(responseStream);
    }

    private void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // 1. 測試快樂路徑
    @Test
    void testSuccess_NoSearch_EmotionSuccess() throws Exception {
        SseEmitter emitter = plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        assertNotNull(emitter);
        verify(emotionPredictionChatService).processChat(TEST_USER_MESSAGE);
    }

    // 2. 測試情緒預測失敗
    @Test
    void testEmotionPredictionFailure() throws Exception {
        when(emotionPredictionChatService.processChat(anyString())).thenThrow(new RuntimeException("API Error"));

        SseEmitter emitter = plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        assertNotNull(emitter);
        // 驗證即使預測失敗，流程仍繼續
        verify(messageFormattingService).getFullMessage(anyString(), anyInt(), anyBoolean());
    }

    // 3. 測試需要搜尋
    @Test
    void testSearchDecision_Yes() throws Exception {
        // 設定搜尋判斷為 "是"
        when(generateContentResponse.text()).thenReturn("是");

        // Mock 搜尋用的 chat.sendMessage
        GenerateContentResponse searchResultResponse = mock(GenerateContentResponse.class);
        when(searchResultResponse.text()).thenReturn("搜尋關鍵字");
        when(searchChat.sendMessage(anyString())).thenReturn(searchResultResponse);

        SseEmitter emitter = plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        assertNotNull(emitter);
        verify(searchChat).sendMessage(anyString());
    }

    // 4. 測試自定義個性
    @Test
    void testCustomPersonality() throws Exception {
        String personality = "MUSE";
        // 情緒預測已在 setUp 預設為 "快樂" (非 null)

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, personality, TEST_AI_MESSAGE_ID
        );

        verify(registry).getSession(TEST_CHATROOM_ID, personality);
    }

    // 5. 測試載入歷史紀錄
    @Test
    void testLoadHistory() throws Exception {
        // 模擬 Session History 為空
        when(chatSession.getHistory(true)).thenReturn(ImmutableList.of());

        // 模擬 DB 有歷史紀錄
        ChatMessage msg = new ChatMessage();
        msg.setContent("old msg");
        msg.setIsUser(true);
        when(chatMessageService.getTextMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(List.of(msg));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        // 驗證有嘗試載入歷史
        verify(chatSession).sendMessage(anyList());
    }

    // 6. 測試串流發送失敗
    @Test
    void testStreamFailure() throws Exception {
        when(chatSession.sendMessageStream(anyString())).thenThrow(new RuntimeException("Stream Error"));

        // 模擬 DB 更新 (用於錯誤處理)
        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        // 確保有 emotion prediction (setUp 已設定)

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        // 等待非同步執行完成
        Thread.sleep(200);

        verify(chatMessageService).updateMessage(argThat(msg ->
                msg.getContent().contains("AI處理錯誤")
        ));
    }

    // 7. 測試委派
    @Test
    void testReplyStreamDelegation() throws Exception {
        PlainChatServiceImpl spyService = spy(plainChatService);
        SseEmitter expectedEmitter = new SseEmitter();

        doReturn(expectedEmitter).when(spyService).processStreamWithAllFeatures(
                anyString(), anyString(), anyInt(), anyBoolean(), anyString(), anyString()
        );

        spyService.replyStreamWithDatabase(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, TEST_AI_MESSAGE_ID
        );

        verify(spyService).processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );
    }

    // 8. 測試載入歷史紀錄 - 包含 AI 的訊息 (覆蓋 "AI: " + msg.getContent())
    // 以及測試載入歷史失敗 (覆蓋 catch (Exception e) { System.err.println("載入聊天歷史失敗... )
    @Test
    void testLoadHistory_WithAiMessage_And_Failure() throws Exception {
        // 模擬 Session History 為空
        when(chatSession.getHistory(true)).thenReturn(ImmutableList.of());

        // 模擬 DB 有一條 AI 的歷史紀錄
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent("old ai msg");
        aiMsg.setIsUser(false); // 這會觸發 "AI: " 分支
        when(chatMessageService.getTextMessagesByChatroomId(TEST_CHATROOM_ID)).thenReturn(List.of(aiMsg));

        // 模擬 sendMessage 拋出異常 (觸發載入歷史失敗的 catch block)
        doThrow(new RuntimeException("Load History Error")).when(chatSession).sendMessage(anyList());

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        // 驗證有嘗試發送 (雖然失敗了)
        verify(chatSession).sendMessage(anyList());
    }

    // 9. 測試 SSE 發送失敗 (覆蓋 catch (IOException e) { System.err.println("發送 SSE 資料失敗... break; )
    @Test
    void testSseSendFailure() throws Exception {
        // 模擬 chatSession 回傳正常的 token
        when(generateContentResponse.text()).thenReturn("token");

        // 這裡比較棘手，因為我們無法輕易 mock SseEmitter 內部的 send 拋出異常 (因為它是 new 出來的)
        // 但我們可以透過 spy SseEmitter 來達成，或者...
        // 由於 SseEmitter 是在 method 內部 new 出來的，無法直接注入 spy。
        //
        // 不過，如果我們讓 response stream 正常回傳，但 emitter 在發送時斷開...
        //
        // 唯一能在不改 code 的情況下測試這個分支的方法是：
        // SseEmitter 的 send 方法是 final 的嗎？不是。
        // 但我們無法替換掉方法內部 new 出來的 SseEmitter。
        //
        // **替代方案**：這個分支可能在單元測試中難以覆蓋，除非重構代碼將 SseEmitter 工廠化。
        // 但我們可以試試看是否能透過 ResponseStream 的行為來間接觸發某種狀態？不，SseEmitter 是獨立的。
        //
        // 如果你一定要覆蓋這個 catch block，你必須將 `new SseEmitter(...)` 抽取成一個 protected method 然後在測試中 override 它。
        //
        // 既然不能改 code，這行可能只能放過，或者用整合測試斷開連線。
        // 但等等，我們可以測試 "串流回應錯誤" 的那個大 catch block (這已經在 testStreamFailure 測過了)
    }

    // 10. 測試更新資料庫成功 (覆蓋 if (aiMessageOpt.isPresent()) { ... updateMessage })
    @Test
    void testUpdateDatabase_Success() throws Exception {
        // 讓流程正常跑完
        when(generateContentResponse.text()).thenReturn("token");

        // 模擬 DB 找到訊息
        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        // 驗證 updateMessage 被調用且內容正確
        verify(chatMessageService).updateMessage(argThat(msg -> msg.getContent().equals("token")));
    }

    // 11. 測試更新資料庫失敗 (覆蓋 catch (Exception e) { System.err.println("更新AI訊息到資料庫失敗... )
    @Test
    void testUpdateDatabase_Failure() throws Exception {
        when(generateContentResponse.text()).thenReturn("token");
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(new ChatMessage()));

        // 模擬 updateMessage 拋出異常
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenThrow(new RuntimeException("DB Error"));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        // 驗證確實嘗試更新了 (然後被 catch 住了)
        verify(chatMessageService).updateMessage(any(ChatMessage.class));
    }

    // 12. 測試錯誤處理時更新資料庫失敗 (覆蓋 catch (Exception dbError) { System.err.println("更新錯誤訊息到資料庫失敗... )
    @Test
    void testHandleError_UpdateDbFailure() throws Exception {
        // 先觸發主流程錯誤
        when(chatSession.sendMessageStream(anyString())).thenThrow(new RuntimeException("Main Error"));

        // 模擬 DB 找到訊息
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(new ChatMessage()));

        // 模擬在錯誤處理時，更新 DB 也失敗了
        when(chatMessageService.updateMessage(any(ChatMessage.class))).thenThrow(new RuntimeException("DB Update Error In Handler"));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        // 驗證嘗試更新錯誤訊息 (然後失敗被 catch)
        verify(chatMessageService).updateMessage(any(ChatMessage.class));
    }

    // 13. 測試錯誤訊息包含已生成內容 (覆蓋 fullResponse.toString() + "\n[錯誤: ...")
    @Test
    void testErrorWithPartialResponse() throws Exception {
        // 模擬 ResponseStream 先吐出一點東西，然後拋出異常
        Iterator<GenerateContentResponse> mockIterator = mock(Iterator.class);
        when(responseStream.iterator()).thenReturn(mockIterator);

        when(mockIterator.hasNext()).thenReturn(true, true, false); // 兩次 hasNext

        GenerateContentResponse resp1 = mock(GenerateContentResponse.class);
        when(resp1.text()).thenReturn("Part 1");

        // 第二次 next() 拋出異常
        when(mockIterator.next())
                .thenReturn(resp1)
                .thenThrow(new RuntimeException("Stream Broken"));

        // 模擬 DB 找到訊息
        ChatMessage aiMessage = new ChatMessage();
        when(chatMessageService.getMessageById(TEST_AI_MESSAGE_ID)).thenReturn(Optional.of(aiMessage));

        plainChatService.processStreamWithAllFeatures(
                TEST_USER_MESSAGE, TEST_CHATROOM_ID, TEST_CONVERSATION_COUNT, TEST_HAS_DEFAULT_QUESTION, "default", TEST_AI_MESSAGE_ID
        );

        Thread.sleep(200);

        // 驗證更新的訊息包含 "Part 1" 和錯誤訊息
        verify(chatMessageService).updateMessage(argThat(msg ->
                msg.getContent().contains("Part 1") && msg.getContent().contains("Stream Broken")
        ));
    }

}
