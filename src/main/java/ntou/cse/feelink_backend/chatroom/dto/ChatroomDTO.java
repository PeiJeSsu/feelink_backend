package ntou.cse.feelink_backend.chatroom.dto;

public class ChatroomDTO {
    private String chatroomId;
    private String userId;
    private String title;

    // Constructors
    public ChatroomDTO() {}

    public ChatroomDTO(String chatroomId, String userId, String title) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.title = title;
    }

    // Getters and Setters
    public String getChatroomId() { return chatroomId; }
    public void setChatroomId(String chatroomId) { this.chatroomId = chatroomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}