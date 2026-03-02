package services.recrutement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.recrutement.OffreEmploi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

public class LinkedInJobPublisherService {

    private static final String LINKEDIN_UGC_URL = "https://api.linkedin.com/v2/ugcPosts";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final LinkedInOAuthService linkedInOAuthService = new LinkedInOAuthService();

    public PublishResult publishOffer(OffreEmploi offre) {
        if (offre == null) {
            return PublishResult.fail("Offre vide.");
        }

        String token = resolveAccessToken();
        if (token.isBlank()) {
            return PublishResult.fail("Configuration manquante: token LinkedIn (LINKEDIN_ACCESS_TOKEN ou OAuth LINKEDIN_CLIENT_ID/SECRET + refresh/code).");
        }

        String author = resolveOrganizationUrn();
        if (author.isBlank()) {
            return PublishResult.fail("Configuration manquante: LINKEDIN_ORGANIZATION_URN ou LINKEDIN_ORGANIZATION_ID");
        }

        try {
            // Mode texte par defaut pour eviter les rejets de validation media/url.
            PublishResult base = publishWithPayload(token, buildPayload(offre, author, false));
            if (base.isSuccess()) {
                return base;
            }

            // Le mode article est explicite via variable d'environnement.
            if (isArticleMediaEnabled() && !buildJobLink(offre).isBlank()) {
                PublishResult article = publishWithPayload(token, buildPayload(offre, author, true));
                if (article.isSuccess()) {
                    return article;
                }
                return PublishResult.fail(base.getMessage() + " | Fallback article: " + article.getMessage());
            }
            return base;
        } catch (Exception e) {
            return PublishResult.fail("Erreur publication LinkedIn: " + e.getMessage());
        }
    }

    public String buildAuthorizationUrlFromEnv() {
        String clientId = env("LINKEDIN_CLIENT_ID");
        String redirectUri = env("LINKEDIN_REDIRECT_URI");
        String scope = env("LINKEDIN_SCOPE");
        String state = env("LINKEDIN_STATE");
        if (clientId.isBlank() || redirectUri.isBlank()) {
            return "";
        }
        return linkedInOAuthService.buildAuthorizationUrl(clientId, redirectUri, state, scope);
    }

    private String resolveAccessToken() {
        String staticAccessToken = env("LINKEDIN_ACCESS_TOKEN");
        if (!staticAccessToken.isBlank()) {
            return staticAccessToken;
        }

        String clientId = env("LINKEDIN_CLIENT_ID");
        String clientSecret = env("LINKEDIN_CLIENT_SECRET");
        if (clientId.isBlank() || clientSecret.isBlank()) {
            return "";
        }

        String refreshToken = env("LINKEDIN_REFRESH_TOKEN");
        if (!refreshToken.isBlank()) {
            LinkedInOAuthService.TokenResult refresh = linkedInOAuthService.refreshAccessToken(clientId, clientSecret, refreshToken);
            if (refresh.isSuccess()) {
                return refresh.getAccessToken();
            }
            return "";
        }

        String code = env("LINKEDIN_AUTH_CODE");
        String redirectUri = env("LINKEDIN_REDIRECT_URI");
        if (!code.isBlank() && !redirectUri.isBlank()) {
            LinkedInOAuthService.TokenResult exchange = linkedInOAuthService.exchangeAuthorizationCode(clientId, clientSecret, redirectUri, code);
            if (exchange.isSuccess()) {
                return exchange.getAccessToken();
            }
        }
        return "";
    }

    private PublishResult publishWithPayload(String token, String payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LINKEDIN_UGC_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        if (code >= 200 && code < 300) {
            return PublishResult.ok("Offre publiee sur LinkedIn.");
        }
        return PublishResult.fail("LinkedIn API (" + code + "): " + shorten(response.body()));
    }

    private String buildPayload(OffreEmploi offre, String author, boolean includeArticleLink) throws Exception {
        String jobLink = includeArticleLink ? buildJobLink(offre) : "";
        String commentary = buildCommentary(offre, jobLink);

        ObjectNode root = mapper.createObjectNode();
        root.put("author", author);
        root.put("lifecycleState", "PUBLISHED");

        ObjectNode shareCommentary = mapper.createObjectNode();
        shareCommentary.put("text", commentary);

        ObjectNode shareContent = mapper.createObjectNode();
        shareContent.set("shareCommentary", shareCommentary);

        if (jobLink.isBlank()) {
            shareContent.put("shareMediaCategory", "NONE");
        } else {
            shareContent.put("shareMediaCategory", "ARTICLE");
            ArrayNode media = mapper.createArrayNode();
            ObjectNode item = mapper.createObjectNode();
            item.put("status", "READY");
            item.put("originalUrl", jobLink);
            item.set("title", mapper.createObjectNode().put("text", safe(offre.getTitre())));
            item.set("description", mapper.createObjectNode().put("text", safe(offre.getDescription())));
            media.add(item);
            shareContent.set("media", media);
        }

        ObjectNode specific = mapper.createObjectNode();
        specific.set("com.linkedin.ugc.ShareContent", shareContent);
        root.set("specificContent", specific);

        ObjectNode visibility = mapper.createObjectNode();
        visibility.put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC");
        root.set("visibility", visibility);

        return mapper.writeValueAsString(root);
    }

    private boolean isArticleMediaEnabled() {
        String value = env("LINKEDIN_ENABLE_ARTICLE_MEDIA");
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    private String buildCommentary(OffreEmploi offre, String jobLink) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nouvelle offre: ").append(safe(offre.getTitre())).append("\n")
                .append("Departement: ").append(safe(offre.getDepartement())).append(" | Contrat: ").append(safe(offre.getTypeContrat())).append("\n")
                .append("Postes: ").append(Math.max(0, offre.getNombrePostes())).append(" | Publication: ")
                .append(offre.getDatePublication() == null ? LocalDate.now() : offre.getDatePublication()).append("\n");
        if (!safe(offre.getDescription()).isBlank()) {
            sb.append(safe(offre.getDescription())).append("\n");
        }
        if (!jobLink.isBlank()) {
            sb.append("Postuler: ").append(jobLink);
        }
        return sb.toString();
    }

    private String buildJobLink(OffreEmploi offre) {
        String base = env("LINKEDIN_JOB_URL_BASE");
        if (base.isBlank()) {
            return "";
        }
        String cleanBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return cleanBase + "/" + offre.getId();
    }

    private String resolveOrganizationUrn() {
        String urn = env("LINKEDIN_ORGANIZATION_URN");
        if (!urn.isBlank()) {
            return urn;
        }
        String id = env("LINKEDIN_ORGANIZATION_ID");
        if (id.isBlank()) {
            return "";
        }
        return "urn:li:organization:" + id.trim();
    }

    private String env(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
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

    public static class PublishResult {
        private final boolean success;
        private final String message;

        private PublishResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static PublishResult ok(String message) {
            return new PublishResult(true, message);
        }

        public static PublishResult fail(String message) {
            return new PublishResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
