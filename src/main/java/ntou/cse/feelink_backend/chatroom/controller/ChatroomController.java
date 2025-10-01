package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.dto.ChatroomDTO;
import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.service.ChatroomService;
import ntou.cse.feelink_backend.chatroom.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatroomController {

    @Autowired
    private ChatroomService chatroomService;

    @GetMapping
    public ResponseEntity<List<ChatroomDTO>> getAllChatrooms() {
        List<Chatroom> chatrooms = chatroomService.getAllChatrooms();
        List<ChatroomDTO> chatroomDTOs = MapperUtil.toChatroomDTOList(chatrooms);
        return ResponseEntity.ok(chatroomDTOs);
    }

    @GetMapping("/{chatroomId}")
    public ResponseEntity<ChatroomDTO> getChatroomById(@PathVariable String chatroomId) {
        Optional<Chatroom> chatroom = chatroomService.getChatroomById(chatroomId);
        return chatroom.map(room -> ResponseEntity.ok(MapperUtil.toDTO(room)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatroomDTO>> getUserChatrooms(@PathVariable String userId) {
        try {
            List<Chatroom> chatrooms = chatroomService.getChatroomsByUserId(userId);
            List<ChatroomDTO> chatroomDTOs = MapperUtil.toChatroomDTOList(chatrooms);
            return ResponseEntity.ok(chatroomDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<ChatroomDTO> createChatroom(@RequestBody ChatroomDTO chatroomDTO) {
        try {
            Chatroom chatroom = MapperUtil.toEntity(chatroomDTO);
            Chatroom createdChatroom = chatroomService.createChatroom(chatroom);
            ChatroomDTO createdChatroomDTO = MapperUtil.toDTO(createdChatroom);
            return ResponseEntity.ok(createdChatroomDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{chatroomId}")
    public ResponseEntity<ChatroomDTO> updateChatroom(@PathVariable String chatroomId, @RequestBody ChatroomDTO chatroomDTO) {
        try {
            Chatroom updatedChatroom = chatroomService.updateChatroomTitle(chatroomId, chatroomDTO.getTitle());
            ChatroomDTO updatedChatroomDTO = MapperUtil.toDTO(updatedChatroom);
            return ResponseEntity.ok(updatedChatroomDTO);
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
