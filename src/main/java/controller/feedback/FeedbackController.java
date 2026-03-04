package controller.feedback;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.feedback.Feedback;
import models.feedback.FeedbackComment;
import models.feedback.User;
import services.feedback.ServiceFeedback;
import services.feedback.ServiceFeedbackComment;
import controller.congesAbsences.DemandeCongeController;
import utils.Session;
import utils.SlackWebhookClient;
import utils.SentimentApiClient;
import utils.TrelloApiClient;
import utils.ApiConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class FeedbackController {

    @FXML private Label lblTitleFeedback;
    @FXML private Label lblHeroSubtitle;
    @FXML private TextField txtSearchFeedback;
    @FXML private FlowPane flowFeedbacks;
    @FXML private Label lblPageMessage;
    @FXML private ScrollPane scrollFeedbacks;
    @FXML private HBox statsStrip;
    @FXML private VBox statTotalBox;
    @FXML private HBox statsExtraHBox;
    @FXML private VBox analyticsBox;
    @FXML private HBox analyticsCountsHBox;
    @FXML private VBox chartContainer;
    @FXML private Button btnExportCsv;

    private final ServiceFeedback serviceFeedback = new ServiceFeedback();
    private final ServiceFeedbackComment serviceComment = new ServiceFeedbackComment();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private boolean isAdminRh() {
        User u = Session.getUser();
        if (u == null || u.getRole() == null) return false;
        String r = u.getRole().toUpperCase().replace("_", "").replace("-", "");
        return r.startsWith("ADMIN");
    }

    private Integer currentUserId() {
        User u = Session.getUser();
        return u != null ? u.getId() : null;
    }

    @FXML
    public void initialize() {
        if (lblTitleFeedback != null) {
            lblTitleFeedback.setText(isAdminRh() ? "Admin RH — Gestion des Feedbacks" : "Mes Feedbacks");
        }
        if (lblHeroSubtitle != null) {
            lblHeroSubtitle.setText(isAdminRh() ? "Vue d’ensemble et statistiques des retours" : "Vos retours et suggestions");
        }
        if (btnExportCsv != null) btnExportCsv.setVisible(isAdminRh());
        buildStatsStrip();
        if (analyticsBox != null && analyticsCountsHBox != null && chartContainer != null && isAdminRh()) {
            analyticsBox.setVisible(true);
            analyticsBox.setManaged(true);
            buildAnalytics();
        }
        refreshFeedbacks();
    }

    private void buildStatsStrip() {
        if (statTotalBox == null || statsStrip == null) return;
        statTotalBox.getChildren().clear();
        Integer employeId = isAdminRh() ? null : currentUserId();
        long total = serviceFeedback.getTotalCount(employeId);
        Label lblStatTitle = new Label(isAdminRh() ? "Total feedbacks" : "Mes feedbacks");
        lblStatTitle.getStyleClass().add("feedback-stat-label");
        Label lblStatValue = new Label(String.valueOf(total));
        lblStatValue.getStyleClass().add("feedback-stat-value");
        statTotalBox.getChildren().addAll(lblStatTitle, lblStatValue);

        if (statsExtraHBox != null && isAdminRh()) {
            statsExtraHBox.getChildren().clear();
            Map<String, Long> byStatus = serviceFeedback.getCountByStatus();
            for (Map.Entry<String, Long> e : byStatus.entrySet()) {
                VBox card = new VBox(4);
                card.getStyleClass().add("feedback-stat-card");
                card.getStyleClass().add("feedback-stat-card-mini");
                String statusLabel = e.getKey().replace("_", " ");
                Label title = new Label(statusLabel);
                title.getStyleClass().add("feedback-stat-label");
                Label value = new Label(String.valueOf(e.getValue()));
                value.getStyleClass().add("feedback-stat-value-mini");
                card.getChildren().addAll(title, value);
                statsExtraHBox.getChildren().add(card);
            }
        }
    }

    private void buildAnalytics() {
        if (analyticsCountsHBox == null) return;
        analyticsCountsHBox.getChildren().clear();
        Map<String, Long> byCat = serviceFeedback.getCountByCategory();
        Label catHeader = new Label("Par catégorie");
        catHeader.getStyleClass().add("feedback-analytics-header");
        analyticsCountsHBox.getChildren().add(catHeader);
        for (Map.Entry<String, Long> e : byCat.entrySet()) {
            VBox card = new VBox(4);
            card.getStyleClass().add("feedback-analytics-card");
            Label title = new Label(e.getKey());
            title.getStyleClass().add("feedback-analytics-card-label");
            Label value = new Label(String.valueOf(e.getValue()));
            value.getStyleClass().add("feedback-analytics-card-value");
            card.getChildren().addAll(title, value);
            analyticsCountsHBox.getChildren().add(card);
        }
        Map<String, Long> byStatus = serviceFeedback.getCountByStatus();
        Label statusHeader = new Label("Par statut");
        statusHeader.getStyleClass().add("feedback-analytics-header");
        analyticsCountsHBox.getChildren().add(statusHeader);
        for (Map.Entry<String, Long> e : byStatus.entrySet()) {
            VBox card = new VBox(4);
            card.getStyleClass().add("feedback-analytics-card");
            Label title = new Label(e.getKey().replace("_", " "));
            title.getStyleClass().add("feedback-analytics-card-label");
            Label value = new Label(String.valueOf(e.getValue()));
            value.getStyleClass().add("feedback-analytics-card-value");
            card.getChildren().addAll(title, value);
            analyticsCountsHBox.getChildren().add(card);
        }
        if (chartContainer != null) {
            chartContainer.getChildren().clear();
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            chart.setTitle("Feedbacks par mois");
            chart.getStyleClass().add("feedback-bar-chart");
            chart.setLegendVisible(false);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            Map<String, Long> byPeriod = serviceFeedback.getCountByPeriod();
            for (Map.Entry<String, Long> e : byPeriod.entrySet()) {
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            }
            chart.getData().add(series);
            chart.setPrefHeight(220);
            chartContainer.getChildren().add(chart);
        }
    }

    /** Rafraîchit la liste: admin = tous, employé = les siens (par employe_id). */
    @FXML
    public void refreshFeedbacks() {
        if (flowFeedbacks == null) return;
        flowFeedbacks.getChildren().clear();
        String search = (txtSearchFeedback != null && txtSearchFeedback.getText() != null) ? txtSearchFeedback.getText().trim() : "";

        Integer employeId = isAdminRh() ? null : currentUserId();
        List<Feedback> feedbacks = serviceFeedback.getFilteredByEmployeId(employeId, search);
        if (lblPageMessage != null) {
            if (feedbacks.isEmpty()) {
                lblPageMessage.setText(isAdminRh() ? "Aucun feedback trouvé." : "Aucun feedback de votre part.");
            } else {
                lblPageMessage.setText("");
            }
        }
        feedbacks.forEach(f -> flowFeedbacks.getChildren().add(createFeedbackCard(f)));
        buildStatsStrip();
    }

    /** Carte feedback selon DB: contenu/date_envoi/est_anonyme/employe_id/admin_id. */
    private VBox createFeedbackCard(Feedback f) {
        boolean admin = isAdminRh();
        Integer currentId = currentUserId();
        boolean isOwn = !admin && currentId != null && f.getEmployeId() != null && currentId.equals(f.getEmployeId());

        Label lblTitle = new Label(f.isEstAnonyme() ? "Anonyme" : ("Employé #" + (f.getEmployeId() != null ? f.getEmployeId() : "")));
        lblTitle.getStyleClass().add("feedback-card-title");

        HBox badgesRow = new HBox(8);
        badgesRow.getStyleClass().add("feedback-card-badges");
        if (f.getCategory() != null && !f.getCategory().isEmpty()) {
            Label catBadge = new Label(f.getCategory());
            catBadge.getStyleClass().add("feedback-card-badge");
            catBadge.getStyleClass().add("feedback-badge-category");
            badgesRow.getChildren().add(catBadge);
        }
        if (f.getStatus() != null && !f.getStatus().isEmpty()) {
            Label statusBadge = new Label(f.getStatus().replace("_", " "));
            statusBadge.getStyleClass().add("feedback-card-badge");
            statusBadge.getStyleClass().add("feedback-badge-status-" + (f.getStatus().equals("resolu") ? "resolu" : f.getStatus().equals("en_cours") ? "encours" : "nouveau"));
            badgesRow.getChildren().add(statusBadge);
        }

        String dateTxt = "";
        Timestamp ts = f.getDateEnvoi();
        if (ts != null) {
            try {
                dateTxt = DATE_FMT.format(ts.toLocalDateTime());
            } catch (Exception ignored) {
                dateTxt = ts.toString();
            }
        }
        Label lblMeta = new Label((dateTxt.isEmpty() ? "" : ("Envoyé le " + dateTxt))
                + (f.getAdminId() == null ? " · Non traité" : (" · Traité (admin #" + f.getAdminId() + ")")));
        lblMeta.getStyleClass().add("feedback-card-meta");

        Label lblContenu = new Label(f.getContenu() != null ? f.getContenu() : "");
        lblContenu.setWrapText(true);
        lblContenu.getStyleClass().add("feedback-card-body");

        Button btnEdit = new Button("Modifier");
        btnEdit.setOnAction(e -> openFeedbackDialog(f));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setOnAction(e -> deleteFeedback(f));
        btnDelete.setDisable(!admin && !isOwn);

        Button btnTraiter = null;
        if (admin) {
            btnTraiter = new Button(f.getAdminId() == null ? "Marquer traité" : "Déjà traité");
            btnTraiter.setDisable(f.getAdminId() != null);
            btnTraiter.setOnAction(e -> markAsProcessed(f));
        }

        Button btnTrello = null;
        if (admin && TrelloApiClient.isConfigured()) {
            btnTrello = new Button("Créer ticket Trello");
            btnTrello.setOnAction(e -> createTrelloCardFromFeedback(f));
        }

        Button btnSentiment = null;
        if (ApiConfig.SENTIMENT_API_URL != null && !ApiConfig.SENTIMENT_API_URL.isBlank()) {
            btnSentiment = new Button("Voir sentiment");
            btnSentiment.setOnAction(e -> showSentiment(f.getContenu()));
        }

        HBox actions = new HBox(10);
        actions.getStyleClass().add("feedback-card-actions");
        actions.getChildren().add(btnEdit);
        if (btnTraiter != null) actions.getChildren().add(btnTraiter);
        if (btnTrello != null) actions.getChildren().add(btnTrello);
        if (btnSentiment != null) actions.getChildren().add(btnSentiment);
        actions.getChildren().add(btnDelete);

        VBox commentsSection = buildCommentsSection(f, currentId);
        VBox card = new VBox(8, lblTitle, badgesRow, lblMeta, lblContenu, actions, commentsSection);
        card.getStyleClass().add("feedback-card");
        return card;
    }

    private VBox buildCommentsSection(Feedback f, Integer currentId) {
        VBox section = new VBox(6);
        section.getStyleClass().add("feedback-comments-section");
        Label lblCommentsTitle = new Label("Commentaires");
        lblCommentsTitle.getStyleClass().add("feedback-card-meta");
        section.getChildren().add(lblCommentsTitle);
        List<FeedbackComment> comments = serviceComment.getByFeedbackId(f.getId());
        for (FeedbackComment c : comments) {
            String dateStr = c.getDateCreation() != null ? DATE_FMT.format(c.getDateCreation().toLocalDateTime()) : "";
            String author = c.getUserNom() != null ? c.getUserNom() : ("#" + c.getUserId());
            Label line = new Label(author + " · " + dateStr + "\n" + (c.getContenu() != null ? c.getContenu() : ""));
            line.setWrapText(true);
            line.getStyleClass().add("feedback-comment-line");
            section.getChildren().add(line);
        }
        TextField replyField = new TextField();
        replyField.setPromptText("Ajouter une réponse...");
        Button btnReply = new Button("Répondre");
        btnReply.setOnAction(e -> {
            String text = replyField.getText() == null ? "" : replyField.getText().trim();
            if (text.isEmpty()) return;
            if (currentId == null) { showError("Connectez-vous pour répondre."); return; }
            FeedbackComment nc = new FeedbackComment();
            nc.setFeedbackId(f.getId());
            nc.setUserId(currentId);
            nc.setContenu(text);
            if (serviceComment.add(nc)) {
                replyField.clear();
                refreshFeedbacks();
            } else showError("Impossible d'ajouter le commentaire.");
        });
        HBox replyRow = new HBox(8, replyField, btnReply);
        replyField.setPrefWidth(220);
        section.getChildren().add(replyRow);
        return section;
    }

    @FXML
    private void addFeedbackQuick() {
        openFeedbackDialog(null);
    }

    private void openFeedbackDialog(Feedback existing) {
        boolean admin = isAdminRh();
        Integer currentId = currentUserId();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Feedback" : (admin ? "Modifier le feedback" : "Modifier mon feedback"));
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextArea contenu = new TextArea(existing == null ? "" : (existing.getContenu() != null ? existing.getContenu() : ""));
        contenu.setPromptText("Écrivez votre feedback...");
        contenu.setPrefRowCount(5);
        contenu.setWrapText(true);

        CheckBox cbAnonyme = new CheckBox("Envoyer en anonyme");
        cbAnonyme.setSelected(existing != null && existing.isEstAnonyme());

        ComboBox<String> comboCategory = new ComboBox<>();
        comboCategory.getItems().addAll(ServiceFeedback.CATEGORIES);
        comboCategory.setValue(existing != null && existing.getCategory() != null ? existing.getCategory() : "Autre");

        ComboBox<String> comboStatus = new ComboBox<>();
        comboStatus.getItems().addAll(ServiceFeedback.STATUSES);
        comboStatus.setValue(existing != null && existing.getStatus() != null ? existing.getStatus() : "nouveau");
        comboStatus.setVisible(admin);
        comboStatus.setManaged(admin);

        Label infoIds = new Label();
        if (admin && existing != null) {
            infoIds.setText("ID: " + existing.getId()
                    + (existing.getEmployeId() != null ? (" · employe_id: " + existing.getEmployeId()) : "")
                    + (existing.getAdminId() != null ? (" · admin_id: " + existing.getAdminId()) : ""));
        }

        VBox form = new VBox(8,
                (admin && existing != null) ? infoIds : new Label(""),
                new Label("Contenu"), contenu,
                new Label("Catégorie"), comboCategory,
                admin ? new Label("Statut") : new Label(""), admin ? comboStatus : new Label(""),
                cbAnonyme
        );
        form.setPrefWidth(400);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) return;
            String contenuVal = contenu.getText() == null ? "" : contenu.getText().trim();
            boolean anonymeVal = cbAnonyme.isSelected();
            if (contenuVal.isEmpty()) {
                showError("Le contenu est obligatoire.");
                return;
            }
            try {
                Feedback f = existing == null ? new Feedback() : existing;
                f.setContenu(contenuVal);
                f.setEstAnonyme(anonymeVal);
                f.setCategory(comboCategory.getValue() != null ? comboCategory.getValue() : "Autre");
                f.setStatus(comboStatus.getValue() != null ? comboStatus.getValue() : "nouveau");
                if (existing == null) {
                    f.setEmployeId(currentId);
                    Integer newId = serviceFeedback.ajouter(f);
                    if (newId != null) {
                        SlackWebhookClient.notifyNewFeedback(newId, f.getContenu(), f.getEmployeId(), f.isEstAnonyme());
                        setMessage("Feedback enregistré.", false);
                        refreshFeedbacks();
                    } else {
                        showError("Enregistrement impossible.");
                    }
                } else {
                    boolean ok;
                    if (admin) {
                        ok = serviceFeedback.update(f);
                    } else {
                        ok = serviceFeedback.updateByUser(f, currentId);
                    }
                    if (ok) {
                        setMessage("Feedback enregistré.", false);
                        refreshFeedbacks();
                    } else {
                        showError("Enregistrement impossible.");
                    }
                }
            } catch (Exception ex) {
                showError("Erreur: " + ex.getMessage());
            }
        });
    }

    private void deleteFeedback(Feedback f) {
        boolean admin = isAdminRh();
        Integer currentId = currentUserId();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer ce feedback ?");
        confirm.setContentText("Contenu: " + (f.getContenu() != null ? f.getContenu() : ""));
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                boolean ok = admin ? serviceFeedback.delete(f.getId()) : serviceFeedback.deleteByUser(f.getId(), currentId);
                if (ok) {
                    setMessage("Feedback supprimé.", false);
                    refreshFeedbacks();
                } else {
                    showError("Suppression impossible.");
                }
            }
        });
    }

    private void markAsProcessed(Feedback f) {
        Integer adminId = currentUserId();
        if (adminId == null) {
            showError("Admin non connecté.");
            return;
        }
        try {
            f.setAdminId(adminId);
            boolean ok = serviceFeedback.update(f);
            if (ok) {
                setMessage("Feedback marqué comme traité.", false);
                refreshFeedbacks();
            } else {
                showError("Action impossible.");
            }
        } catch (Exception ex) {
            showError("Erreur: " + ex.getMessage());
        }
    }

    private void createTrelloCardFromFeedback(Feedback f) {
        String result = TrelloApiClient.createCardFromFeedback(
                f.getId(),
                f.getContenu(),
                f.getEmployeId(),
                f.isEstAnonyme());
        if (result != null && result.startsWith("ERROR:")) {
            showError(result.substring(6).trim());
        } else if (result != null) {
            setMessage("Ticket Trello créé.", false);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Carte créée:\n" + result);
            a.showAndWait();
        } else {
            showError("Impossible de créer la carte Trello (vérifiez la configuration).");
        }
    }

    private void showSentiment(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            showError("Aucun contenu à analyser.");
            return;
        }
        SentimentApiClient.SentimentResult r = SentimentApiClient.getSentiment(contenu);
        if (r == null) {
            showError("Service sentiment indisponible.");
            return;
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("Sentiment: " + r.label + "\nScore: " + String.format("%.2f", r.score));
        a.showAndWait();
    }

    @FXML
    private void exportCsv() {
        if (!isAdminRh()) return;
        Dialog<Object[]> dateDialog = new Dialog<>();
        dateDialog.setTitle("Exporter en CSV");
        dateDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        javafx.scene.control.DatePicker dateFrom = new javafx.scene.control.DatePicker();
        javafx.scene.control.DatePicker dateTo = new javafx.scene.control.DatePicker();
        ComboBox<String> comboCat = new ComboBox<>();
        comboCat.getItems().add("");
        comboCat.getItems().addAll(ServiceFeedback.CATEGORIES);
        comboCat.setValue("");
        ComboBox<String> comboStat = new ComboBox<>();
        comboStat.getItems().add("");
        comboStat.getItems().addAll(ServiceFeedback.STATUSES);
        comboStat.setValue("");
        VBox form = new VBox(8,
                new Label("Date début"), dateFrom,
                new Label("Date fin"), dateTo,
                new Label("Catégorie (optionnel)"), comboCat,
                new Label("Statut (optionnel)"), comboStat);
        form.setPrefWidth(300);
        dateDialog.getDialogPane().setContent(form);
        dateDialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            java.util.Date from = dateFrom.getValue() != null ? java.sql.Date.valueOf(dateFrom.getValue()) : null;
            java.util.Date to = dateTo.getValue() != null ? java.sql.Date.valueOf(dateTo.getValue()) : null;
            String c = comboCat.getValue() != null && !comboCat.getValue().isEmpty() ? comboCat.getValue() : null;
            String s = comboStat.getValue() != null && !comboStat.getValue().isEmpty() ? comboStat.getValue() : null;
            return new Object[]{from, to, c, s};
        });
        dateDialog.showAndWait().ifPresent(result -> {
            if (result == null || result.length < 4) return;
            java.util.Date from = (java.util.Date) result[0];
            java.util.Date to = (java.util.Date) result[1];
            String cat = (String) result[2];
            String stat = (String) result[3];
            List<Feedback> list = serviceFeedback.getForExport(from, to, cat, stat);
            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            fc.setInitialFileName("feedbacks_export.csv");
            Stage stage = (Stage) (flowFeedbacks != null ? flowFeedbacks.getScene().getWindow() : null);
            if (stage == null) return;
            java.io.File file = fc.showSaveDialog(stage);
            if (file == null) return;
            try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
                w.println("id;contenu;date_envoi;est_anonyme;employe_id;admin_id;category;status");
                for (Feedback f : list) {
                    String contenu = (f.getContenu() != null ? f.getContenu().replace(";", ",").replace("\n", " ") : "");
                    String dateStr = f.getDateEnvoi() != null ? f.getDateEnvoi().toString() : "";
                    w.println(f.getId() + ";\"" + contenu + "\";" + dateStr + ";" + f.isEstAnonyme() + ";" + (f.getEmployeId() != null ? f.getEmployeId() : "") + ";" + (f.getAdminId() != null ? f.getAdminId() : "") + ";" + (f.getCategory() != null ? f.getCategory() : "") + ";" + (f.getStatus() != null ? f.getStatus() : ""));
                }
                setMessage("Exporté " + list.size() + " feedback(s) vers " + file.getName(), false);
            } catch (IOException e) {
                showError("Erreur export: " + e.getMessage());
            }
        });
    }

    /** Retour: employé → Mes Congés, admin → Tableau de bord. */
    @FXML
    public void goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (isAdminRh()) {
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/recrutement/dashboard.fxml"))));
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/DemandeConge.fxml"));
                stage.setScene(new Scene(loader.load()));
                DemandeCongeController ctrl = loader.getController();
                if (Session.getUser() != null) ctrl.setEmployeId(Session.getUser().getId());
            }
            stage.show();
        } catch (IOException e) {
            showError("Retour impossible: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setContentText(message);
        a.showAndWait();
    }

    private void setMessage(String message, boolean isError) {
        if (lblPageMessage != null) {
            lblPageMessage.setText(message);
            lblPageMessage.setStyle(isError ? "-fx-text-fill: #d64545;" : "-fx-text-fill: #2f855a;");
        }
    }
}
