package controller;

import controller.congesAbsences.DemandeCongeController;
import controller.congesAbsences.ManagerAbsenceController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import models.feedback.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.feedback.ServiceUser;
import tests.Main;
import utils.Session;

import java.io.IOException;
import java.text.Normalizer;

public class LoginController {

    public TextField emailField;
    public PasswordField passwordField;
    public Label messageLabel;

    ServiceUser serviceUser = new ServiceUser();

    /** Handler for "Login with Face ID" button – not implemented; use email/password. */
    @FXML
    public void loginWithFace(ActionEvent event) {
        messageLabel.setText("Connexion par Face ID non disponible. Utilisez email et mot de passe.");
    }

    public void login(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        User user = serviceUser.login(email, password);

        if (user == null) {
            messageLabel.setText("Email ou mot de passe incorrect !");
            return;
        }

        Session.setUser(user);

        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            String role = normalizeRole(user.getRole());
            FXMLLoader loader;

            if (isAdminRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/recrutement/dashboard.fxml"));
                stage.setScene(new Scene(loader.load()));
            } else if (isCandidatRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/recrutement/candidat.fxml"));
                stage.setScene(new Scene(loader.load()));
            } else if (isEmployeeRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/DemandeConge.fxml"));
                Scene employeScene = new Scene(loader.load());
                DemandeCongeController employeController = loader.getController();
                employeController.setEmployeId(user.getId());
                stage.setScene(employeScene);
            } else if (isManagerRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/ManagerAbsence.fxml"));
                Scene managerScene = new Scene(loader.load());
                ManagerAbsenceController managerController = loader.getController();
                managerController.setManagerId(user.getId());
                stage.setScene(managerScene);
            } else {
                // Fallback: tout role non reconnu ouvre l'espace employe
                loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/DemandeConge.fxml"));
                Scene employeScene = new Scene(loader.load());
                DemandeCongeController employeController = loader.getController();
                employeController.setEmployeId(user.getId());
                stage.setScene(employeScene);
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur d'ouverture de page: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        return Normalizer.normalize(role, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');
    }

    private boolean isAdminRole(String role) {
        String compact = role.replace("_", "");
        return compact.startsWith("ADMIN");
    }

    private boolean isCandidatRole(String role) {
        String compact = role.replace("_", "");
        return compact.contains("CANDID");
    }

    private boolean isEmployeeRole(String role) {
        String compact = role.replace("_", "");
        return compact.contains("EMPLOY")
                || compact.contains("EMPLO")
                || compact.contains("EPLO");
    }

    private boolean isManagerRole(String role) {
        String compact = role.replace("_", "");
        return compact.contains("MANAG");
    }

    public void openSignup(ActionEvent event) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/feedback/signup.fxml"))));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Erreur ouverture signup: " + e.getMessage());
        }
    }
    @FXML
    private void showForgotPassword() {
        try {
            Main.loadScene("/fxml/feedback/ForgotPassword.fxml", "Mot de passe oublié");
        } catch (Exception e) {
            messageLabel.setText("Erreur ouverture page reset: " + e.getMessage());
        }
    }
    @FXML private VBox resetBox;

    @FXML private TextField resetEmailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label resetMessageLabel;
    @FXML
    private void showResetSection() {
        resetBox.setVisible(true);
        resetBox.setManaged(true); // important pour que le VBox prenne l’espace
    }
    @FXML
    private void sendResetEmail() {
        String email = resetEmailField.getText();

        if (email == null || email.isEmpty()) {
            resetMessageLabel.setText("Veuillez entrer votre email.");
            return;
        }

        // Simulation en attendant ton vrai service
        System.out.println("Envoi code à : " + email);

        resetMessageLabel.setText("Code envoyé (simulation).");
    }
    @FXML
    private void resetPassword() {

        String token = tokenField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (token.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            resetMessageLabel.setText("Tous les champs sont obligatoires.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            resetMessageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        // Simulation reset
        System.out.println("Reset password avec token: " + token);

        resetMessageLabel.setText("Mot de passe réinitialisé !");
    }
    @FXML
    private void forgotPassword() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feedback/ForgotPassword.fxml"));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
