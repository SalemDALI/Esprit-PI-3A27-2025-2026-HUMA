package controller.feedback;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.feedback.Feedback;
import models.feedback.User;
import services.feedback.ServiceFeedback;
import controller.congesAbsences.DemandeCongeController;
import utils.Session;

import java.io.IOException;
import java.util.List;

public class FeedbackController {

    private static final List<String> STATUTS = FXCollections.observableArrayList("EN_ATTENTE", "TRAITE", "REJETE");

    @FXML private Label lblTitleFeedback;
    @FXML private TextField txtSearchFeedback;
    @FXML private ComboBox<String> cbFilterStatut;
    @FXML private FlowPane flowFeedbacks;
    @FXML private Label lblPageMessage;
    @FXML private ScrollPane scrollFeedbacks;

    private final ServiceFeedback serviceFeedback = new ServiceFeedback();

    private boolean isAdminRh() {
        User u = Session.getUser();
        if (u == null || u.getRole() == null) return false;
        String r = u.getRole().toUpperCase().replace("_", "").replace("-", "");
        return r.startsWith("ADMIN");
    }

    private String currentUserEmail() {
        User u = Session.getUser();
        return u != null && u.getEmail() != null ? u.getEmail() : null;
    }

    @FXML
    public void initialize() {
        if (cbFilterStatut != null) {
            cbFilterStatut.setItems(FXCollections.observableArrayList("", "EN_ATTENTE", "TRAITE", "REJETE"));
        }
        if (lblTitleFeedback != null) {
            lblTitleFeedback.setText(isAdminRh() ? "Admin RH - Gestion des Feedbacks (traiter les demandes)" : "Mes Feedbacks");
        }
        refreshFeedbacks();
    }

    /** Rafraîchit la liste: admin = tous, employé = les siens (par email). */
    @FXML
    public void refreshFeedbacks() {
        if (flowFeedbacks == null) return;
        flowFeedbacks.getChildren().clear();
        String search = (txtSearchFeedback != null && txtSearchFeedback.getText() != null) ? txtSearchFeedback.getText().trim() : "";
        String statut = (cbFilterStatut != null) ? cbFilterStatut.getValue() : null;
        if (statut != null && statut.isEmpty()) statut = null;

        String userEmail = isAdminRh() ? null : currentUserEmail();
        List<Feedback> feedbacks = serviceFeedback.getFilteredByUserEmail(userEmail, search, statut);
        if (lblPageMessage != null) {
            if (feedbacks.isEmpty()) {
                lblPageMessage.setText(isAdminRh() ? "Aucun feedback trouvé." : "Aucun feedback de votre part.");
            } else {
                lblPageMessage.setText("");
            }
        }
        feedbacks.forEach(f -> flowFeedbacks.getChildren().add(createFeedbackCard(f)));
    }

    /** Crée une carte: admin peut tout modifier (traiter statut); employé seulement modifier message / supprimer les siens. */
    private VBox createFeedbackCard(Feedback f) {
        Label lblEmail = new Label("Email: " + (f.getEmail() != null ? f.getEmail() : ""));
        Label lblMessage = new Label("Message: " + (f.getMessage() != null ? f.getMessage() : ""));
        Label lblStatut = new Label("Statut: " + (f.getStatut() != null ? f.getStatut() : ""));

        boolean admin = isAdminRh();
        boolean isOwn = !admin && (currentUserEmail() != null && currentUserEmail().equals(f.getEmail()));

        Button btnEdit = new Button(admin ? "Traiter" : "Modifier");
        btnEdit.setOnAction(e -> openFeedbackDialog(f));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setOnAction(e -> deleteFeedback(f));
        btnDelete.setDisable(!admin && !isOwn);

        HBox actions = new HBox(10, btnEdit, btnDelete);
        VBox card = new VBox(5, lblEmail, lblMessage, lblStatut, actions);
        card.setStyle("-fx-padding:10; -fx-border-color:#ccc; -fx-border-radius:5; -fx-background-radius:5; -fx-background-color:#f8f8f8;");
        return card;
    }

    @FXML
    private void addFeedbackQuick() {
        openFeedbackDialog(null);
    }

    private void openFeedbackDialog(Feedback existing) {
        boolean admin = isAdminRh();
        String userEmail = currentUserEmail();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Feedback" : (admin ? "Traiter le feedback" : "Modifier mon feedback"));
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField email = new TextField(existing == null ? (admin ? "" : (userEmail != null ? userEmail : "")) : (existing.getEmail() != null ? existing.getEmail() : ""));
        email.setPromptText("email@exemple.com");
        email.setDisable(!admin && existing == null); // employé: email verrouillé à la création
        if (!admin && existing != null) email.setDisable(true); // employé: pas modifier email

        TextArea message = new TextArea(existing == null ? "" : (existing.getMessage() != null ? existing.getMessage() : ""));
        message.setPromptText("Message du feedback...");
        message.setPrefRowCount(4);
        message.setWrapText(true);

        ComboBox<String> statut = new ComboBox<>();
        statut.getItems().setAll(STATUTS);
        statut.setValue(existing == null ? "EN_ATTENTE" : (existing.getStatut() != null ? existing.getStatut() : "EN_ATTENTE"));
        statut.setDisable(!admin); // seul l'admin RH peut changer le statut (traiter)

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
                boolean ok;
                if (existing == null) {
                    ok = serviceFeedback.ajouter(f);
                } else {
                    if (admin) {
                        ok = serviceFeedback.update(f);
                    } else {
                        ok = serviceFeedback.updateByUser(f, userEmail);
                    }
                }
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
        boolean admin = isAdminRh();
        String userEmail = currentUserEmail();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer ce feedback ?");
        confirm.setContentText("Email: " + (f.getEmail() != null ? f.getEmail() : "") + "\nMessage: " + (f.getMessage() != null ? f.getMessage() : ""));
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                boolean ok = admin ? serviceFeedback.delete(f.getId()) : serviceFeedback.deleteByUser(f.getId(), userEmail);
                if (ok) {
                    setMessage("Feedback supprimé.", false);
                    refreshFeedbacks();
                } else {
                    showError("Suppression impossible.");
                }
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
