package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.impl.PlainChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private PlainChatServiceImpl plainChatService;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping
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
}