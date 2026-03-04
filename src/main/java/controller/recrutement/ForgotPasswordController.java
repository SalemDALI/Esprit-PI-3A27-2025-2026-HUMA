package controller.recrutement;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.feedback.ServiceUser;
import utils.TwilioSmsClient;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ForgotPasswordController implements Initializable {

    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfPhone;
    @FXML
    private TextField tfOtp;
    @FXML
    private PasswordField pfNewPassword;
    @FXML
    private PasswordField pfConfirmPassword;
    @FXML
    private VBox passwordBox;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSendSms;

    private final ServiceUser serviceUser = new ServiceUser();
    private String verifiedOtp = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (btnSendSms != null) {
            btnSendSms.setDisable(!TwilioSmsClient.isConfigured());
        }
    }

    private static String generateOtp() {
        Random r = ThreadLocalRandom.current();
        int code = 100_000 + r.nextInt(900_000);
        return String.valueOf(code);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String s = phone.trim().replaceAll("\\s", "");
        if (s.startsWith("00")) s = "+" + s.substring(2);
        else if (s.length() == 10 && s.startsWith("0")) s = "+33" + s.substring(1);
        return s;
    }

    @FXML
    private void sendOtpByEmail() {
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        if (email.isEmpty()) {
            setMessage("Veuillez entrer votre email.", true);
            return;
        }
        if (!serviceUser.userExists(email)) {
            setMessage("Email non trouvé.", true);
            return;
        }

        String otp = generateOtp();
        if (!serviceUser.storeResetToken(email, otp)) {
            setMessage("Impossible de générer le code. Réessayez.", true);
            return;
        }

        if (sendEmailWithOtp(email, otp)) {
            setMessage("Un code OTP à 6 chiffres a été envoyé à votre email.", false);
            verifiedOtp = null;
            passwordBox.setVisible(false);
            passwordBox.setManaged(false);
            pfNewPassword.clear();
            pfConfirmPassword.clear();
        } else {
            setMessage("Erreur lors de l'envoi de l'email.", true);
        }
    }

    @FXML
    private void sendOtpBySms() {
        if (!TwilioSmsClient.isConfigured()) {
            setMessage("SMS non configuré (Twilio). Utilisez l'envoi par email.", true);
            return;
        }
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        String phone = normalizePhone(tfPhone.getText());
        if (email.isEmpty()) {
            setMessage("Veuillez entrer votre email.", true);
            return;
        }
        if (phone.length() < 10) {
            setMessage("Veuillez entrer un numéro de téléphone (ex: +33612345678 ou 0612345678).", true);
            return;
        }
        if (!serviceUser.userExists(email)) {
            setMessage("Email non trouvé.", true);
            return;
        }

        String otp = generateOtp();
        if (!serviceUser.storeResetToken(email, otp)) {
            setMessage("Impossible de générer le code. Réessayez.", true);
            return;
        }

        if (TwilioSmsClient.sendOtpSms(phone, otp)) {
            setMessage("Un code OTP à 6 chiffres a été envoyé par SMS.", false);
            verifiedOtp = null;
            passwordBox.setVisible(false);
            passwordBox.setManaged(false);
            pfNewPassword.clear();
            pfConfirmPassword.clear();
        } else {
            setMessage("Erreur lors de l'envoi du SMS. Vérifiez le numéro (format E.164: +33...).", true);
        }
    }

    @FXML
    private void verifyOtp() {
        String otp = tfOtp.getText() == null ? "" : tfOtp.getText().trim();
        if (otp.length() != 6) {
            setMessage("Le code OTP doit contenir 6 chiffres.", true);
            return;
        }
        if (serviceUser.getByResetToken(otp) == null) {
            setMessage("Code OTP invalide ou expiré.", true);
            return;
        }
        verifiedOtp = otp;
        passwordBox.setVisible(true);
        passwordBox.setManaged(true);
        setMessage("Code correct. Entrez votre nouveau mot de passe.", false);
    }

    @FXML
    private void doResetPassword() {
        if (verifiedOtp == null) {
            setMessage("Vérifiez d'abord le code OTP.", true);
            return;
        }
        String newPass = pfNewPassword.getText() == null ? "" : pfNewPassword.getText();
        String confirm = pfConfirmPassword.getText() == null ? "" : pfConfirmPassword.getText();
        if (newPass.isEmpty() || confirm.isEmpty()) {
            setMessage("Remplissez les deux champs mot de passe.", true);
            return;
        }
        if (!newPass.equals(confirm)) {
            setMessage("Les mots de passe ne correspondent pas.", true);
            return;
        }

        if (serviceUser.resetPassword(verifiedOtp, newPass)) {
            setMessage("Mot de passe modifié. Vous pouvez vous connecter.", false);
            verifiedOtp = null;
            passwordBox.setVisible(false);
            passwordBox.setManaged(false);
            tfOtp.clear();
            pfNewPassword.clear();
            pfConfirmPassword.clear();
        } else {
            setMessage("Erreur lors du changement de mot de passe.", true);
        }
    }

    private void setMessage(String text, boolean error) {
        if (lblMessage != null) {
            lblMessage.setText(text);
            lblMessage.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }

    private boolean sendEmailWithOtp(String to, String otp) {
        final String from = "aminemellouki05@gmail.com";
        final String pass = "nxyw nexo rsdj vzzb";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Code OTP - Réinitialisation mot de passe");
            message.setText("Votre code OTP pour réinitialiser votre mot de passe est :\n\n  " + otp + "\n\nCe code est valide 1 heure. Ne le partagez avec personne.");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
