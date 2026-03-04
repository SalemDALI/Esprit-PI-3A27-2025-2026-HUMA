package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Reusable SMTP sender. Uses ApiConfig.SMTP_USER / SMTP_PASSWORD if set,
 * otherwise no-op (caller can use ForgotPasswordController's config for local use).
 */
public final class MailSender {

    private static String from() { return ApiConfig.SMTP_USER; }
    private static String pass() { return ApiConfig.SMTP_PASSWORD; }

    public static boolean isConfigured() {
        String u = from();
        String p = pass();
        return u != null && !u.isBlank() && p != null && !p.isBlank();
    }

    /**
     * Send an email. Returns false if SMTP not configured or send fails.
     */
    public static boolean send(String to, String subject, String body) {
        String from = from();
        String password = pass();
        if (from == null || from.isBlank() || password == null || password.isBlank()) return false;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Optional: send welcome/verification email after signup. */
    public static boolean sendWelcomeEmail(String to, String nom, String prenom) {
        String subject = "Bienvenue - Compte créé";
        String body = String.format("Bonjour %s %s,\n\nVotre compte a bien été créé. Vous pouvez vous connecter avec votre email et mot de passe.\n\nCordialement.", prenom, nom);
        return send(to, subject, body);
    }

    private MailSender() {}
}
