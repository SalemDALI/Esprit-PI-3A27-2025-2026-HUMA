package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Formation;
import model.Participant;
import services.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class EspaceParticipantsController {

    private static final int EMPLOYE_ID = 1;

    @FXML private TextField formationSelectionneeField;
    @FXML private TextField formationIdField;
    @FXML private TextField dateInscriptionField;
    @FXML private TextField statutField;
    @FXML private TextField rechercheField;
    @FXML private FlowPane catalogueContainer;
    @FXML private VBox mesInscriptionsContainer;
    @FXML private Label totalFormationsLabel;

    private final CrudFormation serviceFormation = new CrudFormation();
    private final CrudParticipant serviceParticipant = new CrudParticipant();

    @FXML
    public void initialize() {
        dateInscriptionField.setText(LocalDate.now().toString());
        actualiserCatalogue();
        chargerMesInscriptions();
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> actualiserCatalogue());
    }

    @FXML
    public void inscrireParticipant() {
        try {
            if (formationIdField.getText().isEmpty()) {
                afficherAlerte("Attention", "Veuillez choisir une formation.", Alert.AlertType.WARNING);
                return;
            }
            int formationId = Integer.parseInt(formationIdField.getText());
            boolean dejaInscrit = serviceParticipant.afficherAll().stream()
                    .anyMatch(p -> p.getEmployeId() == EMPLOYE_ID && p.getFormationId() == formationId);
            if (dejaInscrit) {
                afficherAlerte("Information", "Vous etes deja inscrit!", Alert.AlertType.INFORMATION);
                return;
            }
            serviceParticipant.ajouter(new Participant(Date.valueOf(LocalDate.now()), "En attente", EMPLOYE_ID, formationId));
            afficherAlerte("Succes", "Inscription reussie!", Alert.AlertType.INFORMATION);
            formationSelectionneeField.clear();
            formationIdField.clear();
            chargerMesInscriptions();
        } catch (Exception e) {
            afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void actualiserCatalogue() {
        try {
            catalogueContainer.getChildren().clear();
            List<Formation> formations = serviceFormation.afficherAll();
            String recherche = rechercheField.getText().toLowerCase().trim();
            if (!recherche.isEmpty()) {
                formations = formations.stream()
                        .filter(f -> f.getSujet().toLowerCase().contains(recherche)
                                || f.getFormateur().toLowerCase().contains(recherche)
                                || f.getType().toLowerCase().contains(recherche))
                        .toList();
            }
            totalFormationsLabel.setText(String.valueOf(formations.size()));
            if (formations.isEmpty()) {
                Label empty = new Label("Aucune formation disponible.");
                empty.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
                catalogueContainer.getChildren().add(empty);
            } else {
                for (Formation f : formations)
                    catalogueContainer.getChildren().add(creerCardCatalogue(f));
            }
        } catch (Exception e) {
            afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox creerCardCatalogue(Formation f) {
        // ── Dimensions fixes de la carte ──
        final double CARD_W  = 310;
        final double PHOTO_H = 170;

        VBox card = new VBox(0);
        card.getStyleClass().add("stat-box");
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);
        card.setAlignment(Pos.TOP_LEFT);

        // ── ImageView : remplit exactement le cadre ──
        ImageView imageView = new ImageView();
        imageView.setFitWidth(CARD_W);
        imageView.setFitHeight(PHOTO_H);
        imageView.setPreserveRatio(false);   // ← remplit tout le rectangle
        imageView.setSmooth(true);

        // ── StackPane avec clip arrondi ──
        StackPane photoPane = new StackPane(imageView);
        photoPane.setPrefSize(CARD_W, PHOTO_H);
        photoPane.setMinSize(CARD_W, PHOTO_H);
        photoPane.setMaxSize(CARD_W, PHOTO_H);

        // Fond dégradé visible pendant le chargement
        photoPane.setStyle("-fx-background-color: linear-gradient(to bottom right,#2c3e50,#3d5a73);");

        // Clip : coins arrondis en haut seulement (rayon 12)
        Rectangle clip = new Rectangle(CARD_W, PHOTO_H);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        photoPane.setClip(clip);

        // Label "Chargement..." centré sur le fond
        Label photoLabel = new Label("Chargement...");
        photoLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.5);-fx-font-size:12px;");
        photoPane.getChildren().add(photoLabel);

        // ── Chargement photo en arrière-plan ──
        Thread photoThread = new Thread(() -> {
            String[] result = PexelsService.fetchPhotoUrl(f.getSujet());
            Platform.runLater(() -> {
                if (result != null && result[0] != null) {
                    try {
                        // Double résolution → recadrage parfait
                        Image img = new Image(result[0],
                                (int)(CARD_W * 2), (int)(PHOTO_H * 2),
                                false, true, true);
                        imageView.setImage(img);
                        photoLabel.setVisible(false);
                    } catch (Exception ignored) {
                        photoLabel.setText(f.getSujet());
                        photoLabel.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
                    }
                } else {
                    photoLabel.setText(f.getSujet());
                    photoLabel.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
                }
            });
        });
        photoThread.setDaemon(true);
        photoThread.start();

        // ── Contenu texte ──
        VBox contenu = new VBox(8);
        contenu.setPadding(new Insets(14));

        Label titre = new Label(f.getSujet());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titre.setStyle("-fx-text-fill:#2c3e50;");
        titre.setWrapText(true);
        titre.setMaxWidth(270);

        Label badge = new Label(f.getType());
        badge.getStyleClass().add("request-badge");
        badge.getStyleClass().add(
                (f.getType().equalsIgnoreCase("Presentiel") || f.getType().equalsIgnoreCase("Présentiel"))
                        ? "approved" : "pending");

        VBox details = new VBox(5,
                infoLabel(f.getFormateur()),
                infoLabel(f.getDateDebut() != null ? f.getDateDebut().toString() : ""),
                infoLabel(f.getDuree() + " jours"),
                infoLabel(f.getLocalisation()));

        Separator sep = new Separator();

        Button btnChoisir = creerBouton("Choisir",  "#3498db");
        Button btnQR      = creerBouton("QR Code",  "#9b59b6");
        btnChoisir.setOnAction(e -> selectionnerFormation(f));
        btnQR.setOnAction(e -> { selectionnerFormation(f); QRCodeService.genererEtAfficherQRCode(f); });
        HBox.setHgrow(btnChoisir, Priority.ALWAYS); btnChoisir.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnQR,      Priority.ALWAYS); btnQR.setMaxWidth(Double.MAX_VALUE);
        HBox rangee1 = new HBox(10, btnChoisir, btnQR);

        Button btnMap   = creerBouton("Carte",          "#27ae60");
        Button btnMeteo = creerBouton("Meteo",          "#e67e22");
        Button btnIA    = creerBouton("Description IA", "#303030");
        btnMap.setOnAction(e   -> MapService.afficherCarte(f.getLocalisation()));
        btnMeteo.setOnAction(e -> WeatherService.afficherMeteo(f.getLocalisation()));
        btnIA.setOnAction(e    -> GeminiService.genererDescription(
                f.getSujet(), f.getType() != null ? f.getType() : "Présentiel",
                f.getFormateur(), f.getDuree(), f.getLocalisation(),
                desc -> { Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Description IA"); a.setHeaderText(null);
                    a.setContentText(desc); a.getDialogPane().setMinWidth(520); a.showAndWait(); }));
        HBox.setHgrow(btnMap,   Priority.ALWAYS); btnMap.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnMeteo, Priority.ALWAYS); btnMeteo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnIA,    Priority.ALWAYS); btnIA.setMaxWidth(Double.MAX_VALUE);
        HBox rangee2 = new HBox(8, btnMap, btnMeteo, btnIA);

        contenu.getChildren().addAll(titre, badge, details, sep, rangee1, rangee2);
        card.getChildren().addAll(photoPane, contenu);
        return card;
    }

    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color:" + couleur + ";-fx-text-fill:white;" +
                "-fx-padding:9px 14px;-fx-background-radius:7px;-fx-cursor:hand;" +
                "-fx-font-size:12px;-fx-font-weight:bold;");
        return btn;
    }

    private Label infoLabel(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-text-fill:#7f8c8d;-fx-font-size:12px;");
        l.setWrapText(true);
        l.setMaxWidth(250);
        return l;
    }

    private void selectionnerFormation(Formation f) {
        formationIdField.setText(String.valueOf(f.getId()));
        formationSelectionneeField.setText(f.getSujet() + " — " + f.getFormateur());
    }

    private void chargerMesInscriptions() {
        try {
            mesInscriptionsContainer.getChildren().clear();
            List<Participant> mesInscriptions = serviceParticipant.afficherAll().stream()
                    .filter(p -> p.getEmployeId() == EMPLOYE_ID).toList();
            List<Formation> toutesFormations = serviceFormation.afficherAll();
            if (mesInscriptions.isEmpty()) {
                Label empty = new Label("Aucune inscription pour le moment.");
                empty.setStyle("-fx-text-fill:#7f8c8d;-fx-font-size:12px;-fx-padding:10px;");
                mesInscriptionsContainer.getChildren().add(empty);
                return;
            }
            for (Participant p : mesInscriptions) {
                toutesFormations.stream().filter(f -> f.getId() == p.getFormationId())
                        .findFirst().ifPresent(f -> mesInscriptionsContainer.getChildren()
                                .add(creerCardInscription(p, f)));
            }
        } catch (Exception e) {
            System.err.println("Erreur inscriptions: " + e.getMessage());
        }
    }

    private VBox creerCardInscription(Participant p, Formation f) {
        VBox card = new VBox(5);
        card.getStyleClass().add("request-item");
        card.setPadding(new Insets(10));

        Label titre = new Label(f.getSujet());
        titre.setFont(Font.font("System", FontWeight.BOLD, 13));
        titre.setStyle("-fx-text-fill:#2c3e50;");

        Label date = new Label("Date : " + p.getDateInscription());
        date.setStyle("-fx-text-fill:#7f8c8d;-fx-font-size:11px;");

        Label statutBadge = new Label(p.getResultat());
        statutBadge.getStyleClass().add("request-badge");
        String statut = p.getResultat().toLowerCase();
        statutBadge.getStyleClass().add(
                (statut.contains("valid") || statut.contains("approuv")) ? "approved" : "pending");

        card.getChildren().addAll(titre, date, statutBadge);
        return card;
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void retourAccueil() {
        try {
            Stage stage = (Stage) formationSelectionneeField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/Home.fxml"))));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}