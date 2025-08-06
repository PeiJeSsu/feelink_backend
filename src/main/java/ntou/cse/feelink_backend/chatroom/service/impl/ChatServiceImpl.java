package ntou.cse.feelink_backend.chatroom.service.impl;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ntou.cse.feelink_backend.chatroom.service.MessageFormattingService;

import java.io.IOException;
import java.util.List;

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
}