package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.Canvas;
import ntou.cse.feelink_backend.chatroom.repository.CanvasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CanvasServiceTest {

    @Mock
    private CanvasRepository canvasRepository;

    @InjectMocks
    private CanvasService canvasService;

    // === 測試 saveCanvas ===

    @Test
    void testSaveCanvas_NewCanvas() {
        // Arrange
        String chatroomId = "room1";
        String canvasData = "data1";
        String imageUrl = "url1";

        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.empty());
        when(canvasRepository.save(any(Canvas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Canvas saved = canvasService.saveCanvas(chatroomId, canvasData, imageUrl);

        // Assert
        assertNotNull(saved);
        assertEquals(chatroomId, saved.getChatroomId());
        assertEquals(canvasData, saved.getCanvasData());
        assertEquals(imageUrl, saved.getCanvasImageUrl());

        verify(canvasRepository).save(any(Canvas.class));
    }

    @Test
    void testSaveCanvas_UpdateExistingCanvas() {
        // Arrange
        String chatroomId = "room1";
        String newCanvasData = "data_new";
        String newImageUrl = "url_new";

        Canvas existingCanvas = new Canvas(chatroomId, "data_old", "url_old");
        // 假設有 ID 代表是已存在的資料
        existingCanvas.setId("canvas123");

        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.of(existingCanvas));
        when(canvasRepository.save(any(Canvas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Canvas updated = canvasService.saveCanvas(chatroomId, newCanvasData, newImageUrl);

        // Assert
        assertNotNull(updated);
        assertEquals("canvas123", updated.getId()); // ID 應該保持不變
        assertEquals(newCanvasData, updated.getCanvasData()); // 內容更新
        assertEquals(newImageUrl, updated.getCanvasImageUrl());

        verify(canvasRepository).save(existingCanvas);
    }

    // === 測試 getCanvasByChatroomId ===

    @Test
    void testGetCanvasByChatroomId_Found() {
        // Arrange
        String chatroomId = "room1";
        Canvas canvas = new Canvas(chatroomId, "data", "url");
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.of(canvas));

        // Act
        Optional<Canvas> result = canvasService.getCanvasByChatroomId(chatroomId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(chatroomId, result.get().getChatroomId());
    }

    @Test
    void testGetCanvasByChatroomId_NotFound() {
        // Arrange
        String chatroomId = "room1";
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.empty());

        // Act
        Optional<Canvas> result = canvasService.getCanvasByChatroomId(chatroomId);

        // Assert
        assertFalse(result.isPresent());
    }

    // === 測試 deleteCanvasByChatroomId ===

    @Test
    void testDeleteCanvasByChatroomId_Exists() {
        // Arrange
        String chatroomId = "room1";
        Canvas canvas = new Canvas(chatroomId, "data", "url");
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.of(canvas));

        // Act
        canvasService.deleteCanvasByChatroomId(chatroomId);

        // Assert
        verify(canvasRepository).delete(canvas);
    }

    @Test
    void testDeleteCanvasByChatroomId_NotExists() {
        // Arrange
        String chatroomId = "room1";
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.empty());

        // Act
        canvasService.deleteCanvasByChatroomId(chatroomId);

        // Assert
        verify(canvasRepository, never()).delete(any());
    }
}
