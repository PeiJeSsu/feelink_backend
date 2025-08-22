package ntou.cse.feelink_backend.chatroom.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


class AIDrawingRequestTest {

    private AIDrawingRequest aiDrawingRequest;

    @BeforeEach
    void setUp() {
        aiDrawingRequest = new AIDrawingRequest();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(aiDrawingRequest);
        assertNull(aiDrawingRequest.getText());
        assertNull(aiDrawingRequest.getImageData());
        assertFalse(aiDrawingRequest.isRemoveBackground());
        assertNull(aiDrawingRequest.getChatroomId());
    }

    @Test
    void testTwoParameterConstructor() {
        AIDrawingRequest request = new AIDrawingRequest("test text", "image data");

        assertEquals("test text", request.getText());
        assertEquals("image data", request.getImageData());
        assertFalse(request.isRemoveBackground());
        assertNull(request.getChatroomId());
    }

    @Test
    void testThreeParameterConstructor() {
        AIDrawingRequest request = new AIDrawingRequest("test text", "image data", true);

        assertEquals("test text", request.getText());
        assertEquals("image data", request.getImageData());
        assertTrue(request.isRemoveBackground());
        assertNull(request.getChatroomId());
    }

    @Test
    void testFourParameterConstructor() {
        AIDrawingRequest request = new AIDrawingRequest("test text", "image data", true, "chatroom123");

        assertEquals("test text", request.getText());
        assertEquals("image data", request.getImageData());
        assertTrue(request.isRemoveBackground());
        assertEquals("chatroom123", request.getChatroomId());
    }

    @Test
    void testSettersAndGetters() {
        aiDrawingRequest.setText("new text");
        aiDrawingRequest.setImageData("new image data");
        aiDrawingRequest.setRemoveBackground(true);
        aiDrawingRequest.setChatroomId("newChatroom");

        assertEquals("new text", aiDrawingRequest.getText());
        assertEquals("new image data", aiDrawingRequest.getImageData());
        assertTrue(aiDrawingRequest.isRemoveBackground());
        assertEquals("newChatroom", aiDrawingRequest.getChatroomId());
    }

    @Test
    void testToStringWithImageData() {
        aiDrawingRequest.setText("test");
        aiDrawingRequest.setImageData("some image data");
        aiDrawingRequest.setRemoveBackground(true);
        aiDrawingRequest.setChatroomId("chat123");

        String result = aiDrawingRequest.toString();

        assertTrue(result.contains("text='test'"));
        assertTrue(result.contains("imageData='...'"));
        assertTrue(result.contains("removeBackground=true"));
        assertTrue(result.contains("chatroomId='chat123'"));
    }

    @Test
    void testToStringWithNullImageData() {
        aiDrawingRequest.setText("test");
        aiDrawingRequest.setImageData(null);

        String result = aiDrawingRequest.toString();

        assertTrue(result.contains("imageData='null'"));
    }

    @Test
    void testNullValues() {
        aiDrawingRequest.setText(null);
        aiDrawingRequest.setImageData(null);
        aiDrawingRequest.setChatroomId(null);

        assertNull(aiDrawingRequest.getText());
        assertNull(aiDrawingRequest.getImageData());
        assertNull(aiDrawingRequest.getChatroomId());
    }

    @Test
    void testEmptyStrings() {
        aiDrawingRequest.setText("");
        aiDrawingRequest.setImageData("");
        aiDrawingRequest.setChatroomId("");

        assertEquals("", aiDrawingRequest.getText());
        assertEquals("", aiDrawingRequest.getImageData());
        assertEquals("", aiDrawingRequest.getChatroomId());
    }
}