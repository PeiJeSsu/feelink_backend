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

    @Test
    void testSaveCanvas_NewCanvas() {
        String chatroomId = "room1";
        String canvasData = "data1";
        String imageUrl = "url1";

        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.empty());
        when(canvasRepository.save(any(Canvas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Canvas saved = canvasService.saveCanvas(chatroomId, canvasData, imageUrl);

        assertNotNull(saved);
        assertEquals(chatroomId, saved.getChatroomId());
        assertEquals(canvasData, saved.getCanvasData());
        assertEquals(imageUrl, saved.getCanvasImageUrl());

        verify(canvasRepository).save(any(Canvas.class));
    }

    @Test
    void testSaveCanvas_UpdateExistingCanvas() {
        String chatroomId = "room1";
        String newCanvasData = "data_new";
        String newImageUrl = "url_new";

        Canvas existingCanvas = new Canvas(chatroomId, "data_old", "url_old");
        existingCanvas.setId("canvas123");

        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.of(existingCanvas));
        when(canvasRepository.save(any(Canvas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Canvas updated = canvasService.saveCanvas(chatroomId, newCanvasData, newImageUrl);

        assertNotNull(updated);
        assertEquals("canvas123", updated.getId());
        assertEquals(newCanvasData, updated.getCanvasData());
        assertEquals(newImageUrl, updated.getCanvasImageUrl());

        verify(canvasRepository).save(existingCanvas);
    }


    @Test
    void testGetCanvasByChatroomId_Found() {
        String chatroomId = "room1";
        Canvas canvas = new Canvas(chatroomId, "data", "url");
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.of(canvas));

        Optional<Canvas> result = canvasService.getCanvasByChatroomId(chatroomId);

        assertTrue(result.isPresent());
        assertEquals(chatroomId, result.get().getChatroomId());
    }

    @Test
    void testGetCanvasByChatroomId_NotFound() {
        String chatroomId = "room1";
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.empty());

        Optional<Canvas> result = canvasService.getCanvasByChatroomId(chatroomId);

        assertFalse(result.isPresent());
    }


    @Test
    void testDeleteCanvasByChatroomId_Exists() {
        String chatroomId = "room1";
        Canvas canvas = new Canvas(chatroomId, "data", "url");
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.of(canvas));

        canvasService.deleteCanvasByChatroomId(chatroomId);

        verify(canvasRepository).delete(canvas);
    }

    @Test
    void testDeleteCanvasByChatroomId_NotExists() {
        String chatroomId = "room1";
        when(canvasRepository.findByChatroomId(chatroomId)).thenReturn(Optional.empty());

        canvasService.deleteCanvasByChatroomId(chatroomId);

        verify(canvasRepository, never()).delete(any());
    }
}
