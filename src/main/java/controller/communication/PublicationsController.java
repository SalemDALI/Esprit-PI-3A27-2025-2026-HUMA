package controller.communication;

import controller.congesAbsences.DemandeCongeController;
import controller.congesAbsences.ManagerAbsenceController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.communication.Publication;
import models.communication.PublicationComment;
import models.feedback.User;
import services.communication.PublicationService;
import utils.Session;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PublicationsController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label lblInfo;
    @FXML
    private VBox publicationList;
    @FXML
    private TextArea txtComment;

    private final PublicationService publicationService = new PublicationService();
    private Publication selectedPublication;
    private Node selectedCard;

    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user != null) {
            welcomeLabel.setText("Publications - " + user.getNom() + " " + user.getPrenom());
        }
        renderPublications();
    }

    @FXML
    public void refreshPublications() {
        renderPublications();
        setInfo("", false);
    }

    @FXML
    public void addComment() {
        if (selectedPublication == null) {
            setInfo("Selectionnez une publication.", true);
            return;
        }
        String comment = txtComment.getText() == null ? "" : txtComment.getText().trim();
        if (comment.isBlank()) {
            setInfo("Ecrivez un commentaire.", true);
            return;
        }

        User user = Session.getUser();
        String auteur = user == null ? "Utilisateur" : user.getNom() + " " + user.getPrenom();
        if (publicationService.addComment(selectedPublication.getId(), auteur, comment)) {
            txtComment.clear();
            renderPublications();
            setInfo("Commentaire ajoute.", false);
        } else {
            setInfo("Impossible d'ajouter le commentaire.", true);
        }
    }

    @FXML
    public void back(ActionEvent event) {
        User user = Session.getUser();
        if (user == null) {
            logout(event);
            return;
        }

        String role = user.getRole() == null ? "" : user.getRole().trim().toUpperCase();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader;

            if (role.contains("MANAG")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/ManagerAbsence.fxml"));
                Scene scene = new Scene(loader.load());
                ManagerAbsenceController c = loader.getController();
                c.setManagerId(user.getId());
                stage.setScene(scene);
            } else {
                loader = new FXMLLoader(getClass().getResource("/fxml/congesAbsences/DemandeConge.fxml"));
                Scene scene = new Scene(loader.load());
                DemandeCongeController c = loader.getController();
                c.setEmployeId(user.getId());
                stage.setScene(scene);
            }
            stage.show();
        } catch (IOException e) {
            setInfo("Erreur retour: " + e.getMessage(), true);
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
            setInfo("Erreur logout: " + e.getMessage(), true);
        }
    }

    private void renderPublications() {
        publicationList.getChildren().clear();
        selectedPublication = null;
        selectedCard = null;

        List<Publication> all = publicationService.getAll();
        if (all.isEmpty()) {
            publicationList.getChildren().add(buildCard("Aucune publication", "Pas encore de publication admin."));
            return;
        }

        for (Publication publication : all) {
            String date = publication.getDatePublication() == null ? "" :
                    publication.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String latestComment = "Aucun commentaire";
            if (!publication.getCommentaires().isEmpty()) {
                PublicationComment c = publication.getCommentaires().get(publication.getCommentaires().size() - 1);
                latestComment = "Dernier commentaire: " + c.getAuteur() + " - " + c.getContenu();
            }

            VBox card = buildCard(
                    publication.getTitre(),
                    "Par " + publication.getAuteur() + " | " + date + "\n"
                            + publication.getContenu() + "\n" + latestComment
            );
            card.setOnMouseClicked(e -> {
                if (selectedCard != null) {
                    selectedCard.getStyleClass().remove("entity-card-selected");
                }
                selectedCard = card;
                selectedCard.getStyleClass().add("entity-card-selected");
                selectedPublication = publication;
                setInfo("Selection: " + publication.getTitre(), false);
            });
            publicationList.getChildren().add(card);
        }
    }

    private VBox buildCard(String title, String meta) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("entity-card-title");
        Label metaLabel = new Label(meta);
        metaLabel.getStyleClass().add("entity-card-meta");
        metaLabel.setWrapText(true);
        VBox card = new VBox(6, titleLabel, metaLabel);
        card.getStyleClass().add("entity-card");
        return card;
    }

    private void setInfo(String message, boolean isError) {
        if (lblInfo == null) {
            return;
        }
        lblInfo.setText(message == null ? "" : message);
        if (isError) {
            lblInfo.setStyle("-fx-text-fill: #d64545; -fx-font-weight: 700;");
        } else {
            lblInfo.setStyle("-fx-text-fill: #2f855a; -fx-font-weight: 700;");
        }
    }
}
