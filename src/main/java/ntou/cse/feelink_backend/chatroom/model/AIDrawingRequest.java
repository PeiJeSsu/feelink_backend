package ntou.cse.feelink_backend.chatroom.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AIDrawingRequest {

    private String text;

    @JsonProperty("imageData")
    private String imageData;

    @JsonProperty("removeBackground")
    private boolean removeBackground = false;

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

    @Override
    public String toString() {
        return "AIDrawingRequest{" +
                "text='" + text + '\'' +
                ", imageData='" + (imageData != null ? "..." : "null") + '\'' +
                ", removeBackground=" + removeBackground +
                '}';
    }
}