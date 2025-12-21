package ntou.cse.feelink_backend.chatroom.service;

import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerpApiServiceTest {

    @Mock
    private OkHttpClient mockHttpClient;

    @Mock
    private Call mockCall;

    private OkHttpClient originalHttpClient;
    private String originalApiKey;

    @BeforeEach
    void setUp() {
        originalHttpClient = (OkHttpClient) ReflectionTestUtils.getField(SerpApiService.class, "staticHttpClient");
        originalApiKey = (String) ReflectionTestUtils.getField(SerpApiService.class, "staticApiKey");

        ReflectionTestUtils.setField(SerpApiService.class, "staticHttpClient", mockHttpClient);
        ReflectionTestUtils.setField(SerpApiService.class, "staticApiKey", "test-api-key");
    }

    @AfterEach
    void tearDown() {
        ReflectionTestUtils.setField(SerpApiService.class, "staticHttpClient", originalHttpClient);
        ReflectionTestUtils.setField(SerpApiService.class, "staticApiKey", originalApiKey);
    }

    private void setupMockResponse(int code, String bodyJson) throws IOException {
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 200 ? "OK" : "Error")
                .body(ResponseBody.create(bodyJson, MediaType.get("application/json")))
                .build();

        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);
    }

    @Test
    void testSearch_EmptyQuery() {
        String result = SerpApiService.search("");
        assertEquals("錯誤：搜尋關鍵字不可為空。", result);

        result = SerpApiService.search(null);
        assertEquals("錯誤：搜尋關鍵字不可為空。", result);
    }

    @Test
    void testSearch_Success_WithOrganicResults() throws IOException {
        String jsonResponse = """
            {
                "organic_results": [
                    {
                        "title": "Test Title 1",
                        "link": "http://test1.com",
                        "snippet": "Snippet 1"
                    },
                    {
                        "title": "Test Title 2",
                        "link": "http://test2.com",
                        "snippet": "Snippet 2"
                    }
                ]
            }
            """;
        setupMockResponse(200, jsonResponse);

        String result = SerpApiService.search("test query");

        assertTrue(result.contains("【網路搜尋結果】"));
        assertTrue(result.contains("1. 標題: Test Title 1"));
        assertTrue(result.contains("2. 標題: Test Title 2"));
        assertTrue(result.contains("http://test1.com"));
    }

    @Test
    void testSearch_Success_WithAnswerBox() throws IOException {
        String jsonResponse = """
            {
                "answer_box": {
                    "snippet": "This is a direct answer."
                }
            }
            """;
        setupMockResponse(200, jsonResponse);

        String result = SerpApiService.search("question");

        assertTrue(result.contains("【直接答案】"));
        assertTrue(result.contains("This is a direct answer."));
    }

    @Test
    void testSearch_Success_WithAnswerBox_AnswerField() throws IOException {
        String jsonResponse = """
            {
                "answer_box": {
                    "answer": "42"
                }
            }
            """;
        setupMockResponse(200, jsonResponse);

        String result = SerpApiService.search("meaning of life");

        assertTrue(result.contains("【直接答案】"));
        assertTrue(result.contains("42"));
    }

    @Test
    void testSearch_NoResults() throws IOException {
        String jsonResponse = "{}"; // 空 JSON
        setupMockResponse(200, jsonResponse);

        String result = SerpApiService.search("something weird");

        assertEquals("找不到相關的搜尋結果。", result);
    }

    @Test
    void testSearch_ApiFailure() throws IOException {
        setupMockResponse(401, "{\"error\": \"Invalid API Key\"}");

        String result = SerpApiService.search("test");

        assertEquals("錯誤：搜尋服務請求失敗。", result);
    }

    @Test
    void testSearch_EmptyResponseBody() throws IOException {
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("", MediaType.get("application/json"))) // 空 Body
                .build();

        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        String result = SerpApiService.search("test");

        assertEquals("錯誤：搜尋服務回傳了空的結果。", result);
    }

    @Test
    void testSearch_NetworkException() throws IOException {
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network Timeout"));

        String result = SerpApiService.search("test");

        assertEquals("錯誤：網路連線失敗，無法執行搜尋。", result);
    }

    @Test
    void testSearch_UnknownException() throws IOException {
        when(mockHttpClient.newCall(any(Request.class))).thenThrow(new RuntimeException("Unexpected"));

        String result = SerpApiService.search("test");

        assertEquals("錯誤：處理搜尋結果時發生問題。", result);
    }

    @Test
    void testInit() {
        SerpApiService service = new SerpApiService();
        ReflectionTestUtils.setField(service, "apiKey", "new-key");

        service.init();
        String currentKey = (String) ReflectionTestUtils.getField(SerpApiService.class, "staticApiKey");
        assertEquals("new-key", currentKey);
    }
}
