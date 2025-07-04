package ntou.cse.feelink_backend.chatroom.model;

public class AIDrawingResponse {
    private boolean success;
    private GeminiResponse content;
    private String error;

    public AIDrawingResponse() {}

    public AIDrawingResponse(boolean success, GeminiResponse content) {
        this.success = success;
        this.content = content;
    }

    public AIDrawingResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public GeminiResponse getContent() {
        return content;
    }

    public void setContent(GeminiResponse content) {
        this.content = content;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}