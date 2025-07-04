package ntou.cse.feelink_backend.chatroom.model;

public class AIDrawingRequest {
    private String text;
    private String imageData;

    public AIDrawingRequest() {}

    public AIDrawingRequest(String text, String imageData) {
        this.text = text;
        this.imageData = imageData;
    }

    // Getters and Setters
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
}