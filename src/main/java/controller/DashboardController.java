package controller;

import entities.Candidat;
import entities.Candidature;
import entities.OffreEmploi;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ServiceCandidat;
import services.ServiceCandidature;
import services.ServiceOffre;
import services.ServiceUser;
import utils.Session;

import java.io.IOException;
import java.time.LocalDate;

public class DashboardController {
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab tabUsers;
    @FXML
    private Tab tabCandidats;
    @FXML
    private Tab tabOffres;
    @FXML
    private Tab tabCandidatures;

    @FXML
    private TableView<User> tableUsers;
    @FXML
    private TableColumn<User, Integer> colUserId;
    @FXML
    private TableColumn<User, String> colUserNom;
    @FXML
    private TableColumn<User, String> colUserPrenom;
    @FXML
    private TableColumn<User, String> colUserEmail;
    @FXML
    private TableColumn<User, String> colUserRole;
    @FXML
    private TextField txtUserId;
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMdp;
    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private TableView<Candidat> tableCandidats;
    @FXML
    private TableColumn<Candidat, Integer> colCandId;
    @FXML
    private TableColumn<Candidat, String> colCandNom;
    @FXML
    private TableColumn<Candidat, String> colCandPrenom;
    @FXML
    private TableColumn<Candidat, String> colCandEmail;
    @FXML
    private TableColumn<Candidat, String> colCandRole;
    @FXML
    private TextField txtCandidatId;

    @FXML
    private TableView<OffreEmploi> tableOffres;
    @FXML
    private TableColumn<OffreEmploi, Integer> colOffreId;
    @FXML
    private TableColumn<OffreEmploi, String> colOffreTitre;
    @FXML
    private TableColumn<OffreEmploi, String> colOffreDept;
    @FXML
    private TableColumn<OffreEmploi, String> colOffreType;
    @FXML
    private TableColumn<OffreEmploi, Integer> colOffrePostes;
    @FXML
    private TableColumn<OffreEmploi, LocalDate> colOffreDate;
    @FXML
    private TableColumn<OffreEmploi, Integer> colOffreAdmin;
    @FXML
    private TextField txtOffreId;
    @FXML
    private TextField txtTitre;
    @FXML
    private TextField txtDepartement;
    @FXML
    private TextField txtTypeContrat;
    @FXML
    private TextField txtNombrePostes;
    @FXML
    private DatePicker dpDatePublication;
    @FXML
    private TextField txtAdminId;

    @FXML
    private TableView<Candidature> tableCandidatures;
    @FXML
    private TableColumn<Candidature, Integer> colCadId;
    @FXML
    private TableColumn<Candidature, Integer> colCadCandidat;
    @FXML
    private TableColumn<Candidature, Integer> colCadOffre;
    @FXML
    private TableColumn<Candidature, LocalDate> colCadDate;
    @FXML
    private TableColumn<Candidature, String> colCadStatut;
    @FXML
    private TextField txtCandidatureId;
    @FXML
    private TextField txtCandUserId;
    @FXML
    private TextField txtCandOffreId;
    @FXML
    private DatePicker dpDateCandidature;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private Label lblPageMessage;
    @FXML
    private Label lblCardPostes;
    @FXML
    private Label lblCardDemandes;
    @FXML
    private Label lblCardEmployes;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceCandidat serviceCandidat = new ServiceCandidat();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();

    @FXML
    public void initialize() {
        if (tableUsers != null) {
            initUserSection();
        }
        if (tableCandidats != null) {
            initCandidatSection();
        }
        if (tableOffres != null) {
            initOffreSection();
        }
        if (tableCandidatures != null) {
            initCandidatureSection();
        }
        setPageMessage("", false);

        if (Session.getUser() != null && txtAdminId != null) {
            txtAdminId.setText(String.valueOf(Session.getUser().getId()));
        }

        refreshData();
    }

