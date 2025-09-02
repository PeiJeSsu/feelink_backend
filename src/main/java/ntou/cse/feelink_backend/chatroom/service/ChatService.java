package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface ChatService {
    ChatMessage processChat(MultipartFile file, String userMessage, String chatroomId) throws IOException;
    SseEmitter processChatStream(MultipartFile file, String userMessage, String chatroomId, String aiMessageId) throws IOException;
    SseEmitter processChatStreamWithPersonality(MultipartFile file, String userMessage, String chatroomId, String personality, String aiMessageId) throws IOException;
}