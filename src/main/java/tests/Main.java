package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 820;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("TOKEN = " + System.getenv("LINKEDIN_ACCESS_TOKEN"));
        System.out.println("ORG ID = " + System.getenv("LINKEDIN_ORGANIZATION_ID"));

        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource("/fxml/feedback/login.fxml"))
        ));

        stage.setTitle("Gestion RH");
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMaxWidth(WINDOW_WIDTH);
        stage.setMaxHeight(WINDOW_HEIGHT);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
