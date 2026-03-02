package tests;

import controller.recrutement.ResetPasswordController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 820;

    @Override
    public void start(Stage stage) throws Exception {

        primaryStage = stage;

        System.out.println("TOKEN = " + System.getenv("LINKEDIN_ACCESS_TOKEN"));
        System.out.println("ORG ID = " + System.getenv("LINKEDIN_ORGANIZATION_ID"));

        loadScene("/fxml/feedback/login.fxml", "Gestion RH");
    }

    public static void loadScene(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // 🔹 Méthode spéciale pour ouvrir ResetPassword avec token
    public static void openResetPassword(String token) throws Exception {

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/feedback/ResetPassword.fxml"));
        Scene scene = new Scene(loader.load());

        // récupérer controller
        ResetPasswordController controller = loader.getController();
        controller.setToken(token);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Réinitialiser mot de passe");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}