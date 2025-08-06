package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessage> analyse(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam("userMessage") String userMessage,
            @RequestParam("chatroomId") String chatroomId) throws IOException {

        // 如果有檔案，先保存繪圖資料
        if (file != null && !file.isEmpty()) {
            // 將檔案轉換為 Base64 字串儲存
            String fileContent = Base64.getEncoder().encodeToString(file.getBytes());

            ChatMessage drawingMessage = new ChatMessage();
            drawingMessage.setChatroomId(chatroomId);
            drawingMessage.setIsUser(true);
            drawingMessage.setIsDrawingData(true);
            drawingMessage.setContent(fileContent);
            drawingMessage.setSentAt(new Date());
            chatMessageService.createMessage(drawingMessage);
        }

        // 如果有文字訊息，保存文字訊息
        if (userMessage != null && !userMessage.trim().isEmpty()) {
            ChatMessage textMessage = new ChatMessage();
            textMessage.setChatroomId(chatroomId);
            textMessage.setIsUser(true);
            textMessage.setIsDrawingData(false);
            textMessage.setContent(userMessage);
            textMessage.setSentAt(new Date());
            chatMessageService.createMessage(textMessage);
        }

        // 處理分析並取得 AI 回覆
        ChatMessage aiResponse = chatService.processChat(file, userMessage, chatroomId);
        aiResponse.setChatroomId(chatroomId);
        aiResponse.setIsDrawingData(false);
        aiResponse.setSentAt(new Date());

        // 保存 AI 回覆
        ChatMessage savedAiMessage = chatMessageService.createMessage(aiResponse);

        return ResponseEntity.ok(savedAiMessage);
    }
}