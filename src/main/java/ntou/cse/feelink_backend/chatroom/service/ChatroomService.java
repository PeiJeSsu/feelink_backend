package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.repository.ChatroomRepository;
import ntou.cse.feelink_backend.chatroom.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ChatroomService {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public List<Chatroom> getAllChatrooms() {
        return chatroomRepository.findAll();
    }

    public Optional<Chatroom> getChatroomById(String chatroomId) {
        return chatroomRepository.findById(chatroomId);
    }

    public List<Chatroom> getChatroomsByUserId(String userId) {
        return chatroomRepository.findByUserId(userId);
    }

    public Chatroom createChatroom(Chatroom chatroom) {
        return chatroomRepository.save(chatroom);
    }

    public Chatroom updateChatroom(Chatroom chatroom) {
        return chatroomRepository.save(chatroom);
    }

    public void deleteChatroom(String chatroomId) {
        // 刪除相關的訊息和分析
        chatMessageRepository.deleteByChatroomId(chatroomId);
        // 刪除聊天室
        chatroomRepository.deleteById(chatroomId);
    }

}