    private void initUserSection() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtUserId.setText(String.valueOf(selected.getId()));
                txtNom.setText(selected.getNom());
                txtPrenom.setText(selected.getPrenom());
                txtEmail.setText(selected.getEmail());
                txtMdp.setText(selected.getMdp());
                cbRole.setValue(selected.getRole());
            }
        });
    }

    private void initCandidatSection() {
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCandPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        tableCandidats.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtCandidatId.setText(String.valueOf(selected.getId()));
            }
        });
    }

    private void initOffreSection() {
        colOffreId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOffreTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colOffreDept.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colOffreType.setCellValueFactory(new PropertyValueFactory<>("typeContrat"));
        colOffrePostes.setCellValueFactory(new PropertyValueFactory<>("nombrePostes"));
        colOffreDate.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colOffreAdmin.setCellValueFactory(new PropertyValueFactory<>("adminId"));

        tableOffres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtOffreId.setText(String.valueOf(selected.getId()));
                txtTitre.setText(selected.getTitre());
                txtDepartement.setText(selected.getDepartement());
                txtTypeContrat.setText(selected.getTypeContrat());
                txtNombrePostes.setText(String.valueOf(selected.getNombrePostes()));
                dpDatePublication.setValue(selected.getDatePublication());
                txtAdminId.setText(String.valueOf(selected.getAdminId()));
            }
        });
    }

    private void initCandidatureSection() {
        colCadId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCadCandidat.setCellValueFactory(new PropertyValueFactory<>("candidatId"));
        colCadOffre.setCellValueFactory(new PropertyValueFactory<>("offreId"));
        colCadDate.setCellValueFactory(new PropertyValueFactory<>("dateCandidature"));
        colCadStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        tableCandidatures.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtCandidatureId.setText(String.valueOf(selected.getId()));
                txtCandUserId.setText(String.valueOf(selected.getCandidatId()));
                txtCandOffreId.setText(String.valueOf(selected.getOffreId()));
                dpDateCandidature.setValue(selected.getDateCandidature());
                cbStatut.setValue(selected.getStatut());
            }
        });
    }

    @FXML
    public void refreshData() {
        if (tableUsers != null) {
            tableUsers.getItems().setAll(serviceUser.getAll());
        }
        if (tableCandidats != null) {
            tableCandidats.getItems().setAll(serviceCandidat.getAll());
        }
        if (tableOffres != null) {
            tableOffres.getItems().setAll(serviceOffre.getAll());
        }
        if (tableCandidatures != null) {
            tableCandidatures.getItems().setAll(serviceCandidature.getAll());
        }
        refreshDashboardStats();
        setPageMessage("", false);
    }

    @FXML
    public void openRecrutement(ActionEvent event) {
        navigateTo(event, "/fxml/recrutement.fxml");
    }

    @FXML
    public void openDashboard(ActionEvent event) {
        navigateTo(event, "/fxml/dashboard.fxml");
    }

    @FXML
    public void openConges(ActionEvent event) {
        navigateTo(event, "/fxml/conges.fxml");
    }

    @FXML
    public void openAbsences(ActionEvent event) {
        navigateTo(event, "/fxml/absences.fxml");
    }

    @FXML
    public void openCommunication(ActionEvent event) {
        navigateTo(event, "/fxml/communication.fxml");
    }

    @FXML
    public void openFeedback(ActionEvent event) {
        navigateTo(event, "/fxml/feedback.fxml");
    }

    @FXML
    public void openParametres(ActionEvent event) {
        navigateTo(event, "/fxml/parametres.fxml");
    }

    private void refreshDashboardStats() {
        if (lblCardPostes != null) {
            lblCardPostes.setText(String.valueOf(serviceOffre.countPostesOuverts()));
        }
        if (lblCardDemandes != null) {
            lblCardDemandes.setText(String.valueOf(serviceCandidature.countByStatut("EN_ATTENTE")));
        }
        if (lblCardEmployes != null) {
            lblCardEmployes.setText(String.valueOf(serviceUser.countByRole("EMPLOYE")));
        }
    }

    @FXML
    public void addUser() {
        try {
            User user = new User();
            user.setNom(txtNom.getText().trim());
            user.setPrenom(txtPrenom.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setMdp(txtMdp.getText().trim());
            user.setRole(cbRole.getValue());
            if (serviceUser.ajouter(user)) {
                refreshData();
                clearUserForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateUser() {
        if (txtUserId.getText().isBlank()) {
            showError("Selectionnez un user");
            return;
        }
        try {
            User user = new User();
            user.setId(Integer.parseInt(txtUserId.getText()));
            user.setNom(txtNom.getText().trim());
            user.setPrenom(txtPrenom.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setMdp(txtMdp.getText().trim());
            user.setRole(cbRole.getValue());
            if (serviceUser.update(user)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteUser() {
        if (txtUserId.getText().isBlank()) {
            showError("Selectionnez un user");
            return;
        }
        if (serviceUser.delete(Integer.parseInt(txtUserId.getText()))) {
            refreshData();
            clearUserForm();
        }
    }

    @FXML
    public void clearUserForm() {
        txtUserId.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtMdp.clear();
        cbRole.setValue(null);
    }

    @FXML
    public void addCandidat() {
        try {
            Candidat c = new Candidat(Integer.parseInt(txtCandidatId.getText().trim()), "");
            if (serviceCandidat.ajouter(c)) {
                refreshData();
                clearCandidatForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateCandidat() {
        try {
            Candidat c = new Candidat(Integer.parseInt(txtCandidatId.getText().trim()), "");
            if (serviceCandidat.update(c)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteCandidat() {
        try {
            if (serviceCandidat.delete(Integer.parseInt(txtCandidatId.getText().trim()))) {
                refreshData();
                clearCandidatForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void clearCandidatForm() {
        txtCandidatId.clear();
    }

    @FXML
    public void addOffre() {
        try {
            OffreEmploi o = new OffreEmploi();
            o.setTitre(txtTitre.getText().trim());
            o.setDescription("");
            o.setDepartement(txtDepartement.getText().trim());
            o.setTypeContrat(txtTypeContrat.getText().trim());
            o.setNombrePostes(Integer.parseInt(txtNombrePostes.getText().trim()));
            o.setDatePublication(dpDatePublication.getValue() == null ? LocalDate.now() : dpDatePublication.getValue());
            o.setAdminId(Integer.parseInt(txtAdminId.getText().trim()));
            if (serviceOffre.ajouter(o)) {
                refreshData();
                clearOffreForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateOffre() {
        if (txtOffreId.getText().isBlank()) {
            showError("Selectionnez une offre");
            return;
        }
        try {
            OffreEmploi o = new OffreEmploi();
            o.setId(Integer.parseInt(txtOffreId.getText()));
            o.setTitre(txtTitre.getText().trim());
            o.setDescription("");
            o.setDepartement(txtDepartement.getText().trim());
            o.setTypeContrat(txtTypeContrat.getText().trim());
            o.setNombrePostes(Integer.parseInt(txtNombrePostes.getText().trim()));
            o.setDatePublication(dpDatePublication.getValue() == null ? LocalDate.now() : dpDatePublication.getValue());
            o.setAdminId(Integer.parseInt(txtAdminId.getText().trim()));
            if (serviceOffre.update(o)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteOffre() {
        if (txtOffreId.getText().isBlank()) {
            showError("Selectionnez une offre");
            return;
        }
        if (serviceOffre.delete(Integer.parseInt(txtOffreId.getText()))) {
            refreshData();
            clearOffreForm();
        }
    }

    @FXML
    public void clearOffreForm() {
        txtOffreId.clear();
        txtTitre.clear();
        txtDepartement.clear();
        txtTypeContrat.clear();
        txtNombrePostes.clear();
        dpDatePublication.setValue(null);
        if (Session.getUser() != null) {
            txtAdminId.setText(String.valueOf(Session.getUser().getId()));
        } else {
            txtAdminId.clear();
        }
    }

    @FXML
    public void addCandidature() {
        try {
            Candidature c = new Candidature();
            c.setCandidatId(Integer.parseInt(txtCandUserId.getText().trim()));
            c.setOffreId(Integer.parseInt(txtCandOffreId.getText().trim()));
            c.setDateCandidature(dpDateCandidature.getValue() == null ? LocalDate.now() : dpDateCandidature.getValue());
            c.setStatut(cbStatut.getValue() == null ? "EN_ATTENTE" : cbStatut.getValue());
            if (serviceCandidature.ajouter(c)) {
                refreshData();
                clearCandidatureForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateCandidature() {
        if (txtCandidatureId.getText().isBlank()) {
            showError("Selectionnez une candidature");
            return;
        }
        try {
            Candidature c = new Candidature();
            c.setId(Integer.parseInt(txtCandidatureId.getText()));
            c.setCandidatId(Integer.parseInt(txtCandUserId.getText().trim()));
            c.setOffreId(Integer.parseInt(txtCandOffreId.getText().trim()));
            c.setDateCandidature(dpDateCandidature.getValue() == null ? LocalDate.now() : dpDateCandidature.getValue());
            c.setStatut(cbStatut.getValue() == null ? "EN_ATTENTE" : cbStatut.getValue());
            if (serviceCandidature.update(c)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteCandidature() {
        if (txtCandidatureId.getText().isBlank()) {
            showError("Selectionnez une candidature");
            return;
        }
        if (serviceCandidature.supprimer(Integer.parseInt(txtCandidatureId.getText()))) {
            refreshData();
            clearCandidatureForm();
        }
    }

    @FXML
    public void clearCandidatureForm() {
        txtCandidatureId.clear();
        txtCandUserId.clear();
        txtCandOffreId.clear();
        dpDateCandidature.setValue(null);
        cbStatut.setValue(null);
    }

    @FXML
    public void accepter() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected != null && serviceCandidature.updateStatut(selected.getId(), "ACCEPTEE")) {
            refreshData();
        }
    }

    @FXML
    public void refuser() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected != null && serviceCandidature.updateStatut(selected.getId(), "REFUSEE")) {
            refreshData();
        }
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        setPageMessage(message, true);
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

    private void navigateTo(ActionEvent event, String fxml) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxml))));
            stage.show();
        } catch (IOException e) {
            showError("Navigation impossible: " + e.getMessage());
        }
    }
}
