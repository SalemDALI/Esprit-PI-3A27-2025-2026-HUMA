package tn.esprit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.CrudFormation;
import services.CrudParticipant;

public class HomeController {

    @FXML private Label totalFormationsLabel;
    @FXML private Label totalParticipantsLabel;

    private CrudFormation serviceFormation = new CrudFormation();
    private CrudParticipant serviceParticipant = new CrudParticipant();

    @FXML
    public void initialize() {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        try {
            int totalFormations = serviceFormation.afficherAll().size();
            int totalParticipants = serviceParticipant.afficherAll().size();

            totalFormationsLabel.setText(String.valueOf(totalFormations));
            totalParticipantsLabel.setText(String.valueOf(totalParticipants));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques : " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirGestionFormations() {
        ouvrirNouvelleScene("/GestionFormations.fxml", "Gestion des Formations");
    }

    @FXML
    public void ouvrirEspaceParticipants() {
        ouvrirNouvelleScene("/EspaceParticipants.fxml", "Espace Participants");
    }

    @FXML
    public void ouvrirTableauBordAdmin() {
        ouvrirNouvelleScene("/AdminDashboard.fxml", "Tableau de Bord Administrateur");
    }

    /**
     * Remplace la scène dans la MÊME fenêtre (comme goAdmin dans ton exemple)
     * Utilise totalFormationsLabel pour récupérer le Stage courant
     */
    private void ouvrirNouvelleScene(String fxmlFile, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Récupère le Stage actuel via un nœud déjà présent dans la scène
            Stage stage = (Stage) totalFormationsLabel.getScene().getWindow();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}