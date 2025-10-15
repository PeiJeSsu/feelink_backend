package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.Canvas;
import ntou.cse.feelink_backend.chatroom.service.CanvasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/canvas")
@CrossOrigin(origins = "*")
public class CanvasController {

    @Autowired
    private CanvasService canvasService;

    @PostMapping("/save")
    public ResponseEntity<String> saveCanvas(@RequestBody Map<String, String> request) {
        try {
            String chatroomId = request.get("chatroomId");
            String canvasData = request.get("canvasData");

            if (chatroomId == null || chatroomId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("聊天室 ID 不能為空");
            }

            if (canvasData == null) {
                return ResponseEntity.badRequest().body("畫布資料不能為空");
            }

            canvasService.saveCanvas(chatroomId, canvasData);
            return ResponseEntity.ok("畫布儲存成功");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("畫布儲存失敗: " + e.getMessage());
        }
    }

    @GetMapping("/chatroom/{chatroomId}")
    public ResponseEntity<String> getChatroomCanvas(@PathVariable String chatroomId) {
        try {
            if (chatroomId == null || chatroomId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("聊天室 ID 不能為空");
            }

            Optional<Canvas> canvas = canvasService.getCanvasByChatroomId(chatroomId);

            if (canvas.isPresent()) {
                return ResponseEntity.ok(canvas.get().getCanvasData());
            } else {
                return ResponseEntity.ok("");
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("取得畫布失敗: " + e.getMessage());
        }
    }

    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<String> deleteCanvas(@PathVariable String chatroomId) {
        try {
            if (chatroomId == null || chatroomId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("聊天室 ID 不能為空");
            }

            canvasService.deleteCanvasByChatroomId(chatroomId);
            return ResponseEntity.ok("畫布刪除成功");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("刪除畫布失敗: " + e.getMessage());
        }
    }
}