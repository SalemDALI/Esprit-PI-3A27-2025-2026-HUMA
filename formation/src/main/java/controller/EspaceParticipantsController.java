package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Separator;
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

    private static final int EMPLOYE_ID = 4;

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
        System.out.println("✅ Espace Participants chargé pour l'employé ID : " + EMPLOYE_ID);
    }

    // ======================== S'INSCRIRE ========================
    @FXML
    public void inscrireParticipant() {
        try {
            if (formationIdField.getText().isEmpty()) {
                afficherAlerte("Attention",
                        "Veuillez choisir une formation dans le catalogue.",
                        Alert.AlertType.WARNING);
                return;
            }

            int formationId = Integer.parseInt(formationIdField.getText());

            List<Participant> tousParticipants = serviceParticipant.afficherAll();
            boolean dejaInscrit = tousParticipants.stream()
                    .anyMatch(p -> p.getEmployeId() == EMPLOYE_ID
                            && p.getFormationId() == formationId);

            if (dejaInscrit) {
                afficherAlerte("Information",
                        "Vous etes deja inscrit a cette formation !",
                        Alert.AlertType.INFORMATION);
                return;
            }

            Participant participant = new Participant(
                    Date.valueOf(LocalDate.now()),
                    "En attente",
                    EMPLOYE_ID,
                    formationId
            );

            serviceParticipant.ajouter(participant);
            afficherAlerte("Succes",
                    "Inscription reussie ! Statut : En attente de validation.",
                    Alert.AlertType.INFORMATION);

            formationSelectionneeField.clear();
            formationIdField.clear();
            chargerMesInscriptions();

        } catch (Exception e) {
            afficherAlerte("Erreur",
                    "Erreur lors de l'inscription : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== CATALOGUE ========================
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
                empty.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-padding: 10px;");
                catalogueContainer.getChildren().add(empty);
            } else {
                for (Formation f : formations) {
                    catalogueContainer.getChildren().add(creerCardCatalogue(f));
                }
            }

        } catch (Exception e) {
            afficherAlerte("Erreur",
                    "Erreur chargement catalogue : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== CARTE CATALOGUE ========================
    private VBox creerCardCatalogue(Formation f) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-box");
        card.setPrefWidth(310);
        card.setMinHeight(230);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(18));

        // Titre
        Label titre = new Label(f.getSujet());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titre.setStyle("-fx-text-fill: #2c3e50;");
        titre.setWrapText(true);
        titre.setMaxWidth(270);

        // Badge type
        Label badge = new Label(f.getType());
        badge.getStyleClass().add("request-badge");
        if (f.getType().equalsIgnoreCase("Presentiel")
                || f.getType().equalsIgnoreCase("Présentiel")) {
            badge.getStyleClass().add("approved");
        } else {
            badge.getStyleClass().add("pending");
        }

        // Details
        VBox details = new VBox(5);
        details.getChildren().addAll(
                infoLabel("⬧  " + f.getFormateur()),
                infoLabel("⬧  " + f.getDateDebut()),
                infoLabel("⬧  " + f.getDuree() + " jours"),
                infoLabel("⬧  " + f.getLocalisation())
        );

        // Separateur
        Separator sep = new Separator();

        // ===== RANGEE 1 : Choisir + QR Code =====
        Button btnChoisir = creerBouton("Choisir",        "#3498db");
        Button btnQR      = creerBouton("QR Code",        "#9b59b6");

        btnChoisir.setOnAction(e -> selectionnerFormation(f));
        btnQR.setOnAction(e -> {
            selectionnerFormation(f);
            QRCodeService.genererEtAfficherQRCode(f);
        });

        HBox.setHgrow(btnChoisir, Priority.ALWAYS);
        HBox.setHgrow(btnQR,      Priority.ALWAYS);
        btnChoisir.setMaxWidth(Double.MAX_VALUE);
        btnQR.setMaxWidth(Double.MAX_VALUE);
        HBox rangee1 = new HBox(10, btnChoisir, btnQR);

        // ===== RANGEE 2 : Carte + Meteo + Description IA =====
        Button btnMap   = creerBouton("Carte",         "#27ae60");
        Button btnMeteo = creerBouton("Meteo",         "#e67e22");
        Button btnIA    = creerBouton("Description IA","#303030");

        btnMap.setOnAction(e -> MapService.afficherCarte(f.getLocalisation()));
        btnMeteo.setOnAction(e -> WeatherService.afficherMeteo(f.getLocalisation()));
        btnIA.setOnAction(e -> GeminiService.genererDescription(
                f.getSujet(),
                f.getType() != null ? f.getType() : "Présentiel",
                f.getFormateur(),
                f.getDuree(),
                f.getLocalisation(),
                description -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Description IA");
                    alert.setHeaderText("Générée par Gemini IA");
                    alert.setContentText(description);
                    alert.getDialogPane().setMinWidth(520);
                    alert.showAndWait();
                }
        ));

        HBox.setHgrow(btnMap,   Priority.ALWAYS);
        HBox.setHgrow(btnMeteo, Priority.ALWAYS);
        HBox.setHgrow(btnIA,    Priority.ALWAYS);
        btnMap.setMaxWidth(Double.MAX_VALUE);
        btnMeteo.setMaxWidth(Double.MAX_VALUE);
        btnIA.setMaxWidth(Double.MAX_VALUE);
        HBox rangee2 = new HBox(8, btnMap, btnMeteo, btnIA);

        card.getChildren().addAll(titre, badge, details, sep, rangee1, rangee2);
        return card;
    }

    // Helper : bouton uniforme
    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle(
                "-fx-background-color: " + couleur + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 9px 14px;"
                        + "-fx-background-radius: 7px;"
                        + "-fx-cursor: hand;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );
        return btn;
    }
    private Label infoLabel(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        l.setWrapText(true);
        l.setMaxWidth(250);
        return l;
    }

    // ======================== SÉLECTIONNER UNE FORMATION ========================
    private void selectionnerFormation(Formation f) {
        formationIdField.setText(String.valueOf(f.getId()));
        formationSelectionneeField.setText(f.getSujet() + " — " + f.getFormateur());
    }

    // ======================== MES INSCRIPTIONS ========================
    private void chargerMesInscriptions() {
        try {
            mesInscriptionsContainer.getChildren().clear();

            List<Participant> tousParticipants = serviceParticipant.afficherAll();
            List<Formation> toutesFormations = serviceFormation.afficherAll();

            List<Participant> mesInscriptions = tousParticipants.stream()
                    .filter(p -> p.getEmployeId() == EMPLOYE_ID)
                    .toList();

            if (mesInscriptions.isEmpty()) {
                Label empty = new Label("Aucune inscription pour le moment.");
                empty.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-padding: 10px;");
                mesInscriptionsContainer.getChildren().add(empty);
                return;
            }

            for (Participant p : mesInscriptions) {
                Formation formation = toutesFormations.stream()
                        .filter(f -> f.getId() == p.getFormationId())
                        .findFirst()
                        .orElse(null);

                if (formation != null) {
                    mesInscriptionsContainer.getChildren()
                            .add(creerCardInscription(p, formation));
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement inscriptions : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== CARTE INSCRIPTION ========================
    private VBox creerCardInscription(Participant p, Formation f) {
        VBox card = new VBox(5);
        card.getStyleClass().add("request-item");
        card.setPadding(new Insets(10));

        Label titre = new Label(f.getSujet());
        titre.setFont(Font.font("System", FontWeight.BOLD, 13));
        titre.setStyle("-fx-text-fill: #2c3e50;");

        Label date = new Label("Date : " + p.getDateInscription());
        date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        Label statutBadge = new Label(p.getResultat());
        statutBadge.getStyleClass().add("request-badge");
        String statut = p.getResultat().toLowerCase();
        if (statut.contains("valid") || statut.contains("approuv")) {
            statutBadge.getStyleClass().add("approved");
        } else {
            statutBadge.getStyleClass().add("pending");
        }

        card.getChildren().addAll(titre, date, statutBadge);
        return card;
    }

    // ======================== ALERTE ========================
    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ======================== RETOUR ========================
    @FXML
    public void retourAccueil() {
        try {
            Stage stage = (Stage) formationSelectionneeField.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/Home.fxml"))
            );
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}