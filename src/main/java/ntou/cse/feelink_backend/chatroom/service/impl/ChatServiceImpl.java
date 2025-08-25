package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private DrawingAnalysisChatServiceImpl drawingAnalysisChatService;

    @Autowired
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final MessageFormattingService messageFormattingService = new MessageFormattingService();

    @Override
    public ChatMessage processChat(MultipartFile file, String userMessage, String chatroomId) throws IOException {
        // 取得該聊天室的歷史訊息來提供上下文
        List<ChatMessage> chatHistory = chatMessageService.getMessagesByChatroomId(chatroomId);

        // 進行情緒預測
        String emotionPrediction = emotionPredictionChatService.processChat(userMessage);

        // 建立分析提示詞，包含情緒預測結果
        String analysisPrompt = userMessage + "\n\n先前的情緒預測結果：" + emotionPrediction;

        // 進行繪圖分析，傳入 chatroomId 而非 sessionId
        String drawingAnalysisResponse = drawingAnalysisChatService.processChat(file, analysisPrompt, chatroomId);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setContent(drawingAnalysisResponse);
        aiMsg.setIsUser(false);

        return aiMsg;
    }

    @Override
    public SseEmitter processChatStreamWithPersonalityAndDatabase(
            MultipartFile file, String userMessage, String chatroomId, 
            String personality, String aiMessageId) throws IOException {

        // 取得該聊天室的歷史訊息來提供上下文
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

        // 建立分析提示詞，包含情緒預測結果
        String analysisPrompt = userMessage;
        if (!emotionPrediction.isEmpty()) {
            analysisPrompt += "\n\n先前的情緒預測結果：" + emotionPrediction;
        }

        // 宣告為 final 變數以供 lambda 使用
        final MultipartFile finalFile = file;
        final String finalAnalysisPrompt = analysisPrompt;
        final String finalChatroomId = chatroomId;
        final String finalPersonality = personality;
        final String finalAiMessageId = aiMessageId;

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 設置較長的超時時間

        new Thread(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                // 檢查是否支援個性化功能
                boolean supportsPersonality = checkPersonalitySupport();
                
                if (supportsPersonality) {
                    // 使用個性化串流分析服務
                    drawingAnalysisChatService.processChatStreamWithPersonality(
                        finalFile, finalAnalysisPrompt, finalChatroomId, finalPersonality,
                        // onToken callback
                        (token) -> {
                            fullResponse.append(token);
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                System.err.println("發送 SSE 資料失敗: " + e.getMessage());
                            }
                        },
                        // onComplete callback
                        () -> updateDatabaseAndComplete(finalAiMessageId, fullResponse, emitter),
                        // onError callback
                        (error) -> handleErrorAndUpdate(finalAiMessageId, fullResponse, error, emitter)
                    );
                } else {
                    // 使用基本串流分析服務
                    drawingAnalysisChatService.processChatStream(
                        finalFile, finalAnalysisPrompt, finalChatroomId,
                        // onToken callback
                        (token) -> {
                            fullResponse.append(token);
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                System.err.println("發送 SSE 資料失敗: " + e.getMessage());
                            }
                        },
                        // onComplete callback
                        () -> updateDatabaseAndComplete(finalAiMessageId, fullResponse, emitter),
                        // onError callback
                        (error) -> handleErrorAndUpdate(finalAiMessageId, fullResponse, error, emitter)
                    );
                }

            } catch (Exception e) {
                System.err.println("串流處理初始化錯誤: " + e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }


    @Override
    public SseEmitter processChatStream(MultipartFile file, String userMessage, String chatroomId) throws IOException {
        return processChatStreamWithPersonalityAndDatabase(file, userMessage, chatroomId, "default", null);
    }

    @Override
    public SseEmitter processChatStreamWithPersonality(MultipartFile file, String userMessage, String sessionId, String personality) throws IOException {
        return processChatStreamWithPersonalityAndDatabase(file, userMessage, sessionId, personality, null);
    }


    private boolean checkPersonalitySupport() {
        try {
            // 透過反射檢查 drawingAnalysisChatService 是否有個性化方法
            drawingAnalysisChatService.getClass().getMethod("processChatStreamWithPersonality", 
                MultipartFile.class, String.class, String.class, String.class, 
                java.util.function.Consumer.class, Runnable.class, java.util.function.Consumer.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

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

    private void handleErrorAndUpdate(String aiMessageId, StringBuilder fullResponse, Exception error, SseEmitter emitter) {
        System.err.println("串流分析錯誤: " + error.getMessage());

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