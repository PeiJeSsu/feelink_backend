package ntou.cse.feelink_backend.chatroom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String messageId;

    @Field("chatroomId")
    private String chatroomId;

    @Field("isUser")
    private Boolean isUser; // true為user，false為AI

    @Field("isDrawingData")
    private Boolean isDrawingData; // true為畫布資料，false為一般訊息

    @Field("content")
    private String content; // 為訊息或畫布資料

    @Field("sentAt")
    private Date sentAt;

}