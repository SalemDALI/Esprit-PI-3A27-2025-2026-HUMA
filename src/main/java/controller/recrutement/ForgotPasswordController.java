package controller.recrutement;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.feedback.ServiceUser;
import tests.Main;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.Properties;
import java.util.UUID;

public class ForgotPasswordController {

    @FXML
    private TextField tfEmail;
    @FXML
    private Label lblMessage;

    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void sendResetEmail() {
        String email = tfEmail.getText().trim();

        if(email.isEmpty()) {
            lblMessage.setText("Veuillez entrer votre email !");
            return;
        }

        if(!serviceUser.userExists(email)) {
            lblMessage.setText("Email non trouvé !");
            return;
        }

        String token = UUID.randomUUID().toString();

        if(!serviceUser.storeResetToken(email, token)) {
            lblMessage.setText("Impossible de générer le lien. Réessayez.");
            return;
        }

        String resetLink = "http://localhost/reset?token=" + token; // Exemple de lien, tu peux adapter

        if(sendEmail(email, resetLink)) {
            lblMessage.setStyle("-fx-text-fill:green;");
            lblMessage.setText("Email de réinitialisation envoyé !");
        } else {
            lblMessage.setText("Erreur lors de l'envoi de l'email !");
        }
    }

    private boolean sendEmail(String to, String resetLink) {
        final String from = "ton.email@gmail.com";  // ton email
        final String pass = "tonMotDePasse";       // ton mot de passe ou App Password Gmail

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Réinitialisation de mot de passe");
            message.setText("Pour réinitialiser votre mot de passe, cliquez sur ce lien :\n" + resetLink);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}