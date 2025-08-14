package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface ChatService {
    // 原本的非串流處理方法
    ChatMessage processChat(MultipartFile file, String userMessage, String chatroomId) throws IOException;

    // 新增的串流處理方法
    SseEmitter processChatStream(MultipartFile file, String userMessage, String chatroomId, String aiMessageId) throws IOException;
}