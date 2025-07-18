package ntou.cse.feelink_backend.chatroom.model;

public class GeminiResponse {
    private boolean success;
    private String message;
    private String imageData;
    private String error;

    public GeminiResponse() {}

    public GeminiResponse(boolean success, String message, String imageData) {
        this.success = success;
        this.message = message;
        this.imageData = imageData;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

// Request DTO


