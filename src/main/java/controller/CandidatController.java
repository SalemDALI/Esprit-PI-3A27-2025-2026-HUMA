package controller;

import models.recrutement.Candidature;
import models.recrutement.Entretien;
import models.recrutement.OffreEmploi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.recrutement.ServiceCandidature;
import services.recrutement.ServiceEntretien;
import services.recrutement.ServiceOffre;
import utils.CvFileStorageUtil;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CandidatController {

    @FXML
    private VBox tableOffres;

    @FXML
    private VBox tableCandidatures;

    @FXML
    private Label lblCvPath;
    @FXML
    private Label lblPageMessage;

    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private final ServiceEntretien serviceEntretien = new ServiceEntretien();
    private String selectedCvPath;
    private OffreEmploi selectedOffre;
    private VBox selectedOffreCard;

    @FXML
    public void initialize() {
        renderOffresCards();
        setPageMessage("", false);
        refreshSuiviCandidatures();
    }

    @FXML
    public void postuler() {
        OffreEmploi offre = selectedOffre;
        if (offre == null) {
            showError("Selectionnez une offre avant de postuler.");
            return;
        }
        if (selectedCvPath == null || selectedCvPath.isBlank()) {
            showError("Veuillez telecharger un CV PDF.");
            return;
        }
        if (Session.getUser() == null) {
            showError("Session invalide. Reconnectez-vous.");
            return;
        }

        Candidature c = new Candidature();
        c.setCandidatId(Session.getUser().getId());
        c.setOffreId(offre.getId());
        c.setDateCandidature(LocalDate.now());
        c.setStatut("EN_ATTENTE");
        c.setCheminCv(selectedCvPath);

        if (serviceCandidature.ajouter(c)) {
            refreshSuiviCandidatures();
            selectedCvPath = null;
            lblCvPath.setText("Aucun CV selectionne");
            showInfo("Candidature envoyee avec succes.");
        } else {
            showError("Echec lors de l'envoi de la candidature.");
        }
    }

    @FXML
    public void uploadCv(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selectionner un CV (PDF)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        File selected = chooser.showOpenDialog(((Stage) tableOffres.getScene().getWindow()));
        if (selected == null) {
            return;
        }

        if (!selected.getName().toLowerCase().endsWith(".pdf")) {
            showError("Le fichier doit etre au format PDF.");
            return;
        }

        try {
            if (Session.getUser() == null) {
                showError("Session invalide. Reconnectez-vous.");
                return;
            }
            selectedCvPath = CvFileStorageUtil.savePdfCv(selected.toPath(), Session.getUser().getId());
            lblCvPath.setText(selectedCvPath);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Erreur lors de la sauvegarde du CV: " + e.getMessage());
        }
    }

    @FXML
    public void refreshSuiviCandidatures() {
        if (Session.getUser() == null) {
            tableCandidatures.getChildren().clear();
            return;
        }
        tableCandidatures.getChildren().clear();
        List<Entretien> entretiens = serviceEntretien.getByCandidatId(Session.getUser().getId());
        Map<Integer, Entretien> latestByCandidature = entretiens.stream()
                .collect(Collectors.toMap(
                        Entretien::getCandidatureId,
                        e -> e,
                        (a, b) -> {
                            if (a.getDateEntretien() == null) return b;
                            if (b.getDateEntretien() == null) return a;
                            return a.getDateEntretien().isAfter(b.getDateEntretien()) ? a : b;
                        }
                ));

        for (Candidature candidature : serviceCandidature.getByCandidatId(Session.getUser().getId())) {
            VBox card = buildCard(
                    candidature.getOffreTitre() == null ? "Offre" : candidature.getOffreTitre(),
                    "Date: " + candidature.getDateCandidature() + " | Statut candidature: " + candidature.getStatut()
            );
            Entretien entretien = latestByCandidature.get(candidature.getId());
            if (entretien != null) {
                Label entretienLabel = new Label(
                        "Entretien: " + entretien.getDateEntretien() + " | Manager: " + safe(entretien.getManagerNom())
                                + " | Statut: " + safe(entretien.getStatut())
                );
                entretienLabel.getStyleClass().add("entity-card-meta");
                Button startButton = new Button("Démarrer entretien");
                startButton.getStyleClass().add("btn");
                startButton.setOnAction(e -> openEntretienWebView(entretien.getMeetLink(), "Entretien Candidat"));
                HBox actions = new HBox(8, startButton);
                card.getChildren().addAll(entretienLabel, actions);
            }
            tableCandidatures.getChildren().add(card);
        }
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) tableOffres.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/feedback/login.fxml"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        setPageMessage(message, true);
    }

    private void showInfo(String message) {
        setPageMessage(message, false);
    }

    private void setPageMessage(String message, boolean isError) {
        if (lblPageMessage == null) {
            return;
        }
        lblPageMessage.setText(message == null ? "" : message);
        if (isError) {
            lblPageMessage.setStyle("-fx-text-fill: #d64545; -fx-font-weight: 700;");
        } else {
            lblPageMessage.setStyle("-fx-text-fill: #2f855a; -fx-font-weight: 700;");
        }
    }

    private void renderOffresCards() {
        tableOffres.getChildren().clear();
        selectedOffre = null;
        selectedOffreCard = null;

        for (OffreEmploi offre : serviceOffre.getAll()) {
            VBox card = buildCard(
                    offre.getTitre(),
                    "Departement: " + offre.getDepartement()
            );
            card.setOnMouseClicked(event -> {
                if (selectedOffreCard != null) {
                    selectedOffreCard.getStyleClass().remove("entity-card-selected");
                }
                card.getStyleClass().add("entity-card-selected");
                selectedOffreCard = card;
                selectedOffre = offre;
                setPageMessage("Offre selectionnee: " + offre.getTitre(), false);
            });
            tableOffres.getChildren().add(card);
        }
    }

    private VBox buildCard(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("entity-card-title");
        titleLabel.setWrapText(true);
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("entity-card-meta");
        subtitleLabel.setWrapText(true);
        VBox card = new VBox(4, titleLabel, subtitleLabel);
        card.getStyleClass().add("entity-card");
        return card;
    }

    private void openEntretienWebView(String url, String title) {
        if (url == null || url.isBlank()) {
            showError("Lien entretien manquant.");
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI.create(url));
                showInfo("Ouverture entretien dans navigateur.");
                return;
            }
            Stage stage = new Stage();
            WebView webView = new WebView();
            webView.getEngine().load(url);
            stage.setScene(new Scene(webView, 1100, 760));
            stage.setTitle(title == null ? "Entretien" : title);
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir l'entretien: " + e.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
