package controller.feedback;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.feedback.User;
import services.feedback.ServiceUser;
import utils.Session;

import java.io.IOException;
import java.util.List;

public class UsersListController {

    @FXML private VBox boxUsers;
    @FXML private Label lblMessage;

    private final ServiceUser serviceUser = new ServiceUser();

    private boolean isAdmin() {
        User u = Session.getUser();
        return u != null && u.getRole() != null
                && u.getRole().toUpperCase().replace("_", "").replace("-", "").startsWith("ADMIN");
    }

    @FXML
    public void initialize() {
        if (!isAdmin()) {
            if (lblMessage != null) lblMessage.setText("Accès réservé aux administrateurs.");
            return;
        }
        refreshList();
    }

    private void refreshList() {
        if (boxUsers == null) return;
        boxUsers.getChildren().clear();
        List<User> users = serviceUser.getAll();
        for (User u : users) {
            HBox row = new HBox(12);
            row.getStyleClass().add("feedback-card");
            row.setSpacing(12);
            String actifStr = u.isActif() ? "Actif" : "Désactivé";
            Label info = new Label("ID " + u.getId() + " · " + (u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "") + " · " + (u.getEmail() != null ? u.getEmail() : "") + " · " + (u.getRole() != null ? u.getRole() : "") + " · " + actifStr);
            info.setWrapText(true);
            Button btnToggle = new Button(u.isActif() ? "Désactiver" : "Activer");
            btnToggle.setOnAction(e -> toggleUser(u));
            row.getChildren().addAll(info, btnToggle);
            boxUsers.getChildren().add(row);
        }
    }

    private void toggleUser(User u) {
        if (!isAdmin()) return;
        boolean newState = !u.isActif();
        if (u.getId() == Session.getUser().getId()) {
            setMessage("Vous ne pouvez pas désactiver votre propre compte.", true);
            return;
        }
        if (serviceUser.setActive(u.getId(), newState)) {
            u.setActif(newState);
            setMessage(newState ? "Utilisateur activé." : "Utilisateur désactivé.", false);
            refreshList();
        } else {
            setMessage("Erreur lors de la mise à jour.", true);
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/recrutement/dashboard.fxml"))));
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Retour impossible: " + e.getMessage()).showAndWait();
        }
    }

    private void setMessage(String text, boolean error) {
        if (lblMessage != null) {
            lblMessage.setText(text);
            lblMessage.setStyle(error ? "-fx-text-fill: #d64545;" : "-fx-text-fill: #2f855a;");
        }
    }
}
