package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Absence;
import services.ServiceAbsence;
import utils.Session;

import java.io.IOException;

public class DemandeCongeController {

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> typeAbsence;
    @FXML private Label welcomeLabel;

    private final ServiceAbsence service = new ServiceAbsence();
    private int employeId;

    public void setEmployeId(int id) {
        this.employeId = id;
        if (welcomeLabel != null && Session.getUser() != null) {
            welcomeLabel.setText("Bienvenue " + Session.getUser().getNom() + " " + Session.getUser().getPrenom());
        }
    }

    @FXML
    public void initialize() {
        if (welcomeLabel != null && Session.getUser() != null) {
            welcomeLabel.setText("Bienvenue " + Session.getUser().getNom() + " " + Session.getUser().getPrenom());
        }
    }

    @FXML
    public void handleDemande() {
        if (employeId <= 0) {
            new Alert(Alert.AlertType.ERROR, "Employe non initialise.").show();
            return;
        }
        if (dateDebut.getValue() == null || dateFin.getValue() == null || typeAbsence.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.").show();
            return;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            new Alert(Alert.AlertType.WARNING, "La date fin doit etre apres la date debut.").show();
            return;
        }

        Absence a = new Absence(
                employeId,
                dateDebut.getValue(),
                dateFin.getValue(),
                typeAbsence.getValue(),
                "EN_ATTENTE"
        );

        if (service.demanderConge(a)) {
            new Alert(Alert.AlertType.INFORMATION, "Demande envoyee").show();
            dateDebut.setValue(null);
            dateFin.setValue(null);
            typeAbsence.setValue(null);
        } else {
            new Alert(Alert.AlertType.ERROR, "Echec lors de l'envoi de la demande.").show();
        }
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) dateDebut.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur logout: " + e.getMessage()).show();
        }
    }
}
