package controller.feedback;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controller.congesAbsences.DemandeCongeController;
import models.feedback.User;
import services.feedback.ServiceUser;
import utils.Session;

import java.io.IOException;

public class ProfileController {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfCurrent;
    @FXML private PasswordField pfNew;
    @FXML private PasswordField pfConfirm;
    @FXML private Label lblMessage;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        User u = Session.getUser();
        if (u == null) return;
        if (tfNom != null) tfNom.setText(u.getNom() != null ? u.getNom() : "");
        if (tfPrenom != null) tfPrenom.setText(u.getPrenom() != null ? u.getPrenom() : "");
        if (tfEmail != null) tfEmail.setText(u.getEmail() != null ? u.getEmail() : "");
    }

    @FXML
    private void saveProfile() {
        User u = Session.getUser();
        if (u == null) {
            setMessage("Non connecté.", true);
            return;
        }
        String nom = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String prenom = tfPrenom.getText() == null ? "" : tfPrenom.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            setMessage("Nom, prénom et email sont obligatoires.", true);
            return;
        }
        if (serviceUser.emailExistsExcludingId(email, u.getId())) {
            setMessage("Cet email est déjà utilisé par un autre compte.", true);
            return;
        }
        if (serviceUser.updateProfile(u.getId(), nom, prenom, email)) {
            u.setNom(nom);
            u.setPrenom(prenom);
            u.setEmail(email);
            Session.setUser(u);
            setMessage("Profil enregistré.", false);
        } else {
            setMessage("Erreur lors de l'enregistrement.", true);
        }
    }

    @FXML
    private void changePassword() {
        User u = Session.getUser();
        if (u == null) {
            setMessage("Non connecté.", true);
            return;
        }
        String current = pfCurrent.getText() == null ? "" : pfCurrent.getText();
        String newPass = pfNew.getText() == null ? "" : pfNew.getText();
        String confirm = pfConfirm.getText() == null ? "" : pfConfirm.getText();
        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            setMessage("Remplissez tous les champs mot de passe.", true);
            return;
        }
        if (!serviceUser.checkPassword(u.getId(), current)) {
            setMessage("Mot de passe actuel incorrect.", true);
            return;
        }
        if (!newPass.equals(confirm)) {
            setMessage("Les deux nouveaux mots de passe ne correspondent pas.", true);
            return;
        }
        if (serviceUser.updatePassword(u.getId(), newPass)) {
            u.setMdp(newPass);
            pfCurrent.clear();
            pfNew.clear();
            pfConfirm.clear();
            setMessage("Mot de passe modifié.", false);
        } else {
            setMessage("Erreur lors du changement de mot de passe.", true);
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            User u = Session.getUser();
            if (u != null && u.getRole() != null) {
                String r = u.getRole().toUpperCase().replace("_", "").replace("-", "");
                if (r.startsWith("ADMIN")) {
                    stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/recrutement/dashboard.fxml"))));
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/DemandeConge.fxml"));
                    stage.setScene(new Scene(loader.load()));
                    DemandeCongeController ctrl = loader.getController();
                    if (ctrl != null && u != null) ctrl.setEmployeId(u.getId());
                }
            } else {
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/recrutement/dashboard.fxml"))));
            }
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
