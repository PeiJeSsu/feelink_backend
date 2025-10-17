package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.Canvas;
import ntou.cse.feelink_backend.chatroom.repository.CanvasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.bson.Document;
import java.util.Optional;

@Service
public class CanvasService {

    @Autowired
    private CanvasRepository canvasRepository;

    public Canvas saveCanvas(String chatroomId, Document canvasData) {
        Optional<Canvas> existingCanvas = canvasRepository.findByChatroomId(chatroomId);

        if (existingCanvas.isPresent()) {
            Canvas canvas = existingCanvas.get();
            canvas.setCanvasData(canvasData);
            return canvasRepository.save(canvas);
        } else {
            Canvas newCanvas = new Canvas(chatroomId, canvasData);
            return canvasRepository.save(newCanvas);
        }
    }


    public Optional<Canvas> getCanvasByChatroomId(String chatroomId) {
        return canvasRepository.findByChatroomId(chatroomId);
    }

    public void deleteCanvasByChatroomId(String chatroomId) {
        Optional<Canvas> existingCanvas = canvasRepository.findByChatroomId(chatroomId);
        existingCanvas.ifPresent(canvasRepository::delete);
    }
}
