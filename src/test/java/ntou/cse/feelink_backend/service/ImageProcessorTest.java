package ntou.cse.feelink_backend.service;

import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.PartMaker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageProcessorTest {

    private final ImageProcessor imageProcessor = new ImageProcessor();

    @Test
    void testProcessImage_WhenFileIsNull() throws IOException {
        assertNull(imageProcessor.processImage(null));
    }

    @Test
    void testProcessImage_WhenFileIsEmpty() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertNull(imageProcessor.processImage(file));
        verify(file, never()).getBytes();
    }

    @Test
    void testProcessImage_WhenFileIsValid() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("fake-image-content".getBytes());

        try (MockedStatic<PartMaker> partMakerMock = Mockito.mockStatic(PartMaker.class)) {
            Part mockPart = mock(Part.class);
            partMakerMock.when(() -> PartMaker.fromMimeTypeAndData(
                            "image/png", "fake-image-content".getBytes()))
                    .thenReturn(mockPart);

            Object result = imageProcessor.processImage(file);

            assertNotNull(result);
            assertTrue(result instanceof Part);
            assertEquals(mockPart, result);

            partMakerMock.verify(() -> PartMaker.fromMimeTypeAndData(
                    "image/png", "fake-image-content".getBytes()), times(1));
        }
    }
}
