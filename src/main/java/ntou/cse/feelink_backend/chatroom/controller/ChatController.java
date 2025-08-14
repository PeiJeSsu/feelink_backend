package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private PlainChatServiceImpl plainChatService;

    @Autowired
    private ChatMessageService chatMessageService;

    // 保留原本的非串流 API
    @PostMapping("/simple")
    public ResponseEntity<ChatMessage> chat(
            @RequestParam("userMessage") String userMessage,
            @RequestParam("chatroomId") String chatroomId,
            @RequestParam(value = "conversationCount", required = false) Integer conversationCount,
            @RequestParam(value = "hasDefaultQuestion", required = false) Boolean hasDefaultQuestion) throws IOException {

        // 先保存使用者的文字訊息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatroomId(chatroomId);
        chatMessage.setIsUser(true);
        chatMessage.setIsDrawingData(false);
        chatMessage.setContent(userMessage);
        chatMessage.setSentAt(new Date());
        chatMessageService.createMessage(chatMessage);

        // 取得 AI 回覆
        ChatMessage aiResponse = plainChatService.reply(userMessage, chatroomId, conversationCount, hasDefaultQuestion);
        aiResponse.setChatroomId(chatroomId);
        aiResponse.setIsDrawingData(false);
        aiResponse.setSentAt(new Date());

        // 保存 AI 回覆
        ChatMessage savedAiMessage = chatMessageService.createMessage(aiResponse);

        return ResponseEntity.ok(savedAiMessage);
    }

    // 新增串流 API - 支援資料庫存儲
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam("userMessage") String userMessage,
            @RequestParam("chatroomId") String chatroomId,
            @RequestParam(value = "conversationCount", required = false) Integer conversationCount,
            @RequestParam(value = "hasDefaultQuestion", required = false) Boolean hasDefaultQuestion) throws IOException {

        // 先保存使用者的文字訊息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setChatroomId(chatroomId);
        userMsg.setIsUser(true);
        userMsg.setIsDrawingData(false);
        userMsg.setContent(userMessage);
        userMsg.setSentAt(new Date());
        chatMessageService.createMessage(userMsg);

        // 建立 AI 回應的訊息記錄，先暫存空內容
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setChatroomId(chatroomId);
        aiMsg.setIsUser(false);
        aiMsg.setIsDrawingData(false);
        aiMsg.setContent(""); // 初始為空，之後會更新
        aiMsg.setSentAt(new Date());
        ChatMessage savedAiMsg = chatMessageService.createMessage(aiMsg);

        // 呼叫串流服務，並傳入儲存的訊息ID以便後續更新
        return plainChatService.replyStreamWithDatabase(
                userMessage, chatroomId, conversationCount, hasDefaultQuestion,
                savedAiMsg.getMessageId()
        );
    }
}