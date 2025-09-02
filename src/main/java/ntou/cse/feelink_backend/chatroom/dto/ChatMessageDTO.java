package ntou.cse.feelink_backend.chatroom.dto;

import java.util.Date;

public class ChatMessageDTO {
    private String messageId;
    private String chatroomId;
    private String content;
    private Boolean isUser;
    private Boolean isDrawingData;
    private Date sentAt;

    // Constructors
    public ChatMessageDTO() {}

    public ChatMessageDTO(String messageId, String chatroomId, String content,
                          Boolean isUser, Boolean isDrawingData, Date sentAt) {
        this.messageId = messageId;
        this.chatroomId = chatroomId;
        this.content = content;
        this.isUser = isUser;
        this.isDrawingData = isDrawingData;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatroomId() { return chatroomId; }
    public void setChatroomId(String chatroomId) { this.chatroomId = chatroomId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsUser() { return isUser; }
    public void setIsUser(Boolean isUser) { this.isUser = isUser; }

    public Boolean getIsDrawingData() { return isDrawingData; }
    public void setIsDrawingData(Boolean isDrawingData) { this.isDrawingData = isDrawingData; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
}