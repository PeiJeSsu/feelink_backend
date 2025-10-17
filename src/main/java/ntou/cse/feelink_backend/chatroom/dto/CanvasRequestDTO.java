package ntou.cse.feelink_backend.chatroom.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CanvasRequestDTO {
    private String chatroomId;
    private Map<String, Object> canvasData;
}