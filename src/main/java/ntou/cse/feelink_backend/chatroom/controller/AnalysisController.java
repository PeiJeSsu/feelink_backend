package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatService;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    // 保留原本的非串流 API
    @PostMapping(value = "/simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    // 新增串流 API - 支援資料庫存儲
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyseStream(
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

        // 建立 AI 回應的訊息記錄，先暫存空內容
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setChatroomId(chatroomId);
        aiMsg.setIsUser(false);
        aiMsg.setIsDrawingData(false);
        aiMsg.setContent(""); // 初始為空，之後會更新
        aiMsg.setSentAt(new Date());
        ChatMessage savedAiMsg = chatMessageService.createMessage(aiMsg);

        // 呼叫串流服務，並傳入儲存的訊息ID以便後續更新
        return chatService.processChatStream(file, userMessage, chatroomId, savedAiMsg.getMessageId());
    }
}