package ntou.cse.feelink_backend.chatroom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "canvas")
public class Canvas {
    @Id
    private String id;

    @Field("chatroomId")
    private String chatroomId;

    @Field("canvasData")
    private String canvasData;

    @Field("canvasImageUrl")
    private String canvasImageUrl;

    public Canvas(String chatroomId, String canvasData) {
        this.chatroomId = chatroomId;
        this.canvasData = canvasData;
    }

    public Canvas(String chatroomId, String canvasData, String canvasImageUrl) {
        this.chatroomId = chatroomId;
        this.canvasData = canvasData;
        this.canvasImageUrl = canvasImageUrl;
    }
}