package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.dto.ChatMessageDTO;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages() {
        List<ChatMessage> messages = chatMessageService.getAllMessages();
        List<ChatMessageDTO> messageDTOs = MapperUtil.toChatMessageDTOList(messages);
        return ResponseEntity.ok(messageDTOs);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ChatMessageDTO> getMessageById(@PathVariable String messageId) {
        Optional<ChatMessage> message = chatMessageService.getMessageById(messageId);
        return message.map(msg -> ResponseEntity.ok(MapperUtil.toDTO(msg)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chatroom/{chatroomId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByChatroomId(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> messageDTOs = MapperUtil.toChatMessageDTOList(messages);
        return ResponseEntity.ok(messageDTOs);
    }

    // 只取得使用者訊息
    @GetMapping("/chatroom/{chatroomId}/user")
    public ResponseEntity<List<ChatMessageDTO>> getUserMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> userMessages = messages.stream()
                .filter(ChatMessage::getIsUser)
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userMessages);
    }

    // 只取得AI訊息
    @GetMapping("/chatroom/{chatroomId}/ai")
    public ResponseEntity<List<ChatMessageDTO>> getAIMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> aiMessages = messages.stream()
                .filter(msg -> !msg.getIsUser())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(aiMessages);
    }

    // 新增：只取得一般文字訊息（非畫布資料）
    @GetMapping("/chatroom/{chatroomId}/text")
    public ResponseEntity<List<ChatMessageDTO>> getTextMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> textMessages = messages.stream()
                .filter(msg -> !msg.getIsDrawingData())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(textMessages);
    }

    // 新增：只取得畫布資料
    @GetMapping("/chatroom/{chatroomId}/drawing")
    public ResponseEntity<List<ChatMessageDTO>> getDrawingMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> drawingMessages = messages.stream()
                .filter(ChatMessage::getIsDrawingData)
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(drawingMessages);
    }

    // 新增：取得使用者的畫布資料
    @GetMapping("/chatroom/{chatroomId}/user/drawing")
    public ResponseEntity<List<ChatMessageDTO>> getUserDrawingMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> userDrawingMessages = messages.stream()
                .filter(msg -> msg.getIsUser() && msg.getIsDrawingData())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDrawingMessages);
    }

    // 新增：取得使用者的文字訊息
    @GetMapping("/chatroom/{chatroomId}/user/text")
    public ResponseEntity<List<ChatMessageDTO>> getUserTextMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> userTextMessages = messages.stream()
                .filter(msg -> msg.getIsUser() && !msg.getIsDrawingData())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userTextMessages);
    }

    @PostMapping
    public ResponseEntity<ChatMessageDTO> createMessage(@RequestBody ChatMessageDTO messageDTO) {
        ChatMessage message = MapperUtil.toEntity(messageDTO);
        ChatMessage createdMessage = chatMessageService.createMessage(message);
        ChatMessageDTO createdMessageDTO = MapperUtil.toDTO(createdMessage);
        return ResponseEntity.ok(createdMessageDTO);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ChatMessageDTO> updateMessage(@PathVariable String messageId, @RequestBody ChatMessageDTO messageDTO) {
        messageDTO.setMessageId(messageId);
        ChatMessage message = MapperUtil.toEntity(messageDTO);
        ChatMessage updatedMessage = chatMessageService.updateMessage(message);
        ChatMessageDTO updatedMessageDTO = MapperUtil.toDTO(updatedMessage);
        return ResponseEntity.ok(updatedMessageDTO);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        chatMessageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    // 🎯 新增：刪除聊天室的所有訊息
    @DeleteMapping("/chatroom/{chatroomId}")
    public ResponseEntity<Void> deleteAllMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteAllMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除聊天室訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🎯 新增：刪除聊天室的使用者訊息
    @DeleteMapping("/chatroom/{chatroomId}/user")
    public ResponseEntity<Void> deleteUserMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteUserMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條使用者訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除使用者訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🎯 新增：刪除聊天室的AI訊息
    @DeleteMapping("/chatroom/{chatroomId}/ai")
    public ResponseEntity<Void> deleteAIMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteAIMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條AI訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除AI訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🎯 新增：只刪除聊天室的文字訊息
    @DeleteMapping("/chatroom/{chatroomId}/text")
    public ResponseEntity<Void> deleteTextMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteTextMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條文字訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除文字訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🎯 新增：只刪除聊天室的畫布訊息
    @DeleteMapping("/chatroom/{chatroomId}/drawing")
    public ResponseEntity<Void> deleteDrawingMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteDrawingMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條畫布訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除畫布訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}