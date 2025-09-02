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
@Document(collection = "users")
public class User {
    @Id
    private String userId;

    @Field("nickname")
    private String nickname;

    @Field("AINickname")
    private String AINickname;

    @Field("email")
    private String email;
}