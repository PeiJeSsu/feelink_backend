package ntou.cse.feelink_backend.chatroom.service.impl;

import com.google.genai.Chat;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;
import ntou.cse.feelink_backend.chatroom.service.ChatSessionRegistry;
import ntou.cse.feelink_backend.chatroom.service.ChatInputProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private DrawingAnalysisChatServiceImpl drawingAnalysisChatService;

    @Autowired
    private EmotionPredictionChatServiceImpl emotionPredictionChatService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatSessionRegistry registry;

    // 為每個聊天室添加鎖機制，防止並發問題
    private final ConcurrentHashMap<String, ReentrantLock> chatroomLocks = new ConcurrentHashMap<>();

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
    public SseEmitter processChatStream(MultipartFile file, String userMessage, String chatroomId, String aiMessageId) throws IOException {
        return processChatStreamWithPersonality(file, userMessage, chatroomId, "default", aiMessageId);
    }

    @Override
    public SseEmitter processChatStreamWithPersonality(MultipartFile file, String userMessage, String chatroomId, String personality, String aiMessageId) throws IOException {

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
        final String finalAiMessageId = aiMessageId;

        final Chat chatSession;

        if (personality != null && !"default".equals(personality)) {
            chatSession = registry.getSession(chatroomId, personality);
        } else {
            chatSession = registry.getSession(chatroomId);
        }

        if (chatSession.getHistory(true).isEmpty()) {
            List<ChatMessage> chatHistory = chatMessageService.getTextMessagesByChatroomId(chatroomId);
            if (!chatHistory.isEmpty()) {
                List<Content> contentHistory = new ArrayList<>();
                for (ChatMessage msg : chatHistory) {
                    String formattedContent;
                    if (msg.getIsUser()) {
                        formattedContent = "使用者: " + msg.getContent();
                    } else {
                        formattedContent = "AI: " + msg.getContent();
                    }
                    Content content = Content.fromParts(Part.fromText("以下為曾經彼此的對話紀錄: \n"+formattedContent));
                    contentHistory.add(content);
                }
                try {
                    chatSession.sendMessage(contentHistory);
                } catch (Exception e) {
                    System.err.println("載入聊天歷史失敗: " + e.getMessage());
                }
            }
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        new Thread(() -> {
            StringBuilder fullResponse = new StringBuilder();
            // 獲取該聊天室的鎖
            ReentrantLock lock = chatroomLocks.computeIfAbsent(finalChatroomId, k -> new ReentrantLock());

            try {
                // 加鎖防止同時處理同一個聊天室的請求
                lock.lock();

                String processedText = finalAnalysisPrompt;
                Part processedImage = ChatInputProcessor.processImage(file);

                // 處理圖片
                if (finalFile != null && !finalFile.isEmpty()) {
                    try {
                        processedImage = ChatInputProcessor.processImage(finalFile);
                    } catch (Exception e) {
                        System.err.println("圖片處理失敗: " + e.getMessage());
                    }
                }

                // 發送請求並處理回應串流
                ResponseStream<GenerateContentResponse> responseStream;
                if (processedImage != null) {
                    Content content = Content.fromParts(Part.fromText(processedText), processedImage);
                    responseStream = chatSession.sendMessageStream(content);
                } else {
                    responseStream = chatSession.sendMessageStream(processedText);
                }

                // 確保完整消費串流
                boolean streamConsumed = false;
                try {
                    // 使用增強型 for 迴圈完全消費串流
                    for (GenerateContentResponse response : responseStream) {
                        String token = response.text();
                        if (token != null && !token.isEmpty()) {
                            fullResponse.append(token);
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                System.err.println("發送 SSE 資料失敗: " + e.getMessage());
                                // 即使發送失敗也要繼續消費串流
                            }
                        }
                    }
                    streamConsumed = true;
                    System.out.println("串流已完全消費，聊天室: " + finalChatroomId);
                } catch (Exception streamError) {
                    System.err.println("消費串流時發生錯誤: " + streamError.getMessage());
                    // 如果串流消費過程中出錯，確保拋出異常讓外層處理
                    throw streamError;
                }

                if (!streamConsumed) {
                    System.err.println("警告: 串流可能未完全消費");
                }

                // 更新資料庫
                updateDatabaseAndComplete(finalAiMessageId, fullResponse, emitter);

            } catch (Exception e) {
                System.err.println("串流處理錯誤: " + e.getMessage());
                handleErrorAndUpdate(finalAiMessageId, fullResponse, e, emitter);
            } finally {
                // 確保釋放鎖
                lock.unlock();

                // 清理過期的鎖（可選的優化）
                if (!lock.hasQueuedThreads()) {
                    chatroomLocks.remove(finalChatroomId, lock);
                }
            }
        }).start();

        return emitter;
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

        try {
            emitter.complete();
        } catch (Exception e) {
            System.err.println("完成 SSE 回應失敗: " + e.getMessage());
        }
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

        try {
            emitter.completeWithError(error);
        } catch (Exception e) {
            System.err.println("發送錯誤回應失敗: " + e.getMessage());
        }
    }
}