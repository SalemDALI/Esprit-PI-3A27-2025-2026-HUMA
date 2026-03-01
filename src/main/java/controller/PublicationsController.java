package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import models.Publication;
import models.PublicationComment;
import models.User;
import services.PublicationService;
import utils.Session;

import java.io.File;
import java.util.*;

public class PublicationsController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label lblInfo;
    @FXML
    private VBox publicationList;
    @FXML
    private Button btnSelectImage;
    @FXML
    private Button btnSelectVideo;
    @FXML
    private TextField txtPublicationTitre;
    @FXML
    private TextArea txtPublicationContenu;

    private final PublicationService publicationService = new PublicationService();
    private final List<String> imagesToAdd = new ArrayList<>();
    private final List<String> videosToAdd = new ArrayList<>();

    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user != null) {
            welcomeLabel.setText("Publications - " + user.getNom());
        }

        renderPublications();
    }



    @FXML
    public void publishCommunication() {
        String titre = txtPublicationTitre.getText().trim();
        String contenu = txtPublicationContenu.getText().trim();

        if (titre.isEmpty() || contenu.isEmpty()) {
            lblInfo.setText("Titre et contenu obligatoires");
            return;
        }

        int pubId = publicationService.addPublicationAndGetId(titre, contenu);

        if (pubId > 0) {

            for (String img : imagesToAdd) {
                publicationService.addMedia(pubId, "image", img);
            }

            for (String vid : videosToAdd) {
                publicationService.addMedia(pubId, "video", vid);
            }

            imagesToAdd.clear();
            videosToAdd.clear();

            txtPublicationTitre.clear();
            txtPublicationContenu.clear();

            lblInfo.setText("Publication ajoutée avec succès");

            renderPublications();
        }
    }

    private void renderPublications() {
        publicationList.getChildren().clear();

        List<Publication> publications = publicationService.getAll();

        for (Publication publication : publications) {

            VBox card = new VBox(5);
            card.getStyleClass().add("entity-card");

            Label titre = new Label(publication.getTitre());
            Label contenu = new Label(publication.getContenu());

            card.getChildren().addAll(titre, contenu);

            List<Map<String, String>> medias =
                    publicationService.getMedia(publication.getId());

            for (Map<String, String> media : medias) {
                card.getChildren().add(createMediaNode(media));
            }

            publicationList.getChildren().add(card);
        }
    }

    private Node createMediaNode(Map<String, String> media) {

        String type = media.get("type");
        String path = media.get("path");

        HBox box = new HBox();

        if ("image".equals(type)) {
            ImageView img = new ImageView(new Image("file:" + path));
            img.setFitWidth(120);
            img.setPreserveRatio(true);
            box.getChildren().add(img);
        } else {
            Label video = new Label("Vidéo : " + new File(path).getName());
            box.getChildren().add(video);
        }

        return box;
    }
    private void setInfo(String message, boolean isError) {
        if (lblInfo == null) return;

        lblInfo.setText(message == null ? "" : message);

        if (isError) {
            lblInfo.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            lblInfo.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
    }
}