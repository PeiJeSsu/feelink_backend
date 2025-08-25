package ntou.cse.feelink_backend.chatroom.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AIDrawingRequest {
    private String text;
    
    @JsonProperty("imageData")
    private String imageData;
    
    @JsonProperty("removeBackground")
    private boolean removeBackground = false;
    
    @JsonProperty("chatroomId")
    private String chatroomId;
    
    @JsonProperty("mode")
    private String mode = "drawing";
    
    // 建構子
    public AIDrawingRequest() {}
    
    public AIDrawingRequest(String text, String imageData) {
        this.text = text;
        this.imageData = imageData;
    }
    
    public AIDrawingRequest(String text, String imageData, boolean removeBackground) {
        this.text = text;
        this.imageData = imageData;
        this.removeBackground = removeBackground;
    }
    
    public AIDrawingRequest(String text, String imageData, boolean removeBackground, String chatroomId) {
        this.text = text;
        this.imageData = imageData;
        this.removeBackground = removeBackground;
        this.chatroomId = chatroomId;
    }
    
    // 新增包含所有參數的建構子
    public AIDrawingRequest(String text, String imageData, boolean removeBackground, String chatroomId, String mode) {
        this.text = text;
        this.imageData = imageData;
        this.removeBackground = removeBackground;
        this.chatroomId = chatroomId;
        this.mode = mode;
    }
    
    // Getter 和 Setter
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getImageData() {
        return imageData;
    }
    
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
    
    public boolean isRemoveBackground() {
        return removeBackground;
    }
    
    public void setRemoveBackground(boolean removeBackground) {
        this.removeBackground = removeBackground;
    }
    
    public String getChatroomId() {
        return chatroomId;
    }
    
    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    @Override
    public String toString() {
        return "AIDrawingRequest{" +
                "text='" + text + '\'' +
                ", imageData='" + (imageData != null ? "..." : "null") + '\'' +
                ", removeBackground=" + removeBackground +
                ", chatroomId='" + chatroomId + '\'' +
                ", mode='" + mode + '\'' +
                '}';
    }
}