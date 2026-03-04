package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Client HTTP simple vers un service Face ID externe.
 * Convention d'API (à adapter côté backend si besoin) :
 *
 *  POST /face/enroll
 *    Body JSON: { "user_id": 123, "image_base64": "..." }
 *    Response:  { "status": "ok" } ou { "status": "error", ... }
 *
 *  POST /face/verify
 *    Body JSON: { "image_base64": "..." }
 *    Response:  { "matched_user_id": 123, "score": 0.95 } ou { "matched_user_id": null, ... }
 */
public class FaceApiClient {

    // A adapter à l'URL réelle de ton service Python / autre
    private static final String BASE_URL = "http://localhost:8000";

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static boolean enrollFace(int userId, byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) return false;
            String b64 = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", userId);
            payload.put("image_base64", b64);

            String json = MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/face/enroll"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                return false;
            }
            JsonNode node = MAPPER.readTree(response.body());
            JsonNode status = node.get("status");
            return status != null && "ok".equalsIgnoreCase(status.asText());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retourne l'id utilisateur qui correspond au visage, ou null si aucun.
     */
    public static Integer verifyFace(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) return null;
            String b64 = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> payload = new HashMap<>();
            payload.put("image_base64", b64);

            String json = MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/face/verify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                return null;
            }
            JsonNode node = MAPPER.readTree(response.body());
            JsonNode idNode = node.get("matched_user_id");
            if (idNode == null || idNode.isNull()) {
                return null;
            }
            return idNode.asInt();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

