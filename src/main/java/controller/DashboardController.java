package controller;
import utils.PublicationValidator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import models.CandidateScoringResult;
import models.Candidat;
import models.Candidature;
import models.OffreEmploi;
import models.User;
import models.Absence;
import models.Publication;
import models.PublicationComment;
import models.Formation;
import models.Participant;
import models.Reputation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.ServiceCandidat;
import services.ServiceCandidature;
import services.ServiceOffre;
import services.ServiceUser;
import services.ServiceAbsence;
import services.PublicationService;
import services.CrudFormation;
import services.CrudParticipant;
import services.CandidateScoringService;
import services.ReputationService;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {
    @FXML private VBox badgeChartBox;
    @FXML private VBox top3PublicationsBox;
    @FXML private HBox mediaPreviewBox;
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabUsers;
    @FXML private Tab tabCandidats;
    @FXML private Tab tabOffres;
    @FXML private Tab tabCandidatures;
    @FXML private VBox tableUsers;
    @FXML private TextField txtUserId;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtMdp;
    @FXML private TextField txtManagerId;
    @FXML private ComboBox<String> cbRole;
    @FXML private VBox tableCandidats;
    @FXML private TextField txtCandidatId;
    @FXML private VBox tableOffres;
    @FXML private TextField txtOffreId;
    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> txtDepartement;
    @FXML private TextField txtTypeContrat;
    @FXML private TextField txtNombrePostes;
    @FXML private DatePicker dpDatePublication;
    @FXML private TextField txtAdminId;
    @FXML private VBox tableCandidatures;
    @FXML private TextField txtCandidatureId;
    @FXML private TextField txtCandUserId;
    @FXML private TextField txtCandOffreId;
    @FXML private DatePicker dpDateCandidature;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField txtGlobalSearch;
    @FXML private Label lblKpiTotalCandidatures;
    @FXML private Label lblKpiTauxAcceptation;
    @FXML private Label lblKpiTempsMoyen;
    @FXML private Label lblKpiOffresActives;
    @FXML private Label lblKpiEmployesActifs;
    @FXML private PieChart chartCandidaturesDept;
    @FXML private LineChart<String, Number> chartEvolutionLine;
    @FXML private BarChart<String, Number> chartEvolutionBar;
    @FXML private VBox tableOffresFillRate;
    @FXML private VBox tablePostesPourvoir;
    @FXML private javafx.scene.layout.FlowPane flowUsers;
    @FXML private javafx.scene.layout.FlowPane flowOffres;
    @FXML private javafx.scene.layout.FlowPane flowCandidatures;
    @FXML private TextField txtSearchUsers;
    @FXML private TextField txtSearchOffres;
    @FXML private TextField txtSearchCandidatures;
    @FXML private ComboBox<String> cbFilterRole;
    @FXML private ComboBox<String> cbFilterDepartement;
    @FXML private ComboBox<String> cbFilterStatut;
    @FXML private Label lblUsersPage;
    @FXML private Label lblOffresPage;
    @FXML private Label lblCandidaturesPage;
    @FXML private TextField txtTopCvOffreId;
    @FXML private TextField txtTopCvLimit;
    @FXML private VBox tableTopCv;
    @FXML private Label lblTopCvJsonPath;
    @FXML private Label lblPageMessage;
    @FXML private Label lblCardPostes;
    @FXML private Label lblCardDemandes;
    @FXML private Label lblCardEmployes;
    @FXML private Label lblCardCandidats;
    @FXML private Label lblCardAcceptees;
    @FXML private Label lblCardRefusees;
    @FXML private VBox tableConges;
    @FXML private TextField txtCongeId;
    @FXML private TextField txtCongeEmployeId;
    @FXML private DatePicker dpCongeDebut;
    @FXML private DatePicker dpCongeFin;
    @FXML private ComboBox<String> cbCongeStatut;
    @FXML private VBox tableAbsencesAdmin;
    @FXML private TextField txtAbsenceId;
    @FXML private TextField txtAbsenceEmployeId;
    @FXML private DatePicker dpAbsenceDebut;
    @FXML private DatePicker dpAbsenceFin;
    @FXML private ComboBox<String> cbAbsenceType;
    @FXML private ComboBox<String> cbAbsenceStatut;
    @FXML private VBox tableFormations;
    @FXML private TextField txtFormationId;
    @FXML private TextField txtFormationSujet;
    @FXML private TextField txtFormationFormateur;
    @FXML private TextField txtFormationType;
    @FXML private DatePicker dpFormationDateDebut;
    @FXML private TextField txtFormationDuree;
    @FXML private TextField txtFormationLocalisation;
    @FXML private TextField txtFormationAdminId;
    @FXML private VBox tableParticipations;
    @FXML private TextField txtParticipationId;
    @FXML private DatePicker dpParticipationDate;
    @FXML private TextField txtParticipationResultat;
    @FXML private TextField txtParticipationEmployeId;
    @FXML private TextField txtParticipationFormationId;
    @FXML private TextField txtPublicationTitre;
    @FXML private TextArea txtPublicationContenu;
    @FXML private VBox publicationList;

    // ✅ Réputation FXML
    @FXML private Label lblCountNouveau;
    @FXML private Label lblCountActif;
    @FXML private Label lblCountEngage;
    @FXML private VBox top3Box;

    @FXML private Label lblInfo;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceCandidat serviceCandidat = new ServiceCandidat();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceAbsence serviceAbsence = new ServiceAbsence();
    private final CrudFormation crudFormation = new CrudFormation();
    private final CrudParticipant crudParticipant = new CrudParticipant();
    private final CandidateScoringService candidateScoringService = new CandidateScoringService();
    private static final int USERS_PAGE_SIZE = 6;
    private static final int OFFRES_PAGE_SIZE = 6;
    private static final int CANDIDATURES_PAGE_SIZE = 6;
    private int usersPage = 0;
    private int offresPage = 0;
    private int candidaturesPage = 0;
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
    private Formation selectedFormation;
    private Node selectedFormationCard;
    private Participant selectedParticipant;
    private Node selectedParticipantCard;
    private final List<String> imagesToAdd = new ArrayList<>();
    private final List<String> videosToAdd = new ArrayList<>();

    @FXML
    public void initialize() {
        if (tableUsers != null)          initUserSection();
        if (tableCandidats != null)      initCandidatSection();
        if (tableOffres != null)         initOffreSection();
        if (tableCandidatures != null)   initCandidatureSection();
        if (tableTopCv != null)          initTopCvSection();
        if (tableConges != null)         initCongeSection();
        if (tableAbsencesAdmin != null)  initAbsenceSection();
        if (publicationList != null)     initCommunicationSection();
        if (tableFormations != null)     initFormationSection();
        if (tableParticipations != null) initParticipationSection();
        setPageMessage("", false);
        if (Session.getUser() != null && txtAdminId != null)
            txtAdminId.setText(String.valueOf(Session.getUser().getId()));
        if (Session.getUser() != null && txtFormationAdminId != null)
            txtFormationAdminId.setText(String.valueOf(Session.getUser().getId()));
        initDashboardFilters();
        refreshData();
    }

    private void initDashboardFilters() {
        if (cbFilterRole != null)
            cbFilterRole.getItems().setAll("ADMIN_RH", "MANAGER", "EMPLOYE", "CANDIDAT");
        if (cbFilterDepartement != null) {
            List<String> depts = serviceOffre.getAll().stream()
                    .map(OffreEmploi::getDepartement)
                    .filter(d -> d != null && !d.isBlank())
                    .distinct().sorted().collect(Collectors.toList());
            cbFilterDepartement.getItems().setAll(depts);
        }
    }

    private void initUserSection()          { if (tableUsers != null)         tableUsers.getStyleClass().add("cards-container"); }
    private void initCandidatSection()      { if (tableCandidats != null)     tableCandidats.getStyleClass().add("cards-container"); }
    private void initOffreSection()         { if (tableOffres != null)        tableOffres.getStyleClass().add("cards-container"); }
    private void initCandidatureSection()   { if (tableCandidatures != null)  tableCandidatures.getStyleClass().add("cards-container"); }
    private void initTopCvSection()         { if (tableTopCv != null)         tableTopCv.getStyleClass().add("cards-container"); }
    private void initCongeSection()         { if (tableConges != null)        tableConges.getStyleClass().add("cards-container"); }
    private void initAbsenceSection()       { if (tableAbsencesAdmin != null) tableAbsencesAdmin.getStyleClass().add("cards-container"); }
    private void initCommunicationSection() { if (publicationList != null)    publicationList.getStyleClass().add("cards-container"); }
    private void initFormationSection()     { if (tableFormations != null)    tableFormations.getStyleClass().add("cards-container"); }
    private void initParticipationSection() { if (tableParticipations != null) tableParticipations.getStyleClass().add("cards-container"); }

    @FXML
    public void refreshData() {
        if (tableUsers != null)          renderUserCards();
        if (tableCandidats != null)      renderCandidatCards();
        if (tableOffres != null)         renderOffreCards();
        if (tableCandidatures != null)   renderCandidatureCards();
        if (tableTopCv != null)          tableTopCv.getChildren().clear();
        refreshModernSections();
        refreshKpiAndCharts();
        if (tableConges != null)         renderCongeCards();
        if (tableAbsencesAdmin != null)  renderAbsenceAdminCards();
        if (publicationList != null)     renderCommunicationCards();
        if (tableFormations != null)     renderFormationCards();
        if (tableParticipations != null) renderParticipationCards();
        if (top3Box != null)             renderReputationDashboard();
        refreshDashboardStats();
        setPageMessage("", false);
    }

    @FXML public void refreshModernSections() {
        renderModernUsers();
        renderModernOffres();
        renderModernCandidatures();
    }

    @FXML public void openRecrutement(ActionEvent event)   { navigateTo(event, "/fxml/recrutement/recrutement.fxml"); }
    @FXML public void openDashboard(ActionEvent event)     { navigateTo(event, "/fxml/recrutement/dashboard.fxml"); }
    @FXML public void openConges(ActionEvent event)        { navigateTo(event, "/fxml/congesAbsences/Conges.fxml"); }
    @FXML public void openAbsences(ActionEvent event)      { navigateTo(event, "/fxml/congesAbsences/absences.fxml"); }
    @FXML public void openCommunication(ActionEvent event) { navigateTo(event, "/fxml/communication.fxml"); }
    @FXML public void openFormations(ActionEvent event)    { navigateTo(event, "/fxml/formation/formation.fxml"); }
    @FXML public void openFeedback(ActionEvent event)      { navigateTo(event, "/fxml/feedback/feedback.fxml"); }
    @FXML public void openParametres(ActionEvent event)    { navigateTo(event, "/fxml/parametres.fxml"); }

    private void refreshDashboardStats() {
        if (lblCardPostes != null)    lblCardPostes.setText(String.valueOf(serviceOffre.countPostesOuverts()));
        if (lblCardDemandes != null)  lblCardDemandes.setText(String.valueOf(serviceCandidature.countByStatut("EN_ATTENTE")));
        if (lblCardEmployes != null)  lblCardEmployes.setText(String.valueOf(serviceUser.countByRole("EMPLOYE")));
        if (lblCardCandidats != null) lblCardCandidats.setText(String.valueOf(serviceCandidat.getAll().size()));
        if (lblCardAcceptees != null) lblCardAcceptees.setText(String.valueOf(serviceCandidature.countByStatut("ACCEPTEE")));
        if (lblCardRefusees != null)  lblCardRefusees.setText(String.valueOf(serviceCandidature.countByStatut("REFUSEE")));
    }

    // ══════════════════════════════════════════════════════════
    // COMMUNICATION — renderCommunicationCards
    // ══════════════════════════════════════════════════════════
    private void renderCommunicationCards() {
        publicationList.getChildren().clear();
        selectedPublication = null;
        if (selectedPublicationCard != null) {
            selectedPublicationCard.getStyleClass().remove("entity-card-selected");
            selectedPublicationCard = null;
        }

        List<Publication> publications = publicationService.getAll();
        if (publications.isEmpty()) {
            Label empty = new Label("Aucune publication pour le moment.");
            empty.setStyle("-fx-text-fill:#a0aec0;-fx-font-size:13px;-fx-padding:20;");
            publicationList.getChildren().add(empty);
            return;
        }

        ReputationService reputationService = new ReputationService();

        for (Publication publication : publications) {

            VBox card = new VBox(0);
            card.setStyle(
                    "-fx-background-color:white;" +
                            "-fx-background-radius:14;" +
                            "-fx-border-color:#e8edf4;-fx-border-width:1;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,3);"
            );

            // ── Header ──
            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(16, 20, 10, 20));

            Label avatar = makeCommAvatar(publication.getAuteur(), 40, 13);

            VBox authorInfo = new VBox(3);
            Label auteurLbl = new Label(publication.getAuteur());
            auteurLbl.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#111827;");
            String dateStr = publication.getDatePublication() != null ?
                    publication.getDatePublication().format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm")) : "";
            Label dateLbl = new Label("📅 " + dateStr);
            dateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");
            authorInfo.getChildren().addAll(auteurLbl, dateLbl);

            Region spacerH = new Region();
            HBox.setHgrow(spacerH, Priority.ALWAYS);

            Label commentCount = new Label("💬 " + publication.getCommentaires().size());
            commentCount.setStyle(
                    "-fx-background-color:#f0fdf9;-fx-text-fill:#065f46;" +
                            "-fx-font-size:11px;-fx-font-weight:bold;" +
                            "-fx-padding:3 10;-fx-background-radius:20;"
            );
            header.getChildren().addAll(avatar, authorInfo, spacerH, commentCount);

            // ── Titre ──
            Label titre = new Label(publication.getTitre());
            titre.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#111827;-fx-padding:0 20 4 20;");
            titre.setWrapText(true);

            // ── Contenu ──
            Label contenu = new Label(publication.getContenu());
            contenu.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-padding:0 20 12 20;");
            contenu.setWrapText(true);

            card.getChildren().addAll(header, titre, contenu);

            // ── Médias ──
            List<Map<String, String>> medias = publicationService.getMedia(publication.getId());
            if (!medias.isEmpty()) {
                VBox mediaBox = new VBox(8);
                mediaBox.setPadding(new Insets(0, 20, 12, 20));
                for (Map<String, String> media : medias) {
                    String type = media.get("type");
                    String path = media.get("path");
                    if ("image".equals(type)) {
                        try {
                            ImageView iv = new ImageView(new Image("file:" + path));
                            iv.setFitWidth(500);
                            iv.setFitHeight(300);
                            iv.setPreserveRatio(true);
                            iv.setSmooth(true);
                            VBox imgWrapper = new VBox(iv);
                            imgWrapper.setStyle("-fx-background-color:black;-fx-border-radius:10;-fx-background-radius:10;");
                            imgWrapper.setAlignment(Pos.CENTER);
                            mediaBox.getChildren().add(imgWrapper);
                        } catch (Exception ex) {
                            Label errLbl = new Label("🖼 Image: " + new File(path).getName());
                            errLbl.setStyle("-fx-text-fill:#6b7280;-fx-font-size:11px;");
                            mediaBox.getChildren().add(errLbl);
                        }
                    } else if ("video".equals(type)) {
                        try {
                            Media mediaObj = new Media(new File(path).toURI().toString());
                            MediaPlayer player = new MediaPlayer(mediaObj);
                            MediaView mediaView = new MediaView(player);
                            mediaView.setFitWidth(500);
                            mediaView.setFitHeight(280);
                            mediaView.setPreserveRatio(true);

                            Button btnPlay  = new Button("▶ Play");
                            Button btnPause = new Button("⏸ Pause");
                            Button btnStop  = new Button("⏹ Stop");
                            String bStyle =
                                    "-fx-background-color:#1f2937;-fx-text-fill:white;" +
                                            "-fx-font-size:11px;-fx-cursor:hand;" +
                                            "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:4 12;";
                            btnPlay.setStyle(bStyle);
                            btnPause.setStyle(bStyle);
                            btnStop.setStyle(bStyle);
                            btnPlay.setOnAction(ev -> player.play());
                            btnPause.setOnAction(ev -> player.pause());
                            btnStop.setOnAction(ev -> player.stop());

                            HBox controls = new HBox(8, btnPlay, btnPause, btnStop);
                            controls.setAlignment(Pos.CENTER);
                            controls.setPadding(new Insets(6, 0, 6, 0));

                            Label vidName = new Label("🎬 " + new File(path).getName());
                            vidName.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");

                            VBox videoBox = new VBox(6, vidName, mediaView, controls);
                            videoBox.setAlignment(Pos.CENTER);
                            videoBox.setStyle("-fx-background-color:#111827;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:10;");
                            mediaBox.getChildren().add(videoBox);
                        } catch (Exception ex) {
                            Label errLbl = new Label("🎬 Vidéo: " + new File(path).getName());
                            errLbl.setStyle("-fx-text-fill:#6b7280;-fx-font-size:11px;");
                            mediaBox.getChildren().add(errLbl);
                        }
                    }
                }
                card.getChildren().add(mediaBox);
            }

            // ── Séparateur ──
            Region sep = new Region();
            sep.setStyle("-fx-background-color:#f0f4f8;-fx-min-height:1;-fx-max-height:1;");
            sep.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(sep);

            // ── Commentaires ──
            VBox commentsSection = new VBox(8);
            commentsSection.setPadding(new Insets(12, 20, 14, 20));
            commentsSection.setStyle("-fx-background-color:#fafcff;-fx-background-radius:0 0 14 14;");

            List<PublicationComment> comments = publication.getCommentaires();
            Label commentsTitle = new Label("💬 Commentaires (" + comments.size() + ")");
            commentsTitle.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;-fx-padding:0 0 6 0;");
            commentsSection.getChildren().add(commentsTitle);

            if (comments.isEmpty()) {
                Label noComment = new Label("Aucun commentaire pour le moment.");
                noComment.setStyle("-fx-font-size:12px;-fx-text-fill:#a0aec0;-fx-padding:4 0 0 4;");
                commentsSection.getChildren().add(noComment);
            } else {
                for (PublicationComment comment : comments) {
                    Reputation rep = reputationService.getByUserId(comment.getUserId());

                    HBox commentRow = new HBox(10);
                    commentRow.setAlignment(Pos.TOP_LEFT);

                    Label cAvatar = makeCommAvatar(comment.getAuteur(), 30, 10);

                    VBox bubble = new VBox(4);
                    bubble.setStyle(
                            "-fx-background-color:white;" +
                                    "-fx-border-color:#e8edf4;-fx-border-width:1;" +
                                    "-fx-border-radius:4 12 12 12;-fx-background-radius:4 12 12 12;" +
                                    "-fx-padding:8 12;"
                    );
                    HBox.setHgrow(bubble, Priority.ALWAYS);

                    HBox metaRow = new HBox(8);
                    metaRow.setAlignment(Pos.CENTER_LEFT);

                    Label cAuteur = new Label(comment.getAuteur());
                    cAuteur.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1f2937;");

                    String badgeText  = rep != null ? getBadgeLabel(rep.getBadge()) : "🎗️ Nouveau";
                    String badgeStyle = rep != null ? getBadgeStyle(rep.getBadge()) :
                            "-fx-background-color:linear-gradient(to right,#d1d5db,#9ca3af);" +
                                    "-fx-text-fill:white;-fx-border-color:#6b7280;" +
                                    "-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 10;" +
                                    "-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;";
                    Label badgeLbl = new Label(badgeText);
                    badgeLbl.setStyle(badgeStyle);

                    String cDate = comment.getDateCommentaire() != null ?
                            comment.getDateCommentaire().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                    Label cDateLbl = new Label("· " + cDate);
                    cDateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");

                    metaRow.getChildren().addAll(cAuteur, badgeLbl, cDateLbl);

                    Label cTexte = new Label(comment.getContenu());
                    cTexte.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
                    cTexte.setWrapText(true);

                    Button btnDel = new Button("🗑 Supprimer");
                    btnDel.setStyle(
                            "-fx-background-color:transparent;-fx-text-fill:#e53e3e;" +
                                    "-fx-font-size:10px;-fx-font-weight:bold;-fx-cursor:hand;" +
                                    "-fx-border-color:#fecaca;-fx-border-width:1;" +
                                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:2 8;"
                    );
                    btnDel.setOnAction(ev -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation");
                        alert.setHeaderText("Supprimer ce commentaire ?");
                        alert.setContentText("\"" + comment.getContenu() + "\"");
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                publicationService.deleteComment(
                                        comment.getId(), Session.getUser().getId(), true);
                                renderCommunicationCards();
                                setPageMessage("Commentaire supprimé.", false);
                            }
                        });
                    });

                    bubble.getChildren().addAll(metaRow, cTexte, btnDel);
                    commentRow.getChildren().addAll(cAvatar, bubble);
                    commentsSection.getChildren().add(commentRow);
                }
            }

            card.getChildren().add(commentsSection);

            final VBox cardRef = card;
            card.setOnMouseClicked(event -> {
                if (selectedPublicationCard != null)
                    selectedPublicationCard.getStyleClass().remove("entity-card-selected");
                selectedPublicationCard = cardRef;
                cardRef.getStyleClass().add("entity-card-selected");
                selectedPublication = publication;
                if (txtPublicationTitre  != null) txtPublicationTitre.setText(publication.getTitre());
                if (txtPublicationContenu != null) txtPublicationContenu.setText(publication.getContenu());
            });

            publicationList.getChildren().add(card);
        }
    }

    // ══════════════════════════════════════════════════════════
    // RÉPUTATION DASHBOARD
    // ══════════════════════════════════════════════════════════
    private void renderReputationDashboard() {
        ReputationService reputationService = new ReputationService();

        List<User> employes = serviceUser.getAll().stream()
                .filter(u -> "EMPLOYE".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        // ── Séparer par badge ──
        List<User> listNouveau = new ArrayList<>();
        List<User> listActif   = new ArrayList<>();
        List<User> listEngage  = new ArrayList<>();

        for (User emp : employes) {
            Reputation rep = reputationService.getByUserId(emp.getId());
            String badge = rep != null && rep.getBadge() != null ? rep.getBadge() : "";
            if      (badge.contains("ENGAGE")) listEngage.add(emp);
            else if (badge.contains("ACTIF"))  listActif.add(emp);
            else                               listNouveau.add(emp);
        }

        // ── Compteurs ──
        if (lblCountNouveau != null) lblCountNouveau.setText(String.valueOf(listNouveau.size()));
        if (lblCountActif   != null) lblCountActif.setText(String.valueOf(listActif.size()));
        if (lblCountEngage  != null) lblCountEngage.setText(String.valueOf(listEngage.size()));

        // ══════════════════════════════════════
        // LISTES PAR BADGE
        // ══════════════════════════════════════
        if (badgeChartBox != null) {
            badgeChartBox.getChildren().clear();

            badgeChartBox.getChildren().add(
                    buildBadgeSection("Nouveau", "#6b7280", "#f3f4f6", "#e5e7eb", listNouveau, reputationService)
            );
            badgeChartBox.getChildren().add(
                    buildBadgeSection("Actif", "#3b82f6", "#eff6ff", "#bfdbfe", listActif, reputationService)
            );
            badgeChartBox.getChildren().add(
                    buildBadgeSection("Engage", "#f59e0b", "#fffbeb", "#fde68a", listEngage, reputationService)
            );
        }

        // ══════════════════════════════════════
        // TOP 3 EMPLOYES
        // ══════════════════════════════════════
        if (top3Box != null) {
            top3Box.getChildren().clear();

            List<Reputation> top3 = reputationService.getTop10().stream()
                    .limit(3).collect(Collectors.toList());

            if (top3.isEmpty()) {
                Label empty = new Label("Aucun employe avec des points pour le moment.");
                empty.setStyle("-fx-font-size:12px;-fx-text-fill:#a0aec0;");
                top3Box.getChildren().add(empty);
            } else {
                String[] rankLabels = {"#1", "#2", "#3"};
                String[] rankColors = {"#f59e0b", "#9ca3af", "#cd7f32"};
                String[] bgColors   = {
                        "-fx-background-color:#fffbeb;-fx-border-color:#f59e0b;",
                        "-fx-background-color:#f9fafb;-fx-border-color:#d1d5db;",
                        "-fx-background-color:#fff7ed;-fx-border-color:#cd7f32;"
                };
                int rank = 0;
                for (Reputation rep : top3) {
                    User emp = employes.stream()
                            .filter(u -> u.getId() == rep.getUserId())
                            .findFirst().orElse(null);
                    if (emp == null) { rank++; continue; }

                    String rankLabel = rank < rankLabels.length ? rankLabels[rank] : "#" + (rank + 1);
                    String rankColor = rank < rankColors.length ? rankColors[rank] : "#6b7280";
                    String bgColor   = rank < bgColors.length   ? bgColors[rank]
                            : "-fx-background-color:white;-fx-border-color:#e5e7eb;";

                    HBox row = new HBox(14);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle(bgColor +
                            "-fx-border-width:2;-fx-border-radius:12;-fx-background-radius:12;" +
                            "-fx-padding:14 20;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);"
                    );

                    // Cercle rang
                    Label rankLbl = new Label(rankLabel);
                    rankLbl.setStyle(
                            "-fx-background-color:" + rankColor + ";" +
                                    "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;" +
                                    "-fx-min-width:36;-fx-min-height:36;-fx-max-width:36;-fx-max-height:36;" +
                                    "-fx-background-radius:50;-fx-alignment:center;"
                    );

                    // Avatar
                    Label avatarLbl = makeCommAvatar(emp.getNom() + " " + emp.getPrenom(), 36, 12);

                    // Nom + badge
                    VBox nameBox = new VBox(4);
                    Label nomLbl = new Label(emp.getNom() + " " + emp.getPrenom());
                    nomLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1f2937;");

                    String badge = rep.getBadge() != null ? rep.getBadge() : "";
                    String badgeText, badgeStyle;
                    if (badge.contains("ENGAGE")) {
                        badgeText  = "Engage";
                        badgeStyle = "-fx-background-color:#fef3c7;-fx-text-fill:#92400e;-fx-border-color:#f59e0b;";
                    } else if (badge.contains("ACTIF")) {
                        badgeText  = "Actif";
                        badgeStyle = "-fx-background-color:#dbeafe;-fx-text-fill:#1e40af;-fx-border-color:#3b82f6;";
                    } else {
                        badgeText  = "Nouveau";
                        badgeStyle = "-fx-background-color:#f3f4f6;-fx-text-fill:#374151;-fx-border-color:#d1d5db;";
                    }
                    Label badgeLbl = new Label(badgeText);
                    badgeLbl.setStyle(badgeStyle +
                            "-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:2 8;" +
                            "-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;"
                    );
                    nameBox.getChildren().addAll(nomLbl, badgeLbl);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Score
                    VBox scoreBox = new VBox(1);
                    scoreBox.setAlignment(Pos.CENTER_RIGHT);
                    Label scoreLbl = new Label(String.valueOf(rep.getTotalScore()));
                    scoreLbl.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:" + rankColor + ";");
                    Label ptsLbl = new Label("points");
                    ptsLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
                    scoreBox.getChildren().addAll(scoreLbl, ptsLbl);

                    row.getChildren().addAll(rankLbl, avatarLbl, nameBox, spacer, scoreBox);
                    top3Box.getChildren().add(row);
                    rank++;
                }
            }
        }



        if (top3PublicationsBox != null) {
            top3PublicationsBox.getChildren().clear();

            List<Publication> top3Pubs = publicationService.getAll().stream()
                    .filter(p -> !p.getCommentaires().isEmpty())
                    .sorted((a, b) -> b.getCommentaires().size() - a.getCommentaires().size())
                    .limit(3)
                    .collect(Collectors.toList());

            if (top3Pubs.isEmpty()) {
                Label empty = new Label("Aucune publication commentee pour le moment.");
                empty.setStyle("-fx-font-size:12px;-fx-text-fill:#a0aec0;");
                top3PublicationsBox.getChildren().add(empty);
            } else {
                String[] rankLabels = {"#1", "#2", "#3"};
                String[] rankColors = {"#20c997", "#3b82f6", "#8b5cf6"};
                int maxComments     = top3Pubs.get(0).getCommentaires().size();
                if (maxComments == 0) maxComments = 1;

                int rank = 0;
                for (Publication pub : top3Pubs) {
                    int nbComments = pub.getCommentaires().size();
                    String rankLabel = rank < rankLabels.length ? rankLabels[rank] : "#" + (rank + 1);
                    String rankColor = rank < rankColors.length ? rankColors[rank] : "#6b7280";
                    double ratio = (double) nbComments / maxComments;

                    VBox pubCard = new VBox(10);
                    pubCard.setStyle(
                            "-fx-background-color:white;" +
                                    "-fx-border-color:#e5e7eb;-fx-border-width:1;" +
                                    "-fx-border-radius:12;-fx-background-radius:12;" +
                                    "-fx-padding:16;" +
                                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,2);"
                    );
                    VBox.setMargin(pubCard, new Insets(0, 0, 0, 0));

                    // ── Ligne titre + rang + compteur ──
                    HBox topRow = new HBox(12);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    // Cercle rang
                    Label rankLbl = new Label(rankLabel);
                    rankLbl.setStyle(
                            "-fx-background-color:" + rankColor + ";" +
                                    "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:12px;" +
                                    "-fx-min-width:32;-fx-min-height:32;-fx-max-width:32;-fx-max-height:32;" +
                                    "-fx-background-radius:50;-fx-alignment:center;"
                    );

                    VBox pubInfo = new VBox(4);
                    HBox.setHgrow(pubInfo, Priority.ALWAYS);

                    Label pubTitre = new Label(pub.getTitre());
                    pubTitre.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
                    pubTitre.setWrapText(true);

                    HBox metaRow = new HBox(12);
                    metaRow.setAlignment(Pos.CENTER_LEFT);

                    // Avatar auteur
                    Label auteurAvatar = makeCommAvatar(pub.getAuteur(), 22, 8);
                    Label auteurLbl = new Label(pub.getAuteur());
                    auteurLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");

                    String dateStr = pub.getDatePublication() != null ?
                            pub.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                    Label dateLbl = new Label(dateStr);
                    dateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");

                    metaRow.getChildren().addAll(auteurAvatar, auteurLbl, dateLbl);
                    pubInfo.getChildren().addAll(pubTitre, metaRow);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Compteur commentaires
                    VBox commentBox = new VBox(2);
                    commentBox.setAlignment(Pos.CENTER_RIGHT);
                    Label commentCount = new Label(String.valueOf(nbComments));
                    commentCount.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:" + rankColor + ";");
                    Label commentLabel = new Label("commentaires");
                    commentLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
                    commentBox.getChildren().addAll(commentCount, commentLabel);

                    topRow.getChildren().addAll(rankLbl, pubInfo, spacer, commentBox);
                    pubCard.getChildren().add(topRow);

                    // Apercu contenu
                    String contenuCourt = pub.getContenu() != null ? pub.getContenu() : "";
                    if (contenuCourt.length() > 120) contenuCourt = contenuCourt.substring(0, 120) + "...";
                    if (!contenuCourt.isBlank()) {
                        Label contenuLbl = new Label(contenuCourt);
                        contenuLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
                        contenuLbl.setWrapText(true);
                        pubCard.getChildren().add(contenuLbl);
                    }

                    // Barre progression commentaires
                    HBox barBg = new HBox();
                    barBg.setStyle(
                            "-fx-background-color:#f3f4f6;" +
                                    "-fx-background-radius:6;-fx-min-height:8;-fx-max-height:8;"
                    );
                    barBg.setMaxWidth(Double.MAX_VALUE);

                    Region barFill = new Region();
                    barFill.setStyle(
                            "-fx-background-color:" + rankColor + ";" +
                                    "-fx-background-radius:6;-fx-min-height:8;-fx-max-height:8;"
                    );
                    barFill.setPrefWidth(ratio * 700);
                    barFill.setMaxWidth(ratio * 700);
                    barBg.getChildren().add(barFill);
                    pubCard.getChildren().add(barBg);

                    top3PublicationsBox.getChildren().add(pubCard);
                    rank++;
                }
            }
        }
    }



    // ══════════════════════════════════════════════════════════
    // PUBLICATION ACTIONS
    // ══════════════════════════════════════════════════════════
    @FXML
    public void publishCommunication() {
        if (txtPublicationTitre == null || txtPublicationContenu == null) return;

        User current = Session.getUser();
        if (current == null) { showError("Session invalide."); return; }

        String role = current.getRole() == null ? "" : current.getRole().trim().toUpperCase();
        if (!role.contains("ADMIN")) { showError("Seul ADMIN RH peut publier."); return; }

        String titre   = txtPublicationTitre.getText()  == null ? "" : txtPublicationTitre.getText().trim();
        String contenu = txtPublicationContenu.getText() == null ? "" : txtPublicationContenu.getText().trim();

        // ✅ Validation
        List<String> erreurs = PublicationValidator.validerPublication(titre, contenu);
        if (!erreurs.isEmpty()) {
            afficherErreursPublication(erreurs, titre, contenu);
            return;
        }

        // ✅ Réinitialiser styles si tout est bon
        resetStylesPublication();

        int pubId = publicationService.addPublicationAndGetId(titre, contenu);
        if (pubId > 0) {
            for (String img : imagesToAdd) publicationService.addMedia(pubId, "image", img);
            for (String vid : videosToAdd) publicationService.addMedia(pubId, "video", vid);
            imagesToAdd.clear();
            videosToAdd.clear();
            if (mediaPreviewBox != null) mediaPreviewBox.getChildren().clear();
            txtPublicationTitre.clear();
            txtPublicationContenu.clear();
            renderCommunicationCards();
            setPageMessage("Publication publiee avec succes.", false);
        } else {
            showError("Echec insertion publication.");
        }
    }

    @FXML
    public void updateCommunicationPublication() {
        User current = Session.getUser();
        if (current == null) { showError("Session invalide."); return; }
        if (selectedPublication == null) { showError("Selectionnez une publication."); return; }

        String titre   = txtPublicationTitre.getText()  == null ? "" : txtPublicationTitre.getText().trim();
        String contenu = txtPublicationContenu.getText() == null ? "" : txtPublicationContenu.getText().trim();

        // ✅ Validation
        List<String> erreurs = PublicationValidator.validerPublication(titre, contenu);
        if (!erreurs.isEmpty()) {
            afficherErreursPublication(erreurs, titre, contenu);
            return;
        }

        resetStylesPublication();

        boolean ok = publicationService.updatePublication(
                selectedPublication.getId(), current.getId(), true, titre, contenu);
        if (ok) {
            if (!imagesToAdd.isEmpty() || !videosToAdd.isEmpty()) {
                for (String img : imagesToAdd) publicationService.addMedia(selectedPublication.getId(), "image", img);
                for (String vid : videosToAdd) publicationService.addMedia(selectedPublication.getId(), "video", vid);
                imagesToAdd.clear();
                videosToAdd.clear();
                if (mediaPreviewBox != null) mediaPreviewBox.getChildren().clear();
            }
            renderCommunicationCards();
            setPageMessage("Publication modifiee.", false);
        } else {
            showError("Echec modification.");
        }
    }


    @FXML
    public void deleteCommunicationPublication() {
        User current = Session.getUser();
        if (current == null) { showError("Session invalide."); return; }
        if (selectedPublication == null) { showError("Sélectionnez une publication."); return; }
        if (publicationService.deleteById(selectedPublication.getId())) {
            selectedPublication = null;
            if (selectedPublicationCard != null) {
                selectedPublicationCard.getStyleClass().remove("entity-card-selected");
                selectedPublicationCard = null;
            }
            renderCommunicationCards();
            setPageMessage("Publication supprimée.", false);
        } else {
            showError("Suppression impossible.");
        }
    }

    @FXML
    private void selectImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(publicationList.getScene().getWindow());
        if (file != null) {
            imagesToAdd.add(file.getAbsolutePath());
            ImageView preview = new ImageView(new Image("file:" + file.getAbsolutePath()));
            preview.setFitWidth(80);
            preview.setFitHeight(80);
            preview.setPreserveRatio(true);
            VBox imgBox = new VBox(preview);
            imgBox.setMaxWidth(80);
            imgBox.setMaxHeight(80);
            imgBox.setStyle(
                    "-fx-background-radius:8;-fx-border-radius:8;" +
                            "-fx-border-color:#6ee7cb;-fx-border-width:1.5;-fx-padding:3;"
            );
            if (mediaPreviewBox != null) mediaPreviewBox.getChildren().add(imgBox);
            setPageMessage("Image sélectionnée : " + file.getName(), false);
        }
    }

    @FXML
    private void selectVideo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une vidéo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov", "*.avi", "*.flv"));
        Stage stage = (Stage) publicationList.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            videosToAdd.add(file.getAbsolutePath());
            if (mediaPreviewBox != null)
                mediaPreviewBox.getChildren().add(new Label("Vidéo : " + file.getName()));
            setPageMessage("Vidéo sélectionnée : " + file.getName(), false);
        }
    }

    // ══════════════════════════════════════════════════════════
    // USER CRUD
    // ══════════════════════════════════════════════════════════
    @FXML public void addUser() {
        try {
            User user = new User();
            user.setNom(txtNom.getText().trim());
            user.setPrenom(txtPrenom.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setMdp(txtMdp.getText().trim());
            user.setRole(cbRole.getValue());
            user.setManagerId(parseManagerId());
            if (serviceUser.ajouter(user)) { refreshData(); clearUserForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateUser() {
        if (txtUserId.getText().isBlank()) { showError("Selectionnez un user"); return; }
        try {
            User user = new User();
            user.setId(Integer.parseInt(txtUserId.getText()));
            user.setNom(txtNom.getText().trim());
            user.setPrenom(txtPrenom.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setMdp(txtMdp.getText().trim());
            user.setRole(cbRole.getValue());
            user.setManagerId(parseManagerId());
            if (serviceUser.update(user)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteUser() {
        if (txtUserId.getText().isBlank()) { showError("Selectionnez un user"); return; }
        if (serviceUser.delete(Integer.parseInt(txtUserId.getText()))) { refreshData(); clearUserForm(); }
    }

    @FXML public void clearUserForm() {
        txtUserId.clear(); txtNom.clear(); txtPrenom.clear();
        txtEmail.clear(); txtMdp.clear(); txtManagerId.clear();
        cbRole.setValue(null);
        clearSelection(tableUsers, selectedUserCard);
        selectedUser = null; selectedUserCard = null;
    }

    // ══════════════════════════════════════════════════════════
    // CANDIDAT CRUD
    // ══════════════════════════════════════════════════════════
    @FXML public void addCandidat() {
        try {
            Integer userId = resolveUserId(txtCandidatId.getText(), null);
            if (userId == null) { showError("Candidat introuvable."); return; }
            Candidat c = new Candidat(userId, "");
            if (serviceCandidat.ajouter(c)) { refreshData(); clearCandidatForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateCandidat() {
        try {
            Integer userId = selectedCandidat != null ? selectedCandidat.getId()
                    : resolveUserId(txtCandidatId.getText(), "CANDIDAT");
            if (userId == null) { showError("Selectionnez un candidat."); return; }
            Candidat c = new Candidat(userId, "");
            if (!serviceCandidat.update(c)) showError("Mise a jour impossible.");
            else refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteCandidat() {
        try {
            Integer userId = resolveUserId(txtCandidatId.getText(), null);
            if (userId == null && selectedCandidat != null) userId = selectedCandidat.getId();
            if (userId == null) { showError("Selectionnez un candidat."); return; }
            if (serviceCandidat.delete(userId)) { refreshData(); clearCandidatForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void clearCandidatForm() {
        txtCandidatId.clear();
        clearSelection(tableCandidats, selectedCandidatCard);
        selectedCandidat = null; selectedCandidatCard = null;
    }

    // ══════════════════════════════════════════════════════════
    // OFFRE CRUD
    // ══════════════════════════════════════════════════════════
    @FXML public void addOffre() {
        try {
            OffreEmploi o = new OffreEmploi();
            o.setTitre(txtTitre.getText().trim());
            o.setDescription("");
            o.setDepartement(txtDepartement.getValue() == null ? "" : txtDepartement.getValue().trim());
            o.setTypeContrat(txtTypeContrat.getText().trim());
            o.setNombrePostes(Integer.parseInt(txtNombrePostes.getText().trim()));
            o.setDatePublication(dpDatePublication.getValue() == null ? LocalDate.now() : dpDatePublication.getValue());
            Integer adminId = resolveUserId(txtAdminId.getText(), "ADMIN");
            if (adminId == null) { showError("Responsable RH introuvable."); return; }
            o.setAdminId(adminId);
            if (serviceOffre.ajouter(o)) { refreshData(); clearOffreForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateOffre() {
        if (txtOffreId.getText().isBlank()) { showError("Selectionnez une offre"); return; }
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
            if (adminId == null) { showError("Responsable RH introuvable."); return; }
            o.setAdminId(adminId);
            if (serviceOffre.update(o)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteOffre() {
        if (txtOffreId.getText().isBlank()) { showError("Selectionnez une offre"); return; }
        if (serviceOffre.delete(Integer.parseInt(txtOffreId.getText()))) { refreshData(); clearOffreForm(); }
    }

    @FXML public void clearOffreForm() {
        txtOffreId.clear(); txtTitre.clear(); txtDepartement.setValue(null);
        txtTypeContrat.clear(); txtNombrePostes.clear(); dpDatePublication.setValue(null);
        clearSelection(tableOffres, selectedOffreCard);
        selectedOffre = null; selectedOffreCard = null;
        if (Session.getUser() != null) txtAdminId.setText(String.valueOf(Session.getUser().getId()));
        else txtAdminId.clear();
    }

    // ══════════════════════════════════════════════════════════
    // CANDIDATURE CRUD
    // ══════════════════════════════════════════════════════════
    @FXML public void addCandidature() {
        try {
            Candidature c = new Candidature();
            Integer candidatId = resolveUserId(txtCandUserId.getText(), null);
            Integer offreId    = resolveOffreId(txtCandOffreId.getText());
            if (candidatId == null) { showError("Candidat introuvable."); return; }
            if (offreId    == null) { showError("Offre introuvable.");    return; }
            c.setCandidatId(candidatId);
            c.setOffreId(offreId);
            c.setDateCandidature(dpDateCandidature.getValue() == null ? LocalDate.now() : dpDateCandidature.getValue());
            c.setStatut(cbStatut.getValue() == null ? "EN_ATTENTE" : cbStatut.getValue());
            if (serviceCandidature.ajouter(c)) { refreshData(); clearCandidatureForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateCandidature() {
        if (txtCandidatureId.getText().isBlank()) { showError("Selectionnez une candidature"); return; }
        try {
            Candidature c = new Candidature();
            c.setId(Integer.parseInt(txtCandidatureId.getText()));
            Integer candidatId = resolveUserId(txtCandUserId.getText(), null);
            Integer offreId    = resolveOffreId(txtCandOffreId.getText());
            if (candidatId == null) { showError("Candidat introuvable."); return; }
            if (offreId    == null) { showError("Offre introuvable.");    return; }
            c.setCandidatId(candidatId);
            c.setOffreId(offreId);
            c.setDateCandidature(dpDateCandidature.getValue() == null ? LocalDate.now() : dpDateCandidature.getValue());
            c.setStatut(cbStatut.getValue() == null ? "EN_ATTENTE" : cbStatut.getValue());
            if (serviceCandidature.update(c)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteCandidature() {
        if (txtCandidatureId.getText().isBlank()) { showError("Selectionnez une candidature"); return; }
        if (serviceCandidature.supprimer(Integer.parseInt(txtCandidatureId.getText()))) {
            refreshData(); clearCandidatureForm();
        }
    }

    @FXML public void clearCandidatureForm() {
        txtCandidatureId.clear(); txtCandUserId.clear(); txtCandOffreId.clear();
        dpDateCandidature.setValue(null); cbStatut.setValue(null);
        clearSelection(tableCandidatures, selectedCandidatureCard);
        selectedCandidature = null; selectedCandidatureCard = null;
    }

    @FXML public void analyzeTopCv() {
        if (tableTopCv == null) return;
        try {
            Integer offreId = resolveOffreId(txtTopCvOffreId == null ? null : txtTopCvOffreId.getText());
            if (offreId == null && selectedOffre != null) offreId = selectedOffre.getId();
            if (offreId == null) { showError("Saisissez une offre."); return; }
            int topN = 5;
            if (txtTopCvLimit != null && !txtTopCvLimit.getText().isBlank())
                topN = Integer.parseInt(txtTopCvLimit.getText().trim());
            if (topN <= 0) topN = 5;
            List<CandidateScoringResult> ranking = candidateScoringService.rankTopCandidatesForOffer(offreId, topN);
            tableTopCv.getChildren().clear();
            int rank = 1;
            for (CandidateScoringResult item : ranking) {
                String nom     = (item.getCandidatNom() == null || item.getCandidatNom().isBlank()) ? "Candidat" : item.getCandidatNom();
                String email   = item.getCandidatEmail() == null ? "" : item.getCandidatEmail();
                String xp      = item.getCvAnalysis() == null ? "0" : String.format(Locale.US, "%.1f", item.getCvAnalysis().getYearsExperience());
                String skills  = item.getCvAnalysis() == null || item.getCvAnalysis().getSkills().isEmpty() ? "N/A"
                        : String.join(", ", item.getCvAnalysis().getSkills().stream().limit(6).collect(Collectors.toList()));
                String summary = item.getCvAnalysis() == null ? "" : item.getCvAnalysis().getSummary();
                if (summary == null) summary = "";
                if (summary.length() > 120) summary = summary.substring(0, 120) + "...";
                VBox card = buildCard(
                        "#" + rank + " " + nom + " | score: " + item.getScoreGlobal() + "/100",
                        "email: " + email + " | exp: " + xp + " ans | skills: " + skills
                                + " | " + item.getCommentaire()
                                + (summary.isBlank() ? "" : " | " + summary)
                );
                tableTopCv.getChildren().add(card);
                rank++;
            }
            String jsonPath = candidateScoringService.saveRankingAsJson(offreId, ranking);
            if (lblTopCvJsonPath != null) lblTopCvJsonPath.setText("JSON: " + jsonPath);
            setPageMessage("Top CV généré : " + ranking.size() + " candidat(s).", false);
        } catch (NumberFormatException e) {
            showError("Top N invalide.");
        } catch (Exception e) {
            showError("Erreur analyse Top CV: " + e.getMessage());
        }
    }

    @FXML public void analyzeTopCvQuick() {
        if (txtTopCvLimit != null) txtTopCvLimit.setText("5");
        analyzeTopCv();
    }

    // ══════════════════════════════════════════════════════════
    // CONGÉS / ABSENCES
    // ══════════════════════════════════════════════════════════
    @FXML public void addConge() {
        try {
            Absence a = new Absence();
            a.setEmployeId(Integer.parseInt(txtCongeEmployeId.getText().trim()));
            a.setDateDebut(dpCongeDebut.getValue());
            a.setDateFin(dpCongeFin.getValue());
            a.setTypeAbsence("CONGE");
            a.setStatut(cbCongeStatut.getValue() == null ? "EN_ATTENTE" : cbCongeStatut.getValue());
            if (serviceAbsence.addAdmin(a)) { refreshData(); clearCongeForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateConge() {
        if (txtCongeId.getText().isBlank()) { showError("Selectionnez un conge"); return; }
        try {
            Absence a = new Absence();
            a.setId(Integer.parseInt(txtCongeId.getText().trim()));
            a.setEmployeId(Integer.parseInt(txtCongeEmployeId.getText().trim()));
            a.setDateDebut(dpCongeDebut.getValue());
            a.setDateFin(dpCongeFin.getValue());
            a.setTypeAbsence("CONGE");
            a.setStatut(cbCongeStatut.getValue() == null ? "EN_ATTENTE" : cbCongeStatut.getValue());
            if (serviceAbsence.updateAdmin(a)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteConge() {
        if (txtCongeId.getText().isBlank()) { showError("Selectionnez un conge"); return; }
        if (serviceAbsence.deleteAdmin(Integer.parseInt(txtCongeId.getText().trim()))) {
            refreshData(); clearCongeForm();
        }
    }

    @FXML public void clearCongeForm() {
        txtCongeId.clear(); txtCongeEmployeId.clear();
        dpCongeDebut.setValue(null); dpCongeFin.setValue(null); cbCongeStatut.setValue(null);
        clearSelection(tableConges, selectedCongeCard);
        selectedConge = null; selectedCongeCard = null;
    }

    @FXML public void addAbsenceAdmin() {
        try {
            Absence a = new Absence();
            a.setEmployeId(Integer.parseInt(txtAbsenceEmployeId.getText().trim()));
            a.setDateDebut(dpAbsenceDebut.getValue());
            a.setDateFin(dpAbsenceFin.getValue());
            a.setTypeAbsence(cbAbsenceType.getValue());
            a.setStatut(cbAbsenceStatut.getValue() == null ? "EN_ATTENTE" : cbAbsenceStatut.getValue());
            if (serviceAbsence.addAdmin(a)) { refreshData(); clearAbsenceAdminForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateAbsenceAdmin() {
        if (txtAbsenceId.getText().isBlank()) { showError("Selectionnez une absence"); return; }
        try {
            Absence a = new Absence();
            a.setId(Integer.parseInt(txtAbsenceId.getText().trim()));
            a.setEmployeId(Integer.parseInt(txtAbsenceEmployeId.getText().trim()));
            a.setDateDebut(dpAbsenceDebut.getValue());
            a.setDateFin(dpAbsenceFin.getValue());
            a.setTypeAbsence(cbAbsenceType.getValue());
            a.setStatut(cbAbsenceStatut.getValue() == null ? "EN_ATTENTE" : cbAbsenceStatut.getValue());
            if (serviceAbsence.updateAdmin(a)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteAbsenceAdmin() {
        if (txtAbsenceId.getText().isBlank()) { showError("Selectionnez une absence"); return; }
        if (serviceAbsence.deleteAdmin(Integer.parseInt(txtAbsenceId.getText().trim()))) {
            refreshData(); clearAbsenceAdminForm();
        }
    }

    @FXML public void clearAbsenceAdminForm() {
        txtAbsenceId.clear(); txtAbsenceEmployeId.clear();
        dpAbsenceDebut.setValue(null); dpAbsenceFin.setValue(null);
        cbAbsenceType.setValue(null); cbAbsenceStatut.setValue(null);
        clearSelection(tableAbsencesAdmin, selectedAbsenceAdminCard);
        selectedAbsenceAdmin = null; selectedAbsenceAdminCard = null;
    }

    // ══════════════════════════════════════════════════════════
    // FORMATIONS / PARTICIPATIONS
    // ══════════════════════════════════════════════════════════
    @FXML public void addFormation() {
        try {
            Formation f = new Formation();
            f.setSujet(txtFormationSujet.getText().trim());
            f.setFormateur(txtFormationFormateur.getText().trim());
            f.setType(txtFormationType.getText().trim());
            f.setDateDebut(Date.valueOf(dpFormationDateDebut.getValue() == null ? LocalDate.now() : dpFormationDateDebut.getValue()));
            f.setDuree(Integer.parseInt(txtFormationDuree.getText().trim()));
            f.setLocalisation(txtFormationLocalisation.getText().trim());
            Integer adminId = resolveUserId(txtFormationAdminId.getText(), "ADMIN");
            if (adminId == null) { showError("Responsable RH introuvable."); return; }
            f.setAdminId(adminId);
            if (crudFormation.ajouter(f)) { refreshData(); clearFormationForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateFormation() {
        if (txtFormationId.getText().isBlank()) { showError("Selectionnez une formation"); return; }
        try {
            Formation f = new Formation();
            f.setId(Integer.parseInt(txtFormationId.getText().trim()));
            f.setSujet(txtFormationSujet.getText().trim());
            f.setFormateur(txtFormationFormateur.getText().trim());
            f.setType(txtFormationType.getText().trim());
            f.setDateDebut(Date.valueOf(dpFormationDateDebut.getValue() == null ? LocalDate.now() : dpFormationDateDebut.getValue()));
            f.setDuree(Integer.parseInt(txtFormationDuree.getText().trim()));
            f.setLocalisation(txtFormationLocalisation.getText().trim());
            Integer adminId = resolveUserId(txtFormationAdminId.getText(), "ADMIN");
            if (adminId == null) { showError("Responsable RH introuvable."); return; }
            f.setAdminId(adminId);
            if (crudFormation.modifier(f)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteFormation() {
        if (txtFormationId.getText().isBlank()) { showError("Selectionnez une formation"); return; }
        if (crudFormation.supprimer(Integer.parseInt(txtFormationId.getText().trim()))) {
            refreshData(); clearFormationForm();
        }
    }

    @FXML public void clearFormationForm() {
        txtFormationId.clear(); txtFormationSujet.clear(); txtFormationFormateur.clear();
        txtFormationType.clear(); dpFormationDateDebut.setValue(null);
        txtFormationDuree.clear(); txtFormationLocalisation.clear();
        clearSelection(tableFormations, selectedFormationCard);
        selectedFormation = null; selectedFormationCard = null;
        if (Session.getUser() != null && txtFormationAdminId != null)
            txtFormationAdminId.setText(String.valueOf(Session.getUser().getId()));
        else if (txtFormationAdminId != null) txtFormationAdminId.clear();
    }

    @FXML public void addParticipation() {
        try {
            Participant p = new Participant();
            p.setDateInscription(Date.valueOf(dpParticipationDate.getValue() == null ? LocalDate.now() : dpParticipationDate.getValue()));
            p.setResultat(txtParticipationResultat.getText().trim());
            Integer employeId  = resolveUserId(txtParticipationEmployeId.getText(), "EMPLOYE");
            Integer formationId = resolveFormationId(txtParticipationFormationId.getText());
            if (employeId == null)   { showError("Employe introuvable."); return; }
            if (formationId == null) { showError("Formation introuvable."); return; }
            if (crudParticipant.existsByEmployeAndFormation(employeId, formationId, null)) {
                showError("Employe déjà inscrit à cette formation."); return;
            }
            p.setEmployeId(employeId);
            p.setFormationId(formationId);
            if (crudParticipant.ajouter(p)) { refreshData(); clearParticipationForm(); }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void updateParticipation() {
        if (txtParticipationId.getText().isBlank()) { showError("Selectionnez une participation"); return; }
        try {
            Participant p = new Participant();
            p.setId(Integer.parseInt(txtParticipationId.getText().trim()));
            p.setDateInscription(Date.valueOf(dpParticipationDate.getValue() == null ? LocalDate.now() : dpParticipationDate.getValue()));
            p.setResultat(txtParticipationResultat.getText().trim());
            Integer employeId   = resolveUserId(txtParticipationEmployeId.getText(), "EMPLOYE");
            Integer formationId = resolveFormationId(txtParticipationFormationId.getText());
            if (employeId == null)   { showError("Employe introuvable."); return; }
            if (formationId == null) { showError("Formation introuvable."); return; }
            if (crudParticipant.existsByEmployeAndFormation(employeId, formationId, p.getId())) {
                showError("Employe déjà inscrit."); return;
            }
            p.setEmployeId(employeId);
            p.setFormationId(formationId);
            if (crudParticipant.modifier(p)) refreshData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML public void deleteParticipation() {
        if (txtParticipationId.getText().isBlank()) { showError("Selectionnez une participation"); return; }
        if (crudParticipant.supprimer(Integer.parseInt(txtParticipationId.getText().trim()))) {
            refreshData(); clearParticipationForm();
        }
    }

    @FXML public void clearParticipationForm() {
        txtParticipationId.clear(); dpParticipationDate.setValue(null);
        txtParticipationResultat.clear(); txtParticipationEmployeId.clear();
        txtParticipationFormationId.clear();
        clearSelection(tableParticipations, selectedParticipantCard);
        selectedParticipant = null; selectedParticipantCard = null;
    }

    @FXML public void accepter() {
        if (selectedCandidature != null && serviceCandidature.updateStatut(selectedCandidature.getId(), "ACCEPTEE"))
            refreshData();
    }

    @FXML public void refuser() {
        if (selectedCandidature != null && serviceCandidature.updateStatut(selectedCandidature.getId(), "REFUSEE"))
            refreshData();
    }
    private void afficherErreursPublication(List<String> erreurs, String titre, String contenu) {
        // Bordure rouge sur les champs invalides
        List<String> erreursTitre   = PublicationValidator.validerTitre(titre);
        List<String> erreursContenu = PublicationValidator.validerContenuPublication(contenu);

        if (!erreursTitre.isEmpty() && txtPublicationTitre != null) {
            txtPublicationTitre.setStyle(
                    "-fx-border-color:#ef4444;-fx-border-width:2;" +
                            "-fx-border-radius:6;-fx-background-radius:6;"
            );
        }
        if (!erreursContenu.isEmpty() && txtPublicationContenu != null) {
            txtPublicationContenu.setStyle(
                    "-fx-border-color:#ef4444;-fx-border-width:2;" +
                            "-fx-border-radius:6;-fx-background-radius:6;"
            );
        }

        // Message d'erreur
        showError(PublicationValidator.formaterErreurs(erreurs));
    }

    private void resetStylesPublication() {
        if (txtPublicationTitre != null)
            txtPublicationTitre.setStyle("");
        if (txtPublicationContenu != null)
            txtPublicationContenu.setStyle("");
    }

    // ══════════════════════════════════════════════════════════
    // KPI & CHARTS
    // ══════════════════════════════════════════════════════════
    private void refreshKpiAndCharts() {
        refreshModernKpis();
        refreshModernCharts();
        renderOfferFillAndPostesPanels();
    }

    private void refreshModernKpis() {
        if (lblKpiTotalCandidatures == null) return;
        List<Candidature> candidatures = serviceCandidature.getAll();
        int total    = candidatures.size();
        long accepted = candidatures.stream().filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut())).count();
        double rate  = total == 0 ? 0 : (accepted * 100.0 / total);
        List<Candidature> closed = candidatures.stream()
                .filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut()) || "REFUSEE".equalsIgnoreCase(c.getStatut()))
                .collect(Collectors.toList());
        double avgDays = closed.isEmpty() ? 0 : closed.stream()
                .mapToLong(c -> Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(c.getDateCandidature(), LocalDate.now())))
                .average().orElse(0);
        Map<Integer, Long> acceptedByOffre = candidatures.stream()
                .filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut()))
                .collect(Collectors.groupingBy(Candidature::getOffreId, Collectors.counting()));
        int activeOffers = (int) serviceOffre.getAll().stream()
                .filter(o -> acceptedByOffre.getOrDefault(o.getId(), 0L) < o.getNombrePostes()).count();
        lblKpiTotalCandidatures.setText(String.valueOf(total));
        lblKpiTauxAcceptation.setText(String.format(Locale.US, "%.1f%%", rate));
        lblKpiTempsMoyen.setText(String.format(Locale.US, "%.1f jours", avgDays));
        lblKpiOffresActives.setText(String.valueOf(activeOffers));
        if (lblKpiEmployesActifs != null)
            lblKpiEmployesActifs.setText(String.valueOf(serviceUser.countByRole("EMPLOYE")));
    }

    private void refreshModernCharts() {
        if (chartCandidaturesDept == null) return;
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
        byDept.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> chartCandidaturesDept.getData().add(new PieChart.Data(e.getKey(), e.getValue())));
        if (chartEvolutionLine == null || chartEvolutionBar == null) return;
        Map<YearMonth, List<Candidature>> byMonth = candidatures.stream()
                .collect(Collectors.groupingBy(c -> YearMonth.from(c.getDateCandidature()), LinkedHashMap::new, Collectors.toList()));
        List<YearMonth> months = new ArrayList<>(byMonth.keySet());
        months.sort(Comparator.naturalOrder());
        if (months.size() > 6) months = months.subList(months.size() - 6, months.size());
        XYChart.Series<String, Number> barSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        barSeries.setName("Total Candidatures");
        lineSeries.setName("Taux d'Acceptation");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yy", Locale.FRENCH);
        for (YearMonth month : months) {
            List<Candidature> mRows = byMonth.getOrDefault(month, List.of());
            int t = mRows.size();
            long a = mRows.stream().filter(c -> "ACCEPTEE".equalsIgnoreCase(c.getStatut())).count();
            double r = t == 0 ? 0 : a * 100.0 / t;
            String label = month.format(monthFmt);
            barSeries.getData().add(new XYChart.Data<>(label, t));
            lineSeries.getData().add(new XYChart.Data<>(label, r));
        }
        chartEvolutionBar.getData().setAll(barSeries);
        chartEvolutionLine.getData().setAll(lineSeries);
    }

    private void renderOfferFillAndPostesPanels() {
        if (tableOffresFillRate == null || tablePostesPourvoir == null) return;
        tableOffresFillRate.getChildren().clear();
        tablePostesPourvoir.getChildren().clear();
        List<Candidature> allCandidatures = serviceCandidature.getAll();
        for (OffreEmploi o : serviceOffre.getAll()) {
            int accepted  = (int) allCandidatures.stream()
                    .filter(c -> c.getOffreId() == o.getId() && "ACCEPTEE".equalsIgnoreCase(c.getStatut())).count();
            int total     = Math.max(0, o.getNombrePostes());
            int remaining = Math.max(0, total - accepted);
            double fillRate = total == 0 ? 0 : accepted * 100.0 / total;
            tableOffresFillRate.getChildren().add(buildCard(o.getTitre(),
                    o.getDepartement() + " | " + accepted + "/" + total + " (" + String.format(Locale.US, "%.0f%%", fillRate) + ")"));
            tablePostesPourvoir.getChildren().add(buildCard(o.getTitre(), "Postes restants: " + remaining));
        }
    }

    // ══════════════════════════════════════════════════════════
    // MODERN CARDS (flowpane)
    // ══════════════════════════════════════════════════════════
    private void renderModernUsers() {
        if (flowUsers == null) return;
        String q      = lower(txtSearchUsers);
        String global = lower(txtGlobalSearch);
        String role   = cbFilterRole == null ? null : cbFilterRole.getValue();
        List<User> filtered = serviceUser.getAll().stream()
                .filter(u -> role == null || role.isBlank() || role.equalsIgnoreCase(u.getRole()))
                .filter(u -> matchesUserSearch(u, q) && matchesUserSearch(u, global))
                .sorted(Comparator.comparingInt(User::getId).reversed())
                .collect(Collectors.toList());
        flowUsers.getChildren().setAll(pageSliceUsers(filtered).stream()
                .map(this::buildModernUserCard).collect(Collectors.toList()));
        updatePageLabel(lblUsersPage, usersPage, filtered.size(), USERS_PAGE_SIZE);
    }

    private void renderModernOffres() {
        if (flowOffres == null) return;
        String q      = lower(txtSearchOffres);
        String global = lower(txtGlobalSearch);
        String dept   = cbFilterDepartement == null ? null : cbFilterDepartement.getValue();
        List<OffreEmploi> filtered = serviceOffre.getAll().stream()
                .filter(o -> dept == null || dept.isBlank() || dept.equalsIgnoreCase(o.getDepartement()))
                .filter(o -> matchesOffreSearch(o, q) && matchesOffreSearch(o, global))
                .sorted(Comparator.comparingInt(OffreEmploi::getId).reversed())
                .collect(Collectors.toList());
        flowOffres.getChildren().setAll(pageSliceOffres(filtered).stream()
                .map(this::buildModernOffreCard).collect(Collectors.toList()));
        updatePageLabel(lblOffresPage, offresPage, filtered.size(), OFFRES_PAGE_SIZE);
    }

    private void renderModernCandidatures() {
        if (flowCandidatures == null) return;
        String q      = lower(txtSearchCandidatures);
        String global = lower(txtGlobalSearch);
        String status = cbFilterStatut == null ? null : cbFilterStatut.getValue();
        Map<Integer, User> usersById       = serviceUser.getAll().stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, OffreEmploi> offersById = serviceOffre.getAll().stream().collect(Collectors.toMap(OffreEmploi::getId, o -> o));
        List<Candidature> filtered = serviceCandidature.getAll().stream()
                .filter(c -> status == null || status.isBlank() || status.equalsIgnoreCase(c.getStatut()))
                .filter(c -> matchesCandidatureSearch(c, usersById, offersById, q)
                        && matchesCandidatureSearch(c, usersById, offersById, global))
                .sorted(Comparator.comparingInt(Candidature::getId).reversed())
                .collect(Collectors.toList());
        flowCandidatures.getChildren().setAll(pageSliceCandidatures(filtered).stream()
                .map(c -> buildModernCandidatureCard(c, usersById, offersById)).collect(Collectors.toList()));
        updatePageLabel(lblCandidaturesPage, candidaturesPage, filtered.size(), CANDIDATURES_PAGE_SIZE);
    }

    private VBox buildModernUserCard(User user) {
        Label title   = new Label(user.getNom() + " " + user.getPrenom());
        title.getStyleClass().add("entity-card-title");
        Label email   = new Label(user.getEmail());
        email.getStyleClass().add("entity-card-meta");
        Label role    = buildRoleBadge(user.getRole());
        Button edit   = new Button("Edit");
        edit.setOnAction(e -> openUserDialog(user));
        Button delete = new Button("Delete");
        delete.setOnAction(e -> { serviceUser.delete(user.getId()); refreshData(); });
        HBox actions  = new HBox(8, edit, delete);
        actions.getStyleClass().add("card-actions");
        VBox card = new VBox(8, title, email, role, actions);
        card.getStyleClass().add("entity-card");
        card.setPrefWidth(340);
        return card;
    }

    private VBox buildModernOffreCard(OffreEmploi offre) {
        Label title = new Label(offre.getTitre());
        title.getStyleClass().add("entity-card-title");
        int accepted = (int) serviceCandidature.getAll().stream()
                .filter(c -> c.getOffreId() == offre.getId() && "ACCEPTEE".equalsIgnoreCase(c.getStatut())).count();
        double fillRate = offre.getNombrePostes() <= 0 ? 0 : (accepted * 100.0 / offre.getNombrePostes());
        Label meta = new Label(offre.getDepartement() + " | " + offre.getTypeContrat()
                + " | " + accepted + "/" + offre.getNombrePostes()
                + " (" + String.format(Locale.US, "%.0f%%", fillRate) + ")");
        meta.getStyleClass().add("entity-card-meta");
        Label date = new Label("Publication: " + offre.getDatePublication());
        date.getStyleClass().add("entity-card-meta");
        Button edit   = new Button("Edit");
        edit.setOnAction(e -> openOffreDialog(offre));
        Button delete = new Button("Delete");
        delete.setOnAction(e -> { serviceOffre.delete(offre.getId()); refreshData(); });
        HBox actions  = new HBox(8, edit, delete);
        actions.getStyleClass().add("card-actions");
        VBox card = new VBox(8, title, meta, date, actions);
        card.getStyleClass().add("entity-card");
        card.setPrefWidth(340);
        return card;
    }

    private VBox buildModernCandidatureCard(Candidature c, Map<Integer, User> usersById, Map<Integer, OffreEmploi> offersById) {
        User u        = usersById.get(c.getCandidatId());
        OffreEmploi o = offersById.get(c.getOffreId());
        String nom   = u == null ? ("Candidat #" + c.getCandidatId()) : (u.getNom() + " " + u.getPrenom());
        String offre = o == null ? ("Offre #" + c.getOffreId()) : o.getTitre();
        Label title  = new Label(nom);
        title.getStyleClass().add("entity-card-title");
        Label offerLabel = new Label("Poste: " + offre);
        offerLabel.getStyleClass().add("entity-card-meta");
        Label date   = new Label("Date: " + c.getDateCandidature());
        date.getStyleClass().add("entity-card-meta");
        Label status = buildStatusBadge(c.getStatut());
        Button viewCv = new Button("View CV");
        viewCv.setOnAction(e -> setPageMessage(c.getCheminCv() == null ? "CV non disponible" : c.getCheminCv(), false));
        Button update = new Button("Update Status");
        update.setOnAction(e -> {
            String next = "EN_ATTENTE";
            if ("EN_ATTENTE".equalsIgnoreCase(c.getStatut())) next = "ACCEPTEE";
            else if ("ACCEPTEE".equalsIgnoreCase(c.getStatut())) next = "REFUSEE";
            serviceCandidature.updateStatut(c.getId(), next);
            refreshData();
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> { serviceCandidature.supprimer(c.getId()); refreshData(); });
        HBox actions = new HBox(8, viewCv, update, delete);
        actions.getStyleClass().add("card-actions");
        VBox card = new VBox(8, title, offerLabel, status, date, actions);
        card.getStyleClass().add("entity-card");
        card.setPrefWidth(340);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    // RENDER CARDS (tableXxx VBox)
    // ══════════════════════════════════════════════════════════
    private void renderUserCards() {
        tableUsers.getChildren().clear();
        clearSelection(tableUsers, selectedUserCard);
        selectedUser = null; selectedUserCard = null;
        List<User> allUsers = serviceUser.getAll();
        Map<Integer, User> usersById = allUsers.stream().collect(Collectors.toMap(User::getId, u -> u));
        for (User user : allUsers) {
            String managerDisplay = "-";
            if (user.getManagerId() != null) {
                User manager = usersById.get(user.getManagerId());
                managerDisplay = manager != null ? manager.getNom() + " " + manager.getPrenom() : "Manager inconnu";
            }
            VBox card = buildCard(
                    user.getNom() + " " + user.getPrenom(),
                    user.getEmail() + " | role: " + user.getRole() + " | manager: " + managerDisplay
            );
            card.setOnMouseClicked(event -> {
                selectCard(tableUsers, selectedUserCard, card);
                selectedUserCard = card; selectedUser = user;
                txtUserId.setText(String.valueOf(user.getId()));
                txtNom.setText(user.getNom()); txtPrenom.setText(user.getPrenom());
                txtEmail.setText(user.getEmail()); txtMdp.setText(user.getMdp());
                txtManagerId.setText(user.getManagerId() == null ? "" : String.valueOf(user.getManagerId()));
                cbRole.setValue(user.getRole());
            });
            tableUsers.getChildren().add(card);
        }
    }

    private void renderCandidatCards() {
        tableCandidats.getChildren().clear();
        clearSelection(tableCandidats, selectedCandidatCard);
        selectedCandidat = null; selectedCandidatCard = null;
        for (Candidat candidat : serviceCandidat.getAll()) {
            VBox card = buildCard(candidat.getNom() + " " + candidat.getPrenom(),
                    candidat.getEmail() + " | role: " + candidat.getRole());
            card.setOnMouseClicked(event -> {
                selectCard(tableCandidats, selectedCandidatCard, card);
                selectedCandidatCard = card; selectedCandidat = candidat;
                txtCandidatId.setText(candidat.getNom() + " " + candidat.getPrenom());
            });
            tableCandidats.getChildren().add(card);
        }
    }

    private void renderOffreCards() {
        tableOffres.getChildren().clear();
        clearSelection(tableOffres, selectedOffreCard);
        selectedOffre = null; selectedOffreCard = null;
        for (OffreEmploi offre : serviceOffre.getAll()) {
            VBox card = buildCard(offre.getTitre(),
                    offre.getDepartement() + " | " + offre.getTypeContrat()
                            + " | postes: " + offre.getNombrePostes() + " | date: " + offre.getDatePublication());
            card.setOnMouseClicked(event -> {
                selectCard(tableOffres, selectedOffreCard, card);
                selectedOffreCard = card; selectedOffre = offre;
                txtOffreId.setText(String.valueOf(offre.getId()));
                txtTitre.setText(offre.getTitre());
                txtDepartement.setValue(offre.getDepartement());
                txtTypeContrat.setText(offre.getTypeContrat());
                txtNombrePostes.setText(String.valueOf(offre.getNombrePostes()));
                dpDatePublication.setValue(offre.getDatePublication());
                txtAdminId.setText(String.valueOf(offre.getAdminId()));
                if (txtTopCvOffreId != null) txtTopCvOffreId.setText(String.valueOf(offre.getId()));
            });
            tableOffres.getChildren().add(card);
        }
    }

    private void renderCandidatureCards() {
        tableCandidatures.getChildren().clear();
        clearSelection(tableCandidatures, selectedCandidatureCard);
        selectedCandidature = null; selectedCandidatureCard = null;
        Map<Integer, User> usersById       = serviceUser.getAll().stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, OffreEmploi> offresById = serviceOffre.getAll().stream().collect(Collectors.toMap(OffreEmploi::getId, o -> o));
        for (Candidature candidature : serviceCandidature.getAll()) {
            User candidat     = usersById.get(candidature.getCandidatId());
            OffreEmploi offre = offresById.get(candidature.getOffreId());
            VBox card = buildCard("Candidature",
                    "candidat: " + (candidat == null ? "inconnu" : candidat.getNom() + " " + candidat.getPrenom())
                            + " | offre: " + (offre == null ? "inconnue" : offre.getTitre())
                            + " | date: " + candidature.getDateCandidature()
                            + " | statut: " + candidature.getStatut());
            card.setOnMouseClicked(event -> {
                selectCard(tableCandidatures, selectedCandidatureCard, card);
                selectedCandidatureCard = card; selectedCandidature = candidature;
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
        selectedConge = null; selectedCongeCard = null;
        for (Absence conge : serviceAbsence.getCongesAdmin()) {
            String employe = conge.getEmployeNom() == null ? "Employe inconnu" : conge.getEmployeNom();
            VBox card = buildCard(employe,
                    "du " + conge.getDateDebut() + " au " + conge.getDateFin() + " | statut: " + conge.getStatut());
            card.setOnMouseClicked(event -> {
                selectCard(tableConges, selectedCongeCard, card);
                selectedCongeCard = card; selectedConge = conge;
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
        selectedAbsenceAdmin = null; selectedAbsenceAdminCard = null;
        for (Absence absence : serviceAbsence.getAbsencesAdmin()) {
            String employe = absence.getEmployeNom() == null ? "Employe inconnu" : absence.getEmployeNom();
            VBox card = buildCard(employe + " | " + absence.getTypeAbsence(),
                    "du " + absence.getDateDebut() + " au " + absence.getDateFin() + " | statut: " + absence.getStatut());
            card.setOnMouseClicked(event -> {
                selectCard(tableAbsencesAdmin, selectedAbsenceAdminCard, card);
                selectedAbsenceAdminCard = card; selectedAbsenceAdmin = absence;
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
        selectedFormation = null; selectedFormationCard = null;
        Map<Integer, User> usersById = serviceUser.getAll().stream().collect(Collectors.toMap(User::getId, u -> u));
        for (Formation formation : crudFormation.afficherAll()) {
            User admin = usersById.get(formation.getAdminId());
            String adminLabel = admin == null ? "Admin RH inconnu" : (admin.getNom() + " " + admin.getPrenom());
            VBox card = buildCard(formation.getSujet() + " | " + formation.getType(),
                    "formateur: " + formation.getFormateur()
                            + " | date: " + formation.getDateDebut()
                            + " | duree: " + formation.getDuree() + "h"
                            + " | lieu: " + formation.getLocalisation()
                            + " | RH: " + adminLabel);
            card.setOnMouseClicked(event -> {
                selectCard(tableFormations, selectedFormationCard, card);
                selectedFormationCard = card; selectedFormation = formation;
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
        selectedParticipant = null; selectedParticipantCard = null;
        for (Participant participant : crudParticipant.afficherAll()) {
            String employeLabel  = participant.getNomEmploye()  == null ? "Employe inconnu"   : participant.getNomEmploye();
            String formationLabel = participant.getNomFormation() == null ? "Formation inconnue" : participant.getNomFormation();
            VBox card = buildCard(employeLabel + " | " + formationLabel,
                    "inscription: " + participant.getDateInscription() + " | resultat: " + participant.getResultat());
            card.setOnMouseClicked(event -> {
                selectCard(tableParticipations, selectedParticipantCard, card);
                selectedParticipantCard = card; selectedParticipant = participant;
                txtParticipationId.setText(String.valueOf(participant.getId()));
                dpParticipationDate.setValue(participant.getDateInscription() == null ? null : participant.getDateInscription().toLocalDate());
                txtParticipationResultat.setText(participant.getResultat());
                txtParticipationEmployeId.setText(employeLabel);
                txtParticipationFormationId.setText(formationLabel);
            });
            tableParticipations.getChildren().add(card);
        }
    }

    // ══════════════════════════════════════════════════════════
    // DIALOGS
    // ══════════════════════════════════════════════════════════
    @FXML public void addUserQuick()         { openUserDialog(null); }
    @FXML public void addOffreQuick()        { openOffreDialog(null); }
    @FXML public void addCandidatureQuick()  { openCandidatureDialog(); }

    private void openUserDialog(User existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Utilisateur" : "Modifier Utilisateur");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        TextField nom = new TextField(existing == null ? "" : existing.getNom());
        TextField prenom = new TextField(existing == null ? "" : existing.getPrenom());
        TextField email  = new TextField(existing == null ? "" : existing.getEmail());
        PasswordField mdp = new PasswordField();
        if (existing != null && existing.getMdp() != null) mdp.setText(existing.getMdp());
        ComboBox<String> role = new ComboBox<>();
        role.getItems().setAll("ADMIN_RH", "MANAGER", "EMPLOYE", "CANDIDAT");
        role.setValue(existing == null ? "EMPLOYE" : existing.getRole());
        TextField managerId = new TextField(existing == null || existing.getManagerId() == null ? "" : String.valueOf(existing.getManagerId()));
        VBox form = new VBox(8,
                new Label("Nom"), nom, new Label("Prenom"), prenom,
                new Label("Email"), email, new Label("Mot de passe"), mdp,
                new Label("Role"), role, new Label("Manager ID (optionnel)"), managerId);
        form.setPrefWidth(380);
        dialog.getDialogPane().setContent(form);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) return;
            try {
                User u = existing == null ? new User() : existing;
                u.setNom(nom.getText().trim()); u.setPrenom(prenom.getText().trim());
                u.setEmail(email.getText().trim()); u.setMdp(mdp.getText().trim());
                u.setRole(role.getValue());
                String mgrText = managerId.getText() == null ? "" : managerId.getText().trim();
                u.setManagerId(mgrText.isBlank() ? null : Integer.parseInt(mgrText));
                boolean ok = existing == null ? serviceUser.ajouter(u) : serviceUser.update(u);
                if (ok) refreshData();
                else showError("Operation utilisateur echouee.");
            } catch (Exception ex) { showError("Erreur: " + ex.getMessage()); }
        });
    }

    private void openOffreDialog(OffreEmploi existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Offre" : "Modifier Offre");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        TextField titre       = new TextField(existing == null ? "" : existing.getTitre());
        TextField description = new TextField(existing == null ? "" : existing.getDescription());
        TextField departement = new TextField(existing == null ? "" : existing.getDepartement());
        TextField typeContrat = new TextField(existing == null ? "" : existing.getTypeContrat());
        TextField nbPostes    = new TextField(existing == null ? "1" : String.valueOf(existing.getNombrePostes()));
        DatePicker datePub    = new DatePicker(existing == null ? LocalDate.now() : existing.getDatePublication());
        TextField adminId     = new TextField(existing == null
                ? (Session.getUser() == null ? "" : String.valueOf(Session.getUser().getId()))
                : String.valueOf(existing.getAdminId()));
        VBox form = new VBox(8,
                new Label("Titre"), titre, new Label("Description"), description,
                new Label("Departement"), departement, new Label("Type contrat"), typeContrat,
                new Label("Nombre postes"), nbPostes, new Label("Date publication"), datePub,
                new Label("Admin ID"), adminId);
        form.setPrefWidth(420);
        dialog.getDialogPane().setContent(form);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) return;
            try {
                OffreEmploi o = existing == null ? new OffreEmploi() : existing;
                o.setTitre(titre.getText().trim()); o.setDescription(description.getText().trim());
                o.setDepartement(departement.getText().trim()); o.setTypeContrat(typeContrat.getText().trim());
                o.setNombrePostes(Integer.parseInt(nbPostes.getText().trim()));
                o.setDatePublication(datePub.getValue() == null ? LocalDate.now() : datePub.getValue());
                o.setAdminId(Integer.parseInt(adminId.getText().trim()));
                boolean ok = existing == null ? serviceOffre.ajouter(o) : serviceOffre.update(o);
                if (ok) refreshData(); else showError("Operation offre echouee.");
            } catch (Exception ex) { showError("Erreur: " + ex.getMessage()); }
        });
    }

    private void openCandidatureDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Candidature");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        TextField candidatId = new TextField();
        TextField offreId    = new TextField();
        DatePicker date      = new DatePicker(LocalDate.now());
        ComboBox<String> statut = new ComboBox<>();
        statut.getItems().setAll("EN_ATTENTE", "ACCEPTEE", "REFUSEE");
        statut.setValue("EN_ATTENTE");
        TextField cvPath = new TextField();
        VBox form = new VBox(8,
                new Label("Candidat ID"), candidatId, new Label("Offre ID"), offreId,
                new Label("Date candidature"), date, new Label("Statut"), statut,
                new Label("Chemin CV (optionnel)"), cvPath);
        form.setPrefWidth(360);
        dialog.getDialogPane().setContent(form);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) return;
            try {
                Candidature c = new Candidature();
                c.setCandidatId(Integer.parseInt(candidatId.getText().trim()));
                c.setOffreId(Integer.parseInt(offreId.getText().trim()));
                c.setDateCandidature(date.getValue() == null ? LocalDate.now() : date.getValue());
                c.setStatut(statut.getValue());
                c.setCheminCv(cvPath.getText() == null ? "" : cvPath.getText().trim());
                if (serviceCandidature.ajouter(c)) refreshData();
                else showError("Ajout candidature echoue.");
            } catch (Exception ex) { showError("Erreur: " + ex.getMessage()); }
        });
    }

    // ══════════════════════════════════════════════════════════
    // PAGINATION
    // ══════════════════════════════════════════════════════════
    @FXML public void prevUsersPage()        { usersPage = Math.max(0, usersPage - 1); renderModernUsers(); }
    @FXML public void nextUsersPage()        { usersPage++; renderModernUsers(); }
    @FXML public void prevOffresPage()       { offresPage = Math.max(0, offresPage - 1); renderModernOffres(); }
    @FXML public void nextOffresPage()       { offresPage++; renderModernOffres(); }
    @FXML public void prevCandidaturesPage() { candidaturesPage = Math.max(0, candidaturesPage - 1); renderModernCandidatures(); }
    @FXML public void nextCandidaturesPage() { candidaturesPage++; renderModernCandidatures(); }

    // ══════════════════════════════════════════════════════════
    // BADGE / REPUTATION HELPERS
    // ══════════════════════════════════════════════════════════
    private String getBadgeLabel(String badge) {
        return switch (badge) {
            case "🏅 ENGAGE" -> "🏅 Engagé";
            case "🎖️ ACTIF"  -> "🎖️ Actif";
            default           -> "🎗️ Nouveau";
        };
    }

    private String getBadgeStyle(String badge) {
        String color = switch (badge) {
            case "🏅 ENGAGE" ->
                    "-fx-background-color:linear-gradient(to right,#fbbf24,#f59e0b);" +
                            "-fx-text-fill:white;-fx-border-color:#d97706;";
            case "🎖️ ACTIF" ->
                    "-fx-background-color:linear-gradient(to right,#60a5fa,#3b82f6);" +
                            "-fx-text-fill:white;-fx-border-color:#2563eb;";
            default ->
                    "-fx-background-color:linear-gradient(to right,#d1d5db,#9ca3af);" +
                            "-fx-text-fill:white;-fx-border-color:#6b7280;";
        };
        return color +
                "-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 10;" +
                "-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;";
    }

    private Label makeCommAvatar(String name, int size, int fontSize) {
        String initials = "?";
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+");
            initials = parts.length >= 2
                    ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
                    : ("" + parts[0].charAt(0)).toUpperCase();
        }
        String[] colors = {"#20c997","#1f2937","#f59e0b","#3b82f6","#8b5cf6","#ec4899","#ef4444","#06b6d4"};
        String color = colors[Math.abs(name == null ? 0 : name.hashCode()) % colors.length];
        Label av = new Label(initials);
        av.setStyle(
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:" + fontSize + "px;" +
                        "-fx-min-width:" + size + ";-fx-min-height:" + size + ";" +
                        "-fx-max-width:" + size + ";-fx-max-height:" + size + ";" +
                        "-fx-background-radius:50;-fx-alignment:center;"
        );
        return av;
    }

    // ══════════════════════════════════════════════════════════
    // GENERIC HELPERS
    // ══════════════════════════════════════════════════════════
    private Label buildRoleBadge(String role) {
        String value = role == null ? "UNKNOWN" : role.toUpperCase(Locale.ROOT);
        Label label  = new Label(value);
        label.getStyleClass().add("badge");
        if (value.contains("ADMIN"))   label.getStyleClass().add("badge-admin");
        else if (value.contains("MANAGER")) label.getStyleClass().add("badge-manager");
        else if (value.contains("EMPLOYE")) label.getStyleClass().add("badge-employe");
        else label.getStyleClass().add("badge-candidat");
        return label;
    }

    private Label buildStatusBadge(String status) {
        String value = status == null ? "EN_ATTENTE" : status.toUpperCase(Locale.ROOT);
        Label label  = new Label(value);
        label.getStyleClass().add("badge");
        if ("ACCEPTEE".equals(value))    label.getStyleClass().add("badge-accepted");
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
        int to   = Math.min(list.size(), from + USERS_PAGE_SIZE);
        return from >= to ? List.of() : list.subList(from, to);
    }

    private List<OffreEmploi> pageSliceOffres(List<OffreEmploi> list) {
        int maxPage = Math.max(0, (list.size() - 1) / OFFRES_PAGE_SIZE);
        offresPage = Math.min(offresPage, maxPage);
        int from = offresPage * OFFRES_PAGE_SIZE;
        int to   = Math.min(list.size(), from + OFFRES_PAGE_SIZE);
        return from >= to ? List.of() : list.subList(from, to);
    }

    private List<Candidature> pageSliceCandidatures(List<Candidature> list) {
        int maxPage = Math.max(0, (list.size() - 1) / CANDIDATURES_PAGE_SIZE);
        candidaturesPage = Math.min(candidaturesPage, maxPage);
        int from = candidaturesPage * CANDIDATURES_PAGE_SIZE;
        int to   = Math.min(list.size(), from + CANDIDATURES_PAGE_SIZE);
        return from >= to ? List.of() : list.subList(from, to);
    }

    private void updatePageLabel(Label label, int currentPage, int totalRows, int pageSize) {
        if (label == null) return;
        int totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));
        label.setText("Page " + (currentPage + 1) + " / " + totalPages);
    }

    private Integer parseManagerId() {
        if (txtManagerId == null || txtManagerId.getText() == null || txtManagerId.getText().isBlank()) return null;
        return resolveUserId(txtManagerId.getText(), "MANAGER");
    }

    private Integer resolveUserId(String rawInput, String requiredRoleContains) {
        if (rawInput == null) return null;
        String input = rawInput.trim();
        if (input.isBlank()) return null;
        if (input.matches("\\d+")) return Integer.parseInt(input);
        String normalized = input.toLowerCase();
        for (User u : serviceUser.getAll()) {
            if (requiredRoleContains != null) {
                String role = u.getRole() == null ? "" : u.getRole().toUpperCase();
                if (!role.contains(requiredRoleContains.toUpperCase())) continue;
            }
            String fullName = (u.getNom() + " " + u.getPrenom()).trim().toLowerCase();
            String email    = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
            if (normalized.equals(fullName) || normalized.equals(email)) return u.getId();
        }
        return null;
    }

    private Integer resolveOffreId(String rawInput) {
        if (rawInput == null) return null;
        String input = rawInput.trim();
        if (input.isBlank()) return null;
        if (input.matches("\\d+")) return Integer.parseInt(input);
        String normalized = input.toLowerCase();
        for (OffreEmploi offre : serviceOffre.getAll()) {
            String titre = offre.getTitre() == null ? "" : offre.getTitre().toLowerCase();
            if (normalized.equals(titre)) return offre.getId();
        }
        return null;
    }

    private Integer resolveFormationId(String rawInput) {
        if (rawInput == null) return null;
        String input = rawInput.trim();
        if (input.isBlank()) return null;
        if (input.matches("\\d+")) return Integer.parseInt(input);
        Integer id = crudFormation.findIdBySujet(input);
        if (id != null) return id;
        String normalized = input.toLowerCase();
        for (Formation formation : crudFormation.afficherAll()) {
            String sujet = formation.getSujet() == null ? "" : formation.getSujet().toLowerCase();
            if (normalized.equals(sujet)) return formation.getId();
        }
        return null;
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
        if (newCard != null && !newCard.getStyleClass().contains("entity-card-selected"))
            newCard.getStyleClass().add("entity-card-selected");
    }

    private void clearSelection(VBox container, Node selectedCard) {
        if (container == null || selectedCard == null) return;
        selectedCard.getStyleClass().remove("entity-card-selected");
    }

    private void showError(String message) { setPageMessage(message, true); }

    private void setPageMessage(String message, boolean isError) {
        if (lblPageMessage == null) {
            if (lblInfo != null) {
                lblInfo.setText(message == null ? "" : message);
                lblInfo.setStyle(isError
                        ? "-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:5 14;-fx-background-radius:20;"
                        : "-fx-background-color:#d1fae5;-fx-text-fill:#065f46;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:5 14;-fx-background-radius:20;");
            }
            return;
        }
        lblPageMessage.setText(message == null ? "" : message);
        lblPageMessage.setStyle(isError
                ? "-fx-text-fill:#d64545;-fx-font-weight:700;"
                : "-fx-text-fill:#2f855a;-fx-font-weight:700;");
    }

    private void navigateTo(ActionEvent event, String fxml) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxml))));
            stage.show();
        } catch (IOException e) { showError("Navigation impossible: " + e.getMessage()); }
    }

    @FXML
    public void openChatbot(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/chatbot.fxml"))));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private VBox buildBadgeSection(String badgeLabel, String color, String lightBg, String borderColor,
                                   List<User> users, ReputationService reputationService) {
        VBox section = new VBox(0);
        section.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:" + borderColor + ";-fx-border-width:1;" +
                        "-fx-border-radius:10;-fx-background-radius:10;"
        );
        VBox.setMargin(section, new Insets(0, 0, 12, 0));

        // Header de la section
        HBox sectionHeader = new HBox(10);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(10, 16, 10, 16));
        sectionHeader.setStyle("-fx-background-color:" + lightBg + ";-fx-background-radius:10 10 0 0;");

        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill:" + color + ";-fx-font-size:12px;");

        Label titleLbl = new Label(badgeLabel);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label countLbl = new Label(users.size() + " employe(s)");
        countLbl.setStyle(
                "-fx-background-color:" + color + ";-fx-text-fill:white;" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:3 10;-fx-background-radius:20;"
        );
        sectionHeader.getChildren().addAll(dot, titleLbl, sp, countLbl);
        section.getChildren().add(sectionHeader);

        if (users.isEmpty()) {
            Label empty = new Label("Aucun employe dans cette categorie.");
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#a0aec0;-fx-padding:12 16;");
            section.getChildren().add(empty);
            return section;
        }

        // Calcul score max pour les barres
        int maxScore = users.stream()
                .mapToInt(emp -> {
                    Reputation r = reputationService.getByUserId(emp.getId());
                    return r != null ? r.getTotalScore() : 0;
                }).max().orElse(1);
        if (maxScore == 0) maxScore = 1;

        for (int i = 0; i < users.size(); i++) {
            User emp = users.get(i);
            Reputation rep = reputationService.getByUserId(emp.getId());
            int score = rep != null ? rep.getTotalScore() : 0;
            double ratio = Math.min(1.0, (double) score / maxScore);

            HBox empRow = new HBox(12);
            empRow.setAlignment(Pos.CENTER_LEFT);
            empRow.setPadding(new Insets(10, 16, 10, 16));
            empRow.setStyle(i % 2 == 0
                    ? "-fx-background-color:#fafafa;"
                    : "-fx-background-color:white;"
            );

            // Avatar
            Label avatar = makeCommAvatar(emp.getNom() + " " + emp.getPrenom(), 34, 11);

            // Infos nom + email
            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label nameLbl = new Label(emp.getNom() + " " + emp.getPrenom());
            nameLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1f2937;");
            Label emailLbl = new Label(emp.getEmail() != null ? emp.getEmail() : "");
            emailLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
            info.getChildren().addAll(nameLbl, emailLbl);

            // Barre + score
            VBox barBox = new VBox(4);
            barBox.setAlignment(Pos.CENTER_RIGHT);
            barBox.setMinWidth(180);

            HBox barBg = new HBox();
            barBg.setStyle(
                    "-fx-background-color:#e5e7eb;" +
                            "-fx-background-radius:4;-fx-min-height:6;-fx-max-height:6;"
            );
            barBg.setPrefWidth(180);

            Region barFill = new Region();
            barFill.setPrefWidth(ratio * 180);
            barFill.setMaxWidth(ratio * 180);
            barFill.setStyle(
                    "-fx-background-color:" + color + ";" +
                            "-fx-background-radius:4;-fx-min-height:6;-fx-max-height:6;"
            );
            barBg.getChildren().add(barFill);

            Label scoreLbl = new Label(score + " pts");
            scoreLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
            barBox.getChildren().addAll(barBg, scoreLbl);

            empRow.getChildren().addAll(avatar, info, barBox);

            // Séparateur entre lignes
            section.getChildren().add(empRow);
            if (i < users.size() - 1) {
                Region sepLine = new Region();
                sepLine.setStyle("-fx-background-color:#f3f4f6;-fx-min-height:1;-fx-max-height:1;");
                sepLine.setMaxWidth(Double.MAX_VALUE);
                section.getChildren().add(sepLine);
            }
        }
        return section;
    }

}