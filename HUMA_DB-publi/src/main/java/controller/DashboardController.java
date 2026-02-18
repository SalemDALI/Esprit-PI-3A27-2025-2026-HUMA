package controller;

import models.Candidat;
import models.Candidature;
import models.OffreEmploi;
import models.User;
import models.Absence;
import models.Publication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceCandidat;
import services.ServiceCandidature;
import services.ServiceOffre;
import services.ServiceUser;
import services.ServiceAbsence;
import services.PublicationService;
import utils.Session;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private VBox tableUsers;
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
    private TextField txtManagerId;
    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private VBox tableCandidats;
    @FXML
    private TextField txtCandidatId;

    @FXML
    private VBox tableOffres;
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
    private VBox tableCandidatures;
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

    @FXML
    private VBox tableConges;
    @FXML
    private TextField txtCongeId;
    @FXML
    private TextField txtCongeEmployeId;
    @FXML
    private DatePicker dpCongeDebut;
    @FXML
    private DatePicker dpCongeFin;
    @FXML
    private ComboBox<String> cbCongeStatut;

    @FXML
    private VBox tableAbsencesAdmin;
    @FXML
    private TextField txtAbsenceId;
    @FXML
    private TextField txtAbsenceEmployeId;
    @FXML
    private DatePicker dpAbsenceDebut;
    @FXML
    private DatePicker dpAbsenceFin;
    @FXML
    private ComboBox<String> cbAbsenceType;
    @FXML
    private ComboBox<String> cbAbsenceStatut;
    @FXML
    private TextField txtPublicationTitre;
    @FXML
    private TextArea txtPublicationContenu;
    @FXML
    private VBox publicationList;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceCandidat serviceCandidat = new ServiceCandidat();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceAbsence serviceAbsence = new ServiceAbsence();
    private User selectedUser;
    private Candidat selectedCandidat;
    private OffreEmploi selectedOffre;
    private Candidature selectedCandidature;
    private Node selectedUserCard;
    private Node selectedCandidatCard;
    private Node selectedOffreCard;
    private Node selectedCandidatureCard;
    private Absence selectedConge;
    private Node selectedCongeCard;
    private Absence selectedAbsenceAdmin;
    private Node selectedAbsenceAdminCard;
    private Publication selectedPublication;
    private Node selectedPublicationCard;
    private final PublicationService publicationService = new PublicationService();

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
        if (tableConges != null) {
            initCongeSection();
        }
        if (tableAbsencesAdmin != null) {
            initAbsenceSection();
        }
        if (publicationList != null) {
            initCommunicationSection();
        }
        setPageMessage("", false);

        if (Session.getUser() != null && txtAdminId != null) {
            txtAdminId.setText(String.valueOf(Session.getUser().getId()));
        }

        refreshData();
    }

    private void initUserSection() {
        tableUsers.getStyleClass().add("cards-container");
    }

    private void initCandidatSection() {
        tableCandidats.getStyleClass().add("cards-container");
    }

    private void initOffreSection() {
        tableOffres.getStyleClass().add("cards-container");
    }

    private void initCandidatureSection() {
        tableCandidatures.getStyleClass().add("cards-container");
    }

    private void initCongeSection() {
        tableConges.getStyleClass().add("cards-container");
    }

    private void initAbsenceSection() {
        tableAbsencesAdmin.getStyleClass().add("cards-container");
    }

    private void initCommunicationSection() {
        publicationList.getStyleClass().add("cards-container");
    }

    @FXML
    public void refreshData() {
        if (tableUsers != null) {
            renderUserCards();
        }
        if (tableCandidats != null) {
            renderCandidatCards();
        }
        if (tableOffres != null) {
            renderOffreCards();
        }
        if (tableCandidatures != null) {
            renderCandidatureCards();
        }
        if (tableConges != null) {
            renderCongeCards();
        }
        if (tableAbsencesAdmin != null) {
            renderAbsenceAdminCards();
        }
        if (publicationList != null) {
            renderCommunicationCards();
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
            user.setManagerId(parseManagerId());
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
            user.setManagerId(parseManagerId());
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
        txtManagerId.clear();
        cbRole.setValue(null);
        clearSelection(tableUsers, selectedUserCard);
        selectedUser = null;
        selectedUserCard = null;
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
        clearSelection(tableCandidats, selectedCandidatCard);
        selectedCandidat = null;
        selectedCandidatCard = null;
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
        clearSelection(tableOffres, selectedOffreCard);
        selectedOffre = null;
        selectedOffreCard = null;
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
        clearSelection(tableCandidatures, selectedCandidatureCard);
        selectedCandidature = null;
        selectedCandidatureCard = null;
    }

    @FXML
    public void accepter() {
        Candidature selected = selectedCandidature;
        if (selected != null && serviceCandidature.updateStatut(selected.getId(), "ACCEPTEE")) {
            refreshData();
        }
    }

    @FXML
    public void refuser() {
        Candidature selected = selectedCandidature;
        if (selected != null && serviceCandidature.updateStatut(selected.getId(), "REFUSEE")) {
            refreshData();
        }
    }

    @FXML
    public void addConge() {
        try {
            Absence a = new Absence();
            a.setEmployeId(Integer.parseInt(txtCongeEmployeId.getText().trim()));
            a.setDateDebut(dpCongeDebut.getValue());
            a.setDateFin(dpCongeFin.getValue());
            a.setTypeAbsence("CONGE");
            a.setStatut(cbCongeStatut.getValue() == null ? "EN_ATTENTE" : cbCongeStatut.getValue());
            if (serviceAbsence.addAdmin(a)) {
                refreshData();
                clearCongeForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateConge() {
        if (txtCongeId.getText().isBlank()) {
            showError("Selectionnez un conge");
            return;
        }
        try {
            Absence a = new Absence();
            a.setId(Integer.parseInt(txtCongeId.getText().trim()));
            a.setEmployeId(Integer.parseInt(txtCongeEmployeId.getText().trim()));
            a.setDateDebut(dpCongeDebut.getValue());
            a.setDateFin(dpCongeFin.getValue());
            a.setTypeAbsence("CONGE");
            a.setStatut(cbCongeStatut.getValue() == null ? "EN_ATTENTE" : cbCongeStatut.getValue());
            if (serviceAbsence.updateAdmin(a)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteConge() {
        if (txtCongeId.getText().isBlank()) {
            showError("Selectionnez un conge");
            return;
        }
        if (serviceAbsence.deleteAdmin(Integer.parseInt(txtCongeId.getText().trim()))) {
            refreshData();
            clearCongeForm();
        }
    }

    @FXML
    public void clearCongeForm() {
        txtCongeId.clear();
        txtCongeEmployeId.clear();
        dpCongeDebut.setValue(null);
        dpCongeFin.setValue(null);
        cbCongeStatut.setValue(null);
        clearSelection(tableConges, selectedCongeCard);
        selectedConge = null;
        selectedCongeCard = null;
    }

    @FXML
    public void addAbsenceAdmin() {
        try {
            Absence a = new Absence();
            a.setEmployeId(Integer.parseInt(txtAbsenceEmployeId.getText().trim()));
            a.setDateDebut(dpAbsenceDebut.getValue());
            a.setDateFin(dpAbsenceFin.getValue());
            a.setTypeAbsence(cbAbsenceType.getValue());
            a.setStatut(cbAbsenceStatut.getValue() == null ? "EN_ATTENTE" : cbAbsenceStatut.getValue());
            if (serviceAbsence.addAdmin(a)) {
                refreshData();
                clearAbsenceAdminForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateAbsenceAdmin() {
        if (txtAbsenceId.getText().isBlank()) {
            showError("Selectionnez une absence");
            return;
        }
        try {
            Absence a = new Absence();
            a.setId(Integer.parseInt(txtAbsenceId.getText().trim()));
            a.setEmployeId(Integer.parseInt(txtAbsenceEmployeId.getText().trim()));
            a.setDateDebut(dpAbsenceDebut.getValue());
            a.setDateFin(dpAbsenceFin.getValue());
            a.setTypeAbsence(cbAbsenceType.getValue());
            a.setStatut(cbAbsenceStatut.getValue() == null ? "EN_ATTENTE" : cbAbsenceStatut.getValue());
            if (serviceAbsence.updateAdmin(a)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteAbsenceAdmin() {
        if (txtAbsenceId.getText().isBlank()) {
            showError("Selectionnez une absence");
            return;
        }
        if (serviceAbsence.deleteAdmin(Integer.parseInt(txtAbsenceId.getText().trim()))) {
            refreshData();
            clearAbsenceAdminForm();
        }
    }

    @FXML
    public void clearAbsenceAdminForm() {
        txtAbsenceId.clear();
        txtAbsenceEmployeId.clear();
        dpAbsenceDebut.setValue(null);
        dpAbsenceFin.setValue(null);
        cbAbsenceType.setValue(null);
        cbAbsenceStatut.setValue(null);
        clearSelection(tableAbsencesAdmin, selectedAbsenceAdminCard);
        selectedAbsenceAdmin = null;
        selectedAbsenceAdminCard = null;
    }

    @FXML
    public void publishCommunication() {
        if (txtPublicationTitre == null || txtPublicationContenu == null) {
            return;
        }
        User current = Session.getUser();
        if (current == null) {
            showError("Session invalide.");
            return;
        }
        String role = current.getRole() == null ? "" : current.getRole().trim().toUpperCase();
        if (!role.contains("ADMIN")) {
            showError("Seul ADMIN RH peut publier une publication.");
            return;
        }
        String titre = txtPublicationTitre.getText() == null ? "" : txtPublicationTitre.getText().trim();
        String contenu = txtPublicationContenu.getText() == null ? "" : txtPublicationContenu.getText().trim();
        if (titre.isBlank() || contenu.isBlank()) {
            showError("Titre et contenu sont obligatoires.");
            return;
        }

        String auteur = current.getNom() + " " + current.getPrenom();
        publicationService.publish(titre, contenu, auteur);

        txtPublicationTitre.clear();
        txtPublicationContenu.clear();
        renderCommunicationCards();
        setPageMessage("Publication publiee avec succes.", false);
    }

    @FXML
    public void deleteCommunicationPublication() {
        User current = Session.getUser();
        if (current == null) {
            showError("Session invalide.");
            return;
        }
        String role = current.getRole() == null ? "" : current.getRole().trim().toUpperCase();
        if (!role.contains("ADMIN")) {
            showError("Seul ADMIN RH peut supprimer une publication.");
            return;
        }
        if (selectedPublication == null) {
            showError("Selectionnez une publication a supprimer.");
            return;
        }
        if (publicationService.deleteById(selectedPublication.getId())) {
            selectedPublication = null;
            if (selectedPublicationCard != null) {
                selectedPublicationCard.getStyleClass().remove("entity-card-selected");
                selectedPublicationCard = null;
            }
            renderCommunicationCards();
            setPageMessage("Publication supprimee.", false);
        } else {
            showError("Suppression impossible.");
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

    private void renderUserCards() {
        tableUsers.getChildren().clear();
        clearSelection(tableUsers, selectedUserCard);
        selectedUser = null;
        selectedUserCard = null;
        List<User> allUsers = serviceUser.getAll();
        Map<Integer, User> usersById = allUsers.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (User user : allUsers) {
            String managerDisplay = "-";
            if (user.getManagerId() != null) {
                User manager = usersById.get(user.getManagerId());
                if (manager != null) {
                    managerDisplay = manager.getNom() + " " + manager.getPrenom();
                } else {
                    managerDisplay = "Manager inconnu";
                }
            }
            VBox card = buildCard(
                    user.getNom() + " " + user.getPrenom(),
                    user.getEmail() + " | role: " + user.getRole()
                            + " | manager: " + managerDisplay
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableUsers, selectedUserCard, card);
                selectedUserCard = card;
                selectedUser = user;
                txtUserId.setText(String.valueOf(user.getId()));
                txtNom.setText(user.getNom());
                txtPrenom.setText(user.getPrenom());
                txtEmail.setText(user.getEmail());
                txtMdp.setText(user.getMdp());
                txtManagerId.setText(user.getManagerId() == null ? "" : String.valueOf(user.getManagerId()));
                cbRole.setValue(user.getRole());
            });
            tableUsers.getChildren().add(card);
        }
    }

    private Integer parseManagerId() {
        if (txtManagerId == null || txtManagerId.getText() == null || txtManagerId.getText().isBlank()) {
            return null;
        }
        return Integer.parseInt(txtManagerId.getText().trim());
    }

    private void renderCandidatCards() {
        tableCandidats.getChildren().clear();
        clearSelection(tableCandidats, selectedCandidatCard);
        selectedCandidat = null;
        selectedCandidatCard = null;
        for (Candidat candidat : serviceCandidat.getAll()) {
            VBox card = buildCard(
                    candidat.getNom() + " " + candidat.getPrenom(),
                    candidat.getEmail() + " | role: " + candidat.getRole()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableCandidats, selectedCandidatCard, card);
                selectedCandidatCard = card;
                selectedCandidat = candidat;
                txtCandidatId.setText(String.valueOf(candidat.getId()));
            });
            tableCandidats.getChildren().add(card);
        }
    }

    private void renderOffreCards() {
        tableOffres.getChildren().clear();
        clearSelection(tableOffres, selectedOffreCard);
        selectedOffre = null;
        selectedOffreCard = null;
        for (OffreEmploi offre : serviceOffre.getAll()) {
            VBox card = buildCard(
                    offre.getTitre(),
                    offre.getDepartement() + " | " + offre.getTypeContrat()
                            + " | postes: " + offre.getNombrePostes()
                            + " | date: " + offre.getDatePublication()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableOffres, selectedOffreCard, card);
                selectedOffreCard = card;
                selectedOffre = offre;
                txtOffreId.setText(String.valueOf(offre.getId()));
                txtTitre.setText(offre.getTitre());
                txtDepartement.setText(offre.getDepartement());
                txtTypeContrat.setText(offre.getTypeContrat());
                txtNombrePostes.setText(String.valueOf(offre.getNombrePostes()));
                dpDatePublication.setValue(offre.getDatePublication());
                txtAdminId.setText(String.valueOf(offre.getAdminId()));
            });
            tableOffres.getChildren().add(card);
        }
    }

    private void renderCandidatureCards() {
        tableCandidatures.getChildren().clear();
        clearSelection(tableCandidatures, selectedCandidatureCard);
        selectedCandidature = null;
        selectedCandidatureCard = null;
        Map<Integer, User> usersById = serviceUser.getAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, OffreEmploi> offresById = serviceOffre.getAll().stream()
                .collect(Collectors.toMap(OffreEmploi::getId, o -> o));
        for (Candidature candidature : serviceCandidature.getAll()) {
            User candidat = usersById.get(candidature.getCandidatId());
            String candidatLabel = candidat == null
                    ? "Candidat inconnu"
                    : candidat.getNom() + " " + candidat.getPrenom();
            OffreEmploi offre = offresById.get(candidature.getOffreId());
            String offreLabel = offre == null ? "Offre inconnue" : offre.getTitre();
            VBox card = buildCard(
                    "Candidature",
                    "candidat: " + candidatLabel
                            + " | offre: " + offreLabel
                            + " | date: " + candidature.getDateCandidature()
                            + " | statut: " + candidature.getStatut()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableCandidatures, selectedCandidatureCard, card);
                selectedCandidatureCard = card;
                selectedCandidature = candidature;
                txtCandidatureId.setText(String.valueOf(candidature.getId()));
                txtCandUserId.setText(String.valueOf(candidature.getCandidatId()));
                txtCandOffreId.setText(String.valueOf(candidature.getOffreId()));
                dpDateCandidature.setValue(candidature.getDateCandidature());
                cbStatut.setValue(candidature.getStatut());
            });
            tableCandidatures.getChildren().add(card);
        }
    }

    private void renderCongeCards() {
        tableConges.getChildren().clear();
        clearSelection(tableConges, selectedCongeCard);
        selectedConge = null;
        selectedCongeCard = null;
        for (Absence conge : serviceAbsence.getCongesAdmin()) {
            String employe = conge.getEmployeNom() == null ? "Employe #" + conge.getEmployeId() : conge.getEmployeNom();
            VBox card = buildCard(
                    employe,
                    "du " + conge.getDateDebut() + " au " + conge.getDateFin() + " | statut: " + conge.getStatut()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableConges, selectedCongeCard, card);
                selectedCongeCard = card;
                selectedConge = conge;
                txtCongeId.setText(String.valueOf(conge.getId()));
                txtCongeEmployeId.setText(String.valueOf(conge.getEmployeId()));
                dpCongeDebut.setValue(conge.getDateDebut());
                dpCongeFin.setValue(conge.getDateFin());
                cbCongeStatut.setValue(conge.getStatut());
            });
            tableConges.getChildren().add(card);
        }
    }

    private void renderAbsenceAdminCards() {
        tableAbsencesAdmin.getChildren().clear();
        clearSelection(tableAbsencesAdmin, selectedAbsenceAdminCard);
        selectedAbsenceAdmin = null;
        selectedAbsenceAdminCard = null;
        for (Absence absence : serviceAbsence.getAbsencesAdmin()) {
            String employe = absence.getEmployeNom() == null ? "Employe #" + absence.getEmployeId() : absence.getEmployeNom();
            VBox card = buildCard(
                    employe + " | " + absence.getTypeAbsence(),
                    "du " + absence.getDateDebut() + " au " + absence.getDateFin() + " | statut: " + absence.getStatut()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableAbsencesAdmin, selectedAbsenceAdminCard, card);
                selectedAbsenceAdminCard = card;
                selectedAbsenceAdmin = absence;
                txtAbsenceId.setText(String.valueOf(absence.getId()));
                txtAbsenceEmployeId.setText(String.valueOf(absence.getEmployeId()));
                dpAbsenceDebut.setValue(absence.getDateDebut());
                dpAbsenceFin.setValue(absence.getDateFin());
                cbAbsenceType.setValue(absence.getTypeAbsence());
                cbAbsenceStatut.setValue(absence.getStatut());
            });
            tableAbsencesAdmin.getChildren().add(card);
        }
    }

    private void renderCommunicationCards() {
        publicationList.getChildren().clear();
        selectedPublication = null;
        if (selectedPublicationCard != null) {
            selectedPublicationCard.getStyleClass().remove("entity-card-selected");
            selectedPublicationCard = null;
        }
        List<Publication> publications = publicationService.getAll();
        if (publications.isEmpty()) {
            publicationList.getChildren().add(buildCard("Aucune publication", "Publiez votre premiere communication."));
            return;
        }
        for (Publication publication : publications) {
            Label titleLabel = new Label(publication.getTitre());
            titleLabel.getStyleClass().add("entity-card-title");
            String date = publication.getDatePublication() == null ? "" :
                    publication.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Label metaLabel = new Label("Par " + publication.getAuteur() + " | " + date);
            metaLabel.getStyleClass().add("entity-card-meta");
            Label contentLabel = new Label(publication.getContenu());
            contentLabel.getStyleClass().add("entity-card-meta");
            contentLabel.setWrapText(true);
            VBox card = new VBox(6, titleLabel, metaLabel, contentLabel);
            card.getStyleClass().add("entity-card");
            card.setOnMouseClicked(event -> {
                if (selectedPublicationCard != null) {
                    selectedPublicationCard.getStyleClass().remove("entity-card-selected");
                }
                selectedPublicationCard = card;
                selectedPublicationCard.getStyleClass().add("entity-card-selected");
                selectedPublication = publication;
            });
            publicationList.getChildren().add(card);
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

    private void selectCard(VBox container, Node previousCard, Node newCard) {
        clearSelection(container, previousCard);
        if (newCard != null && !newCard.getStyleClass().contains("entity-card-selected")) {
            newCard.getStyleClass().add("entity-card-selected");
        }
    }

    private void clearSelection(VBox container, Node selectedCard) {
        if (container == null || selectedCard == null) {
            return;
        }
        selectedCard.getStyleClass().remove("entity-card-selected");
    }

}
