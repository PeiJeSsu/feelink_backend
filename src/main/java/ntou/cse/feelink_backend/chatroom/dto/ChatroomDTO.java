package ntou.cse.feelink_backend.chatroom.dto;

import ntou.cse.feelink_backend.common.enums.AIPartner;

public class ChatroomDTO {
    private String chatroomId;
    private String userId;
    private String title;
    private AIPartner aiPartner;

    // Constructors
    public ChatroomDTO() {}

    public ChatroomDTO(String chatroomId, String userId, String title, AIPartner aiPartner) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.title = title;
        this.aiPartner = aiPartner;
    }

    // Getters and Setters
    public String getChatroomId() { return chatroomId; }
    public void setChatroomId(String chatroomId) { this.chatroomId = chatroomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public AIPartner getAiPartner() { return aiPartner; }
    public void setAiPartner(AIPartner aiPartner) { this.aiPartner = aiPartner; }

    public String getAiPartnerDisplayName() {
        return aiPartner != null ? aiPartner.getDisplayName() : null;
    }

    public String getAiPartnerEnglishName() {
        return aiPartner != null ? aiPartner.getEnglishName() : null;
    }
}