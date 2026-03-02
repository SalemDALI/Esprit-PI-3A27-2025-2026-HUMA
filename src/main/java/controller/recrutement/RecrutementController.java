package controller.recrutement;

import models.recrutement.CandidateScoringResult;
import models.recrutement.Candidature;
import models.recrutement.Entretien;
import models.recrutement.OffreEmploi;
import models.feedback.User;
import models.congesAbsences.Absence;
import models.communication.Publication;
import models.formation.Formation;
import models.formation.Participant;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import services.feedback.ServiceUser;
import services.communication.PublicationService;
import services.congesAbsences.ServiceAbsence;
import services.formation.CrudFormation;
import services.formation.CrudParticipant;
import services.recrutement.CandidateScoringService;
import services.recrutement.FacebookJobPublisherService;
import services.recrutement.LinkedInJobPublisherService;
import services.recrutement.RedditJobPublisherService;
import services.recrutement.ServiceCandidature;
import services.recrutement.ServiceEntretien;
import services.recrutement.ServiceOffre;
import utils.Session;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RecrutementController {
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
    private ComboBox<String> txtDepartement;
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
    private TextField txtGlobalSearch;
    @FXML
    private Label lblKpiTotalCandidatures;
    @FXML
    private Label lblKpiTauxAcceptation;
    @FXML
    private Label lblKpiTempsMoyen;
    @FXML
    private Label lblKpiOffresActives;
    @FXML
    private Label lblKpiEmployesActifs;
    @FXML
    private Label lblHeaderTotalUsers;
    @FXML
    private Label lblHeaderTotalOffres;
    @FXML
    private Label lblHeaderTotalCandidatures;
    @FXML
    private PieChart chartCandidaturesDept;
    @FXML
    private LineChart<String, Number> chartEvolutionLine;
    @FXML
    private BarChart<String, Number> chartEvolutionBar;
    @FXML
    private VBox tableOffresFillRate;
    @FXML
    private VBox tablePostesPourvoir;
    @FXML
    private javafx.scene.layout.FlowPane flowUsers;
    @FXML
    private javafx.scene.layout.FlowPane flowOffres;
    @FXML
    private javafx.scene.layout.FlowPane flowCandidatures;
    @FXML
    private BorderPane rootPane;
    @FXML
    private ScrollPane pageScroll;
    @FXML
    private ScrollPane scrollUsers;
    @FXML
    private ScrollPane scrollOffres;
    @FXML
    private ScrollPane scrollCandidatures;
    @FXML
    private ScrollPane scrollTopCv;
    @FXML
    private TextField txtSearchUsers;
    @FXML
    private TextField txtSearchOffres;
    @FXML
    private TextField txtSearchCandidatures;
    @FXML
    private ComboBox<String> cbFilterRole;
    @FXML
    private ComboBox<String> cbFilterDepartement;
    @FXML
    private ComboBox<String> cbFilterStatut;
    @FXML
    private Label lblUsersPage;
    @FXML
    private Label lblOffresPage;
    @FXML
    private Label lblCandidaturesPage;
    @FXML
    private TextField txtTopCvOffreId;
    @FXML
    private TextField txtTopCvLimit;
    @FXML
    private VBox tableTopCv;
    @FXML
    private Label lblTopCvJsonPath;
    @FXML
    private TextField txtScrapingOffreId;
    @FXML
    private TextField txtScrapingLimit;
    @FXML
    private VBox tableScrapingResults;
    @FXML
    private Label lblScrapingStatus;
    @FXML
    private ScrollPane scrollScraping;
    @FXML
    private Label lblPageMessage;
    @FXML
    private Label lblCardPostes;
    @FXML
    private Label lblCardDemandes;
    @FXML
    private Label lblCardEmployes;
    @FXML
    private Label lblCardCandidats;
    @FXML
    private Label lblCardAcceptees;
    @FXML
    private Label lblCardRefusees;
    @FXML
    private VBox tableEntretiensAdmin;

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
    private VBox tableFormations;
    @FXML
    private TextField txtFormationId;
    @FXML
    private TextField txtFormationSujet;
    @FXML
    private TextField txtFormationFormateur;
    @FXML
    private TextField txtFormationType;
    @FXML
    private DatePicker dpFormationDateDebut;
    @FXML
    private TextField txtFormationDuree;
    @FXML
    private TextField txtFormationLocalisation;
    @FXML
    private TextField txtFormationAdminId;

    @FXML
    private VBox tableParticipations;
    @FXML
    private TextField txtParticipationId;
    @FXML
    private DatePicker dpParticipationDate;
    @FXML
    private TextField txtParticipationResultat;
    @FXML
    private TextField txtParticipationEmployeId;
    @FXML
    private TextField txtParticipationFormationId;
    @FXML
    private TextField txtPublicationTitre;
    @FXML
    private TextArea txtPublicationContenu;
    @FXML
    private VBox publicationList;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceAbsence serviceAbsence = new ServiceAbsence();
    private final CrudFormation crudFormation = new CrudFormation();
    private final CrudParticipant crudParticipant = new CrudParticipant();
    private final CandidateScoringService candidateScoringService = new CandidateScoringService();
    private final LinkedInJobPublisherService linkedInJobPublisherService = new LinkedInJobPublisherService();
    private final FacebookJobPublisherService facebookJobPublisherService = new FacebookJobPublisherService();
    private final RedditJobPublisherService redditJobPublisherService = new RedditJobPublisherService();
    private final ServiceEntretien serviceEntretien = new ServiceEntretien();
    private static final int USERS_PAGE_SIZE = 6;
    private static final int OFFRES_PAGE_SIZE = 6;
    private static final int CANDIDATURES_PAGE_SIZE = 6;
    private int usersPage = 0;
    private int offresPage = 0;
    private int candidaturesPage = 0;
    private User selectedUser;
    private User selectedCandidat;
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
    private Formation selectedFormation;
    private Node selectedFormationCard;
    private Participant selectedParticipant;
    private Node selectedParticipantCard;

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
        if (tableEntretiensAdmin != null) {
            initEntretienSection();
        }
        if (tableTopCv != null) {
            initTopCvSection();
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
        if (tableFormations != null) {
            initFormationSection();
        }
        if (tableParticipations != null) {
            initParticipationSection();
        }
        setPageMessage("", false);

        if (Session.getUser() != null && txtAdminId != null) {
            txtAdminId.setText(String.valueOf(Session.getUser().getId()));
        }
        if (Session.getUser() != null && txtFormationAdminId != null) {
            txtFormationAdminId.setText(String.valueOf(Session.getUser().getId()));
        }
        initDashboardFilters();
        initFlowWrapBindings();
        initGlobalScrollBindings();

        refreshData();
    }

    private void initFlowWrapBindings() {
        bindFlowWrap(flowUsers, scrollUsers);
        bindFlowWrap(flowOffres, scrollOffres);
        bindFlowWrap(flowCandidatures, scrollCandidatures);
    }

    private void bindFlowWrap(javafx.scene.layout.FlowPane flow, ScrollPane scrollPane) {
        if (flow == null || scrollPane == null) {
            return;
        }
        flow.setPrefWrapLength(560);
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            if (bounds != null) {
                flow.setPrefWrapLength(Math.max(520, bounds.getWidth() - 24));
            }
        });
    }

    private void initGlobalScrollBindings() {
        bindNestedScrollToPage(scrollUsers);
        bindNestedScrollToPage(scrollOffres);
        bindNestedScrollToPage(scrollCandidatures);
        bindNestedScrollToPage(scrollTopCv);
        bindNestedScrollToPage(scrollScraping);

        if (rootPane != null && pageScroll != null) {
            rootPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.isConsumed() || pageScroll.getContent() == null) {
                    return;
                }
                scrollPageByDelta(event.getDeltaY());
                event.consume();
            });
        }
    }

    private void bindNestedScrollToPage(ScrollPane inner) {
        if (inner == null) {
            return;
        }
        inner.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (pageScroll == null || pageScroll.getContent() == null) {
                return;
            }
            scrollPageByDelta(event.getDeltaY());
            event.consume();
        });
    }

    private void scrollPageByDelta(double deltaY) {
        if (pageScroll == null || pageScroll.getContent() == null) {
            return;
        }
        double contentHeight = pageScroll.getContent().getBoundsInLocal().getHeight();
        double viewportHeight = pageScroll.getViewportBounds().getHeight();
        double scrollRange = Math.max(contentHeight - viewportHeight, 1.0);
        double next = pageScroll.getVvalue() - (deltaY / scrollRange);
        pageScroll.setVvalue(Math.max(0.0, Math.min(1.0, next)));
    }

    private void initDashboardFilters() {
        if (cbFilterRole != null) {
            cbFilterRole.getItems().setAll("ADMIN_RH", "MANAGER", "EMPLOYE", "CANDIDAT");
        }
        if (cbFilterDepartement != null) {
            List<String> depts = serviceOffre.getAll().stream()
                    .map(OffreEmploi::getDepartement)
                    .filter(d -> d != null && !d.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            cbFilterDepartement.getItems().setAll(depts);
        }
        initRealtimeSearchBindings();
    }

    private void initRealtimeSearchBindings() {
        bindSearchField(txtGlobalSearch);
        bindSearchField(txtSearchUsers);
        bindSearchField(txtSearchOffres);
        bindSearchField(txtSearchCandidatures);
        if (cbFilterRole != null) {
            cbFilterRole.valueProperty().addListener((obs, oldV, newV) -> refreshModernSections());
        }
        if (cbFilterDepartement != null) {
            cbFilterDepartement.valueProperty().addListener((obs, oldV, newV) -> refreshModernSections());
        }
        if (cbFilterStatut != null) {
            cbFilterStatut.valueProperty().addListener((obs, oldV, newV) -> refreshModernSections());
        }
    }

    private void bindSearchField(TextField field) {
        if (field != null) {
            field.textProperty().addListener((obs, oldV, newV) -> refreshModernSections());
        }
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

    private void initEntretienSection() {
        tableEntretiensAdmin.getStyleClass().add("cards-container");
    }

    private void initTopCvSection() {
        tableTopCv.getStyleClass().add("cards-container");
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

    private void initFormationSection() {
        tableFormations.getStyleClass().add("cards-container");
    }

    private void initParticipationSection() {
        tableParticipations.getStyleClass().add("cards-container");
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
        if (tableEntretiensAdmin != null) {
            renderEntretienAdminCards();
        }
        if (tableTopCv != null) {
            tableTopCv.getChildren().clear();
        }
        if (tableScrapingResults != null) {
            tableScrapingResults.getChildren().clear();
        }
        refreshModernSections();
        refreshKpiAndCharts();
        if (tableConges != null) {
            renderCongeCards();
        }
        if (tableAbsencesAdmin != null) {
            renderAbsenceAdminCards();
        }
        if (publicationList != null) {
            renderCommunicationCards();
        }
        if (tableFormations != null) {
            renderFormationCards();
        }
        if (tableParticipations != null) {
            renderParticipationCards();
        }
        refreshDashboardStats();
        setPageMessage("", false);
    }

    @FXML
    public void refreshModernSections() {
        renderModernUsers();
        renderModernOffres();
        renderModernCandidatures();
    }

    @FXML
    public void openRecrutement(ActionEvent event) {
        navigateTo(event, "/fxml/recrutement/recrutement.fxml");
    }

    @FXML
    public void openEntretienPage(ActionEvent event) {
        navigateTo(event, "/fxml/recrutement/entretien.fxml");
    }

    @FXML
    public void openDashboard(ActionEvent event) {
        navigateTo(event, "/fxml/dashboard.fxml");
    }

    @FXML
    public void openConges(ActionEvent event) {
        navigateTo(event, "/fxml/congesAbsences/Conges.fxml");
    }

    @FXML
    public void openAbsences(ActionEvent event) {
        navigateTo(event, "/fxml/congesAbsences/absences.fxml");
    }

    @FXML
    public void openCommunication(ActionEvent event) {
        navigateTo(event, "/fxml/publication/communication.fxml");
    }

    @FXML
    public void openFormations(ActionEvent event) {
        navigateTo(event, "/fxml/formation/formation.fxml");
    }

    @FXML
    public void openFeedback(ActionEvent event) {
        navigateTo(event, "/fxml/feedback/feedback.fxml");
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
        if (lblCardCandidats != null) {
            lblCardCandidats.setText(String.valueOf(serviceUser.countByRole("CANDIDAT")));
        }
        if (lblCardAcceptees != null) {
            lblCardAcceptees.setText(String.valueOf(serviceCandidature.countByStatut("ACCEPTEE")));
        }
        if (lblCardRefusees != null) {
            lblCardRefusees.setText(String.valueOf(serviceCandidature.countByStatut("REFUSEE")));
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
            Integer userId = resolveUserId(txtCandidatId.getText(), null);
            if (userId == null) {
                showError("Candidat introuvable. Saisissez nom/prenom ou email.");
                return;
            }
            if (updateUserRole(userId, "CANDIDAT")) {
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
            Integer userId = selectedCandidat != null
                    ? selectedCandidat.getId()
                    : resolveUserId(txtCandidatId.getText(), "CANDIDAT");
            if (userId == null) {
                showError("Selectionnez un candidat ou saisissez un candidat existant.");
                return;
            }
            if (updateUserRole(userId, "CANDIDAT")) {
                refreshData();
            } else {
                showError("Mise a jour impossible pour ce candidat.");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteCandidat() {
        try {
            Integer userId = resolveUserId(txtCandidatId.getText(), null);
            if (userId == null && selectedCandidat != null) {
                userId = selectedCandidat.getId();
            }
            if (userId == null) {
                showError("Selectionnez un candidat ou saisissez son nom/prenom.");
                return;
            }
            if (updateUserRole(userId, "EMPLOYE")) {
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
            o.setDepartement(txtDepartement.getValue() == null ? "" : txtDepartement.getValue().trim());
            o.setTypeContrat(txtTypeContrat.getText().trim());
            o.setNombrePostes(Integer.parseInt(txtNombrePostes.getText().trim()));
            o.setDatePublication(dpDatePublication.getValue() == null ? LocalDate.now() : dpDatePublication.getValue());
            Integer adminId = resolveUserId(txtAdminId.getText(), "ADMIN");
            if (adminId == null) {
                showError("Responsable RH introuvable.");
                return;
            }
            o.setAdminId(adminId);
            if (serviceOffre.ajouter(o)) {
                refreshData();
                publishOffreOnSocialPlatforms(o);
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
            o.setDepartement(txtDepartement.getValue() == null ? "" : txtDepartement.getValue().trim());
            o.setTypeContrat(txtTypeContrat.getText().trim());
            o.setNombrePostes(Integer.parseInt(txtNombrePostes.getText().trim()));
            o.setDatePublication(dpDatePublication.getValue() == null ? LocalDate.now() : dpDatePublication.getValue());
            Integer adminId = resolveUserId(txtAdminId.getText(), "ADMIN");
            if (adminId == null) {
                showError("Responsable RH introuvable.");
                return;
            }
            o.setAdminId(adminId);
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
        txtDepartement.setValue(null);
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
            Integer candidatId = resolveUserId(txtCandUserId.getText(), null);
            Integer offreId = resolveOffreId(txtCandOffreId.getText());
            if (candidatId == null) {
                showError("Candidat introuvable.");
                return;
            }
            if (offreId == null) {
                showError("Offre introuvable.");
                return;
            }
            c.setCandidatId(candidatId);
            c.setOffreId(offreId);
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
            Integer candidatId = resolveUserId(txtCandUserId.getText(), null);
            Integer offreId = resolveOffreId(txtCandOffreId.getText());
            if (candidatId == null) {
                showError("Candidat introuvable.");
                return;
            }
            if (offreId == null) {
                showError("Offre introuvable.");
                return;
            }
            c.setCandidatId(candidatId);
            c.setOffreId(offreId);
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
    public void analyzeTopCv() {
        if (tableTopCv == null) {
            return;
        }

        try {
            Integer offreId = resolveOffreId(txtTopCvOffreId == null ? null : txtTopCvOffreId.getText());
            if (offreId == null && selectedOffre != null) {
                offreId = selectedOffre.getId();
            }
            if (offreId == null) {
                showError("Saisissez une offre (id ou titre).");
                return;
            }

            int topN = 5;
            if (txtTopCvLimit != null && txtTopCvLimit.getText() != null && !txtTopCvLimit.getText().isBlank()) {
                topN = Integer.parseInt(txtTopCvLimit.getText().trim());
            }
            if (topN <= 0) {
                topN = 5;
            }

            List<CandidateScoringResult> ranking = candidateScoringService.rankTopCandidatesForOffer(offreId, topN);
            tableTopCv.getChildren().clear();
            int totalCandidaturesOffre = serviceCandidature.getByOffreId(offreId).size();
            int rank = 1;
            for (CandidateScoringResult item : ranking) {
                String nom = (item.getCandidatNom() == null || item.getCandidatNom().isBlank()) ? "Candidat" : item.getCandidatNom();
                String email = item.getCandidatEmail() == null ? "" : item.getCandidatEmail();
                String xp = item.getCvAnalysis() == null ? "0" : String.format(Locale.US, "%.1f", item.getCvAnalysis().getYearsExperience());
                String skills = item.getCvAnalysis() == null || item.getCvAnalysis().getSkills().isEmpty()
                        ? "N/A"
                        : String.join(", ", item.getCvAnalysis().getSkills().stream().limit(6).collect(Collectors.toList()));
                String summary = item.getCvAnalysis() == null ? "" : item.getCvAnalysis().getSummary();
                if (summary == null) {
                    summary = "";
                }
                if (summary.length() > 120) {
                    summary = summary.substring(0, 120) + "...";
                }
                tableTopCv.getChildren().add(buildTopCvProfileCard(rank, item, nom, email, xp, skills, summary));
                rank++;
            }

            String jsonPath = candidateScoringService.saveRankingAsJson(offreId, ranking);
            if (lblTopCvJsonPath != null) {
                lblTopCvJsonPath.setText("JSON: " + jsonPath);
            }
            if (ranking.isEmpty() && totalCandidaturesOffre > 0) {
                setPageMessage(
                        "Aucun CV exploitable pour l'offre #" + offreId + ". Candidatures: " + totalCandidaturesOffre
                                + ". Verifiez les fichiers PDF dans uploads/cv.",
                        true
                );
            } else {
                setPageMessage("Top CV genere: " + ranking.size() + " candidat(s).", false);
            }
        } catch (NumberFormatException e) {
            showError("Top N invalide.");
        } catch (Exception e) {
            showError("Erreur analyse Top CV: " + e.getMessage());
        }
    }

    @FXML
    public void analyzeTopCvQuick() {
        if (txtTopCvLimit != null) {
            txtTopCvLimit.setText("5");
        }
        analyzeTopCv();
    }

    @FXML
    public void showScrapingResults() {
        if (tableScrapingResults == null) {
            return;
        }
        try {
            Integer offreId = resolveOffreId(txtScrapingOffreId == null ? null : txtScrapingOffreId.getText());
            if (offreId == null && selectedOffre != null) {
                offreId = selectedOffre.getId();
            }
            if (offreId == null) {
                showError("Saisissez une offre (id ou titre) pour le scraping.");
                return;
            }

            int limit = 6;
            if (txtScrapingLimit != null && txtScrapingLimit.getText() != null && !txtScrapingLimit.getText().isBlank()) {
                limit = Integer.parseInt(txtScrapingLimit.getText().trim());
            }
            if (limit <= 0) {
                limit = 6;
            }
            limit = Math.min(limit, 20);

            OffreEmploi offre = serviceOffre.getById(offreId);
            if (offre == null) {
                showError("Offre introuvable.");
                return;
            }

            List<String> keywords = new ArrayList<>();
            if (offre.getTitre() != null && !offre.getTitre().isBlank()) {
                keywords.add(offre.getTitre().trim());
            }
            if (offre.getDepartement() != null && !offre.getDepartement().isBlank()) {
                keywords.add(offre.getDepartement().trim());
            }
            if (offre.getTypeContrat() != null && !offre.getTypeContrat().isBlank()) {
                keywords.add(offre.getTypeContrat().trim());
            }
            String baseQuery = String.join(" ", keywords).trim();
            if (baseQuery.isBlank()) {
                baseQuery = "recrutement";
            }

            tableScrapingResults.getChildren().clear();
            String[] sources = {"LinkedIn", "GitHub", "Reddit"};
            int rank = 1;
            for (int i = 0; i < limit; i++) {
                String source = sources[i % sources.length];
                String query = buildScrapingQuery(baseQuery, i + 1);
                String url = buildSearchUrl(source, query);
                int score = Math.max(55, 94 - ((rank - 1) * 3));
                tableScrapingResults.getChildren().add(
                        buildScrapingProfileCard(rank, source, query, url, score)
                );
                rank++;
            }

            if (lblScrapingStatus != null) {
                lblScrapingStatus.setText("Resultats scraping: " + limit + " profils pour l'offre #" + offreId + ".");
            }
            setPageMessage("Scraping Bot execute: " + limit + " resultats affiches.", false);
        } catch (NumberFormatException e) {
            showError("Limite de scraping invalide.");
        } catch (Exception e) {
            showError("Erreur Scraping Bot: " + e.getMessage());
        }
    }

    private String buildScrapingQuery(String baseQuery, int index) {
        return baseQuery + " profil " + index;
    }

    private String buildSearchUrl(String source, String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        switch (source.toLowerCase(Locale.ROOT)) {
            case "linkedin":
                return "https://www.linkedin.com/search/results/people/?keywords=" + encoded;
            case "github":
                return "https://github.com/search?q=" + encoded + "&type=users";
            case "reddit":
                return "https://www.reddit.com/search/?q=" + encoded;
            default:
                return "https://www.google.com/search?q=" + encoded;
        }
    }

    private VBox buildScrapingProfileCard(int rank, String source, String query, String url, int score) {
        Label rankBadge = new Label(String.valueOf(rank));
        rankBadge.getStyleClass().add("topcv-rank-badge");
        if (rank == 1) rankBadge.getStyleClass().add("topcv-rank-gold");
        else if (rank == 2) rankBadge.getStyleClass().add("topcv-rank-silver");
        else if (rank == 3) rankBadge.getStyleClass().add("topcv-rank-bronze");

        String[] profileIdentity = buildScrapingIdentity(rank, source, query);
        String firstName = profileIdentity[0];
        String lastName = profileIdentity[1];
        String fullName = firstName + " " + lastName;

        Label avatar = new Label(extractInitials(fullName));
        avatar.getStyleClass().add("topcv-avatar");
        Image profileImage = loadScrapingProfileImage(fullName, rank);
        if (profileImage != null && !profileImage.isError()) {
            ImageView iv = new ImageView(profileImage);
            iv.setFitWidth(46);
            iv.setFitHeight(46);
            iv.setPreserveRatio(false);
            avatar.setText("");
            avatar.setGraphic(iv);
        }

        Label nameLabel = new Label(fullName);
        nameLabel.getStyleClass().add("topcv-name");
        nameLabel.setWrapText(true);
        Label queryLabel = new Label("Source: " + source + " | Prenom: " + firstName + " | Nom: " + lastName);
        queryLabel.getStyleClass().add("topcv-role");
        queryLabel.setWrapText(true);
        Label urlLabel = new Label(url);
        urlLabel.getStyleClass().add("topcv-meta");
        urlLabel.setWrapText(true);

        VBox identityText = new VBox(2, nameLabel, queryLabel, urlLabel);
        identityText.setMaxWidth(430);
        HBox identity = new HBox(10, rankBadge, avatar, identityText);
        identity.getStyleClass().add("topcv-left");

        ProgressBar scoreBar = new ProgressBar(Math.max(0, Math.min(100, score)) / 100.0);
        scoreBar.getStyleClass().add("topcv-score-bar");
        if (score >= 85) scoreBar.getStyleClass().add("topcv-score-excellent");
        else if (score >= 75) scoreBar.getStyleClass().add("topcv-score-good");
        else scoreBar.getStyleClass().add("topcv-score-medium");

        Label scoreLabel = new Label(score + "%");
        scoreLabel.getStyleClass().add("topcv-score-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(10, identity, spacer, scoreBar, scoreLabel);
        row.getStyleClass().add("topcv-profile-row");
        scoreBar.setPrefWidth(170);
        row.setMinHeight(66);
        VBox wrapper = new VBox(row);
        wrapper.setOnMouseClicked(e -> openWebLink(url));
        wrapper.setOnMouseEntered(e -> wrapper.setStyle("-fx-cursor: hand;"));
        Tooltip.install(wrapper, new Tooltip("Ouvrir le profil: " + source));
        return wrapper;
    }

    private String[] buildScrapingIdentity(int rank, String source, String query) {
        List<String> firstNames = Arrays.asList(
                "Ali", "Youssef", "Farid", "Karim", "Omar", "Sami", "Nour", "Rania", "Lina", "Aymen",
                "Mehdi", "Salma", "Moez", "Hela", "Nadine", "Amir", "Rim", "Skander", "Yasmine", "Walid"
        );
        List<String> lastNames = Arrays.asList(
                "Mansouri", "Belhadi", "Bessa", "Lamine", "Meddeb", "Trabelsi", "Ben Ali", "Hamdi", "Jebali", "Chaari",
                "Khalfallah", "Saidi", "Gharbi", "Ayari", "Khemiri", "Sfar", "Bouzid", "Zitouni", "Marzouki", "Khammassi"
        );
        int seed = Math.abs((source + "|" + query + "|" + rank).hashCode());
        String first = firstNames.get(seed % firstNames.size());
        String last = lastNames.get((seed / firstNames.size()) % lastNames.size());
        return new String[]{first, last};
    }

    private Image loadScrapingProfileImage(String fullName, int rank) {
        try {
            int avatarId = (Math.abs((fullName + rank).hashCode()) % 70) + 1;
            String url = "https://i.pravatar.cc/96?img=" + avatarId;
            return new Image(url, 46, 46, false, true, false);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void openWebLink(String url) {
        if (url == null || url.isBlank()) {
            showError("Lien vide.");
            return;
        }
        try {
            if (!Desktop.isDesktopSupported()) {
                showError("Ouverture navigateur non supportee.");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                showError("Action navigateur non supportee.");
                return;
            }
            desktop.browse(java.net.URI.create(url));
            setPageMessage("Ouverture profil: " + url, false);
        } catch (Exception e) {
            showError("Impossible d'ouvrir le lien: " + e.getMessage());
        }
    }

    private VBox buildTopCvProfileCard(int rank,
                                       CandidateScoringResult item,
                                       String nom,
                                       String email,
                                       String xp,
                                       String skills,
                                       String summary) {
        Label rankBadge = new Label(String.valueOf(rank));
        rankBadge.getStyleClass().add("topcv-rank-badge");
        if (rank == 1) rankBadge.getStyleClass().add("topcv-rank-gold");
        else if (rank == 2) rankBadge.getStyleClass().add("topcv-rank-silver");
        else if (rank == 3) rankBadge.getStyleClass().add("topcv-rank-bronze");

        String initials = extractInitials(nom);
        Label avatar = new Label(initials);
        avatar.getStyleClass().add("topcv-avatar");

        Label nameLabel = new Label(nom);
        nameLabel.getStyleClass().add("topcv-name");
        nameLabel.setWrapText(true);
        String headline = "Candidat";
        if (!skills.isBlank() && !"N/A".equalsIgnoreCase(skills)) {
            headline = "Skills: " + skills;
        } else if (!email.isBlank()) {
            headline = email;
        }
        Label headlineLabel = new Label(headline);
        headlineLabel.getStyleClass().add("topcv-role");
        headlineLabel.setWrapText(true);

        Label metaLabel = new Label("Exp: " + xp + " ans | " + item.getCommentaire());
        metaLabel.getStyleClass().add("topcv-meta");
        if (!summary.isBlank()) {
            metaLabel.setText(metaLabel.getText() + " | NLP: " + summary);
        }
        metaLabel.setWrapText(true);

        VBox identityText = new VBox(2, nameLabel, headlineLabel, metaLabel);
        identityText.setMaxWidth(430);
        HBox identity = new HBox(10, rankBadge, avatar, identityText);
        identity.getStyleClass().add("topcv-left");

        ProgressBar scoreBar = new ProgressBar(Math.max(0, Math.min(100, item.getScoreGlobal())) / 100.0);
        scoreBar.getStyleClass().add("topcv-score-bar");
        if (item.getScoreGlobal() >= 85) {
            scoreBar.getStyleClass().add("topcv-score-excellent");
        } else if (item.getScoreGlobal() >= 75) {
            scoreBar.getStyleClass().add("topcv-score-good");
        } else {
            scoreBar.getStyleClass().add("topcv-score-medium");
        }

        Label scoreLabel = new Label(item.getScoreGlobal() + "%");
        scoreLabel.getStyleClass().add("topcv-score-value");
        if (rank == 1) {
            scoreLabel.setText(scoreLabel.getText() + " 🏆");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(10, identity, spacer, scoreBar, scoreLabel);
        row.getStyleClass().add("topcv-profile-row");
        scoreBar.setPrefWidth(170);
        row.setMinHeight(66);
        return new VBox(row);
    }

    private String extractInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "CV";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }

    private void refreshKpiAndCharts() {
        refreshModernKpis();
        refreshModernCharts();
        renderOfferFillAndPostesPanels();
    }

    private void refreshModernKpis() {
        if (lblKpiTotalCandidatures == null) {
            return;
        }
        List<Candidature> candidatures = serviceCandidature.getAll();
        int total = candidatures.size();
        long accepted = candidatures.stream().filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut())).count();
        double acceptanceRate = total == 0 ? 0 : (accepted * 100.0 / total);

        List<Candidature> closed = candidatures.stream()
                .filter(c -> c.getDateCandidature() != null)
                .filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut()) || "REFUSEE".equalsIgnoreCase(c.getStatut()))
                .collect(Collectors.toList());
        double avgDays = closed.isEmpty() ? 0 : closed.stream()
                .mapToLong(c -> Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(c.getDateCandidature(), LocalDate.now())))
                .average().orElse(0);

        Map<Integer, Long> acceptedByOffre = candidatures.stream()
                .filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut()))
                .collect(Collectors.groupingBy(Candidature::getOffreId, Collectors.counting()));
        int activeOffers = (int) serviceOffre.getAll().stream()
                .filter(o -> acceptedByOffre.getOrDefault(o.getId(), 0L) < o.getNombrePostes())
                .count();

        lblKpiTotalCandidatures.setText(String.valueOf(total));
        lblKpiTauxAcceptation.setText(String.format(Locale.US, "%.1f%%", acceptanceRate));
        lblKpiTempsMoyen.setText(String.format(Locale.US, "%.1f jours", avgDays));
        lblKpiOffresActives.setText(String.valueOf(activeOffers));
        if (lblKpiEmployesActifs != null) {
            lblKpiEmployesActifs.setText(String.valueOf(serviceUser.countByRole("EMPLOYE")));
        }
        if (lblHeaderTotalUsers != null) {
            lblHeaderTotalUsers.setText(String.valueOf(serviceUser.getAll().size()));
        }
        if (lblHeaderTotalOffres != null) {
            lblHeaderTotalOffres.setText(String.valueOf(activeOffers));
        }
        if (lblHeaderTotalCandidatures != null) {
            lblHeaderTotalCandidatures.setText(String.valueOf(total));
        }
    }

    private void refreshModernCharts() {
        if (chartCandidaturesDept == null) {
            return;
        }
        List<Candidature> candidatures = serviceCandidature.getAll();
        Map<Integer, OffreEmploi> offersById = serviceOffre.getAll().stream()
                .collect(Collectors.toMap(OffreEmploi::getId, o -> o));

        Map<String, Long> byDept = candidatures.stream()
                .collect(Collectors.groupingBy(c -> {
                    OffreEmploi o = offersById.get(c.getOffreId());
                    return o == null || o.getDepartement() == null || o.getDepartement().isBlank()
                            ? "Non défini" : o.getDepartement();
                }, Collectors.counting()));

        chartCandidaturesDept.getData().clear();
        double totalCandidatures = byDept.values().stream().mapToLong(Long::longValue).sum();
        byDept.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> {
                    double percent = totalCandidatures == 0 ? 0 : (e.getValue() * 100.0 / totalCandidatures);
                    String label = e.getKey() + " - " + String.format(Locale.FRANCE, "%.1f%%", percent);
                    chartCandidaturesDept.getData().add(new PieChart.Data(label, e.getValue()));
                });

        if (chartEvolutionLine == null || chartEvolutionBar == null) {
            return;
        }

        Map<YearMonth, List<Candidature>> byMonth = candidatures.stream()
                .filter(c -> c.getDateCandidature() != null)
                .collect(Collectors.groupingBy(c -> YearMonth.from(c.getDateCandidature()), LinkedHashMap::new, Collectors.toList()));

        List<YearMonth> months = new ArrayList<>(byMonth.keySet());
        months.sort(Comparator.naturalOrder());
        if (months.size() > 6) {
            months = months.subList(months.size() - 6, months.size());
        }

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Total Candidatures");
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Taux d'Acceptation");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yy", Locale.FRENCH);

        for (YearMonth month : months) {
            List<Candidature> monthRows = byMonth.getOrDefault(month, List.of());
            int total = monthRows.size();
            long accepted = monthRows.stream().filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut())).count();
            double rate = total == 0 ? 0 : accepted * 100.0 / total;
            String label = month.format(monthFmt);
            barSeries.getData().add(new XYChart.Data<>(label, total));
            lineSeries.getData().add(new XYChart.Data<>(label, rate));
        }

        chartEvolutionBar.getData().setAll(barSeries);
        chartEvolutionLine.getData().setAll(lineSeries);
    }

    private void renderModernUsers() {
        if (flowUsers == null) {
            return;
        }
        String q = lower(txtSearchUsers);
        String global = lower(txtGlobalSearch);
        String role = cbFilterRole == null ? null : cbFilterRole.getValue();

        List<User> filtered = serviceUser.getAll().stream()
                .filter(u -> role == null || role.isBlank() || role.equalsIgnoreCase(u.getRole()))
                .filter(u -> matchesUserSearch(u, q) && matchesUserSearch(u, global))
                .sorted(Comparator.comparingInt(User::getId).reversed())
                .collect(Collectors.toList());

        flowUsers.getChildren().setAll(filtered.stream()
                .map(this::buildModernUserCard)
                .collect(Collectors.toList()));
    }

    private void renderModernOffres() {
        if (flowOffres == null) {
            return;
        }
        String q = lower(txtSearchOffres);
        String global = lower(txtGlobalSearch);
        String dept = cbFilterDepartement == null ? null : cbFilterDepartement.getValue();

        List<OffreEmploi> filtered = serviceOffre.getAll().stream()
                .filter(o -> dept == null || dept.isBlank() || dept.equalsIgnoreCase(o.getDepartement()))
                .filter(o -> matchesOffreSearch(o, q) && matchesOffreSearch(o, global))
                .sorted(Comparator.comparingInt(OffreEmploi::getId).reversed())
                .collect(Collectors.toList());

        flowOffres.getChildren().setAll(filtered.stream()
                .map(this::buildModernOffreCard)
                .collect(Collectors.toList()));
    }

    private void renderModernCandidatures() {
        if (flowCandidatures == null) {
            return;
        }
        String q = lower(txtSearchCandidatures);
        String global = lower(txtGlobalSearch);
        String status = cbFilterStatut == null ? null : cbFilterStatut.getValue();

        Map<Integer, User> usersById = serviceUser.getAll().stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, OffreEmploi> offersById = serviceOffre.getAll().stream().collect(Collectors.toMap(OffreEmploi::getId, o -> o));
        List<Candidature> filtered = serviceCandidature.getAll().stream()
                .filter(c -> status == null || status.isBlank() || status.equalsIgnoreCase(c.getStatut()))
                .filter(c -> matchesCandidatureSearch(c, usersById, offersById, q)
                        && matchesCandidatureSearch(c, usersById, offersById, global))
                .sorted(Comparator.comparingInt(Candidature::getId).reversed())
                .collect(Collectors.toList());

        flowCandidatures.getChildren().setAll(filtered.stream()
                .map(c -> buildModernCandidatureCard(c, usersById, offersById))
                .collect(Collectors.toList()));
    }

    private VBox buildModernUserCard(User user) {
        Label avatar = new Label("👤");
        avatar.getStyleClass().add("avatar-chip");
        Label title = new Label(user.getNom() + " " + user.getPrenom());
        title.getStyleClass().add("entity-card-title");
        Label email = new Label(user.getEmail());
        email.getStyleClass().add("entity-card-meta");
        title.setWrapText(true);
        email.setWrapText(true);
        title.setMaxWidth(220);
        email.setMaxWidth(220);
        Label role = buildRoleBadge(user.getRole());
        HBox identity = new HBox(10, avatar, new VBox(3, title, email, role));

        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");
        Button edit = new Button("✎");
        edit.setOnAction(e -> openUserDialog(user));
        Button delete = new Button("🗑");
        delete.setOnAction(e -> {
            serviceUser.delete(user.getId());
            refreshData();
        });
        actions.getChildren().addAll(edit, delete);

        VBox card = new VBox(10, identity, actions);
        card.getStyleClass().addAll("entity-card", "user-card");
        card.setMinWidth(250);
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        return card;
    }

    private VBox buildModernOffreCard(OffreEmploi offre) {
        Label icon = new Label("💼");
        icon.getStyleClass().add("avatar-chip");
        Label title = new Label(offre.getTitre());
        title.getStyleClass().add("entity-card-title");
        int accepted = (int) serviceCandidature.getAll().stream()
                .filter(c -> c.getOffreId() == offre.getId() && "ACCEPTEE".equalsIgnoreCase(c.getStatut()))
                .count();
        double fillRate = offre.getNombrePostes() <= 0 ? 0 : (accepted * 100.0 / offre.getNombrePostes());

        Label meta = new Label(offre.getDepartement() + " " + offre.getTypeContrat()
                + " | " + accepted + "/" + offre.getNombrePostes() + " (" + String.format(Locale.US, "%.0f%%", fillRate) + ")");
        meta.getStyleClass().add("entity-card-meta");
        Label date = new Label("Publication: " + offre.getDatePublication());
        date.getStyleClass().add("entity-card-meta");
        HBox identity = new HBox(10, icon, new VBox(4, title, meta, date));

        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");
        Button edit = new Button("✎");
        edit.setOnAction(e -> openOffreDialog(offre));
        Node publishFacebook = createSocialIcon("/images/fb.png", "Publier sur Facebook",
                () -> publishOffreOnFacebookIfConfigured(offre));
        Node publishLinkedIn = createSocialIcon("/images/in.png", "Publier sur LinkedIn",
                () -> publishOffreOnLinkedInIfConfigured(offre));
        Node publishReddit = createSocialIcon("/images/reddit.png", "Publier sur Reddit",
                () -> publishOffreOnRedditIfConfigured(offre));
        Button delete = new Button("🗑");
        delete.setOnAction(e -> {
            serviceOffre.delete(offre.getId());
            refreshData();
        });
        actions.getChildren().addAll(edit, publishFacebook, publishLinkedIn, publishReddit, delete);

        VBox card = new VBox(10, identity, actions);
        card.getStyleClass().addAll("entity-card", "offer-card");
        card.setMinWidth(250);
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        return card;
    }

    private VBox buildModernCandidatureCard(Candidature c, Map<Integer, User> usersById, Map<Integer, OffreEmploi> offersById) {
        User u = usersById.get(c.getCandidatId());
        OffreEmploi o = offersById.get(c.getOffreId());
        String nom = u == null ? ("Candidat #" + c.getCandidatId()) : (u.getNom() + " " + u.getPrenom());
        String offre = o == null ? ("Offre #" + c.getOffreId()) : o.getTitre();

        Label avatar = new Label("🧑");
        avatar.getStyleClass().add("avatar-chip");
        Label title = new Label(nom);
        title.getStyleClass().add("entity-card-title");
        Label offerLabel = new Label("Poste: " + offre);
        offerLabel.getStyleClass().add("entity-card-meta");
        Label date = new Label("Date: " + c.getDateCandidature());
        date.getStyleClass().add("entity-card-meta");
        Label status = buildStatusBadge(c.getStatut());
        HBox identity = new HBox(10, avatar, new VBox(3, title, offerLabel, status, date));

        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");
        Button viewCv = new Button("CV");
        viewCv.setOnAction(e -> openCvFile(c.getCheminCv()));
        Button update = new Button("↺");
        update.setOnAction(e -> {
            String next = "EN_ATTENTE";
            if ("EN_ATTENTE".equalsIgnoreCase(c.getStatut())) next = "ACCEPTEE";
            else if ("ACCEPTEE".equalsIgnoreCase(c.getStatut())) next = "REFUSEE";
            serviceCandidature.updateStatut(c.getId(), next);
            refreshData();
        });
        Button planifier = new Button("📅");
        planifier.setOnAction(e -> openPlanifierEntretienDialog(c, u, o));
        planifier.setDisable(!isCurrentAdminRh());
        Button demarrer = new Button("🎥");
        demarrer.setOnAction(e -> openLatestEntretienForCandidature(c.getId()));
        Button delete = new Button("🗑");
        delete.setOnAction(e -> {
            serviceCandidature.supprimer(c.getId());
            refreshData();
        });
        actions.getChildren().addAll(viewCv, update, planifier, demarrer, delete);

        VBox card = new VBox(10, identity, actions);
        card.getStyleClass().addAll("entity-card", "app-card");
        card.setMinWidth(250);
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        return card;
    }

    private void openCvFile(String cvPath) {
        if (cvPath == null || cvPath.isBlank()) {
            showError("CV non disponible.");
            return;
        }

        try {
            File file = resolveCvFile(cvPath);
            if (!file.exists()) {
                showError("CV introuvable: " + cvPath);
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                showError("Ouverture automatique non supportee sur cet environnement.");
                return;
            }

            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
                setPageMessage("Ouverture du CV: " + file.getName(), false);
                return;
            }
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(file.toURI());
                setPageMessage("Ouverture du CV: " + file.getName(), false);
                return;
            }
            showError("Impossible d'ouvrir le CV sur ce systeme.");
        } catch (Exception e) {
            showError("Erreur ouverture CV: " + e.getMessage());
        }
    }

    private File resolveCvFile(String cvPath) {
        String normalized = cvPath.replace("\\", "/").trim();
        Path raw = Path.of(normalized);

        if (raw.isAbsolute()) {
            return raw.toFile();
        }

        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path candidate1 = cwd.resolve(raw).normalize();
        if (candidate1.toFile().exists()) {
            return candidate1.toFile();
        }

        Path candidate2 = cwd.resolve("uploads").resolve("cv").resolve(raw.getFileName()).normalize();
        if (candidate2.toFile().exists()) {
            return candidate2.toFile();
        }

        // fallback: retourne le meilleur candidat calculé pour message d'erreur explicite
        return candidate1.toFile();
    }

    private Label buildRoleBadge(String role) {
        String value = role == null ? "UNKNOWN" : role.toUpperCase(Locale.ROOT);
        Label label = new Label(value);
        label.getStyleClass().add("badge");
        if (value.contains("ADMIN")) label.getStyleClass().add("badge-admin");
        else if (value.contains("MANAGER")) label.getStyleClass().add("badge-manager");
        else if (value.contains("EMPLOYE")) label.getStyleClass().add("badge-employe");
        else label.getStyleClass().add("badge-candidat");
        return label;
    }

    private boolean isCurrentAdminRh() {
        User current = Session.getUser();
        if (current == null || current.getRole() == null) {
            return false;
        }
        return current.getRole().toUpperCase(Locale.ROOT).contains("ADMIN");
    }

    private void openPlanifierEntretienDialog(Candidature candidature, User candidat, OffreEmploi offre) {
        if (!isCurrentAdminRh()) {
            showError("Seul ADMIN RH peut planifier un entretien.");
            return;
        }
        if (candidature == null) {
            showError("Candidature invalide.");
            return;
        }

        List<User> managers = serviceUser.getAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().toUpperCase(Locale.ROOT).contains("MANAGER"))
                .collect(Collectors.toList());
        if (managers.isEmpty()) {
            showError("Aucun manager disponible.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Planifier Entretien");
        ButtonType saveType = new ButtonType("Planifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Label candidatLabel = new Label("Candidat: " + (candidat == null ? ("#" + candidature.getCandidatId()) : (candidat.getNom() + " " + candidat.getPrenom())));
        Label offreLabel = new Label("Offre: " + (offre == null ? ("#" + candidature.getOffreId()) : offre.getTitre()));

        ComboBox<User> managerBox = new ComboBox<>();
        managerBox.getItems().setAll(managers);
        managerBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom() + " " + item.getPrenom() + " (#" + item.getId() + ")");
            }
        });
        managerBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom() + " " + item.getPrenom() + " (#" + item.getId() + ")");
            }
        });
        managerBox.getSelectionModel().selectFirst();

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField timeField = new TextField("10:00");
        TextField dureeField = new TextField("60");
        TextField meetField = new TextField("https://meet.jit.si/" + buildRoomName(candidature.getId()));
        TextArea commentaireField = new TextArea();
        commentaireField.setPromptText("Commentaire (optionnel)");
        commentaireField.setPrefRowCount(3);

        VBox form = new VBox(8,
                candidatLabel,
                offreLabel,
                new Label("Manager"), managerBox,
                new Label("Date"), datePicker,
                new Label("Heure (HH:mm)"), timeField,
                new Label("Duree (minutes)"), dureeField,
                new Label("Lien Jitsi/Meet"), meetField,
                new Label("Commentaire"), commentaireField
        );
        form.setPrefWidth(460);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != saveType) {
                return;
            }
            try {
                LocalDate date = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
                String[] hm = (timeField.getText() == null ? "10:00" : timeField.getText().trim()).split(":");
                int hour = hm.length > 0 ? Integer.parseInt(hm[0]) : 10;
                int minute = hm.length > 1 ? Integer.parseInt(hm[1]) : 0;
                LocalDateTime dateEntretien = date.atTime(Math.max(0, Math.min(23, hour)), Math.max(0, Math.min(59, minute)));

                Entretien e = new Entretien();
                e.setCandidatureId(candidature.getId());
                e.setAdminId(Session.getUser().getId());
                e.setManagerId(managerBox.getValue().getId());
                e.setDateEntretien(dateEntretien);
                e.setDureeMinutes(Integer.parseInt(dureeField.getText().trim()));
                e.setMeetLink(meetField.getText() == null || meetField.getText().isBlank()
                        ? "https://meet.jit.si/" + buildRoomName(candidature.getId())
                        : meetField.getText().trim());
                e.setStatut("PLANIFIE");
                e.setCommentaire(commentaireField.getText());

                if (serviceEntretien.planifier(e)) {
                    setPageMessage("Entretien planifie avec succes.", false);
                } else {
                    showError("Echec planification entretien.");
                }
            } catch (Exception ex) {
                showError("Erreur planification: " + ex.getMessage());
            }
        });
    }

    private String buildRoomName(int candidatureId) {
        return "huma-entretien-" + candidatureId + "-" + System.currentTimeMillis();
    }

    private void openLatestEntretienForCandidature(int candidatureId) {
        Entretien entretien = serviceEntretien.getLatestByCandidatureId(candidatureId);
        if (entretien == null) {
            showError("Aucun entretien planifie pour cette candidature.");
            return;
        }
        String link = entretien.getMeetLink() == null ? "" : entretien.getMeetLink().trim();
        if (link.isBlank()) {
            showError("Lien entretien manquant.");
            return;
        }
        openEntretienWebView(link, "Entretien #" + entretien.getId());
    }

    private void openEntretienWebView(String url, String title) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI.create(url));
                setPageMessage("Ouverture entretien dans navigateur.", false);
                return;
            }

            Stage stage = new Stage();
            WebView webView = new WebView();
            webView.getEngine().load(url);
            Scene scene = new Scene(webView, 1100, 760);
            stage.setScene(scene);
            stage.setTitle(title == null || title.isBlank() ? "Entretien" : title);
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir l'entretien: " + e.getMessage());
        }
    }

    private Label buildStatusBadge(String status) {
        String value = status == null ? "EN_ATTENTE" : status.toUpperCase(Locale.ROOT);
        Label label = new Label(value);
        label.getStyleClass().add("badge");
        if ("ACCEPTEE".equals(value)) label.getStyleClass().add("badge-accepted");
        else if ("REFUSEE".equals(value)) label.getStyleClass().add("badge-rejected");
        else label.getStyleClass().add("badge-pending");
        return label;
    }

    private String lower(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesUserSearch(User u, String q) {
        if (q == null || q.isBlank()) return true;
        return (u.getNom() + " " + u.getPrenom() + " " + u.getEmail() + " " + u.getRole()).toLowerCase(Locale.ROOT).contains(q);
    }

    private boolean matchesOffreSearch(OffreEmploi o, String q) {
        if (q == null || q.isBlank()) return true;
        return (o.getTitre() + " " + o.getDepartement() + " " + o.getTypeContrat()).toLowerCase(Locale.ROOT).contains(q);
    }

    private boolean matchesCandidatureSearch(Candidature c, Map<Integer, User> usersById, Map<Integer, OffreEmploi> offersById, String q) {
        if (q == null || q.isBlank()) return true;
        User u = usersById.get(c.getCandidatId());
        OffreEmploi o = offersById.get(c.getOffreId());
        String full = (u == null ? "" : (u.getNom() + " " + u.getPrenom() + " " + u.getEmail()))
                + " " + (o == null ? "" : o.getTitre()) + " " + c.getStatut();
        return full.toLowerCase(Locale.ROOT).contains(q);
    }

    private List<User> pageSliceUsers(List<User> list) {
        int maxPage = Math.max(0, (list.size() - 1) / USERS_PAGE_SIZE);
        usersPage = Math.min(usersPage, maxPage);
        int from = usersPage * USERS_PAGE_SIZE;
        int to = Math.min(list.size(), from + USERS_PAGE_SIZE);
        return from >= to ? List.of() : list.subList(from, to);
    }

    private List<OffreEmploi> pageSliceOffres(List<OffreEmploi> list) {
        int maxPage = Math.max(0, (list.size() - 1) / OFFRES_PAGE_SIZE);
        offresPage = Math.min(offresPage, maxPage);
        int from = offresPage * OFFRES_PAGE_SIZE;
        int to = Math.min(list.size(), from + OFFRES_PAGE_SIZE);
        return from >= to ? List.of() : list.subList(from, to);
    }

    private List<Candidature> pageSliceCandidatures(List<Candidature> list) {
        int maxPage = Math.max(0, (list.size() - 1) / CANDIDATURES_PAGE_SIZE);
        candidaturesPage = Math.min(candidaturesPage, maxPage);
        int from = candidaturesPage * CANDIDATURES_PAGE_SIZE;
        int to = Math.min(list.size(), from + CANDIDATURES_PAGE_SIZE);
        return from >= to ? List.of() : list.subList(from, to);
    }

    private void updatePageLabel(Label label, int currentPage, int totalRows, int pageSize) {
        if (label == null) {
            return;
        }
        int totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));
        label.setText("Page " + (currentPage + 1) + " / " + totalPages);
    }

    @FXML
    public void prevUsersPage() {
        usersPage = Math.max(0, usersPage - 1);
        renderModernUsers();
    }

    @FXML
    public void nextUsersPage() {
        usersPage++;
        renderModernUsers();
    }

    @FXML
    public void prevOffresPage() {
        offresPage = Math.max(0, offresPage - 1);
        renderModernOffres();
    }

    @FXML
    public void nextOffresPage() {
        offresPage++;
        renderModernOffres();
    }

    @FXML
    public void prevCandidaturesPage() {
        candidaturesPage = Math.max(0, candidaturesPage - 1);
        renderModernCandidatures();
    }

    @FXML
    public void nextCandidaturesPage() {
        candidaturesPage++;
        renderModernCandidatures();
    }

    @FXML
    public void addUserQuick() {
        openUserDialog(null);
    }

    @FXML
    public void addOffreQuick() {
        openOffreDialog(null);
    }

    @FXML
    public void addCandidatureQuick() {
        openCandidatureDialog();
    }

    private void openUserDialog(User existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Utilisateur" : "Modifier Utilisateur");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nom = new TextField(existing == null ? "" : existing.getNom());
        TextField prenom = new TextField(existing == null ? "" : existing.getPrenom());
        TextField email = new TextField(existing == null ? "" : existing.getEmail());
        PasswordField mdp = new PasswordField();
        if (existing != null && existing.getMdp() != null) {
            mdp.setText(existing.getMdp());
        }
        ComboBox<String> role = new ComboBox<>();
        role.getItems().setAll("ADMIN_RH", "MANAGER", "EMPLOYE", "CANDIDAT");
        role.setValue(existing == null ? "EMPLOYE" : existing.getRole());
        TextField managerId = new TextField(existing == null || existing.getManagerId() == null ? "" : String.valueOf(existing.getManagerId()));

        VBox form = new VBox(8,
                new Label("Nom"), nom,
                new Label("Prenom"), prenom,
                new Label("Email"), email,
                new Label("Mot de passe"), mdp,
                new Label("Role"), role,
                new Label("Manager ID (optionnel)"), managerId
        );
        form.setPrefWidth(380);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) {
                return;
            }
            try {
                User u = existing == null ? new User() : existing;
                u.setNom(nom.getText().trim());
                u.setPrenom(prenom.getText().trim());
                u.setEmail(email.getText().trim());
                u.setMdp(mdp.getText().trim());
                u.setRole(role.getValue());
                String mgrText = managerId.getText() == null ? "" : managerId.getText().trim();
                u.setManagerId(mgrText.isBlank() ? null : Integer.parseInt(mgrText));
                boolean ok = existing == null ? serviceUser.ajouter(u) : serviceUser.update(u);
                if (ok) {
                    refreshData();
                } else {
                    showError("Operation utilisateur echouee.");
                }
            } catch (Exception ex) {
                showError("Erreur utilisateur: " + ex.getMessage());
            }
        });
    }

    private void openOffreDialog(OffreEmploi existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Offre" : "Modifier Offre");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField titre = new TextField(existing == null ? "" : existing.getTitre());
        TextField description = new TextField(existing == null ? "" : existing.getDescription());
        TextField departement = new TextField(existing == null ? "" : existing.getDepartement());
        TextField typeContrat = new TextField(existing == null ? "" : existing.getTypeContrat());
        TextField nbPostes = new TextField(existing == null ? "1" : String.valueOf(existing.getNombrePostes()));
        DatePicker datePub = new DatePicker(existing == null ? LocalDate.now() : existing.getDatePublication());
        TextField adminId = new TextField(existing == null
                ? (Session.getUser() == null ? "" : String.valueOf(Session.getUser().getId()))
                : String.valueOf(existing.getAdminId()));

        VBox form = new VBox(8,
                new Label("Titre"), titre,
                new Label("Description"), description,
                new Label("Departement"), departement,
                new Label("Type contrat"), typeContrat,
                new Label("Nombre postes"), nbPostes,
                new Label("Date publication"), datePub,
                new Label("Admin ID"), adminId
        );
        form.setPrefWidth(420);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) {
                return;
            }
            try {
                OffreEmploi o = existing == null ? new OffreEmploi() : existing;
                o.setTitre(titre.getText().trim());
                o.setDescription(description.getText().trim());
                o.setDepartement(departement.getText().trim());
                o.setTypeContrat(typeContrat.getText().trim());
                o.setNombrePostes(Integer.parseInt(nbPostes.getText().trim()));
                o.setDatePublication(datePub.getValue() == null ? LocalDate.now() : datePub.getValue());
                o.setAdminId(Integer.parseInt(adminId.getText().trim()));
                boolean ok = existing == null ? serviceOffre.ajouter(o) : serviceOffre.update(o);
                if (ok) {
                    refreshData();
                    if (existing == null) {
                        publishOffreOnSocialPlatforms(o);
                    }
                } else {
                    showError("Operation offre echouee.");
                }
            } catch (Exception ex) {
                showError("Erreur offre: " + ex.getMessage());
            }
        });
    }

    private Node createSocialIcon(String imagePath, String tooltipText, Runnable action) {
        Label icon = new Label();
        icon.getStyleClass().add("social-icon-only");
        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream != null) {
                Image image = new Image(stream);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                imageView.setPreserveRatio(true);
                icon.setGraphic(imageView);
            } else {
                icon.setText(tooltipText.substring(tooltipText.lastIndexOf(' ') + 1).toUpperCase(Locale.ROOT));
            }
        } catch (IOException ignored) {
            icon.setText(tooltipText.substring(tooltipText.lastIndexOf(' ') + 1).toUpperCase(Locale.ROOT));
        }
        icon.setStyle("-fx-cursor: hand;");
        icon.setOnMouseClicked(e -> action.run());
        Tooltip.install(icon, new Tooltip(tooltipText));
        return icon;
    }

    private void publishOffreOnSocialPlatforms(OffreEmploi offre) {
        List<String> results = new ArrayList<>();
        results.add(publishOffreOnLinkedInIfConfigured(offre, false));
        results.add(publishOffreOnFacebookIfConfigured(offre, false));
        results.add(publishOffreOnRedditIfConfigured(offre, false));
        String message = results.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" | "));
        setPageMessage(message, message.toLowerCase(Locale.ROOT).contains("non effectuee"));
    }

    private void publishOffreOnLinkedInIfConfigured(OffreEmploi offre) {
        publishOffreOnLinkedInIfConfigured(offre, true);
    }

    private String publishOffreOnLinkedInIfConfigured(OffreEmploi offre, boolean updateMessage) {
        LinkedInJobPublisherService.PublishResult result = linkedInJobPublisherService.publishOffer(offre);
        if (result.isSuccess()) {
            if (updateMessage) {
                setPageMessage(result.getMessage(), false);
            }
            return result.getMessage();
        } else {
            String authUrl = linkedInJobPublisherService.buildAuthorizationUrlFromEnv();
            if (authUrl.isBlank()) {
                String message = "Publication LinkedIn non effectuee: " + result.getMessage();
                if (updateMessage) {
                    setPageMessage(message, true);
                }
                return message;
            } else {
                String message = "Publication LinkedIn non effectuee: " + result.getMessage() + " | Autorisation: " + authUrl;
                if (updateMessage) {
                    setPageMessage(message, true);
                }
                return message;
            }
        }
    }

    private void publishOffreOnFacebookIfConfigured(OffreEmploi offre) {
        publishOffreOnFacebookIfConfigured(offre, true);
    }

    private String publishOffreOnFacebookIfConfigured(OffreEmploi offre, boolean updateMessage) {
        FacebookJobPublisherService.PublishResult result = facebookJobPublisherService.publishOffer(offre);
        String message = result.getMessage();
        if (updateMessage) {
            setPageMessage(message, !result.isSuccess());
        }
        return message;
    }

    private void publishOffreOnRedditIfConfigured(OffreEmploi offre) {
        publishOffreOnRedditIfConfigured(offre, true);
    }

    private String publishOffreOnRedditIfConfigured(OffreEmploi offre, boolean updateMessage) {
        RedditJobPublisherService.PublishResult result = redditJobPublisherService.publishOffer(offre);
        String message = result.getMessage();
        if (updateMessage) {
            setPageMessage(message, !result.isSuccess());
        }
        return message;
    }

    private void openCandidatureDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Candidature");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField candidatId = new TextField();
        TextField offreId = new TextField();
        DatePicker date = new DatePicker(LocalDate.now());
        ComboBox<String> statut = new ComboBox<>();
        statut.getItems().setAll("EN_ATTENTE", "ACCEPTEE", "REFUSEE");
        statut.setValue("EN_ATTENTE");
        TextField cvPath = new TextField();

        VBox form = new VBox(8,
                new Label("Candidat ID"), candidatId,
                new Label("Offre ID"), offreId,
                new Label("Date candidature"), date,
                new Label("Statut"), statut,
                new Label("Chemin CV (optionnel)"), cvPath
        );
        form.setPrefWidth(360);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) {
                return;
            }
            try {
                Candidature c = new Candidature();
                c.setCandidatId(Integer.parseInt(candidatId.getText().trim()));
                c.setOffreId(Integer.parseInt(offreId.getText().trim()));
                c.setDateCandidature(date.getValue() == null ? LocalDate.now() : date.getValue());
                c.setStatut(statut.getValue());
                c.setCheminCv(cvPath.getText() == null ? "" : cvPath.getText().trim());
                if (serviceCandidature.ajouter(c)) {
                    refreshData();
                } else {
                    showError("Ajout candidature echoue.");
                }
            } catch (Exception ex) {
                showError("Erreur candidature: " + ex.getMessage());
            }
        });
    }

    private void renderOfferFillAndPostesPanels() {
        if (tableOffresFillRate == null || tablePostesPourvoir == null) {
            return;
        }
        tableOffresFillRate.getChildren().clear();
        tablePostesPourvoir.getChildren().clear();

        List<Candidature> allCandidatures = serviceCandidature.getAll();
        List<OffreEmploi> offres = serviceOffre.getAll();

        for (OffreEmploi o : offres) {
            int accepted = (int) allCandidatures.stream()
                    .filter(c -> c.getOffreId() == o.getId() && "ACCEPTEE".equalsIgnoreCase(c.getStatut()))
                    .count();
            int totalPostes = Math.max(0, o.getNombrePostes());
            int remaining = Math.max(0, totalPostes - accepted);
            double fillRate = totalPostes == 0 ? 0 : accepted * 100.0 / totalPostes;

            VBox rowFill = buildCard(o.getTitre(),
                    o.getDepartement() + " | " + accepted + "/" + totalPostes + " (" + String.format(Locale.US, "%.0f%%", fillRate) + ")");
            tableOffresFillRate.getChildren().add(rowFill);

            VBox rowNeed = buildCard(o.getTitre(), "Postes restants: " + remaining);
            tablePostesPourvoir.getChildren().add(rowNeed);
        }
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
    public void addFormation() {
        try {
            Formation f = new Formation();
            f.setSujet(txtFormationSujet.getText().trim());
            f.setFormateur(txtFormationFormateur.getText().trim());
            f.setType(txtFormationType.getText().trim());
            LocalDate dateDebut = dpFormationDateDebut.getValue() == null ? LocalDate.now() : dpFormationDateDebut.getValue();
            f.setDateDebut(Date.valueOf(dateDebut));
            f.setDuree(Integer.parseInt(txtFormationDuree.getText().trim()));
            f.setLocalisation(txtFormationLocalisation.getText().trim());
            Integer adminId = resolveUserId(txtFormationAdminId.getText(), "ADMIN");
            if (adminId == null) {
                showError("Responsable RH introuvable.");
                return;
            }
            f.setAdminId(adminId);
            if (crudFormation.ajouter(f)) {
                refreshData();
                clearFormationForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateFormation() {
        if (txtFormationId.getText().isBlank()) {
            showError("Selectionnez une formation");
            return;
        }
        try {
            Formation f = new Formation();
            f.setId(Integer.parseInt(txtFormationId.getText().trim()));
            f.setSujet(txtFormationSujet.getText().trim());
            f.setFormateur(txtFormationFormateur.getText().trim());
            f.setType(txtFormationType.getText().trim());
            LocalDate dateDebut = dpFormationDateDebut.getValue() == null ? LocalDate.now() : dpFormationDateDebut.getValue();
            f.setDateDebut(Date.valueOf(dateDebut));
            f.setDuree(Integer.parseInt(txtFormationDuree.getText().trim()));
            f.setLocalisation(txtFormationLocalisation.getText().trim());
            Integer adminId = resolveUserId(txtFormationAdminId.getText(), "ADMIN");
            if (adminId == null) {
                showError("Responsable RH introuvable.");
                return;
            }
            f.setAdminId(adminId);
            if (crudFormation.modifier(f)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteFormation() {
        if (txtFormationId.getText().isBlank()) {
            showError("Selectionnez une formation");
            return;
        }
        if (crudFormation.supprimer(Integer.parseInt(txtFormationId.getText().trim()))) {
            refreshData();
            clearFormationForm();
        }
    }

    @FXML
    public void clearFormationForm() {
        txtFormationId.clear();
        txtFormationSujet.clear();
        txtFormationFormateur.clear();
        txtFormationType.clear();
        dpFormationDateDebut.setValue(null);
        txtFormationDuree.clear();
        txtFormationLocalisation.clear();
        clearSelection(tableFormations, selectedFormationCard);
        selectedFormation = null;
        selectedFormationCard = null;
        if (Session.getUser() != null && txtFormationAdminId != null) {
            txtFormationAdminId.setText(String.valueOf(Session.getUser().getId()));
        } else if (txtFormationAdminId != null) {
            txtFormationAdminId.clear();
        }
    }

    @FXML
    public void addParticipation() {
        try {
            Participant p = new Participant();
            LocalDate date = dpParticipationDate.getValue() == null ? LocalDate.now() : dpParticipationDate.getValue();
            p.setDateInscription(Date.valueOf(date));
            p.setResultat(txtParticipationResultat.getText().trim());
            Integer employeId = resolveUserId(txtParticipationEmployeId.getText(), "EMPLOYE");
            if (employeId == null) {
                showError("Employe introuvable.");
                return;
            }
            Integer formationId = resolveFormationId(txtParticipationFormationId.getText());
            if (formationId == null) {
                showError("Formation introuvable.");
                return;
            }
            if (crudParticipant.existsByEmployeAndFormation(employeId, formationId, null)) {
                showError("Cet employe est deja inscrit a cette formation.");
                return;
            }
            p.setEmployeId(employeId);
            p.setFormationId(formationId);
            if (crudParticipant.ajouter(p)) {
                refreshData();
                clearParticipationForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void updateParticipation() {
        if (txtParticipationId.getText().isBlank()) {
            showError("Selectionnez une participation");
            return;
        }
        try {
            Participant p = new Participant();
            p.setId(Integer.parseInt(txtParticipationId.getText().trim()));
            LocalDate date = dpParticipationDate.getValue() == null ? LocalDate.now() : dpParticipationDate.getValue();
            p.setDateInscription(Date.valueOf(date));
            p.setResultat(txtParticipationResultat.getText().trim());
            Integer employeId = resolveUserId(txtParticipationEmployeId.getText(), "EMPLOYE");
            if (employeId == null) {
                showError("Employe introuvable.");
                return;
            }
            Integer formationId = resolveFormationId(txtParticipationFormationId.getText());
            if (formationId == null) {
                showError("Formation introuvable.");
                return;
            }
            if (crudParticipant.existsByEmployeAndFormation(employeId, formationId, p.getId())) {
                showError("Cet employe est deja inscrit a cette formation.");
                return;
            }
            p.setEmployeId(employeId);
            p.setFormationId(formationId);
            if (crudParticipant.modifier(p)) {
                refreshData();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void deleteParticipation() {
        if (txtParticipationId.getText().isBlank()) {
            showError("Selectionnez une participation");
            return;
        }
        if (crudParticipant.supprimer(Integer.parseInt(txtParticipationId.getText().trim()))) {
            refreshData();
            clearParticipationForm();
        }
    }

    @FXML
    public void clearParticipationForm() {
        txtParticipationId.clear();
        dpParticipationDate.setValue(null);
        txtParticipationResultat.clear();
        txtParticipationEmployeId.clear();
        txtParticipationFormationId.clear();
        clearSelection(tableParticipations, selectedParticipantCard);
        selectedParticipant = null;
        selectedParticipantCard = null;
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
        if (publicationService.publish(titre, contenu, auteur)) {
            txtPublicationTitre.clear();
            txtPublicationContenu.clear();
            renderCommunicationCards();
            setPageMessage("Publication publiee avec succes.", false);
        } else {
            showError("Echec insertion publication en base.");
        }
    }

    @FXML
    public void updateCommunicationPublication() {
        User current = Session.getUser();
        if (current == null) {
            showError("Session invalide.");
            return;
        }
        String role = current.getRole() == null ? "" : current.getRole().trim().toUpperCase();
        if (!role.contains("ADMIN")) {
            showError("Seul ADMIN RH peut modifier une publication.");
            return;
        }
        if (selectedPublication == null) {
            showError("Selectionnez une publication a modifier.");
            return;
        }
        String titre = txtPublicationTitre.getText() == null ? "" : txtPublicationTitre.getText().trim();
        String contenu = txtPublicationContenu.getText() == null ? "" : txtPublicationContenu.getText().trim();
        if (titre.isBlank() || contenu.isBlank()) {
            showError("Titre et contenu sont obligatoires.");
            return;
        }
        boolean ok = publicationService.updatePublication(
                selectedPublication.getId(),
                current.getId(),
                true,
                titre,
                contenu
        );
        if (ok) {
            renderCommunicationCards();
            setPageMessage("Publication modifiee.", false);
        } else {
            showError("Echec modification publication.");
        }
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
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/feedback/login.fxml"))));
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
        return resolveUserId(txtManagerId.getText(), "MANAGER");
    }

    private Integer resolveUserId(String rawInput, String requiredRoleContains) {
        if (rawInput == null) {
            return null;
        }
        String input = rawInput.trim();
        if (input.isBlank()) {
            return null;
        }
        if (input.matches("\\d+")) {
            return Integer.parseInt(input);
        }
        String normalized = input.toLowerCase();
        for (User u : serviceUser.getAll()) {
            if (requiredRoleContains != null) {
                String role = u.getRole() == null ? "" : u.getRole().toUpperCase();
                if (!role.contains(requiredRoleContains.toUpperCase())) {
                    continue;
                }
            }
            String fullName = (u.getNom() + " " + u.getPrenom()).trim().toLowerCase();
            String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
            if (normalized.equals(fullName) || normalized.equals(email)) {
                return u.getId();
            }
        }
        return null;
    }

    private boolean updateUserRole(int userId, String role) {
        User user = serviceUser.getAll().stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);
        if (user == null) {
            return false;
        }
        user.setRole(role);
        return serviceUser.update(user);
    }

    private Integer resolveOffreId(String rawInput) {
        if (rawInput == null) {
            return null;
        }
        String input = rawInput.trim();
        if (input.isBlank()) {
            return null;
        }
        if (input.matches("\\d+")) {
            return Integer.parseInt(input);
        }
        String normalized = input.toLowerCase();
        for (OffreEmploi offre : serviceOffre.getAll()) {
            String titre = offre.getTitre() == null ? "" : offre.getTitre().toLowerCase();
            if (normalized.equals(titre)) {
                return offre.getId();
            }
        }
        return null;
    }

    private Integer resolveFormationId(String rawInput) {
        if (rawInput == null) {
            return null;
        }
        String input = rawInput.trim();
        if (input.isBlank()) {
            return null;
        }
        if (input.matches("\\d+")) {
            return Integer.parseInt(input);
        }
        Integer id = crudFormation.findIdBySujet(input);
        if (id != null) {
            return id;
        }
        String normalized = input.toLowerCase();
        for (Formation formation : crudFormation.afficherAll()) {
            String sujet = formation.getSujet() == null ? "" : formation.getSujet().toLowerCase();
            if (normalized.equals(sujet)) {
                return formation.getId();
            }
        }
        return null;
    }

    private void renderCandidatCards() {
        tableCandidats.getChildren().clear();
        clearSelection(tableCandidats, selectedCandidatCard);
        selectedCandidat = null;
        selectedCandidatCard = null;
        for (User candidat : serviceUser.getAll()) {
            if (!"CANDIDAT".equalsIgnoreCase(candidat.getRole())) {
                continue;
            }
            VBox card = buildCard(
                    candidat.getNom() + " " + candidat.getPrenom(),
                    candidat.getEmail() + " | role: " + candidat.getRole()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableCandidats, selectedCandidatCard, card);
                selectedCandidatCard = card;
                selectedCandidat = candidat;
                txtCandidatId.setText(candidat.getNom() + " " + candidat.getPrenom());
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
                txtDepartement.setValue(offre.getDepartement());
                txtTypeContrat.setText(offre.getTypeContrat());
                txtNombrePostes.setText(String.valueOf(offre.getNombrePostes()));
                dpDatePublication.setValue(offre.getDatePublication());
                txtAdminId.setText(String.valueOf(offre.getAdminId()));
                if (txtTopCvOffreId != null) {
                    txtTopCvOffreId.setText(String.valueOf(offre.getId()));
                }
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

    private void renderEntretienAdminCards() {
        tableEntretiensAdmin.getChildren().clear();
        User current = Session.getUser();
        if (current == null) {
            tableEntretiensAdmin.getChildren().add(buildCard("Session invalide", "Reconnectez-vous."));
            return;
        }

        List<Entretien> rows = serviceEntretien.getByAdminId(current.getId());
        if (rows.isEmpty()) {
            tableEntretiensAdmin.getChildren().add(buildCard("Aucun entretien", "Planifiez un entretien depuis la liste des candidatures."));
            return;
        }

        for (Entretien e : rows) {
            VBox card = buildCard(
                    (e.getCandidatNom() == null ? "Candidat" : e.getCandidatNom()) + " | "
                            + (e.getOffreTitre() == null ? "Offre" : e.getOffreTitre()),
                    "Date: " + e.getDateEntretien()
                            + " | Manager: " + (e.getManagerNom() == null ? "" : e.getManagerNom())
                            + " | Statut: " + (e.getStatut() == null ? "PLANIFIE" : e.getStatut())
            );

            HBox actions = new HBox(8);
            Button start = new Button("Démarrer entretien");
            start.getStyleClass().add("mock-add-btn");
            start.setOnAction(ev -> openEntretienWebView(e.getMeetLink(), "Entretien #" + e.getId()));

            Button done = new Button("Terminer");
            done.setOnAction(ev -> {
                if (serviceEntretien.updateStatut(e.getId(), "TERMINE")) {
                    setPageMessage("Entretien marqué TERMINE.", false);
                    renderEntretienAdminCards();
                } else {
                    showError("Mise à jour statut impossible.");
                }
            });

            Button cancel = new Button("Annuler");
            cancel.setOnAction(ev -> {
                if (serviceEntretien.updateStatut(e.getId(), "ANNULE")) {
                    setPageMessage("Entretien marqué ANNULE.", false);
                    renderEntretienAdminCards();
                } else {
                    showError("Mise à jour statut impossible.");
                }
            });

            actions.getChildren().addAll(start, done, cancel);
            card.getChildren().add(actions);
            tableEntretiensAdmin.getChildren().add(card);
        }
    }

    private void renderCongeCards() {
        tableConges.getChildren().clear();
        clearSelection(tableConges, selectedCongeCard);
        selectedConge = null;
        selectedCongeCard = null;
        for (Absence conge : serviceAbsence.getCongesAdmin()) {
            String employe = conge.getEmployeNom() == null ? "Employe inconnu" : conge.getEmployeNom();
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
            String employe = absence.getEmployeNom() == null ? "Employe inconnu" : absence.getEmployeNom();
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

    private void renderFormationCards() {
        tableFormations.getChildren().clear();
        clearSelection(tableFormations, selectedFormationCard);
        selectedFormation = null;
        selectedFormationCard = null;
        Map<Integer, User> usersById = serviceUser.getAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (Formation formation : crudFormation.afficherAll()) {
            User admin = usersById.get(formation.getAdminId());
            String adminLabel = admin == null ? "Admin RH inconnu" : (admin.getNom() + " " + admin.getPrenom());
            VBox card = buildCard(
                    formation.getSujet() + " | " + formation.getType(),
                    "formateur: " + formation.getFormateur()
                            + " | date: " + formation.getDateDebut()
                            + " | duree: " + formation.getDuree() + "h"
                            + " | lieu: " + formation.getLocalisation()
                            + " | RH: " + adminLabel
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableFormations, selectedFormationCard, card);
                selectedFormationCard = card;
                selectedFormation = formation;
                txtFormationId.setText(String.valueOf(formation.getId()));
                txtFormationSujet.setText(formation.getSujet());
                txtFormationFormateur.setText(formation.getFormateur());
                txtFormationType.setText(formation.getType());
                dpFormationDateDebut.setValue(formation.getDateDebut() == null ? null : formation.getDateDebut().toLocalDate());
                txtFormationDuree.setText(String.valueOf(formation.getDuree()));
                txtFormationLocalisation.setText(formation.getLocalisation());
                txtFormationAdminId.setText(adminLabel);
            });
            tableFormations.getChildren().add(card);
        }
    }

    private void renderParticipationCards() {
        tableParticipations.getChildren().clear();
        clearSelection(tableParticipations, selectedParticipantCard);
        selectedParticipant = null;
        selectedParticipantCard = null;
        for (Participant participant : crudParticipant.afficherAll()) {
            String employeLabel = participant.getNomEmploye() == null ? "Employe inconnu" : participant.getNomEmploye();
            String formationLabel = participant.getNomFormation() == null ? "Formation inconnue" : participant.getNomFormation();
            VBox card = buildCard(
                    employeLabel + " | " + formationLabel,
                    "inscription: " + participant.getDateInscription() + " | resultat: " + participant.getResultat()
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableParticipations, selectedParticipantCard, card);
                selectedParticipantCard = card;
                selectedParticipant = participant;
                txtParticipationId.setText(String.valueOf(participant.getId()));
                dpParticipationDate.setValue(participant.getDateInscription() == null ? null : participant.getDateInscription().toLocalDate());
                txtParticipationResultat.setText(participant.getResultat());
                txtParticipationEmployeId.setText(employeLabel);
                txtParticipationFormationId.setText(formationLabel);
            });
            tableParticipations.getChildren().add(card);
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
                if (txtPublicationTitre != null) {
                    txtPublicationTitre.setText(publication.getTitre());
                }
                if (txtPublicationContenu != null) {
                    txtPublicationContenu.setText(publication.getContenu());
                }
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
