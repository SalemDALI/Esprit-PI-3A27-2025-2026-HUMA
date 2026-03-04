package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Calls an external sentiment API. Expects POST /sentiment with {"text":"..."}
 * and returns {"sentiment":"positive|negative|neutral", "score":0.85}.
 * No-op if SENTIMENT_API_URL is empty.
 */
public final class SentimentApiClient {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static class SentimentResult {
        public final String label;
        public final double score;

        public SentimentResult(String label, double score) {
            this.label = label != null ? label : "neutral";
            this.score = score;
        }
    }

    /**
     * Returns sentiment for the given text, or null if API disabled/fails.
     */
    public static SentimentResult getSentiment(String text) {
        String base = ApiConfig.SENTIMENT_API_URL;
        if (base == null || base.isBlank() || text == null || text.isBlank()) return null;
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("text", text);
            String json = MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/sentiment"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) return null;
            JsonNode node = MAPPER.readTree(response.body());
            String label = node.has("sentiment") ? node.get("sentiment").asText() : "neutral";
            double score = node.has("score") ? node.get("score").asDouble() : 0.5;
            return new SentimentResult(label, score);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SentimentApiClient() {}
}
