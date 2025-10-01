package ntou.cse.feelink_backend.chatroom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import ntou.cse.feelink_backend.common.enums.AIPartner;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chatrooms")
public class Chatroom {
    @Id
    private String chatroomId;

    @Field("userId")
    private String userId;

    @Field("title")
    private String title;

    @Field("aiPartner")
    private AIPartner aiPartner;
}