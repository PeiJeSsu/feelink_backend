package ntou.cse.feelink_backend.chatroom.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GeminiResponse model
 */
class GeminiResponseTest {

    private GeminiResponse geminiResponse;

    @BeforeEach
    void setUp() {
        geminiResponse = new GeminiResponse();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(geminiResponse);
        assertFalse(geminiResponse.isSuccess());
        assertNull(geminiResponse.getMessage());
        assertNull(geminiResponse.getImageData());
        assertNull(geminiResponse.getError());
    }

    @Test
    void testParameterizedConstructor() {
        GeminiResponse response = new GeminiResponse(true, "Success message", "base64ImageData");

        assertTrue(response.isSuccess());
        assertEquals("Success message", response.getMessage());
        assertEquals("base64ImageData", response.getImageData());
        assertNull(response.getError());
    }

    @Test
    void testSettersAndGetters() {
        geminiResponse.setSuccess(true);
        geminiResponse.setMessage("Test message");
        geminiResponse.setImageData("test image data");
        geminiResponse.setError("test error");

        assertTrue(geminiResponse.isSuccess());
        assertEquals("Test message", geminiResponse.getMessage());
        assertEquals("test image data", geminiResponse.getImageData());
        assertEquals("test error", geminiResponse.getError());
    }

    @Test
    void testSuccessScenario() {
        geminiResponse.setSuccess(true);
        geminiResponse.setMessage("Drawing generated successfully");
        geminiResponse.setImageData("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        assertTrue(geminiResponse.isSuccess());
        assertNotNull(geminiResponse.getMessage());
        assertNotNull(geminiResponse.getImageData());
        assertNull(geminiResponse.getError());
        assertTrue(geminiResponse.getImageData().startsWith("data:image/png;base64"));
    }

    @Test
    void testErrorScenario() {
        geminiResponse.setSuccess(false);
        geminiResponse.setError("Failed to generate drawing");

        assertFalse(geminiResponse.isSuccess());
        assertEquals("Failed to generate drawing", geminiResponse.getError());
        assertNull(geminiResponse.getMessage());
        assertNull(geminiResponse.getImageData());
    }

    @Test
    void testPartialSuccessScenario() {
        // Success with message but no image data
        geminiResponse.setSuccess(true);
        geminiResponse.setMessage("Request processed but no image generated");
        geminiResponse.setImageData(null);

        assertTrue(geminiResponse.isSuccess());
        assertNotNull(geminiResponse.getMessage());
        assertNull(geminiResponse.getImageData());
    }

    @Test
    void testNullValues() {
        geminiResponse.setSuccess(false);
        geminiResponse.setMessage(null);
        geminiResponse.setImageData(null);
        geminiResponse.setError(null);

        assertFalse(geminiResponse.isSuccess());
        assertNull(geminiResponse.getMessage());
        assertNull(geminiResponse.getImageData());
        assertNull(geminiResponse.getError());
    }

    @Test
    void testEmptyStrings() {
        geminiResponse.setMessage("");
        geminiResponse.setImageData("");
        geminiResponse.setError("");

        assertEquals("", geminiResponse.getMessage());
        assertEquals("", geminiResponse.getImageData());
        assertEquals("", geminiResponse.getError());
    }

    @Test
    void testLongImageData() {
        StringBuilder longImageData = new StringBuilder("data:image/jpeg;base64,");
        // Simulate a long base64 string
        for (int i = 0; i < 1000; i++) {
            longImageData.append("A");
        }

        geminiResponse.setImageData(longImageData.toString());
        assertEquals(longImageData.toString(), geminiResponse.getImageData());
        assertTrue(geminiResponse.getImageData().length() > 1000);
    }

    @Test
    void testDifferentImageFormats() {
        String[] imageFormats = {
                "data:image/png;base64,iVBORw0KGgo=",
                "data:image/jpeg;base64,/9j/4AAQSkZJRg=",
                "data:image/gif;base64,R0lGODlhAQABAIA=",
                "data:image/webp;base64,UklGRh4AAABXRUJQVlA="
        };

        for (String format : imageFormats) {
            geminiResponse.setImageData(format);
            assertEquals(format, geminiResponse.getImageData());
            assertTrue(geminiResponse.getImageData().startsWith("data:image/"));
        }
    }

    @Test
    void testErrorMessages() {
        String[] errorMessages = {
                "Network timeout",
                "Invalid API key",
                "Rate limit exceeded",
                "Internal server error",
                "Invalid image format"
        };

        for (String error : errorMessages) {
            geminiResponse.setError(error);
            assertEquals(error, geminiResponse.getError());
        }
    }

    @Test
    void testResponseChaining() {
        // Test that you can set success and then change to error
        geminiResponse.setSuccess(true);
        geminiResponse.setMessage("Initially successful");

        assertTrue(geminiResponse.isSuccess());
        assertEquals("Initially successful", geminiResponse.getMessage());

        // Change to error state
        geminiResponse.setSuccess(false);
        geminiResponse.setError("Something went wrong");
        geminiResponse.setMessage(null);

        assertFalse(geminiResponse.isSuccess());
        assertEquals("Something went wrong", geminiResponse.getError());
        assertNull(geminiResponse.getMessage());
    }
}