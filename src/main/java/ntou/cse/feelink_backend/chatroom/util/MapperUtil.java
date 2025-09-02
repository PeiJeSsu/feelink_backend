// MapperUtil.java
package ntou.cse.feelink_backend.chatroom.util;

import ntou.cse.feelink_backend.chatroom.dto.ChatMessageDTO;
import ntou.cse.feelink_backend.chatroom.dto.ChatroomDTO;
import ntou.cse.feelink_backend.chatroom.dto.UserDTO;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import ntou.cse.feelink_backend.chatroom.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class MapperUtil {

    // ChatMessage 相關轉換
    public static ChatMessageDTO toDTO(ChatMessage entity) {
        if (entity == null) return null;
        return new ChatMessageDTO(
                entity.getMessageId(),
                entity.getChatroomId(),
                entity.getContent(),
                entity.getIsUser(),
                entity.getIsDrawingData(),
                entity.getSentAt()
        );
    }

    public static ChatMessage toEntity(ChatMessageDTO dto) {
        if (dto == null) return null;
        ChatMessage entity = new ChatMessage();
        entity.setMessageId(dto.getMessageId());
        entity.setChatroomId(dto.getChatroomId());
        entity.setContent(dto.getContent());
        entity.setIsUser(dto.getIsUser());
        entity.setIsDrawingData(dto.getIsDrawingData());
        entity.setSentAt(dto.getSentAt());
        return entity;
    }

    public static List<ChatMessageDTO> toChatMessageDTOList(List<ChatMessage> entities) {
        return entities.stream()
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
    }

    // Chatroom 相關轉換
    public static ChatroomDTO toDTO(Chatroom entity) {
        if (entity == null) return null;
        return new ChatroomDTO(
                entity.getChatroomId(),
                entity.getUserId(),
                entity.getTitle()
        );
    }

    public static Chatroom toEntity(ChatroomDTO dto) {
        if (dto == null) return null;
        Chatroom entity = new Chatroom();
        entity.setChatroomId(dto.getChatroomId());
        entity.setUserId(dto.getUserId());
        entity.setTitle(dto.getTitle());
        return entity;
    }

    public static List<ChatroomDTO> toChatroomDTOList(List<Chatroom> entities) {
        return entities.stream()
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
    }

    // User 相關轉換
    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;
        return new UserDTO(
                entity.getUserId(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getAINickname()
        );
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User entity = new User();
        entity.setUserId(dto.getUserId());
        entity.setEmail(dto.getEmail());
        entity.setNickname(dto.getNickname());
        entity.setAINickname(dto.getAINickname());
        return entity;
    }

    public static List<UserDTO> toUserDTOList(List<User> entities) {
        return entities.stream()
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
    }
}