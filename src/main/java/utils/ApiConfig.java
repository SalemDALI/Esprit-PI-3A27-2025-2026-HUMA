package utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Optional API keys/URLs. App works without them; set via -D or api-keys.local.properties.
 * Example: -Dslack.webhook.url=https://hooks.slack.com/...
 */
public final class ApiConfig {

    private static Properties loadLocal() {
        Properties p = new Properties();
        try {
            Path local = Paths.get(System.getProperty("user.dir", ".")).resolve("api-keys.local.properties");
            if (Files.isRegularFile(local)) {
                try (InputStream in = Files.newInputStream(local)) {
                    p.load(in);
                }
            }
        } catch (Exception ignored) { }
        return p;
    }

    private static String get(String sysKey, String propKey) {
        String v = System.getProperty(sysKey);
        if (v != null && !v.isBlank()) return v;
        return loadLocal().getProperty(propKey, "");
    }

    private static String defaultSentiment(String v) {
        return (v == null || v.isBlank()) ? "http://localhost:8001" : v;
    }

    /** Slack Incoming Webhook URL (Feedback → channel). Empty = disabled. */
    public static String SLACK_WEBHOOK_URL = get("slack.webhook.url", "slack.webhook.url");

    /** Sentiment API base URL (e.g. http://localhost:8001). Empty = disabled. */
    public static String SENTIMENT_API_URL = defaultSentiment(get("sentiment.api.url", "sentiment.api.url"));

    /** Trello: API key + token. Empty = "Create ticket" disabled. */
    public static String TRELLO_API_KEY = get("trello.api.key", "trello.api.key");
    public static String TRELLO_TOKEN = get("trello.token", "trello.token");
    /** ID of the list (board) where to create the card. */
    public static String TRELLO_LIST_ID = get("trello.list.id", "trello.list.id");

    /** Twilio: account SID, auth token, from number. Empty = SMS disabled. */
    public static String TWILIO_SID = get("twilio.sid", "twilio.sid");
    public static String TWILIO_TOKEN = get("twilio.token", "twilio.token");
    public static String TWILIO_FROM = get("twilio.from", "twilio.from");

    /** SMTP for MailSender (welcome/verification). Uses same as ForgotPassword if not set. */
    public static String SMTP_USER = get("smtp.user", "smtp.user");
    public static String SMTP_PASSWORD = get("smtp.password", "smtp.password");

    private ApiConfig() {}
}
