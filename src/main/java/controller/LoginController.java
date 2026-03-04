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
import utils.FaceCameraUtil;
import utils.FaceApiClient;

import java.io.IOException;
import java.text.Normalizer;

public class LoginController {

    public TextField emailField;
    public PasswordField passwordField;
    public Label messageLabel;

    ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void loginWithFace(ActionEvent event) {
        try {
            byte[] data = FaceCameraUtil.captureFaceImageOnce();
            if (data == null || data.length == 0) {
                messageLabel.setText("Impossible de capturer l'image depuis la caméra.");
                return;
            }
            Integer matchedId = FaceApiClient.verifyFace(data);
            if (matchedId == null) {
                messageLabel.setText("Aucun compte ne correspond à ce visage.");
                return;
            }
            User user = serviceUser.getById(matchedId);
            if (user == null) {
                messageLabel.setText("Utilisateur introuvable pour ce visage.");
                return;
            }
            if (!user.isActif()) {
                messageLabel.setText("Ce compte est désactivé.");
                return;
            }
            Session.setUser(user);
            openHomeFor(user);
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur Face ID: " + e.getMessage());
        }
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
        openHomeFor(user);
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

    private void openHomeFor(User user) {
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
            e.printStackTrace();
            messageLabel.setText("Erreur ouverture page reset: " + e.getMessage());
        }
    }
    // Ancien flux de reset inline supprimé (on utilise maintenant la vue ForgotPassword.fxml)
}
