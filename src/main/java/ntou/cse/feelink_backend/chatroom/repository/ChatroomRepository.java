package ntou.cse.feelink_backend.chatroom.repository;

import ntou.cse.feelink_backend.chatroom.model.Chatroom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatroomRepository extends MongoRepository<Chatroom, String> {
    List<Chatroom> findByUserId(String userId);
}