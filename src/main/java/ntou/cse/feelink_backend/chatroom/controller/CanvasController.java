package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.Canvas;
import ntou.cse.feelink_backend.chatroom.service.CanvasService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/api/canvas")
@CrossOrigin(origins = "*")
public class CanvasController {

    @Autowired
    private CanvasService canvasService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveCanvas(
            @RequestParam("chatroomId") String chatroomId,
            @RequestPart("canvasData") MultipartFile canvasFile) {
        try {
            if (chatroomId == null || chatroomId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("聊天室 ID 不能為空");
            }

            if (canvasFile == null || canvasFile.isEmpty()) {
                return ResponseEntity.badRequest().body("畫布資料不能為空");
            }

            if (canvasFile.getSize() > 50 * 1024 * 1024) { // 50MB
                return ResponseEntity.badRequest().body("畫布資料過大，請減少繪圖內容");
            }

            String jsonContent = new String(canvasFile.getBytes(), StandardCharsets.UTF_8);
            Document canvasData = Document.parse(jsonContent);

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
                return ResponseEntity.ok(canvas.get().getCanvasData().toJson());
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