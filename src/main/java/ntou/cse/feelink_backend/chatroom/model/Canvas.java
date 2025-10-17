package ntou.cse.feelink_backend.chatroom.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.bson.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "canvas")
public class Canvas {
    @Id
    private String id;

    @Field("chatroomId")
    private String chatroomId;

    @Field("canvasData")
    private Document canvasData;

    public Canvas(String chatroomId, Document canvasData) {
        this.chatroomId = chatroomId;
        this.canvasData = canvasData;
    }
}
