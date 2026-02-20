package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import services.ServiceUser;

import java.io.IOException;

public class SignupController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void signup(ActionEvent event) {
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String prenom = prenomField.getText() == null ? "" : prenomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();

        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setMdp(password);
        user.setRole("CANDIDAT");
        user.setManagerId(null);

        if (serviceUser.ajouter(user)) {
            messageLabel.setStyle("-fx-text-fill: #2f855a;");
            messageLabel.setText("Compte cree. Connectez-vous.");
        } else {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Erreur creation compte (email peut-etre deja utilise).");
        }
    }

    @FXML
    public void backToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Erreur retour login: " + e.getMessage());
        }
    }
}
