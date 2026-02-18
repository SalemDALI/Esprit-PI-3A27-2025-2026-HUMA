package controller;

import models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUser;
import utils.Session;

import java.io.IOException;

public class LoginController {

    // ====== FXML Fields ======
    public TextField emailField;
    public PasswordField passwordField;
    public Label messageLabel;

    // ====== Service ======
    ServiceUser serviceUser = new ServiceUser();

    // ====== LOGIN METHOD ======
    public void login(ActionEvent event) {

        String email = emailField.getText();
        String password = passwordField.getText();

        // Vérification champs vides
        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        // Appel service
        User user = serviceUser.login(email, password);

        if (user != null) {

            // 🔐 Sauvegarder utilisateur connecté
            Session.setUser(user);

            try {
                Stage stage = (Stage) emailField.getScene().getWindow();

                // 🔀 Redirection selon rôle
                if (user.getRole().equals("ADMIN_RH")) {

                    stage.setScene(new Scene(
                            FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"))
                    ));

                } else if (user.getRole().equals("CANDIDAT")) {

                    stage.setScene(new Scene(
                            FXMLLoader.load(getClass().getResource("/fxml/candidat.fxml"))
                    ));

                }

                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            messageLabel.setText("Email ou mot de passe incorrect !");
        }
    }
}
