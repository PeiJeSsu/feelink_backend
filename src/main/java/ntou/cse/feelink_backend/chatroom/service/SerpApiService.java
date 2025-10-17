package ntou.cse.feelink_backend.chatroom.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class SerpApiService {

    @Value("${serpapi.key}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient();

    private static String staticApiKey;
    private static OkHttpClient staticHttpClient;

    @PostConstruct
    public void init() {
        staticApiKey = this.apiKey;
        staticHttpClient = this.httpClient;
    }

    public static String search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "錯誤：搜尋關鍵字不可為空。";
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://serpapi.com/search.json").newBuilder();
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("engine", "google");
        urlBuilder.addQueryParameter("api_key", staticApiKey);
        urlBuilder.addQueryParameter("gl", "tw");
        urlBuilder.addQueryParameter("hl", "zh-tw");

        Request request = new Request.Builder().url(urlBuilder.build()).build();

        try (Response response = staticHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("SerpApi 請求失敗，狀態碼: " + response.code());
                return "錯誤：搜尋服務請求失敗。";
            }

            String responseBody = response.body().string();
            if (responseBody == null || responseBody.isEmpty()) {
                return "錯誤：搜尋服務回傳了空的結果。";
            }

            JsonObject results = JsonParser.parseString(responseBody).getAsJsonObject();
            return formatResults(results);

        } catch (IOException e) {
            System.err.println("呼叫 SerpApi 時發生網路錯誤: " + e.getMessage());
            return "錯誤：網路連線失敗，無法執行搜尋。";
        } catch (Exception e) {
            System.err.println("處理 SerpApi 結果時發生未知錯誤: " + e.getMessage());
            return "錯誤：處理搜尋結果時發生問題。";
        }
    }

    private static String formatResults(JsonObject resultJson) {
        StringBuilder resultBuilder = new StringBuilder();

        if (resultJson.has("answer_box")) {
            JsonObject answerBox = resultJson.getAsJsonObject("answer_box");
            String snippet = answerBox.has("snippet") ? answerBox.get("snippet").getAsString() : null;
            if (snippet == null && answerBox.has("answer")) {
                snippet = answerBox.get("answer").getAsString();
            }
            if (snippet != null) {
                resultBuilder.append("【直接答案】\n").append(snippet).append("\n\n");
            }
        }

        if (resultJson.has("organic_results")) {
            JsonArray organicResults = resultJson.getAsJsonArray("organic_results");
            resultBuilder.append("【網路搜尋結果】\n");
            int count = Math.min(3, organicResults.size());

            for (int i = 0; i < count; i++) {
                JsonObject result = organicResults.get(i).getAsJsonObject();
                String title = result.has("title") ? result.get("title").getAsString() : "(無標題)";
                String link = result.has("link") ? result.get("link").getAsString() : "(無連結)";
                String snippet = result.has("snippet") ? result.get("snippet").getAsString() : "";

                resultBuilder.append(String.format("%d. 標題: %s\n", i + 1, title));
                resultBuilder.append(String.format("   網址: %s\n", link));
                resultBuilder.append(String.format("   摘要: %s\n\n", snippet.replace("\n", " ")));
            }
        }
        if (resultBuilder.length() == 0) {
            return "找不到相關的搜尋結果。";
        }
        return resultBuilder.toString();
    }
}
