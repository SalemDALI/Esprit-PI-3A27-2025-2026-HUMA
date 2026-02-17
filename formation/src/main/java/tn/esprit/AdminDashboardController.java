package tn.esprit;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Formation;
import model.Participant;
import services.CrudFormation;
import services.CrudParticipant;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private Label totalFormationsLabel;
    @FXML private Label totalParticipantsLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label validesLabel;
    @FXML private TextField rechercheField;
    @FXML private VBox formationsContainer;

    private CrudFormation serviceFormation = new CrudFormation();
    private CrudParticipant serviceParticipant = new CrudParticipant();

    @FXML
    public void initialize() {
        actualiserDonnees();

        // Listener pour la recherche
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> {
            actualiserDonnees();
        });
    }

    @FXML
    public void actualiserDonnees() {
        try {
            List<Formation> formations = serviceFormation.afficherAll();
            List<Participant> participants = serviceParticipant.afficherAll();

            // Mise à jour des statistiques
            totalFormationsLabel.setText(String.valueOf(formations.size()));
            totalParticipantsLabel.setText(String.valueOf(participants.size()));

            long enAttente = participants.stream()
                    .filter(p -> p.getResultat().toLowerCase().contains("attente"))
                    .count();
            long valides = participants.stream()
                    .filter(p -> p.getResultat().toLowerCase().contains("validé") ||
                            p.getResultat().toLowerCase().contains("approuvé"))
                    .count();

            enAttenteLabel.setText(String.valueOf(enAttente));
            validesLabel.setText(String.valueOf(valides));

            // Afficher les formations avec leurs participants
            afficherFormationsAvecParticipants(formations, participants);

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors du chargement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void afficherFormationsAvecParticipants(List<Formation> formations, List<Participant> participants) {
        formationsContainer.getChildren().clear();

        String recherche = rechercheField.getText().toLowerCase().trim();

        for (Formation f : formations) {
            // Filtrer par recherche
            if (!recherche.isEmpty()) {
                boolean match = f.getSujet().toLowerCase().contains(recherche) ||
                        f.getFormateur().toLowerCase().contains(recherche) ||
                        f.getLocalisation().toLowerCase().contains(recherche);
                if (!match) continue;
            }

            // Trouver les participants de cette formation
            List<Participant> participantsFormation = participants.stream()
                    .filter(p -> p.getFormationId() == f.getId())
                    .toList();

            VBox formationCard = creerCardFormationAdmin(f, participantsFormation);
            formationsContainer.getChildren().add(formationCard);
        }

        if (formationsContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Aucune formation trouvée");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-padding: 20px;");
            formationsContainer.getChildren().add(emptyLabel);
        }
    }

    private VBox creerCardFormationAdmin(Formation f, List<Participant> participants) {
        VBox mainCard = new VBox(12);
        mainCard.getStyleClass().add("request-item");
        mainCard.setPadding(new Insets(20));

        // En-tête de la formation
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox formationInfo = new VBox(5);
        formationInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(formationInfo, javafx.scene.layout.Priority.ALWAYS);

        Label titre = new Label("📚 " + f.getSujet());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setStyle("-fx-text-fill: #2c3e50;");

        HBox details = new HBox(15);
        details.getChildren().addAll(
                creerInfoLabel("👤 " + f.getFormateur()),
                creerInfoLabel("📍 " + f.getType()),
                creerInfoLabel("📅 " + f.getDateDebut()),
                creerInfoLabel("⏱️ " + f.getDuree() + " jours"),
                creerInfoLabel("🌍 " + f.getLocalisation())
        );

        formationInfo.getChildren().addAll(titre, details);

        // Badge du nombre de participants
        Label badgeParticipants = new Label(participants.size() + " participants");
        badgeParticipants.getStyleClass().add("request-badge");
        if (participants.size() > 0) {
            badgeParticipants.getStyleClass().add("approved");
        } else {
            badgeParticipants.getStyleClass().add("pending");
        }
        badgeParticipants.setStyle(badgeParticipants.getStyle() +
                "-fx-font-size: 13px; -fx-padding: 8px 12px;");

        header.getChildren().addAll(formationInfo, badgeParticipants);

        // Liste des participants
        VBox participantsSection = new VBox(8);

        if (participants.isEmpty()) {
            Label aucunParticipant = new Label("Aucun participant inscrit");
            aucunParticipant.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-padding: 10px 0;");
            participantsSection.getChildren().add(aucunParticipant);
        } else {
            Label participantsTitre = new Label("Participants inscrits:");
            participantsTitre.setFont(Font.font("System", FontWeight.BOLD, 14));
            participantsTitre.setStyle("-fx-text-fill: #34495e; -fx-padding: 10px 0 5px 0;");
            participantsSection.getChildren().add(participantsTitre);

            for (Participant p : participants) {
                participantsSection.getChildren().add(creerCardParticipant(p));
            }
        }

        mainCard.getChildren().addAll(header, new Separator(), participantsSection);

        return mainCard;
    }

    private HBox creerCardParticipant(Participant p) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5px;");

        VBox infoParticipant = new VBox(3);
        HBox.setHgrow(infoParticipant, javafx.scene.layout.Priority.ALWAYS);

        Label employeId = new Label("Employé ID: " + p.getEmployeId());
        employeId.setFont(Font.font("System", FontWeight.BOLD, 13));
        employeId.setStyle("-fx-text-fill: #2c3e50;");

        Label dateInscription = new Label("📅 Inscrit le: " + p.getDateInscription());
        dateInscription.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        infoParticipant.getChildren().addAll(employeId, dateInscription);

        // Statut actuel
        Label statutLabel = new Label(p.getResultat());
        statutLabel.getStyleClass().add("request-badge");
        if (p.getResultat().toLowerCase().contains("validé") ||
                p.getResultat().toLowerCase().contains("approuvé")) {
            statutLabel.getStyleClass().add("approved");
        } else {
            statutLabel.getStyleClass().add("pending");
        }

        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnValider = new Button("✓ Valider");
        btnValider.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-padding: 6px 12px; -fx-background-radius: 4px; " +
                "-fx-cursor: hand; -fx-font-size: 11px;");
        btnValider.setOnAction(e -> changerStatut(p, "Validé"));

        Button btnRefuser = new Button("✗ Refuser");
        btnRefuser.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 6px 12px; -fx-background-radius: 4px; " +
                "-fx-cursor: hand; -fx-font-size: 11px;");
        btnRefuser.setOnAction(e -> changerStatut(p, "Refusé"));

        Button btnSupprimer = new Button("🗑️");
        btnSupprimer.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-padding: 6px 10px; -fx-background-radius: 4px; " +
                "-fx-cursor: hand; -fx-font-size: 11px;");
        btnSupprimer.setOnAction(e -> supprimerParticipant(p));

        actions.getChildren().addAll(btnValider, btnRefuser, btnSupprimer);

        card.getChildren().addAll(infoParticipant, statutLabel, actions);

        return card;
    }

    private void changerStatut(Participant p, String nouveauStatut) {
        try {
            p.setResultat(nouveauStatut);
            serviceParticipant.modifier(p);
            afficherAlerte("Succès", "Statut modifié avec succès!", Alert.AlertType.INFORMATION);
            actualiserDonnees();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void supprimerParticipant(Participant p) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer ce participant?");
        confirmation.setContentText("Cette action est irréversible!");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                serviceParticipant.supprimer(p.getId());
                afficherAlerte("Succès", "Participant supprimé avec succès!", Alert.AlertType.INFORMATION);
                actualiserDonnees();
            } catch (Exception e) {
                afficherAlerte("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private Label creerInfoLabel(String texte) {
        Label label = new Label(texte);
        label.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        return label;
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}