package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.feedback.User;
import services.feedback.ServiceUser;
import utils.FaceApiClient;

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
    @FXML
    private TextField faceImageField;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void signup(ActionEvent event) {
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String prenom = prenomField.getText() == null ? "" : prenomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();
        String facePath = faceImageField != null && faceImageField.getText() != null ? faceImageField.getText().trim() : "";
        byte[] faceBytes = null;

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
        // Charge le fichier image choisi, si présent
        if (!facePath.isEmpty()) {
            try {
                java.nio.file.Path p = java.nio.file.Paths.get(facePath);
                faceBytes = java.nio.file.Files.readAllBytes(p);
                user.setFaceImage(faceBytes); // on garde aussi la copie locale en BLOB si souhaité
            } catch (Exception e) {
                messageLabel.setText("Image visage invalide: " + e.getMessage());
                return;
            }
        }

        Integer newId = serviceUser.ajouterEtRetournerId(user);
        if (newId == null) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Erreur creation compte (email peut-etre deja utilise).");
            return;
        }

        if (utils.MailSender.isConfigured()) {
            utils.MailSender.sendWelcomeEmail(email, nom, prenom);
        }

        boolean faceOk = true;
        if (faceBytes != null) {
            faceOk = FaceApiClient.enrollFace(newId, faceBytes);
        }

        if (faceOk) {
            messageLabel.setStyle("-fx-text-fill: #2f855a;");
            messageLabel.setText(faceBytes != null
                    ? "Compte cree avec Face ID. Connectez-vous."
                    : "Compte cree. Connectez-vous (Face ID non configure).");
        } else {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Compte cree, mais echec de l'enregistrement Face ID.");
        }
    }

    @FXML
    public void chooseFaceImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image pour Face ID");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) nomField.getScene().getWindow();
        java.io.File file = chooser.showOpenDialog(stage);
        if (file != null && faceImageField != null) {
            faceImageField.setText(file.getAbsolutePath());
            messageLabel.setText("");
        }
    }

    @FXML
    public void backToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/feedback/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Erreur retour login: " + e.getMessage());
        }
    }
}
