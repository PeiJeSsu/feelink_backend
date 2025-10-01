package ntou.cse.feelink_backend.chatroom.dto;

import ntou.cse.feelink_backend.common.enums.AIPartner;

public class UserDTO {
    private String userId;
    private String email;
    private String nickname;
    private String AINickname;
    private AIPartner preferredAIPartner;

    // Constructors
    public UserDTO() {}

    public UserDTO(String userId, String email, String nickname, String AINickname, AIPartner preferredAIPartner) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.AINickname = AINickname;
        this.preferredAIPartner = preferredAIPartner;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAINickname() { return AINickname; }
    public void setAINickname(String AINickname) { this.AINickname = AINickname; }

    public AIPartner getPreferredAIPartner() { return preferredAIPartner; }
    public void setPreferredAIPartner(AIPartner preferredAIPartner) {
        this.preferredAIPartner = preferredAIPartner;
    }

    public String getPreferredAIPartnerDisplayName() {
        return preferredAIPartner != null ? preferredAIPartner.getDisplayName() : null;
    }
}