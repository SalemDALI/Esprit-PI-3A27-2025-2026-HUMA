package controller;

import models.Candidature;
import models.OffreEmploi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ServiceCandidature;
import services.ServiceOffre;
import utils.CvFileStorageUtil;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class CandidatController {

    @FXML
    private TableView<OffreEmploi> tableOffres;

    @FXML
    private TableColumn<OffreEmploi, String> colTitre;

    @FXML
    private TableColumn<OffreEmploi, String> colDept;

    @FXML
    private TableView<Candidature> tableCandidatures;

    @FXML
    private TableColumn<Candidature, String> colOffreCandidature;

    @FXML
    private TableColumn<Candidature, LocalDate> colDateCandidature;

    @FXML
    private TableColumn<Candidature, String> colStatutCandidature;

    @FXML
    private Label lblCvPath;
    @FXML
    private Label lblPageMessage;

    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private String selectedCvPath;

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colOffreCandidature.setCellValueFactory(new PropertyValueFactory<>("offreTitre"));
        colDateCandidature.setCellValueFactory(new PropertyValueFactory<>("dateCandidature"));
        colStatutCandidature.setCellValueFactory(new PropertyValueFactory<>("statut"));

        tableOffres.getItems().setAll(serviceOffre.getAll());
        setPageMessage("", false);
        refreshSuiviCandidatures();
    }

    @FXML
    public void postuler() {
        OffreEmploi offre = tableOffres.getSelectionModel().getSelectedItem();
        if (offre == null) {
            showError("Selectionnez une offre avant de postuler.");
            return;
        }
        if (selectedCvPath == null || selectedCvPath.isBlank()) {
            showError("Veuillez telecharger un CV PDF.");
            return;
        }
        if (Session.getUser() == null) {
            showError("Session invalide. Reconnectez-vous.");
            return;
        }

        Candidature c = new Candidature();
        c.setCandidatId(Session.getUser().getId());
        c.setOffreId(offre.getId());
        c.setDateCandidature(LocalDate.now());
        c.setStatut("EN_ATTENTE");
        c.setCheminCv(selectedCvPath);

        if (serviceCandidature.ajouter(c)) {
            refreshSuiviCandidatures();
            selectedCvPath = null;
            lblCvPath.setText("Aucun CV selectionne");
            showInfo("Candidature envoyee avec succes.");
        } else {
            showError("Echec lors de l'envoi de la candidature.");
        }
    }

    @FXML
    public void uploadCv(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selectionner un CV (PDF)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        File selected = chooser.showOpenDialog(((Stage) tableOffres.getScene().getWindow()));
        if (selected == null) {
            return;
        }

        if (!selected.getName().toLowerCase().endsWith(".pdf")) {
            showError("Le fichier doit etre au format PDF.");
            return;
        }

        try {
            selectedCvPath = CvFileStorageUtil.savePdfCv(selected.toPath());
            lblCvPath.setText(selectedCvPath);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Erreur lors de la sauvegarde du CV: " + e.getMessage());
        }
    }

    @FXML
    public void refreshSuiviCandidatures() {
        if (Session.getUser() == null) {
            tableCandidatures.getItems().clear();
            return;
        }
        tableCandidatures.getItems().setAll(serviceCandidature.getByCandidatId(Session.getUser().getId()));
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) tableOffres.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        setPageMessage(message, true);
    }

    private void showInfo(String message) {
        setPageMessage(message, false);
    }

    private void setPageMessage(String message, boolean isError) {
        if (lblPageMessage == null) {
            return;
        }
        lblPageMessage.setText(message == null ? "" : message);
        if (isError) {
            lblPageMessage.setStyle("-fx-text-fill: #d64545; -fx-font-weight: 700;");
        } else {
            lblPageMessage.setStyle("-fx-text-fill: #2f855a; -fx-font-weight: 700;");
        }
    }
}
