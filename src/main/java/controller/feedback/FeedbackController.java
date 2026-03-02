package controller.feedback;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.feedback.Feedback;
import services.feedback.ServiceFeedback;

import java.util.List;

public class FeedbackController {

    private static final List<String> STATUTS = FXCollections.observableArrayList("EN_ATTENTE", "TRAITE", "REJETE");

    @FXML private TextField txtSearchFeedback;
    @FXML private ComboBox<String> cbFilterStatut;
    @FXML private FlowPane flowFeedbacks;
    @FXML private Label lblPageMessage;
    @FXML private ScrollPane scrollFeedbacks;

    private final ServiceFeedback serviceFeedback = new ServiceFeedback();

    @FXML
    public void initialize() {
        if (cbFilterStatut != null) {
            cbFilterStatut.setItems(FXCollections.observableArrayList("", "EN_ATTENTE", "TRAITE", "REJETE"));
        }
        refreshFeedbacks();
    }

    /** Rafraîchit la liste des feedbacks selon la recherche et filtre */
    @FXML
    public void refreshFeedbacks() {
        if (flowFeedbacks == null) return;
        flowFeedbacks.getChildren().clear();
        String search = (txtSearchFeedback != null && txtSearchFeedback.getText() != null) ? txtSearchFeedback.getText().trim() : "";
        String statut = (cbFilterStatut != null) ? cbFilterStatut.getValue() : null;
        if (statut != null && statut.isEmpty()) statut = null;

        List<Feedback> feedbacks = serviceFeedback.getFiltered(search, statut);
        if (lblPageMessage != null) {
            if (feedbacks.isEmpty()) {
                lblPageMessage.setText("Aucun feedback trouvé.");
            } else {
                lblPageMessage.setText("");
            }
        }
        feedbacks.forEach(f -> flowFeedbacks.getChildren().add(createFeedbackCard(f)));
    }

    /** Crée une carte Feedback avec boutons modifier / supprimer */
    private VBox createFeedbackCard(Feedback f) {
        Label lblEmail = new Label("Email: " + (f.getEmail() != null ? f.getEmail() : ""));
        Label lblMessage = new Label("Message: " + (f.getMessage() != null ? f.getMessage() : ""));
        Label lblStatut = new Label("Statut: " + (f.getStatut() != null ? f.getStatut() : ""));

        Button btnEdit = new Button("Modifier");
        btnEdit.setOnAction(e -> openFeedbackDialog(f));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setOnAction(e -> deleteFeedback(f));

        HBox actions = new HBox(10, btnEdit, btnDelete);
        VBox card = new VBox(5, lblEmail, lblMessage, lblStatut, actions);
        card.setStyle("-fx-padding:10; -fx-border-color:#ccc; -fx-border-radius:5; -fx-background-radius:5; -fx-background-color:#f8f8f8;");
        return card;
    }

    /** Ouvre le dialog Ajouter / Modifier feedback (CRUD) */
    @FXML
    private void addFeedbackQuick() {
        openFeedbackDialog(null);
    }

    private void openFeedbackDialog(Feedback existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Feedback" : "Modifier Feedback");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField email = new TextField(existing == null ? "" : (existing.getEmail() != null ? existing.getEmail() : ""));
        email.setPromptText("email@exemple.com");
        TextArea message = new TextArea(existing == null ? "" : (existing.getMessage() != null ? existing.getMessage() : ""));
        message.setPromptText("Message du feedback...");
        message.setPrefRowCount(4);
        message.setWrapText(true);
        ComboBox<String> statut = new ComboBox<>();
        statut.getItems().setAll(STATUTS);
        statut.setValue(existing == null ? "EN_ATTENTE" : (existing.getStatut() != null ? existing.getStatut() : "EN_ATTENTE"));

        VBox form = new VBox(8,
                new Label("Email"), email,
                new Label("Message"), message,
                new Label("Statut"), statut
        );
        form.setPrefWidth(400);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != saveType) return;
            String emailVal = email.getText() == null ? "" : email.getText().trim();
            String messageVal = message.getText() == null ? "" : message.getText().trim();
            String statutVal = statut.getValue() == null ? "EN_ATTENTE" : statut.getValue();
            if (emailVal.isEmpty() || messageVal.isEmpty()) {
                showError("Email et message sont obligatoires.");
                return;
            }
            try {
                Feedback f = existing == null ? new Feedback() : existing;
                f.setEmail(emailVal);
                f.setMessage(messageVal);
                f.setStatut(statutVal);
                boolean ok = existing == null ? serviceFeedback.ajouter(f) : serviceFeedback.update(f);
                if (ok) {
                    setMessage("Feedback enregistré.", false);
                    refreshFeedbacks();
                } else {
                    showError("Enregistrement impossible.");
                }
            } catch (Exception ex) {
                showError("Erreur: " + ex.getMessage());
            }
        });
    }

    private void deleteFeedback(Feedback f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer ce feedback ?");
        confirm.setContentText("Email: " + (f.getEmail() != null ? f.getEmail() : "") + "\nMessage: " + (f.getMessage() != null ? f.getMessage() : ""));
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                if (serviceFeedback.delete(f.getId())) {
                    setMessage("Feedback supprimé.", false);
                    refreshFeedbacks();
                } else {
                    showError("Suppression impossible.");
                }
            }
        });
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
