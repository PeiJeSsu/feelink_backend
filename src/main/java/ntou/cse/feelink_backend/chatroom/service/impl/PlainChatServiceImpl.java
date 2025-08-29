package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PlainChatServiceImpl {

    @Autowired
    private ChatSessionRegistry registry;

    @Autowired
    private MessageFormattingService messageFormattingService;

    @Autowired
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 原本的非串流方法 - 保持向後兼容性
     */
    public ChatMessage reply(String userMessage, String chatroomId, Integer conversationCount, Boolean hasDefaultQuestion) {
        ChatSession chatSession = registry.getSession(chatroomId);
        List<ChatMessage> chatHistory = chatMessageService.getMessagesByChatroomId(chatroomId);
        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setIsUser(false);

        try {
            GenerateContentResponse resp = chatSession.sendMessage(formattedMessage);
            aiMsg.setContent(ResponseHandler.getText(resp));
        } catch (Exception e) {
            aiMsg.setContent("AI處理錯誤: " + e.getMessage());
        }

        return aiMsg;
    }

    /**
     * 基本串流方法 - 不含個性和資料庫存儲
     */
    public SseEmitter replyStream(String userMessage, String chatroomId, Integer conversationCount, Boolean hasDefaultQuestion) throws IOException {
        ChatSession chatSession = registry.getSession(chatroomId);
        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(formattedMessage);
                for (GenerateContentResponse response : responseStream) {
                    String token = ResponseHandler.getText(response);
                    if (token != null && !token.isEmpty()) {
                        emitter.send(SseEmitter.event().data(token));
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    /**
     * 完整功能串流方法 - 包含個性、情緒預測和資料庫存儲
     */
    public SseEmitter replyStreamWithPersonality(String userMessage, String chatroomId,
                                                 Integer conversationCount, Boolean hasDefaultQuestion,
                                                 String personality, String aiMessageId) throws IOException {
        return processStreamWithAllFeatures(userMessage, chatroomId, conversationCount, hasDefaultQuestion, personality, aiMessageId);
    }

    /**
     * 串流方法 - 只包含資料庫存儲，使用預設個性
     */
    public SseEmitter replyStreamWithDatabase(String userMessage, String chatroomId,
                                              Integer conversationCount, Boolean hasDefaultQuestion,
                                              String aiMessageId) throws IOException {
        return processStreamWithAllFeatures(userMessage, chatroomId, conversationCount, hasDefaultQuestion, "default", aiMessageId);
    }

    /**
     * 核心處理方法 - 整合所有功能
     */
    public SseEmitter processStreamWithAllFeatures(String userMessage, String chatroomId,
                                                    Integer conversationCount, Boolean hasDefaultQuestion,
                                                    String personality, String aiMessageId) throws IOException {

        // 取得聊天歷史提供上下文
        List<ChatMessage> chatHistory = chatMessageService.getMessagesByChatroomId(chatroomId);

        // 進行情緒預測
        String emotionPrediction = "";
        if (emotionPredictionChatService != null) {
            try {
                emotionPrediction = emotionPredictionChatService.processChat(userMessage);
            } catch (Exception e) {
                System.err.println("情緒預測失敗: " + e.getMessage());
            }
        }

        // 格式化訊息
        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);

        // 建立分析提示詞，包含情緒預測結果
        final String analysisPrompt;
        if (!emotionPrediction.isEmpty()) {
            analysisPrompt = formattedMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        } else {
            analysisPrompt = formattedMessage;
        }

        // 根據個性參數選擇會話
        final ChatSession chatSession;
        if (personality != null && !"default".equals(personality)) {
            chatSession = registry.getSession(chatroomId, personality);
        } else {
            chatSession = registry.getSession(chatroomId);
        }

        final String finalAiMessageId = aiMessageId;
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 設置較長的超時時間

        new Thread(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(analysisPrompt);

                for (GenerateContentResponse response : responseStream) {
                    String token = ResponseHandler.getText(response);
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