package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.service.ChatroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatrooms")
@CrossOrigin(origins = "*")
public class ChatroomController {

    @Autowired
    private ChatroomService chatroomService;

    @GetMapping
    public ResponseEntity<List<Chatroom>> getAllChatrooms() {
        List<Chatroom> chatrooms = chatroomService.getAllChatrooms();
        return ResponseEntity.ok(chatrooms);
    }

    @GetMapping("/{chatroomId}")
    public ResponseEntity<Chatroom> getChatroomById(@PathVariable String chatroomId) {
        Optional<Chatroom> chatroom = chatroomService.getChatroomById(chatroomId);
        return chatroom.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Chatroom>> getUserChatrooms(@PathVariable String userId) {
        try {
            List<Chatroom> chatrooms = chatroomService.getChatroomsByUserId(userId);
            return ResponseEntity.ok(chatrooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Chatroom> createChatroom(@RequestBody Chatroom chatroom) {
        try {
            Chatroom createdChatroom = chatroomService.createChatroom(chatroom);
            return ResponseEntity.ok(createdChatroom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{chatroomId}")
    public ResponseEntity<Chatroom> updateChatroom(@PathVariable String chatroomId, @RequestBody Chatroom chatroom) {
        try {
            chatroom.setChatroomId(chatroomId);
            Chatroom updatedChatroom = chatroomService.updateChatroom(chatroom);
            return ResponseEntity.ok(updatedChatroom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<Void> deleteChatroom(@PathVariable String chatroomId) {
        try {
            chatroomService.deleteChatroom(chatroomId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}