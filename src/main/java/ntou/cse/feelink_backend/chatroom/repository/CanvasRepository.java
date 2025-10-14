package ntou.cse.feelink_backend.chatroom.repository;

import ntou.cse.feelink_backend.chatroom.model.Canvas;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CanvasRepository extends MongoRepository<Canvas, String> {
    Optional<Canvas> findByChatroomId(String chatroomId);
}