package ntou.cse.feelink_backend.chatroom.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatInputProcessorTest {

    @Test
    void constructorCoverage() throws Exception {
        var constructor = ChatInputProcessor.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
    @Test
    void processText_nullOrBlank() {
        assertEquals("hint", ChatInputProcessor.processText(null, "hint"));
        assertEquals("hint", ChatInputProcessor.processText("", "hint"));
        assertEquals("hint", ChatInputProcessor.processText("   ", "hint"));
    }

    @Test
    void processText_returnsOriginalWhenNotBlank() {
        assertEquals("abc", ChatInputProcessor.processText("abc", "hint"));
    }

    @Test
    void processImage_nullOrEmpty() throws IOException {
        MultipartFile file = null;
        assertNull(ChatInputProcessor.processImage(file));

        file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertNull(ChatInputProcessor.processImage(file));
    }

    @Test
    void processImage_returnsPartMakerResult() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        byte[] data = {1,2,3};
        when(file.getBytes()).thenReturn(data);

        Object result = ChatInputProcessor.processImage(file);
        assertNotNull(result);
    }
}
