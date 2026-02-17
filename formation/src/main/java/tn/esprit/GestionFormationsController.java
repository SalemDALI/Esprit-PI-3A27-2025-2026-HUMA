package tn.esprit;

import javafx.collections.FXCollections;
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
import services.CrudFormation;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class GestionFormationsController {

    @FXML private TextField sujetField;
    @FXML private TextField formateurField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private TextField dureeField;
    @FXML private TextField localisationField;
    @FXML private TextField idFormationField;
    @FXML private TextField rechercheField;
    @FXML private VBox formationsContainer;
    @FXML private Label totalFormationsLabel;

    private CrudFormation serviceFormation = new CrudFormation();

    @FXML
    public void initialize() {
        // ✅ CORRECTION : Charger les items du ComboBox ici dans le controller
        typeCombo.setItems(FXCollections.observableArrayList(
                "Presentiel",
                "En ligne",
                "Hybride"
        ));

        actualiserListe();

        // Recherche en temps reel
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> actualiserListe());
    }

    // ======================== AJOUTER ========================
    @FXML
    public void ajouterFormation() {
        try {
            if (!validerChamps()) {
                afficherAlerte("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
                return;
            }

            Formation formation = new Formation(
                    sujetField.getText().trim(),
                    formateurField.getText().trim(),
                    typeCombo.getValue(),
                    Date.valueOf(dateDebutPicker.getValue()),
                    Integer.parseInt(dureeField.getText().trim()),
                    localisationField.getText().trim()
            );

            serviceFormation.ajouter(formation);
            afficherAlerte("Succes", "Formation ajoutee avec succes !", Alert.AlertType.INFORMATION);
            reinitialiserFormulaire();
            actualiserListe();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "La duree doit etre un nombre entier.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== MODIFIER ========================
    @FXML
    public void modifierFormation() {
        try {
            if (idFormationField.getText().isEmpty()) {
                afficherAlerte("Attention", "Cliquez sur 'Modifier' dans une carte pour selectionner une formation.", Alert.AlertType.WARNING);
                return;
            }
            if (!validerChamps()) {
                afficherAlerte("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
                return;
            }

            Formation formation = new Formation(
                    Integer.parseInt(idFormationField.getText()),
                    sujetField.getText().trim(),
                    formateurField.getText().trim(),
                    typeCombo.getValue(),
                    Date.valueOf(dateDebutPicker.getValue()),
                    Integer.parseInt(dureeField.getText().trim()),
                    localisationField.getText().trim()
            );

            serviceFormation.modifier(formation);
            afficherAlerte("Succes", "Formation modifiee avec succes !", Alert.AlertType.INFORMATION);
            reinitialiserFormulaire();
            actualiserListe();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "La duree doit etre un nombre entier.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== ACTUALISER LISTE ========================
    @FXML
    public void actualiserListe() {
        try {
            formationsContainer.getChildren().clear();
            List<Formation> formations = serviceFormation.afficherAll();

            String recherche = rechercheField.getText().toLowerCase().trim();
            if (!recherche.isEmpty()) {
                formations = formations.stream()
                        .filter(f -> f.getSujet().toLowerCase().contains(recherche)
                                || f.getFormateur().toLowerCase().contains(recherche)
                                || f.getLocalisation().toLowerCase().contains(recherche))
                        .toList();
            }

            totalFormationsLabel.setText(String.valueOf(formations.size()));

            if (formations.isEmpty()) {
                Label empty = new Label("Aucune formation trouvee.");
                empty.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-padding: 20px;");
                formationsContainer.getChildren().add(empty);
            } else {
                for (Formation f : formations) {
                    formationsContainer.getChildren().add(creerCardFormation(f));
                }
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors du chargement : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== CARTE FORMATION ========================
    private VBox creerCardFormation(Formation f) {
        VBox card = new VBox(8);
        card.getStyleClass().add("request-item");
        card.setPadding(new Insets(15));

        // Titre
        Label titre = new Label(f.getSujet());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setStyle("-fx-text-fill: #2c3e50;");

        // Details
        VBox details = new VBox(4);
        details.getChildren().addAll(
                creerDetailLabel("Formateur : " + f.getFormateur()),
                creerDetailLabel("Type      : " + f.getType()),
                creerDetailLabel("Date      : " + f.getDateDebut()),
                creerDetailLabel("Duree     : " + f.getDuree() + " jours"),
                creerDetailLabel("Lieu      : " + f.getLocalisation())
        );

        // Boutons
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_LEFT);
        boutons.setPadding(new Insets(10, 0, 0, 0));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;" +
                " -fx-padding: 6px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> chargerFormationPourModification(f));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                " -fx-padding: 6px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerFormation(f.getId()));

        boutons.getChildren().addAll(btnModifier, btnSupprimer);
        card.getChildren().addAll(titre, details, boutons);
        return card;
    }

    private Label creerDetailLabel(String texte) {
        Label label = new Label(texte);
        label.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        return label;
    }

    // ======================== CHARGER POUR MODIFICATION ========================
    private void chargerFormationPourModification(Formation f) {
        idFormationField.setText(String.valueOf(f.getId()));
        sujetField.setText(f.getSujet());
        formateurField.setText(f.getFormateur());
        typeCombo.setValue(f.getType());
        dateDebutPicker.setValue(f.getDateDebut().toLocalDate());
        dureeField.setText(String.valueOf(f.getDuree()));
        localisationField.setText(f.getLocalisation());
    }

    // ======================== SUPPRIMER ========================
    private void supprimerFormation(int id) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la formation ?");
        confirmation.setContentText("Cette action est irreversible !");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                serviceFormation.supprimer(id);
                afficherAlerte("Succes", "Formation supprimee !", Alert.AlertType.INFORMATION);
                actualiserListe();
            } catch (Exception e) {
                afficherAlerte("Erreur", "Erreur suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // ======================== REINITIALISER ========================
    @FXML
    public void reinitialiserFormulaire() {
        idFormationField.clear();
        sujetField.clear();
        formateurField.clear();
        typeCombo.setValue(null);
        dateDebutPicker.setValue(null);
        dureeField.clear();
        localisationField.clear();
    }

    // ======================== VALIDATION ========================
    private boolean validerChamps() {
        return !sujetField.getText().trim().isEmpty()
                && !formateurField.getText().trim().isEmpty()
                && typeCombo.getValue() != null
                && dateDebutPicker.getValue() != null
                && !dureeField.getText().trim().isEmpty()
                && !localisationField.getText().trim().isEmpty();
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
    // ======================== RETOUR ========================

}