package com.huma.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public final class ViewNavigator {
    private ViewNavigator() {
    }

    public static void loadScene(Stage stage, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(ViewNavigator.class.getResource(fxmlPath));
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            AlertUtil.error("Erreur interface", "Chargement impossible: " + e.getMessage());
        }
    }

    public static FXMLLoader loadCenter(BorderPane container, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            container.setCenter(root);
            return loader;
        } catch (IOException e) {
            AlertUtil.error("Erreur interface", "Chargement impossible: " + e.getMessage());
            return null;
        }
    }

    public static FXMLLoader openModal(String fxmlPath, String title, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root));
            stage.showAndWait();
            return loader;
        } catch (IOException e) {
            AlertUtil.error("Erreur interface", "Ouverture de la fenetre impossible: " + e.getMessage());
            return null;
        }
    }
}
