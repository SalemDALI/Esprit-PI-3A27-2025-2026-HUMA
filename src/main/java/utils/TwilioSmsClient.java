package utils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Send SMS via Twilio REST API. No-op if ApiConfig.TWILIO_* not set.
 * Use for OTP by SMS or 2FA. To number must be E.164 (e.g. +33612345678).
 */
public final class TwilioSmsClient {

    private static final String TWILIO_API = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";
    private static final java.net.http.HttpClient HTTP = java.net.http.HttpClient.newHttpClient();

    public static boolean isConfigured() {
        return ApiConfig.TWILIO_SID != null && !ApiConfig.TWILIO_SID.isBlank()
                && ApiConfig.TWILIO_TOKEN != null && !ApiConfig.TWILIO_TOKEN.isBlank()
                && ApiConfig.TWILIO_FROM != null && !ApiConfig.TWILIO_FROM.isBlank();
    }

    /**
     * Send SMS to the given number (E.164). Returns true if sent successfully.
     */
    public static boolean sendSms(String toPhoneNumber, String message) {
        if (!isConfigured()) return false;
        try {
            String url = String.format(TWILIO_API, ApiConfig.TWILIO_SID);
            String body = "To=" + URLEncoder.encode(toPhoneNumber, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(ApiConfig.TWILIO_FROM, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((ApiConfig.TWILIO_SID + ":" + ApiConfig.TWILIO_TOKEN).getBytes(StandardCharsets.UTF_8)))
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Send OTP by SMS (e.g. for forgot password or 2FA). */
    public static boolean sendOtpSms(String toPhoneNumber, String otpCode) {
        return sendSms(toPhoneNumber, "Votre code OTP : " + otpCode + ". Ne le partagez pas.");
    }
}
