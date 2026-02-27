package controller.congesAbsences;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import models.congesAbsences.Absence;
import services.congesAbsences.ServiceAbsence;
import utils.Session;

import java.io.IOException;

public class DemandeCongeController {

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> typeAbsence;
    @FXML private Label welcomeLabel;
    @FXML private Label lblInfo;
    @FXML private VBox tableMyAbsences;

    private final ServiceAbsence service = new ServiceAbsence();
    private int employeId;
    private Absence selectedAbsence;
    private Node selectedCard;

    public void setEmployeId(int id) {
        this.employeId = id;
        if (welcomeLabel != null && Session.getUser() != null) {
            welcomeLabel.setText("Bienvenue " + Session.getUser().getNom() + " " + Session.getUser().getPrenom());
        }
        refreshMyAbsences();
    }

    @FXML
    public void initialize() {
        if (welcomeLabel != null && Session.getUser() != null) {
            welcomeLabel.setText("Bienvenue " + Session.getUser().getNom() + " " + Session.getUser().getPrenom());
        }
        refreshMyAbsences();
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
            refreshMyAbsences();
        } else {
            new Alert(Alert.AlertType.ERROR, "Echec lors de l'envoi de la demande.").show();
        }
    }

    @FXML
    public void updateDemande() {
        if (selectedAbsence == null) {
            setInfo("Selectionnez une demande.", true);
            return;
        }
        if (dateDebut.getValue() == null || dateFin.getValue() == null || typeAbsence.getValue() == null) {
            setInfo("Remplissez les champs.", true);
            return;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            setInfo("La date fin doit etre apres la date debut.", true);
            return;
        }
        Absence a = new Absence();
        a.setId(selectedAbsence.getId());
        a.setDateDebut(dateDebut.getValue());
        a.setDateFin(dateFin.getValue());
        a.setTypeAbsence(typeAbsence.getValue());
        if (service.updateByEmploye(a, employeId)) {
            setInfo("Demande modifiee.", false);
            refreshMyAbsences();
        } else {
            setInfo("Modification impossible (statut non EN_ATTENTE?).", true);
        }
    }

    @FXML
    public void deleteDemande() {
        if (selectedAbsence == null) {
            setInfo("Selectionnez une demande.", true);
            return;
        }
        if (service.deleteByEmploye(selectedAbsence.getId(), employeId)) {
            setInfo("Demande supprimee.", false);
            clearForm();
            refreshMyAbsences();
        } else {
            setInfo("Suppression impossible (statut non EN_ATTENTE?).", true);
        }
    }

    @FXML
    public void refreshMyAbsences() {
        if (tableMyAbsences == null) {
            return;
        }
        tableMyAbsences.getChildren().clear();
        selectedAbsence = null;
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("entity-card-selected");
            selectedCard = null;
        }
        if (employeId <= 0) {
            return;
        }
        for (Absence a : service.getByEmploye(employeId)) {
            VBox card = buildCard(
                    a.getTypeAbsence(),
                    "du " + a.getDateDebut() + " au " + a.getDateFin() + " | statut: " + a.getStatut()
            );
            card.setOnMouseClicked(event -> {
                if (selectedCard != null) {
                    selectedCard.getStyleClass().remove("entity-card-selected");
                }
                selectedCard = card;
                selectedCard.getStyleClass().add("entity-card-selected");
                selectedAbsence = a;
                dateDebut.setValue(a.getDateDebut());
                dateFin.setValue(a.getDateFin());
                typeAbsence.setValue(a.getTypeAbsence());
                setInfo("Selection demande #" + a.getId(), false);
            });
            tableMyAbsences.getChildren().add(card);
        }
    }

    private VBox buildCard(String title, String meta) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("entity-card-title");
        Label metaLabel = new Label(meta);
        metaLabel.getStyleClass().add("entity-card-meta");
        VBox card = new VBox(4, titleLabel, metaLabel);
        card.getStyleClass().add("entity-card");
        return card;
    }

    private void clearForm() {
        dateDebut.setValue(null);
        dateFin.setValue(null);
        typeAbsence.setValue(null);
        selectedAbsence = null;
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("entity-card-selected");
            selectedCard = null;
        }
    }

    private void setInfo(String message, boolean isError) {
        if (lblInfo == null) {
            return;
        }
        lblInfo.setText(message == null ? "" : message);
        if (isError) {
            lblInfo.setStyle("-fx-text-fill: #d64545; -fx-font-weight: 700;");
        } else {
            lblInfo.setStyle("-fx-text-fill: #2f855a; -fx-font-weight: 700;");
        }
    }

    @FXML
    public void openPublications(ActionEvent event) {
        try {
            Stage stage = (Stage) dateDebut.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/publication/publications.fxml"))));
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture publications: " + e.getMessage()).show();
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
