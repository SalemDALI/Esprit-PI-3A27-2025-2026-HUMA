package controller.recrutement;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import services.feedback.ServiceUser;

public class ResetPasswordController {

    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label lblMessage;

    private String token;

    private ServiceUser serviceUser = new ServiceUser();

    public void setToken(String token) {
        this.token = token;
    }

    @FXML
    private void resetPassword() {
        String pass = pfPassword.getText();
        String confirm = pfConfirmPassword.getText();

        if(pass.isEmpty() || confirm.isEmpty()) {
            lblMessage.setText("Veuillez remplir tous les champs !");
            return;
        }

        if(!pass.equals(confirm)) {
            lblMessage.setText("Les mots de passe ne correspondent pas !");
            return;
        }

        String hashed = BCrypt.hashpw(pass, BCrypt.gensalt());

        if(serviceUser.resetPassword(token, hashed)) {
            lblMessage.setStyle("-fx-text-fill:green;");
            lblMessage.setText("Mot de passe réinitialisé avec succès !");
        } else {
            lblMessage.setText("Lien invalide ou expiré !");
        }
    }
}