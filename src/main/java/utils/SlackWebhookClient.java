package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Sends a message to a Slack channel via Incoming Webhook.
 * Set ApiConfig.SLACK_WEBHOOK_URL to enable.
 */
public final class SlackWebhookClient {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    /**
     * Notify Slack of a new feedback. No-op if SLACK_WEBHOOK_URL is empty.
     */
    public static boolean notifyNewFeedback(int feedbackId, String contenu, Integer employeId, boolean anonyme) {
        String url = ApiConfig.SLACK_WEBHOOK_URL;
        if (url == null || url.isBlank()) return false;

        String author = anonyme ? "Anonyme" : ("Employé #" + (employeId != null ? employeId : "?"));
        String text = String.format("*Nouveau feedback #%d*\n> %s\n_%s_",
                feedbackId,
                (contenu != null && contenu.length() > 200) ? contenu.substring(0, 200) + "…" : (contenu != null ? contenu : ""),
                author);

        return sendSlackMessage(text);
    }

    public static boolean sendSlackMessage(String text) {
        String url = ApiConfig.SLACK_WEBHOOK_URL;
        if (url == null || url.isBlank()) return false;
        try {
            String json = "{\"text\":\"" + escapeJson(text) + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> r = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private SlackWebhookClient() {}
}
