package com.example.chatapp.model;

public class ChatMessage {
    private Long id;
    private String content;
    private boolean isUser;
    private String sessionId;

    public ChatMessage() {
    }

    public ChatMessage(String content, boolean isUser, String sessionId) {
        this.content = content;
        this.isUser = isUser;
        this.sessionId = sessionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}