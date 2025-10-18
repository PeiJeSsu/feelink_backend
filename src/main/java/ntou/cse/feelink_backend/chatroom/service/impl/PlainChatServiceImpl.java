package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlainChatServiceImpl {

    private final ChatSessionRegistry registry;
    private final MessageFormattingService messageFormattingService;
    private final EmotionPredictionChatServiceImpl emotionPredictionChatService;
    private final ChatMessageService chatMessageService;
    public PlainChatServiceImpl(ChatSessionRegistry registry,
                               MessageFormattingService messageFormattingService,
                               EmotionPredictionChatServiceImpl emotionPredictionChatService,
                               ChatMessageService chatMessageService) {
        this.registry = registry;
        this.messageFormattingService = messageFormattingService;
        this.emotionPredictionChatService = emotionPredictionChatService;
        this.chatMessageService = chatMessageService;
    }
    @Autowired
    private Client client;

    /**
     * 含資料庫存儲的串流方法
     */
    public SseEmitter replyStreamWithDatabase(String userMessage, String chatroomId,
                                              Integer conversationCount, Boolean hasDefaultQuestion,
                                              String aiMessageId) throws IOException {
        return processStreamWithAllFeatures(userMessage, chatroomId, conversationCount, hasDefaultQuestion, "default", aiMessageId);
    }

    /**
     * 完整功能的串流方法 - 含個性、情緒預測和資料庫存儲
     */
    public SseEmitter processStreamWithAllFeatures(String userMessage, String chatroomId,
                                                    Integer conversationCount, Boolean hasDefaultQuestion,
                                                    String personality, String aiMessageId) throws IOException {
        // 進行情緒預測
        String emotionPrediction = "";
        if (emotionPredictionChatService != null) {
            try {
                emotionPrediction = emotionPredictionChatService.processChat(userMessage);
            } catch (Exception e) {
                System.err.println("情緒預測失敗: " + e.getMessage());
            }
        }

        GenerateContentConfig needsSearchConfig = GenerateContentConfig.builder()
                .candidateCount(1)
                .maxOutputTokens(10)
                .systemInstruction(Content.fromParts(Part.fromText("回應是或者否，一個字，判斷是否要搜尋。")))
                .temperature(0.1f)
                .build();

        GenerateContentConfig SearchConfig = GenerateContentConfig.builder()
                .candidateCount(1)
                .maxOutputTokens(100)
                .systemInstruction(Content.fromParts(Part.fromText("根據使用者的對話，重構搜尋字")))
                .build();

        GenerateContentResponse needsSearch = client.models.generateContent(
                "gemini-2.5-flash-lite",
                "分級為"+ emotionPrediction +"\n判斷以下對話是否要上網搜尋，得到的分級為3一定要上網查詢相關功能，使用者說出提供、搜尋、推薦則一定回答是，若要則回答\"是\"即可，反之則回答\"否\"\n 對話為:\n" + userMessage,
                needsSearchConfig
        );

        Chat searchChat = client.chats.create("gemini-2.5-flash-lite", SearchConfig);

        String searchResult = "";
        String decision = "否";
        try {
            if (needsSearch != null && needsSearch.text() != null) {
                decision = needsSearch.text().trim();
                System.out.println(">>> [搜尋判斷] 使用者問題: " + userMessage);
                System.out.println(">>> [搜尋判斷] AI 決定: " + decision);
            }
        } catch (Exception e) {
            System.err.println(">>> [搜尋判斷] 執行失敗: " + e.getMessage());
            decision = "否";
        }
        if (decision.contains("是")) {
            System.out.println(">>> [主流程] 需要搜尋，開始執行");
            try {
                GenerateContentResponse toSearch = searchChat.sendMessage("分級為"+ emotionPrediction +"\n根據使用者的對話，回傳要上網查找甚麼關鍵字，若分級為3搜尋諮商、音樂、文章等資源，以下為使用者的話\n" + userMessage);
                System.out.println("Tosearch"+ toSearch.text());
                searchResult = SerpApiService.search(toSearch.text());
                System.out.println(">>> [主流程] 搜尋完成，結果長度: " + searchResult.length());
            } catch (Exception e) {
                System.err.println("搜尋執行失敗: " + e.getMessage());
            }
        } else {
            System.out.println(">>> [主流程] 不需要搜尋，直接回應");
        }

        // 格式化訊息
        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);

        // 建立分析提示詞，包含情緒預測結果
        StringBuilder promptBuilder = new StringBuilder(formattedMessage);

        if (!emotionPrediction.isEmpty()) {
            promptBuilder.append("\n\n先前的情緒預測結果：").append(emotionPrediction);
        }

        if (!searchResult.isEmpty()) {
            promptBuilder.append("\n\n以下為搜尋到的結果，根據結果提供給使用者，以對應名稱的相關訊息，後面提供\"網址\"的方式\n 例如 xxxxxxxxx [https://] 的形式，可以直接點擊的網址並且不同資源要換行").append(searchResult);
        }

        final String analysisPrompt = promptBuilder.toString();
        System.out.println("analysisPrompt" + analysisPrompt);
        // 根據個性參數選擇會話
        final Chat chatSession;

        if (personality != null && !"default".equals(personality)) {
            chatSession = registry.getSession(chatroomId, personality);
        } else {
            chatSession = registry.getSession(chatroomId);
        }

        // AI 取得聊天歷史紀錄
        if (chatSession.getHistory(true).isEmpty()) {
            List<ChatMessage> chatHistory = chatMessageService.getTextMessagesByChatroomId(chatroomId);
            if (!chatHistory.isEmpty()) {
                List<Content> contentHistory = new ArrayList<>();
                contentHistory.add(Content.fromParts(Part.fromText("以下為曾經的對話紀錄: \n")));
                for (ChatMessage msg : chatHistory) {
                    String formattedContent;
                    if (msg.getIsUser()) {
                        formattedContent = "使用者: " + msg.getContent();
                    } else {
                        formattedContent = "AI: " + msg.getContent();
                    }
                    Content content = Content.fromParts(Part.fromText(formattedContent));
                    contentHistory.add(content);
                }
                try {
                    contentHistory.add(Content.fromParts(Part.fromText("請直接回應使用者，不要一直介紹自己在這裡")));
                    chatSession.sendMessage(contentHistory);
                } catch (Exception e) {
                    System.err.println("載入聊天歷史失敗: " + e.getMessage());
                }
            }
        }

        final String finalAiMessageId = aiMessageId;
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        new Thread(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(analysisPrompt);

                for (GenerateContentResponse response : responseStream) {
                    String token = response.text();
                    if (token != null && !token.isEmpty()) {
                        fullResponse.append(token);

                        // 發送 token 到前端
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            System.err.println("發送 SSE 資料失敗: " + e.getMessage());
                            break;
                        }
                    }
                }
                // 串流結束後，更新資料庫
                updateDatabaseAndComplete(finalAiMessageId, fullResponse, emitter);

            } catch (Exception e) {
                System.err.println("串流回應錯誤: " + e.getMessage());
                handleErrorAndUpdate(finalAiMessageId, fullResponse, e, emitter);
            }
        }).start();

        return emitter;
    }

    /**
     * 成功完成時更新資料庫
     */
    private void updateDatabaseAndComplete(String aiMessageId, StringBuilder fullResponse, SseEmitter emitter) {
        if (aiMessageId != null) {
            try {
                Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(aiMessageId);
                if (aiMessageOpt.isPresent()) {
                    ChatMessage aiMessage = aiMessageOpt.get();
                    aiMessage.setContent(fullResponse.toString());
                    chatMessageService.updateMessage(aiMessage);
                }
            } catch (Exception e) {
                System.err.println("更新AI訊息到資料庫失敗: " + e.getMessage());
            }
        }
        emitter.complete();
    }

    /**
     * 錯誤處理並更新資料庫
     */
    private void handleErrorAndUpdate(String aiMessageId, StringBuilder fullResponse, Exception error, SseEmitter emitter) {
        System.err.println("串流文字回應錯誤: " + error.getMessage());

        // 如果發生錯誤，也要更新資料庫
        if (aiMessageId != null) {
            try {
                Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(aiMessageId);
                if (aiMessageOpt.isPresent()) {
                    ChatMessage aiMessage = aiMessageOpt.get();
                    String errorContent = fullResponse.length() > 0 ?
                            fullResponse.toString() + "\n[錯誤: " + error.getMessage() + "]" :
                            "AI處理錯誤: " + error.getMessage();
                    aiMessage.setContent(errorContent);
                    chatMessageService.updateMessage(aiMessage);
                }
            } catch (Exception dbError) {
                System.err.println("更新錯誤訊息到資料庫失敗: " + dbError.getMessage());
            }
        }

        emitter.completeWithError(error);
    }
}
