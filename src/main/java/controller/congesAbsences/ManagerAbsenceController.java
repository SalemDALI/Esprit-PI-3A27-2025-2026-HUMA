package controller.congesAbsences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import models.congesAbsences.Absence;
import models.recrutement.Entretien;
import services.congesAbsences.ServiceAbsence;
import services.recrutement.ServiceEntretien;
import utils.Session;

import java.awt.Desktop;
import java.io.IOException;

public class ManagerAbsenceController {

    @FXML
    private VBox tableAbsences;
    @FXML
    private VBox tableEntretiensManager;
    @FXML
    private Label lblActionMessage;

    private final ServiceAbsence service = new ServiceAbsence();
    private final ServiceEntretien serviceEntretien = new ServiceEntretien();
    private int managerId;
    private Absence selectedAbsence;
    private Node selectedCard;
    private Entretien selectedEntretien;
    private Node selectedEntretienCard;

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
        if (service.changerStatut(selectedAbsence.getId(), "ACCEPTE", managerId, "Valide par manager")) {
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
        if (service.changerStatut(selectedAbsence.getId(), "REFUSE", managerId, "Refuse par manager")) {
            setMessage("Conge refuse.", false);
            refresh();
        } else {
            setMessage("Echec de mise a jour du conge.", true);
        }
    }

    @FXML
    void refresh() {
        tableAbsences.getChildren().clear();
        if (tableEntretiensManager != null) {
            tableEntretiensManager.getChildren().clear();
        }
        selectedAbsence = null;
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("entity-card-selected");
            selectedCard = null;
        }
        selectedEntretien = null;
        if (selectedEntretienCard != null) {
            selectedEntretienCard.getStyleClass().remove("entity-card-selected");
            selectedEntretienCard = null;
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

        renderEntretiensManager();

        setMessage("", false);
    }

    @FXML
    void demarrerEntretien() {
        if (selectedEntretien == null) {
            setMessage("Selectionnez un entretien.", true);
            return;
        }
        openEntretienWebView(selectedEntretien.getMeetLink(), "Entretien Manager");
    }

    @FXML
    void openPublications(ActionEvent event) {
        try {
            Stage stage = (Stage) tableAbsences.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/publication/publications.fxml"))));
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
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/feedback/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VBox buildCard(String title, String meta) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("entity-card-title");
        titleLabel.setWrapText(true);
        Label metaLabel = new Label(meta);
        metaLabel.getStyleClass().add("entity-card-meta");
        metaLabel.setWrapText(true);
        VBox card = new VBox(4, titleLabel, metaLabel);
        card.getStyleClass().add("entity-card");
        return card;
    }

    private void renderEntretiensManager() {
        if (tableEntretiensManager == null || managerId <= 0) {
            return;
        }
        for (Entretien e : serviceEntretien.getByManagerId(managerId)) {
            VBox card = buildCard(
                    safe(e.getCandidatNom()) + " | " + safe(e.getOffreTitre()),
                    "Date: " + e.getDateEntretien()
                            + " | Duree: " + e.getDureeMinutes() + "min"
                            + " | Statut: " + safe(e.getStatut())
            );
            card.setOnMouseClicked(event -> {
                if (selectedEntretienCard != null) {
                    selectedEntretienCard.getStyleClass().remove("entity-card-selected");
                }
                selectedEntretienCard = card;
                selectedEntretienCard.getStyleClass().add("entity-card-selected");
                selectedEntretien = e;
                setMessage("Entretien selectionne: " + safe(e.getCandidatNom()), false);
            });
            tableEntretiensManager.getChildren().add(card);
        }
    }

    private void openEntretienWebView(String url, String title) {
        if (url == null || url.isBlank()) {
            setMessage("Lien entretien manquant.", true);
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI.create(url));
                setMessage("Ouverture entretien dans navigateur.", false);
                return;
            }
            Stage stage = new Stage();
            WebView webView = new WebView();
            webView.getEngine().load(url);
            stage.setScene(new Scene(webView, 1100, 760));
            stage.setTitle(title == null ? "Entretien" : title);
            stage.show();
        } catch (Exception ex) {
            setMessage("Impossible d'ouvrir l'entretien: " + ex.getMessage(), true);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
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
