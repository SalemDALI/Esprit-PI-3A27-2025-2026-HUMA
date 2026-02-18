package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Absence;
import services.ServiceAbsence;
import utils.Session;

import java.io.IOException;

public class ManagerAbsenceController {

    @FXML
    private VBox tableAbsences;
    @FXML
    private Label lblActionMessage;

    private final ServiceAbsence service = new ServiceAbsence();
    private int managerId;
    private Absence selectedAbsence;
    private Node selectedCard;

    public void setManagerId(int id) {
        this.managerId = id;
        refresh();
    }

    @FXML
    void accepter() {
        if (selectedAbsence == null) {
            setMessage("Selectionnez un conge.", true);
            return;
        }
        if (service.changerStatut(selectedAbsence.getId(), "ACCEPTE")) {
            setMessage("Conge accepte.", false);
            refresh();
        } else {
            setMessage("Echec de mise a jour du conge.", true);
        }
    }

    @FXML
    void refuser() {
        if (selectedAbsence == null) {
            setMessage("Selectionnez un conge.", true);
            return;
        }
        if (service.changerStatut(selectedAbsence.getId(), "REFUSE")) {
            setMessage("Conge refuse.", false);
            refresh();
        } else {
            setMessage("Echec de mise a jour du conge.", true);
        }
    }

    @FXML
    void refresh() {
        tableAbsences.getChildren().clear();
        selectedAbsence = null;
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("entity-card-selected");
            selectedCard = null;
        }

        if (managerId <= 0) {
            setMessage("Manager non initialise.", true);
            return;
        }

        for (Absence absence : service.getCongesEquipe(managerId)) {
            String employe = absence.getEmployeNom() == null ? "Employe" : absence.getEmployeNom();
            VBox card = buildCard(
                    employe,
                    absence.getTypeAbsence() + " | du " + absence.getDateDebut() + " au " + absence.getDateFin()
                            + " | statut: " + absence.getStatut()
            );
            card.setOnMouseClicked(event -> {
                if (selectedCard != null) {
                    selectedCard.getStyleClass().remove("entity-card-selected");
                }
                selectedCard = card;
                selectedCard.getStyleClass().add("entity-card-selected");
                selectedAbsence = absence;
                setMessage("Selection: " + employe, false);
            });
            tableAbsences.getChildren().add(card);
        }

        setMessage("", false);
    }

    @FXML
    void openPublications(ActionEvent event) {
        try {
            Stage stage = (Stage) tableAbsences.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/publications.fxml"))));
            stage.show();
        } catch (IOException e) {
            setMessage("Erreur ouverture publications: " + e.getMessage(), true);
        }
    }

    @FXML
    void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) tableAbsences.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void setMessage(String message, boolean isError) {
        if (lblActionMessage == null) {
            return;
        }
        lblActionMessage.setText(message == null ? "" : message);
        if (isError) {
            lblActionMessage.setStyle("-fx-text-fill: #d64545; -fx-font-weight: 700;");
        } else {
            lblActionMessage.setStyle("-fx-text-fill: #2f855a; -fx-font-weight: 700;");
        }
    }
}
