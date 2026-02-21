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
import services.MapService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.sql.Date;
import java.time.LocalDate;
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
        typeCombo.setItems(FXCollections.observableArrayList(
                "Presentiel",
                "En ligne",
                "Hybride"
        ));

        // Autoriser uniquement les chiffres dans le champ durée
        dureeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                dureeField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        actualiserListe();
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> actualiserListe());
    }

    // ======================== AJOUTER ========================
    @FXML
    public void ajouterFormation() {
        String erreur = validerSaisie();
        if (erreur != null) {
            afficherAlerte("Erreur de saisie", erreur, Alert.AlertType.ERROR);
            return;
        }

        try {
            Formation formation = new Formation(
                    sujetField.getText().trim(),
                    formateurField.getText().trim(),
                    typeCombo.getValue(),
                    Date.valueOf(dateDebutPicker.getValue()),
                    Integer.parseInt(dureeField.getText().trim()),
                    localisationField.getText().trim()
            );

            serviceFormation.ajouter(formation);
            afficherAlerte("Succès", "Formation ajoutée avec succès !", Alert.AlertType.INFORMATION);
            reinitialiserFormulaire();
            actualiserListe();

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== MODIFIER ========================
    @FXML
    public void modifierFormation() {
        if (idFormationField.getText().isEmpty()) {
            afficherAlerte("Attention",
                    "Veuillez cliquer sur 'Modifier' dans une carte pour sélectionner une formation.",
                    Alert.AlertType.WARNING);
            return;
        }

        String erreur = validerSaisie();
        if (erreur != null) {
            afficherAlerte("Erreur de saisie", erreur, Alert.AlertType.ERROR);
            return;
        }

        try {
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
            afficherAlerte("Succès", "Formation modifiée avec succès !", Alert.AlertType.INFORMATION);
            reinitialiserFormulaire();
            actualiserListe();

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ======================== VALIDATION SAISIE ========================
    private String validerSaisie() {

        // 1. Sujet
        String sujet = sujetField.getText().trim();
        if (sujet.isEmpty()) return "Le sujet est obligatoire.";
        if (sujet.length() < 3) return "Le sujet doit contenir au moins 3 caractères.";
        if (sujet.length() > 100) return "Le sujet ne doit pas dépasser 100 caractères.";

        // 2. Formateur
        String formateur = formateurField.getText().trim();
        if (formateur.isEmpty()) return "Le nom du formateur est obligatoire.";
        if (formateur.length() < 3) return "Le nom du formateur doit contenir au moins 3 caractères.";
        if (!formateur.matches("[a-zA-ZÀ-ÿ\\s\\-']+"))
            return "Le nom du formateur ne doit contenir que des lettres.";

        // 3. Type
        if (typeCombo.getValue() == null) return "Veuillez sélectionner le type de formation.";

        // 4. Date de début
        if (dateDebutPicker.getValue() == null) return "La date de début est obligatoire.";
        if (dateDebutPicker.getValue().isBefore(LocalDate.now()))
            return "La date de début ne peut pas être dans le passé.";

        // 5. Durée
        String dureeStr = dureeField.getText().trim();
        if (dureeStr.isEmpty()) return "La durée est obligatoire.";
        try {
            int duree = Integer.parseInt(dureeStr);
            if (duree <= 0) return "La durée doit être un nombre positif (minimum 1 jour).";
            if (duree > 365) return "La durée ne peut pas dépasser 365 jours.";
        } catch (NumberFormatException e) {
            return "La durée doit être un nombre entier valide.";
        }

        // 6. Localisation
        String localisation = localisationField.getText().trim();
        if (localisation.isEmpty()) return "La localisation est obligatoire.";
        if (localisation.length() < 2) return "La localisation doit contenir au moins 2 caractères.";

        return null; // tout est valide
    }

    // ======================== CHOISIR LOCALISATION SUR CARTE ========================
    @FXML
    public void choisirSurLaCarte() {
        // Ouvre OpenStreetMap → formateur clique sur une ville
        // → localisationField rempli automatiquement
        MapService.choisirLocalisation(ville -> {
            localisationField.setText(ville);
            System.out.println("✅ Localisation choisie : " + ville);
        });
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
                Label empty = new Label("Aucune formation trouvée.");
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

        Label titre = new Label(f.getSujet());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setStyle("-fx-text-fill: #2c3e50;");

        VBox details = new VBox(4);
        details.getChildren().addAll(
                creerDetailLabel("Formateur : " + f.getFormateur()),
                creerDetailLabel("Type      : " + f.getType()),
                creerDetailLabel("Date      : " + f.getDateDebut()),
                creerDetailLabel("Durée     : " + f.getDuree() + " jours"),
                creerDetailLabel("Lieu      : " + f.getLocalisation())
        );

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
        confirmation.setContentText("Cette action est irréversible !");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                serviceFormation.supprimer(id);
                afficherAlerte("Succès", "Formation supprimée !", Alert.AlertType.INFORMATION);
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
            Stage stage = (Stage) sujetField.getScene().getWindow();
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