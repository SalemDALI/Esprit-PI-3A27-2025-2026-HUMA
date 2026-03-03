package controller;

import models.Reputation;
import services.ReputationService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import models.Publication;
import models.PublicationComment;
import models.User;
import services.PublicationService;
import utils.Session;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javafx.application.Platform;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PublicationsController {

    @FXML private Label welcomeLabel;
    @FXML private Label lblInfo;
    @FXML private VBox publicationList;
    @FXML private Button btnSelectImage;
    @FXML private Button btnSelectVideo;
    @FXML private TextField txtPublicationTitre;
    @FXML private TextArea txtPublicationContenu;
    @FXML private HBox mediaPreviewBox;

    private final PublicationService publicationService = new PublicationService();
    private final List<String> imagesToAdd = new ArrayList<>();
    private final List<String> videosToAdd = new ArrayList<>();

    private Publication selectedPublication = null;
    private PublicationComment editingComment = null;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user != null && welcomeLabel != null)
            welcomeLabel.setText("Bienvenue, " + user.getNom());
        renderPublications();
    }

    @FXML
    public void publishCommunication() {
        String titre = txtPublicationTitre.getText().trim();
        String contenu = txtPublicationContenu.getText().trim();
        if (titre.isEmpty() || contenu.isEmpty()) {
            setInfo("Titre et contenu obligatoires", true);
            return;
        }
        int pubId = publicationService.addPublicationAndGetId(titre, contenu);
        if (pubId > 0) {
            for (String img : imagesToAdd) publicationService.addMedia(pubId, "image", img);
            for (String vid : videosToAdd) publicationService.addMedia(pubId, "video", vid);
            imagesToAdd.clear();
            videosToAdd.clear();
            if (mediaPreviewBox != null) mediaPreviewBox.getChildren().clear();
            txtPublicationTitre.clear();
            txtPublicationContenu.clear();
            setInfo("✅  Publication ajoutée", false);
            renderPublications();
        } else {
            setInfo("Erreur publication", true);
        }
    }

    @FXML
    public void selectImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(btnSelectImage != null ? btnSelectImage.getScene().getWindow() : null);
        if (file != null) {
            imagesToAdd.add(file.getAbsolutePath());
            if (mediaPreviewBox != null) {
                ImageView iv = new ImageView(new Image("file:" + file.getAbsolutePath()));
                iv.setFitWidth(72);
                iv.setFitHeight(72);
                iv.setPreserveRatio(true);
                VBox w = new VBox(iv);
                w.setStyle("-fx-background-color:white;-fx-border-color:#6ee7cb;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:3;");
                w.setMaxWidth(78);
                w.setMaxHeight(78);
                mediaPreviewBox.getChildren().add(w);
            }
        }
    }

    @FXML
    public void selectVideo() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mkv"));
        File file = fc.showOpenDialog(btnSelectVideo != null ? btnSelectVideo.getScene().getWindow() : null);
        if (file != null) {
            videosToAdd.add(file.getAbsolutePath());
            if (mediaPreviewBox != null) {
                Label lbl = new Label("🎬 " + file.getName());
                lbl.setStyle("-fx-background-color:#f0fdf9;-fx-border-color:#6ee7cb;-fx-border-width:1;-fx-border-radius:6;-fx-padding:4 8;-fx-font-size:11px;");
                mediaPreviewBox.getChildren().add(lbl);
            }
        }
    }

    @FXML public void refreshPublications() { renderPublications(); }
    @FXML public void back() {}
    @FXML public void logout() { Session.setUser(null); }

    // ── Render ────────────────────────────────────────────
    private void renderPublications() {
        publicationList.getChildren().clear();
        List<Publication> publications = publicationService.getAll();
        if (publications.isEmpty()) {
            Label empty = new Label("Aucune publication pour le moment.");
            empty.setStyle("-fx-text-fill:#a0aec0;-fx-font-size:14px;-fx-padding:40;");
            publicationList.getChildren().add(empty);
            return;
        }
        for (Publication pub : publications)
            publicationList.getChildren().add(buildPublicationCard(pub));
    }

    // ── Publication card ──────────────────────────────────
    private VBox buildPublicationCard(Publication pub) {
        boolean isSelected = selectedPublication != null &&
                selectedPublication.getId() == pub.getId();

        User currentUser = Session.getUser();
        int userId = currentUser != null ? currentUser.getId() : 0;

        Map<String, Object> reactions = publicationService.getReactions(pub.getId(), userId);
        int likes = (int) reactions.get("likes");
        int dislikes = (int) reactions.get("dislikes");
        String userReaction = (String) reactions.get("userReaction");

        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-radius:" + (isSelected ? "0 14 14 0" : "14") + ";" +
                        "-fx-border-color:" + (isSelected ? "#20c997" : "#e8edf4") + ";" +
                        "-fx-border-width:" + (isSelected ? "0 0 0 5" : "1") + ";" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,3);" +
                        "-fx-cursor:hand;"
        );

        // ── Header ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 10, 20));

        Label avatar = makeAvatar(pub.getAuteur(), 38, 13);
        VBox info = new VBox(2);

        // ✅ Pas de badge sur la publication (admin seulement publie)
        String dateStr = pub.getDatePublication() != null ? pub.getDatePublication().format(DATE_FMT) : "";
        Label dateLbl = new Label("📅 " + dateStr);
        dateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");

        Label auteurLbl = new Label(pub.getAuteur());
        auteurLbl.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#111827;");

        info.getChildren().addAll(auteurLbl, dateLbl);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("💬 " + pub.getCommentaires().size());
        badge.setStyle("-fx-background-color:#f0fdf9;-fx-text-fill:#065f46;-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:3 10;-fx-background-radius:20;");
        header.getChildren().addAll(avatar, info, sp, badge);

        // ── Titre + Contenu ──
        Label titre = new Label(pub.getTitre());
        titre.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#111827;-fx-padding:0 20 4 20;");
        titre.setWrapText(true);
        Label contenu = new Label(pub.getContenu());
        contenu.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-padding:0 20 12 20;");
        contenu.setWrapText(true);

        card.getChildren().addAll(header, titre, contenu);

        // ── Médias ──
        List<Map<String, String>> medias = publicationService.getMedia(pub.getId());
        if (!medias.isEmpty()) {
            HBox mRow = new HBox(8);
            mRow.setPadding(new Insets(0, 20, 12, 20));
            mRow.setAlignment(Pos.CENTER);
            HBox.setHgrow(mRow, Priority.ALWAYS);
            for (Map<String, String> m : medias) mRow.getChildren().add(createMediaNode(m));
            card.getChildren().add(mRow);
        }

        // ── Séparateur ──
        Region sep = new Region();
        sep.setStyle("-fx-background-color:#f0f4f8;-fx-min-height:1;-fx-max-height:1;");
        sep.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(sep);

        // ── Barre réactions ──
        card.getChildren().add(buildReactionBar(pub, likes, dislikes, userReaction));

        // ── Séparateur ──
        Region sep2 = new Region();
        sep2.setStyle("-fx-background-color:#f0f4f8;-fx-min-height:1;-fx-max-height:1;");
        sep2.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(sep2);

        // ── Commentaires ──
        if (!pub.getCommentaires().isEmpty())
            card.getChildren().add(buildCommentsSection(pub));

        // ── Zone saisie commentaire ──
        if (isSelected)
            card.getChildren().add(buildCommentInput(pub));

        card.setOnMouseClicked(e -> {
            selectedPublication = pub;
            editingComment = null;
            renderPublications();
        });

        return card;
    }

    // ── Barre réactions ───────────────────────────────────
    private HBox buildReactionBar(Publication pub, int likes, int dislikes, String userReaction) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color:white;");

        User currentUser = Session.getUser();
        int userId = currentUser != null ? currentUser.getId() : 0;

        boolean likedByUser    = "LIKE".equals(userReaction);
        boolean dislikedByUser = "DISLIKE".equals(userReaction);

        // ✅ Déclaration AVANT utilisation
        Button btnLike = new Button("👍  " + likes);
        btnLike.setStyle(likedByUser
                ? "-fx-background-color:#d1fae5;-fx-text-fill:#065f46;-fx-font-weight:bold;-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:#20c997;-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 16;"
                : "-fx-background-color:#f8fafc;-fx-text-fill:#374151;-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:#e2e8f0;-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 16;"
        );
        btnLike.setOnAction(e -> {
            if (likedByUser) publicationService.removeReaction(pub.getId(), userId);
            else publicationService.addReaction(pub.getId(), userId, "LIKE");
            renderPublications();
        });

        Button btnDislike = new Button("👎  " + dislikes);
        btnDislike.setStyle(dislikedByUser
                ? "-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-font-weight:bold;-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:#f87171;-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 16;"
                : "-fx-background-color:#f8fafc;-fx-text-fill:#374151;-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:#e2e8f0;-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 16;"
        );
        btnDislike.setOnAction(e -> {
            if (dislikedByUser) publicationService.removeReaction(pub.getId(), userId);
            else publicationService.addReaction(pub.getId(), userId, "DISLIKE");
            renderPublications();
        });


        // ── ComboBox TRADUIRE ──
        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("🇬🇧 Anglais", "🇸🇦 Arabe", "🇪🇸 Espagnol", "🇩🇪 Allemand", "🇮🇹 Italien");
        langBox.setPromptText("🌐 Traduire");
        langBox.setStyle(
                "-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;" +
                        "-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;" +
                        "-fx-font-size:12px;-fx-cursor:hand;"
        );
        langBox.setPrefWidth(140);

        langBox.setOnAction(e -> {
            String selected = langBox.getValue();
            if (selected == null) return;
            Map<String, String> langCodes = new HashMap<>();
            langCodes.put("🇬🇧 Anglais", "en");
            langCodes.put("🇸🇦 Arabe",   "ar");
            langCodes.put("🇪🇸 Espagnol", "es");
            langCodes.put("🇩🇪 Allemand", "de");
            langCodes.put("🇮🇹 Italien",  "it");
            String targetLang = langCodes.get(selected);
            String textToTranslate = pub.getTitre() + "\n" + pub.getContenu();
            langBox.setDisable(true);
            new Thread(() -> {
                try {
                    String translated = translateText(textToTranslate, targetLang);
                    Platform.runLater(() -> {
                        showTranslationDialog(pub.getTitre(), translated, selected);
                        langBox.setDisable(false);
                        langBox.setValue(null);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showTranslationDialog("Erreur", "❌ " + ex.getMessage(), selected);
                        langBox.setDisable(false);
                        langBox.setValue(null);
                    });
                }
            }).start();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnComment = new Button("💬  Commenter");
        btnComment.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#6b7280;" +
                        "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;" +
                        "-fx-border-color:#e2e8f0;-fx-border-width:1;" +
                        "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 14;"
        );
        btnComment.setOnAction(e -> {
            selectedPublication = pub;
            editingComment = null;
            renderPublications();
        });

        bar.getChildren().addAll(btnLike, btnDislike, langBox, spacer, btnComment);
        return bar;
    }

    // ── Section commentaires ──────────────────────────────
    private VBox buildCommentsSection(Publication pub) {
        VBox section = new VBox(0);
        section.setPadding(new Insets(10, 20, 10, 20));
        section.setStyle("-fx-background-color:#fafcff;");

        Label title = new Label("Commentaires");
        title.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;-fx-padding:0 0 8 0;");
        section.getChildren().add(title);

        for (PublicationComment comment : pub.getCommentaires()) {
            if (editingComment != null && editingComment.getId() == comment.getId())
                section.getChildren().add(buildEditCommentRow(comment));
            else
                section.getChildren().add(buildCommentRow(comment));
        }
        return section;
    }

    // ── Commentaire normal ────────────────────────────────
    private HBox buildCommentRow(PublicationComment comment) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));

        Label avatar = makeAvatar(comment.getAuteur(), 28, 10);

        VBox content = new VBox(3);
        HBox.setHgrow(content, Priority.ALWAYS);

        VBox bubble = new VBox(4);
        bubble.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#e8edf4;-fx-border-width:1;" +
                        "-fx-border-radius:4 14 14 14;-fx-background-radius:4 14 14 14;" +
                        "-fx-padding:8 12 8 12;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),4,0,0,1);"
        );

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label auteurLbl = new Label(comment.getAuteur());
        auteurLbl.setStyle("-fx-font-weight:bold;-fx-font-size:12px;-fx-text-fill:#1f2937;");

        // ✅ Badge employé seulement
        ReputationService reputationService = new ReputationService();
        Reputation rep = reputationService.getByUserId(comment.getUserId());
        if (rep != null) {
            Label badgeLbl = new Label(getBadgeLabel(rep.getBadge()));
            badgeLbl.setStyle(getBadgeStyle(rep.getBadge()));
            metaRow.getChildren().addAll(auteurLbl, badgeLbl);
        } else {
            metaRow.getChildren().add(auteurLbl);
        }

        String dateStr = comment.getDateCommentaire() != null ? comment.getDateCommentaire().format(DATE_FMT) : "";
        Label dateLbl = new Label("· " + dateStr);
        dateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
        metaRow.getChildren().add(dateLbl);

        Label texte = new Label(comment.getContenu());
        texte.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;");
        texte.setWrapText(true);

        bubble.getChildren().addAll(metaRow, texte);
        content.getChildren().add(bubble);

        User currentUser = Session.getUser();
        boolean isOwner = currentUser != null && currentUser.getId() == comment.getUserId();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (isOwner || isAdmin) {
            HBox actions = new HBox(12);
            actions.setPadding(new Insets(4, 0, 0, 4));

            if (isOwner) {
                Button btnEdit = new Button("Modifier");
                btnEdit.setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:#1d4ed8;" +
                                "-fx-font-size:11px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:2 0;-fx-border-width:0;"
                );
                btnEdit.setOnAction(e -> {
                    editingComment = comment;
                    renderPublications();
                });
                actions.getChildren().add(btnEdit);
            }

            Button btnDel = new Button("Supprimer");
            btnDel.setStyle(
                    "-fx-background-color:transparent;-fx-text-fill:#e53e3e;" +
                            "-fx-font-size:11px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:2 0;-fx-border-width:0;"
            );
            btnDel.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Supprimer ce commentaire ?");
                alert.setContentText("\"" + comment.getContenu() + "\"");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        publicationService.deleteComment(comment.getId(), currentUser.getId(), isAdmin);
                        setInfo("Commentaire supprimé", false);
                        renderPublications();
                    }
                });
            });
            actions.getChildren().add(btnDel);
            content.getChildren().add(actions);
        }

        row.getChildren().addAll(avatar, content);
        return row;
    }

    // ── Formulaire édition commentaire ────────────────────
    private VBox buildEditCommentRow(PublicationComment comment) {
        VBox editBox = new VBox(10);
        editBox.setStyle(
                "-fx-background-color:#f0fdf9;" +
                        "-fx-border-color:#20c997;-fx-border-width:0 0 0 3;" +
                        "-fx-border-radius:0 8 8 0;-fx-background-radius:0 8 8 0;" +
                        "-fx-padding:12 14 12 14;"
        );

        Label editTitle = new Label("Modifier votre commentaire");
        editTitle.setStyle("-fx-font-weight:bold;-fx-font-size:12px;-fx-text-fill:#065f46;");

        TextField editField = new TextField(comment.getContenu());
        editField.setStyle(
                "-fx-background-color:white;-fx-border-color:#20c997;" +
                        "-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:9 14;-fx-font-size:13px;-fx-text-fill:#1f2937;"
        );

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button btnSave = new Button("Enregistrer");
        btnSave.setStyle(
                "-fx-background-color:linear-gradient(to right,#20c997,#0ea47a);" +
                        "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:12px;" +
                        "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 16;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(32,201,151,0.35),8,0,0,3);"
        );
        btnSave.setOnAction(e -> {
            String newText = editField.getText().trim();
            if (newText.isEmpty()) { setInfo("Commentaire vide", true); return; }
            User u = Session.getUser();
            boolean ok = publicationService.updateComment(
                    comment.getId(), u != null ? u.getId() : 0,
                    u != null && "ADMIN".equalsIgnoreCase(u.getRole()), newText);
            if (ok) {
                setInfo("Commentaire modifié", false);
                editingComment = null;
                renderPublications();
            } else {
                setInfo("Erreur modification", true);
            }
        });

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#6b7280;" +
                        "-fx-font-size:12px;-fx-cursor:hand;" +
                        "-fx-border-color:#e2e8f0;-fx-border-width:1;" +
                        "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 14;"
        );
        btnCancel.setOnAction(e -> {
            editingComment = null;
            renderPublications();
        });

        btnRow.getChildren().addAll(btnSave, btnCancel);
        editBox.getChildren().addAll(editTitle, editField, btnRow);
        return editBox;
    }

    // ── Zone saisie commentaire ───────────────────────────
    private HBox buildCommentInput(Publication pub) {
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(12, 20, 12, 20));
        inputRow.setStyle("-fx-background-color:#f8fafc;");

        User user = Session.getUser();
        Label avatar = makeAvatar(user != null ? user.getNom() : "?", 32, 11);

        TextField field = new TextField();
        field.setPromptText("Écrire un commentaire...");
        field.setStyle(
                "-fx-background-color:white;-fx-border-color:#e2e8f0;" +
                        "-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;" +
                        "-fx-padding:8 14;-fx-font-size:13px;-fx-text-fill:#1f2937;"
        );
        HBox.setHgrow(field, Priority.ALWAYS);

        Button sendBtn = new Button("Envoyer ✉️");
        sendBtn.setStyle(
                "-fx-background-color:linear-gradient(to right,#20c997,#0ea47a);" +
                        "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:12px;" +
                        "-fx-border-radius:20;-fx-background-radius:20;" +
                        "-fx-padding:8 16;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(32,201,151,0.35),8,0,0,3);"
        );

        // ✅ setOnKeyTyped et setOnAction déclarés AVANT sendBtn.setOnAction
        field.setOnKeyTyped(e -> field.setStyle(
                "-fx-background-color:white;-fx-border-color:#e2e8f0;" +
                        "-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;" +
                        "-fx-padding:8 14;-fx-font-size:13px;-fx-text-fill:#1f2937;"
        ));
        field.setOnAction(e -> sendBtn.fire());

        sendBtn.setOnAction(e -> {
            String text = field.getText().trim();
            if (text.isEmpty()) return;

            // ✅ Vérification toxicité
            if (isCommentToxic(text)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Commentaire refusé");
                alert.setHeaderText("⚠️ Commentaire inapproprié");
                alert.setContentText(
                        "Votre commentaire contient des mots inappropriés.\n" +
                                "Merci de respecter vos collègues et de reformuler votre message."
                );
                alert.getDialogPane().setStyle("-fx-background-color:white;");
                alert.showAndWait();
                field.setStyle(
                        "-fx-background-color:#fff5f5;-fx-border-color:#f87171;" +
                                "-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;" +
                                "-fx-padding:8 14;-fx-font-size:13px;-fx-text-fill:#991b1b;"
                );
                return;
            }

            // ✅ Commentaire propre → envoyer
            if (user != null) {
                publicationService.addComment(pub.getId(),
                        user.getNom() + " " + user.getPrenom(), text);

                // ✅ Mise à jour réputation
                // ✅ Seulement l'employé qui commente
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    new ReputationService().calculerEtSauvegarder(user.getId());
                }

                setInfo("✅  Commentaire ajouté", false);
                renderPublications();
            }
        });

        inputRow.getChildren().addAll(avatar, field, sendBtn);
        return inputRow;
    }

    // ── Helpers ───────────────────────────────────────────
    private Node createMediaNode(Map<String, String> media) {
        String type = media.get("type"), path = media.get("path");

        if ("image".equals(type)) {
            ImageView iv = new ImageView(new Image("file:" + path));
            iv.setFitWidth(480);
            iv.setFitHeight(320);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            VBox w = new VBox(iv);
            w.setStyle(
                    "-fx-background-color:black;" +
                            "-fx-border-radius:12;-fx-background-radius:12;" +
                            "-fx-alignment:center;"
            );
            w.setAlignment(Pos.CENTER);
            w.setMaxWidth(480);
            return w;

        } else {
            try {
                Media media2 = new Media(new File(path).toURI().toString());
                MediaPlayer player = new MediaPlayer(media2);
                MediaView mediaView = new MediaView(player);
                mediaView.setFitWidth(480);
                mediaView.setFitHeight(300);
                mediaView.setPreserveRatio(true);

                Button btnPlay  = new Button("▶ Play");
                Button btnPause = new Button("⏸ Pause");
                Button btnStop  = new Button("⏹ Stop");

                String btnStyle =
                        "-fx-background-color:#1f2937;-fx-text-fill:white;" +
                                "-fx-font-size:12px;-fx-cursor:hand;" +
                                "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:5 14;";
                btnPlay.setStyle(btnStyle);
                btnPause.setStyle(btnStyle);
                btnStop.setStyle(btnStyle);

                btnPlay.setOnAction(e -> player.play());
                btnPause.setOnAction(e -> player.pause());
                btnStop.setOnAction(e -> player.stop());

                HBox controls = new HBox(8, btnPlay, btnPause, btnStop);
                controls.setAlignment(Pos.CENTER);
                controls.setPadding(new Insets(8, 0, 8, 0));

                Label nomFichier = new Label("🎬 " + new File(path).getName());
                nomFichier.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;-fx-padding:0 0 4 0;");

                VBox videoBox = new VBox(6, nomFichier, mediaView, controls);
                videoBox.setAlignment(Pos.CENTER);
                videoBox.setStyle(
                        "-fx-background-color:#111827;" +
                                "-fx-border-radius:12;-fx-background-radius:12;-fx-padding:10;"
                );
                videoBox.setMaxWidth(500);
                return videoBox;

            } catch (Exception e) {
                Label lbl = new Label("🎬 " + new File(path).getName());
                lbl.setStyle("-fx-background-color:#f0fdf9;-fx-text-fill:#065f46;" +
                        "-fx-font-size:11px;-fx-padding:5 10;" +
                        "-fx-border-color:#6ee7cb;-fx-border-width:1;" +
                        "-fx-border-radius:6;-fx-background-radius:6;");
                return lbl;
            }
        }
    }

    private Label makeAvatar(String name, int size, int fontSize) {
        Label av = new Label(getInitials(name));
        av.setStyle(
                "-fx-background-color:" + getAvatarColor(name) + ";" +
                        "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:" + fontSize + "px;" +
                        "-fx-min-width:" + size + ";-fx-min-height:" + size + ";" +
                        "-fx-max-width:" + size + ";-fx-max-height:" + size + ";" +
                        "-fx-background-radius:50;-fx-alignment:center;"
        );
        return av;
    }

    private void setInfo(String msg, boolean isError) {
        if (lblInfo == null) return;
        lblInfo.setText(msg == null ? "" : msg);
        lblInfo.setStyle(
                "-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:5 14;-fx-background-radius:20;" +
                        (isError
                                ? "-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;"
                                : "-fx-background-color:#d1fae5;-fx-text-fill:#065f46;")
        );
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        return p.length >= 2
                ? ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase()
                : ("" + p[0].charAt(0)).toUpperCase();
    }

    private String getAvatarColor(String name) {
        String[] colors = {"#20c997", "#1f2937", "#f59e0b", "#3b82f6", "#8b5cf6", "#ec4899", "#ef4444", "#06b6d4"};
        return colors[Math.abs(name == null ? 0 : name.hashCode()) % colors.length];
    }

    private void showTranslationDialog(String titre, String translatedText, String lang) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Traduction — " + lang);
        alert.setHeaderText("📖 " + titre);
        TextArea area = new TextArea(translatedText);
        area.setWrapText(true);
        area.setEditable(false);
        area.setPrefHeight(180);
        area.setPrefWidth(480);
        area.setStyle("-fx-font-size:13px;-fx-background-color:#f8fafc;");
        alert.getDialogPane().setContent(area);
        alert.getDialogPane().setStyle("-fx-background-color:white;");
        alert.showAndWait();
    }

    private String translateText(String text, String targetLang) throws Exception {
        if (text.length() > 300) text = text.substring(0, 300);
        String encoded  = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
        String langPair = java.net.URLEncoder.encode("fr|" + targetLang, java.nio.charset.StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + langPair;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String resp = response.body();
        if (resp.contains("translatedText")) {
            int start = resp.indexOf("\"translatedText\":\"") + 18;
            int end   = resp.indexOf("\"", start);
            String raw = resp.substring(start, end);
            raw = decodeUnicode(raw);
            return raw
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'");
        }
        throw new Exception("Traduction impossible.");
    }

    private String decodeUnicode(String text) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (i + 5 < text.length() && text.charAt(i) == '\\' && text.charAt(i + 1) == 'u') {
                try {
                    String hex = text.substring(i + 2, i + 6);
                    int code = Integer.parseInt(hex, 16);
                    sb.append((char) code);
                    i += 6;
                } catch (NumberFormatException e) {
                    sb.append(text.charAt(i));
                    i++;
                }
            } else {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    private boolean isCommentToxic(String text) {
        String[] motsInterdits = {
                "idiot", "idiote", "imbecile", "stupide", "stupidit",
                "nul", "nulle", "naze", "cretin", "cretine",
                "connard", "connarde", "con", "conne",
                "salaud", "salope", "batard", "batarde",
                "merde", "merdique", "emmerdeur",
                "putain", "pute", "prostituee",
                "enculé", "encule", "fdp", "fils de pute",
                "tg", "ta gueule", "ferme la gueule", "ferme ta gueule",
                "va te faire", "va te faire foutre", "va te faire voir",
                "clown", "bouffon", "abruti", "abrutie",
                "debile", "attarde", "attardee", "mongol",
                "gros nul", "grande nulle", "bon a rien", "bonne a rien",
                "tu sers a rien", "inutile", "incompetent", "incompetente",
                "raté", "rate", "ratee", "looser", "loser",
                "dégueulasse", "degueulasse", "pourriture",
                "ordure", "dechet", "rebut",
                "chiant", "chiante", "emmerdant", "casse pied",
                "gueule", "ta gueule", "ferme",
                "demissionne", "vire toi", "casse toi", "degage",
                "disparais", "va mourir", "creve",
                "kalb", "kelb", "klab", "hmar", "hmir",
                "kahlouch", "kahloucha", "zamel", "zmel",
                "kahba", "kahbaa", "sharmouta", "charmuta", "sharmuta",
                "ibn el sharmouta", "weld el kahba", "weld lhram", "weld hram",
                "barra", "emchi", "sir emchi", "nik", "nikk",
                "tboun", "tbun", "hashouma", "3ayb",
                "manyak", "maniak", "gahba", "3ahba",
                "bouzbal", "mahboul", "mahboula", "hmaq", "hmag",
                "stupid", "dumb", "moron", "fool",
                "asshole", "bastard", "bitch", "son of a bitch",
                "shut up", "fuck", "fucking", "fucked",
                "shit", "bullshit", "crap",
                "loser", "failure", "pathetic", "worthless",
                "get out", "go away", "useless",
                "retard", "retarded", "crazy", "insane",
                "ugly", "fat", "disgusting",
                "hate you", "i hate", "kill yourself", "die", "go die",
                "je vais te", "je vais tuer", "je vais frapper",
                "tu vas regretter", "tu vas voir",
                "surveille toi", "fais attention a toi",
                "je te surveille", "je sais ou tu habites",
                "tu vas payer", "je vais te detruire",
                "menace", "represailles",
                "raciste", "racism", "racist",
                "nazi", "fasciste", "fascist",
                "terroriste", "terrorist",
                "islamophobie", "antisemite",
                "sexiste", "misogyne", "misogynist",
                "homophobe", "transphobe",
                "sexe", "porno", "pornographie", "nu", "nue", "exhib",
                "pipe", "branleur", "branlette",
                "wtf", "stfu", "gtfo", "kys", "omfg", "fml",
                "va chier", "ta race", "ta mere",
                "nique", "niquer", "niquez", "ptn",
                "bon a rien", "bonne a rien", "nul en tout", "tu fais rien",
                "paresseux", "paresseuse", "fainéant", "faineant",
                "voleur", "voleuse", "menteur", "menteuse",
                "hypocrite", "lache", "traître", "traitre",
                "corrompu", "corrompue"
        };

        String lower = text.toLowerCase()
                .replace("é", "e").replace("è", "e").replace("ê", "e").replace("ë", "e")
                .replace("à", "a").replace("â", "a").replace("ä", "a")
                .replace("ù", "u").replace("û", "u").replace("ü", "u")
                .replace("î", "i").replace("ï", "i")
                .replace("ô", "o").replace("ö", "o")
                .replace("ç", "c")
                .replace("0", "o").replace("1", "i").replace("3", "e")
                .replace("@", "a").replace("$", "s").replace("!", "i");

        for (String mot : motsInterdits) {
            if (lower.contains(mot.toLowerCase())) return true;
        }
        return false;
    }

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
                "-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),4,0,0,2);";
    }



}