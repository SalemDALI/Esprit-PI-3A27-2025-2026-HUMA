package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Absence;
import services.ServiceAbsence;
import utils.Session;

import java.io.IOException;
import java.time.LocalDate;

public class ManagerAbsenceController {

    @FXML
    private TableView<Absence> tableAbsences;
    @FXML
    private TableColumn<Absence, Integer> colEmploye;
    @FXML
    private TableColumn<Absence, String> colType;
    @FXML
    private TableColumn<Absence, LocalDate> colDebut;
    @FXML
    private TableColumn<Absence, LocalDate> colFin;
    @FXML
    private TableColumn<Absence, String> colStatut;
    @FXML
    private Label lblActionMessage;

    private final ServiceAbsence service = new ServiceAbsence();
    private int managerId;

    @FXML
    public void initialize() {
        colEmploye.setCellValueFactory(new PropertyValueFactory<>("employeId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeAbsence"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    public void setManagerId(int id) {
        this.managerId = id;
        refresh();
    }

    @FXML
    void accepter() {
        Absence a = tableAbsences.getSelectionModel().getSelectedItem();
        if (a == null) {
            setMessage("Selectionnez un conge.", true);
            return;
        }
        if (service.changerStatut(a.getId(), "ACCEPTE")) {
            setMessage("Conge accepte.", false);
            refresh();
        } else {
            setMessage("Echec de mise a jour du conge.", true);
        }
    }

    @FXML
    void refuser() {
        Absence a = tableAbsences.getSelectionModel().getSelectedItem();
        if (a == null) {
            setMessage("Selectionnez un conge.", true);
            return;
        }
        if (service.changerStatut(a.getId(), "REFUSE")) {
            setMessage("Conge refuse.", false);
            refresh();
        } else {
            setMessage("Echec de mise a jour du conge.", true);
        }
    }

    @FXML
    void refresh() {
        if (managerId > 0) {
            tableAbsences.getItems().setAll(service.getCongesEquipe(managerId));
            setMessage("", false);
        } else {
            tableAbsences.getItems().clear();
            setMessage("Manager non initialise.", true);
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
