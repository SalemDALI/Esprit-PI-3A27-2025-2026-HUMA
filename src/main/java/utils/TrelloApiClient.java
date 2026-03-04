package utils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Create a Trello card via REST API. Set ApiConfig.TRELLO_API_KEY, TRELLO_TOKEN, TRELLO_LIST_ID.
 */
public final class TrelloApiClient {

    private static final String TRELLO_CREATE = "https://api.trello.com/1/cards";
    private static final java.net.http.HttpClient HTTP = java.net.http.HttpClient.newHttpClient();

    public static boolean isConfigured() {
        return ApiConfig.TRELLO_API_KEY != null && !ApiConfig.TRELLO_API_KEY.isBlank()
                && ApiConfig.TRELLO_TOKEN != null && !ApiConfig.TRELLO_TOKEN.isBlank()
                && ApiConfig.TRELLO_LIST_ID != null && !ApiConfig.TRELLO_LIST_ID.isBlank();
    }

    /**
     * Create a card in the configured list. Returns the card URL on success, or "ERROR: message" on API failure.
     */
    public static String createCard(String title, String description) {
        if (!isConfigured()) return "ERROR: Configuration incomplète (key, token ou list id manquant).";
        try {
            String q = "key=" + URLEncoder.encode(ApiConfig.TRELLO_API_KEY, StandardCharsets.UTF_8)
                    + "&token=" + URLEncoder.encode(ApiConfig.TRELLO_TOKEN, StandardCharsets.UTF_8)
                    + "&idList=" + URLEncoder.encode(ApiConfig.TRELLO_LIST_ID, StandardCharsets.UTF_8)
                    + "&name=" + URLEncoder.encode(title != null ? title : "Sans titre", StandardCharsets.UTF_8)
                    + "&desc=" + URLEncoder.encode(description != null ? description : "", StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TRELLO_CREATE + "?" + q))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}", StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                String body = response.body();
                System.err.println("[Trello] HTTP " + response.statusCode() + ": " + body);
                String msg = body;
                try {
                    com.fasterxml.jackson.databind.JsonNode n = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
                    if (n.has("message")) msg = n.get("message").asText();
                } catch (Exception ignored) { }
                return "ERROR: " + msg;
            }
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());
            return node.has("url") ? node.get("url").asText() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    /** Create a Trello card from a feedback (for admin "Create ticket"). */
    public static String createCardFromFeedback(int feedbackId, String contenu, Integer employeId, boolean anonyme) {
        String title = "Feedback #" + feedbackId + (anonyme ? " (anonyme)" : " (Employé #" + employeId + ")");
        String desc = contenu != null ? contenu : "";
        return createCard(title, desc);
    }
}
