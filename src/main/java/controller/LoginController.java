package controller;

import models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ServiceUser;
import utils.Session;

import java.text.Normalizer;

public class LoginController {

    public TextField emailField;
    public PasswordField passwordField;
    public Label messageLabel;

    ServiceUser serviceUser = new ServiceUser();

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
                loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                stage.setScene(new Scene(loader.load()));
            } else if (isCandidatRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/candidat.fxml"));
                stage.setScene(new Scene(loader.load()));
            } else if (isEmployeeRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/DemandeConge.fxml"));
                Scene employeScene = new Scene(loader.load());
                DemandeCongeController employeController = loader.getController();
                employeController.setEmployeId(user.getId());
                stage.setScene(employeScene);
            } else if (isManagerRole(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/ManagerAbsence.fxml"));
                Scene managerScene = new Scene(loader.load());
                ManagerAbsenceController managerController = loader.getController();
                managerController.setManagerId(user.getId());
                stage.setScene(managerScene);
            } else {
                // Fallback: tout role non reconnu ouvre l'espace employe
                loader = new FXMLLoader(getClass().getResource("/fxml/DemandeConge.fxml"));
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
}
