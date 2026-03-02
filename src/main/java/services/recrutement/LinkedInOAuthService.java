package services.recrutement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LinkedInOAuthService {

    private static final String AUTHORIZE_URL = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String buildAuthorizationUrl(String clientId, String redirectUri, String state, String scope) {
        String safeScope = (scope == null || scope.isBlank()) ? "w_member_social" : scope.trim();
        String safeState = (state == null || state.isBlank()) ? "huma_linkedin_state" : state.trim();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("response_type", "code");
        params.put("client_id", safe(clientId));
        params.put("redirect_uri", safe(redirectUri));
        params.put("state", safeState);
        params.put("scope", safeScope);
        return AUTHORIZE_URL + "?" + toFormData(params);
    }

    public TokenResult exchangeAuthorizationCode(String clientId, String clientSecret, String redirectUri, String code) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", safe(code));
        form.put("redirect_uri", safe(redirectUri));
        form.put("client_id", safe(clientId));
        form.put("client_secret", safe(clientSecret));
        return requestToken(form);
    }

    public TokenResult refreshAccessToken(String clientId, String clientSecret, String refreshToken) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", safe(refreshToken));
        form.put("client_id", safe(clientId));
        form.put("client_secret", safe(clientSecret));
        return requestToken(form);
    }

    private TokenResult requestToken(Map<String, String> formParams) {
        try {
            String form = toFormData(formParams);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                return TokenResult.fail("LinkedIn OAuth (" + status + "): " + shorten(response.body()));
            }

            JsonNode root = mapper.readTree(response.body());
            String accessToken = root.path("access_token").asText("");
            long expiresIn = root.path("expires_in").asLong(0);
            String refreshToken = root.path("refresh_token").asText("");
            long refreshTokenExpiresIn = root.path("refresh_token_expires_in").asLong(0);

            if (accessToken.isBlank()) {
                return TokenResult.fail("LinkedIn OAuth: access_token vide.");
            }

            return TokenResult.ok(accessToken, expiresIn, refreshToken, refreshTokenExpiresIn);
        } catch (Exception e) {
            return TokenResult.fail("Erreur OAuth LinkedIn: " + e.getMessage());
        }
    }

    private String toFormData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(safe(value), StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String shorten(String text) {
        if (text == null) {
            return "";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        return clean.length() <= 220 ? clean : clean.substring(0, 220) + "...";
    }

    public static class TokenResult {
        private final boolean success;
        private final String message;
        private final String accessToken;
        private final long expiresInSeconds;
        private final String refreshToken;
        private final long refreshTokenExpiresInSeconds;

        private TokenResult(
                boolean success,
                String message,
                String accessToken,
                long expiresInSeconds,
                String refreshToken,
                long refreshTokenExpiresInSeconds
        ) {
            this.success = success;
            this.message = message;
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
            this.refreshToken = refreshToken;
            this.refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds;
        }

        public static TokenResult ok(String accessToken, long expiresInSeconds, String refreshToken, long refreshTokenExpiresInSeconds) {
            return new TokenResult(true, "Token LinkedIn genere.", accessToken, expiresInSeconds, refreshToken, refreshTokenExpiresInSeconds);
        }

        public static TokenResult fail(String message) {
            return new TokenResult(false, message, "", 0, "", 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public long getExpiresInSeconds() {
            return expiresInSeconds;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getRefreshTokenExpiresInSeconds() {
            return refreshTokenExpiresInSeconds;
        }
    }
}
