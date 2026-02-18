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

import java.io.IOException;
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

            if ("ADMIN_RH".equals(role) || "ADMIN".equals(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                stage.setScene(new Scene(loader.load()));
            } else if ("CANDIDAT".equals(role)) {
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
                messageLabel.setText("Role non pris en charge: " + user.getRole() + " (" + role + ")");
                return;
            }

            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Erreur d'ouverture de page: " + e.getMessage());
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = Normalizer.normalize(role, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        if ("EMPLOYEE".equals(normalized)
                || "EMPLOYER".equals(normalized)
                || "EMPLOIYER".equals(normalized)
                || "EMPLOIYEE".equals(normalized)
                || "EMPLOYEER".equals(normalized)
                || "EMPLOYE".equals(normalized)) {
            return "EMPLOYE";
        }
        return normalized;
    }

    private boolean isEmployeeRole(String role) {
        return "EMPLOYE".equals(role)
                || role.startsWith("EMPLOYE")
                || role.contains("EMPLOY");
    }

    private boolean isManagerRole(String role) {
        return "MANAGER".equals(role) || role.startsWith("MANAG");
    }
}
