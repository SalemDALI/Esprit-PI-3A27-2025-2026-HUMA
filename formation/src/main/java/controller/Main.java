package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML de la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Créer la scène avec le contenu chargé
            Scene scene = new Scene(root);

            // Configurer la fenêtre principale
            primaryStage.setTitle("Système de Gestion des Formations");
            primaryStage.setScene(scene);

            // Optionnel : Définir une taille minimale
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Afficher la fenêtre
            primaryStage.show();

            System.out.println("✅ Application démarrée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage de l'application:");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args);
    }
}