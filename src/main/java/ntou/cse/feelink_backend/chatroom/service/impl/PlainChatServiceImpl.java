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
    private ChatMessageService chatMessageService;

    @Autowired
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    private final MessageFormattingService messageFormattingService = new MessageFormattingService();

    // 原本的非串流方法
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

    // 新增的串流方法 - 支援資料庫存儲
    public SseEmitter replyStreamWithDatabase(String userMessage, String chatroomId,
                                              Integer conversationCount, Boolean hasDefaultQuestion,
                                              String aiMessageId) throws IOException {

        ChatSession chatSession = registry.getSession(chatroomId);

        // 取得聊天歷史提供上下文
        List<ChatMessage> chatHistory = chatMessageService.getMessagesByChatroomId(chatroomId);

        // 如果有情緒預測服務，先進行情緒分析
        String emotionPrediction = "";
        if (emotionPredictionChatService != null) {
            try {
                emotionPrediction = emotionPredictionChatService.processChat(userMessage);
            } catch (Exception e) {
                System.err.println("情緒預測失敗: " + e.getMessage());
            }
        }

        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);

        // 如果有情緒預測結果，加入到提示中
        final String analysisPrompt;
        if (!emotionPrediction.isEmpty()) {
            analysisPrompt = formattedMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;
        } else {
            analysisPrompt = formattedMessage;
        }

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

                // 串流結束後，更新資料庫中的 AI 訊息內容
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

                emitter.complete();

            } catch (Exception e) {
                System.err.println("串流回應錯誤: " + e.getMessage());

                // 如果發生錯誤，也要更新資料庫
                try {
                    Optional<ChatMessage> aiMessageOpt = chatMessageService.getMessageById(aiMessageId);
                    if (aiMessageOpt.isPresent()) {
                        ChatMessage aiMessage = aiMessageOpt.get();
                        String errorContent = fullResponse.length() > 0 ?
                                fullResponse.toString() + "\n[錯誤: " + e.getMessage() + "]" :
                                "AI處理錯誤: " + e.getMessage();
                        aiMessage.setContent(errorContent);
                        chatMessageService.updateMessage(aiMessage);
                    }
                } catch (Exception dbError) {
                    System.err.println("更新錯誤訊息到資料庫失敗: " + dbError.getMessage());
                }

                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    // 原本的串流方法 - 保留以維持向後相容
    public SseEmitter replyStream(String userMessage, String sessionId, Integer conversationCount,
                                  Boolean hasDefaultQuestion) throws IOException {
        ChatSession chatSession = registry.getSession(sessionId);

        String emotionPrediction = "";
        if (emotionPredictionChatService != null) {
            try {
                emotionPrediction = emotionPredictionChatService.processChat(userMessage);
            } catch (Exception e) {
                System.err.println("情緒預測失敗: " + e.getMessage());
            }
        }

        String formattedMessage = messageFormattingService.getFullMessage(userMessage, conversationCount, hasDefaultQuestion);
        String analysisPrompt = formattedMessage + (emotionPrediction.isEmpty() ? "" : "\n\n先前的情緒預測結果：" + emotionPrediction);

        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                ResponseStream<GenerateContentResponse> responseStream = chatSession.sendMessageStream(analysisPrompt);
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
}