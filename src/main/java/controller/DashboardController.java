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
import javafx.scene.layout.VBox;
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

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceCandidat serviceCandidat = new ServiceCandidat();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private User selectedUser;
    private Candidat selectedCandidat;
    private OffreEmploi selectedOffre;
    private Candidature selectedCandidature;
    private Node selectedUserCard;
    private Node selectedCandidatCard;
    private Node selectedOffreCard;
    private Node selectedCandidatureCard;

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
        for (User user : serviceUser.getAll()) {
            VBox card = buildCard(
                    user.getNom() + " " + user.getPrenom(),
                    user.getEmail() + " | role: " + user.getRole()
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
                cbRole.setValue(user.getRole());
            });
            tableUsers.getChildren().add(card);
        }
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
        for (Candidature candidature : serviceCandidature.getAll()) {
            VBox card = buildCard(
                    "Candidature",
                    "candidat: " + candidature.getCandidatId()
                            + " | offre: " + candidature.getOffreId()
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
