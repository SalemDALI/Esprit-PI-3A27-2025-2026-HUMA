package services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class WeatherService {

    // =====================================================
    // REMPLACEZ PAR VOTRE CLE API OpenWeatherMap
    // Inscription gratuite sur : https://openweathermap.org/register
    // =====================================================
    private static final String API_KEY = "713c7b6626efa9af193bb75e9f1ce972";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    // Charte graphique HUMA
    private static final String EMERAUDE   = "#50C878";
    private static final String ANTHRACITE = "#303030";
    private static final String FOND       = "#F5F5F5";
    private static final String CORAIL     = "#FF7F50";

    // =====================================================
    // Affiche la meteo d'une ville dans une fenetre
    // =====================================================
    public static void afficherMeteo(String ville) {
        Stage stage = new Stage();
        stage.setTitle("Meteo - " + ville);
        stage.setResizable(false);

        // Loading label
        Label loading = new Label("Chargement de la meteo...");
        loading.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        VBox loadingBox = new VBox(loading);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefSize(420, 300);
        loadingBox.setStyle("-fx-background-color: " + FOND + ";");

        Scene scene = new Scene(loadingBox, 420, 300);
        stage.setScene(scene);
        stage.show();

        // Appel API dans un thread separé pour ne pas bloquer l'UI
        Thread thread = new Thread(() -> {
            try {
                // Construction de l'URL API
                String urlStr = BASE_URL + "?q=" + ville.replace(" ", "+")
                        + "&appid=" + API_KEY
                        + "&units=metric"        // temperature en Celsius
                        + "&lang=fr";            // descriptions en francais

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    // Lecture de la reponse JSON
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON
                    JSONObject json = new JSONObject(response.toString());

                    // Extraction des donnees
                    double temp        = json.getJSONObject("main").getDouble("temp");
                    double tempMin     = json.getJSONObject("main").getDouble("temp_min");
                    double tempMax     = json.getJSONObject("main").getDouble("temp_max");
                    int    humidity    = json.getJSONObject("main").getInt("humidity");
                    double windSpeed   = json.getJSONObject("wind").getDouble("speed");
                    String description = json.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("description");
                    String iconCode    = json.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("icon");
                    String cityName    = json.getString("name");
                    String country     = json.getJSONObject("sys").getString("country");

                    // Mise a jour de l'UI dans le JavaFX Thread
                    Platform.runLater(() ->
                            afficherResultat(stage, scene, cityName, country,
                                    temp, tempMin, tempMax, humidity, windSpeed, description, iconCode)
                    );

                } else {
                    Platform.runLater(() ->
                            afficherErreur(stage, scene, "Ville introuvable : " + ville)
                    );
                }

            } catch (Exception e) {
                Platform.runLater(() ->
                        afficherErreur(stage, scene, "Erreur reseau : " + e.getMessage())
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // =====================================================
    // Interface meteo avec charte HUMA
    // =====================================================
    private static void afficherResultat(Stage stage, Scene scene,
                                         String ville, String country,
                                         double temp, double tempMin, double tempMax,
                                         int humidity, double windSpeed,
                                         String description, String iconCode) {

        // ===== HEADER =====
        Label logoLabel = new Label("HUMA");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sousTitre = new Label("Meteo de la Formation");
        sousTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75);");
        VBox logoBox = new VBox(2, logoLabel, sousTitre);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Label nuageIcon = new Label(getWeatherEmoji(iconCode));
        nuageIcon.setStyle("-fx-font-size: 30px;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: " + ANTHRACITE + ";");
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        header.getChildren().addAll(logoBox, nuageIcon);

        // ===== VILLE + TEMPERATURE PRINCIPALE =====
        Label villeLabel = new Label(ville + ", " + country);
        villeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        villeLabel.setStyle("-fx-text-fill: " + ANTHRACITE + ";");

        Label tempLabel = new Label(String.format("%.0f°C", temp));
        tempLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 52));
        tempLabel.setStyle("-fx-text-fill: " + EMERAUDE + ";");

        Label descLabel = new Label(capitaliser(description));
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label minMaxLabel = new Label(String.format("↓ %.0f°C   ↑ %.0f°C", tempMin, tempMax));
        minMaxLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

        VBox centreBox = new VBox(4, villeLabel, tempLabel, descLabel, minMaxLabel);
        centreBox.setAlignment(Pos.CENTER_LEFT);
        centreBox.setPadding(new Insets(20, 20, 10, 20));

        // ===== DETAILS : humidite + vent =====
        HBox detailsBox = new HBox(15,
                detailCard("💧", "Humidite", humidity + " %"),
                detailCard("💨", "Vent",     String.format("%.1f km/h", windSpeed * 3.6)),
                detailCard("🌡️", "Ressenti", String.format("%.0f°C", temp))
        );
        detailsBox.setAlignment(Pos.CENTER);
        detailsBox.setPadding(new Insets(5, 20, 15, 20));

        // ===== FOOTER =====
        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle(
                "-fx-background-color: " + CORAIL + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8px 30px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
        );
        btnFermer.setOnAction(e -> stage.close());

        Label sourceLabel = new Label("Source : OpenWeatherMap API");
        sourceLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #bdc3c7;");

        VBox footer = new VBox(6, btnFermer, sourceLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10, 0, 15, 0));
        footer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1px 0 0 0;"
        );

        // ===== LAYOUT FINAL =====
        VBox root = new VBox(0, header, centreBox, detailsBox, footer);
        root.setStyle("-fx-background-color: " + FOND + ";");

        Scene newScene = new Scene(root, 420, 370);
        stage.setScene(newScene);
    }

    // =====================================================
    // Carte detail (humidite, vent...)
    // =====================================================
    private static VBox detailCard(String emoji, String label, String valeur) {
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 22px;");

        Label valLabel = new Label(valeur);
        valLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        valLabel.setStyle("-fx-text-fill: " + "#303030" + ";");

        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");

        VBox card = new VBox(4, emojiLabel, valLabel, lblLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 18, 12, 18));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 6, 0, 0, 2);"
        );
        return card;
    }

    // =====================================================
    // Affiche un message d'erreur
    // =====================================================
    private static void afficherErreur(Stage stage, Scene scene, String message) {
        Label errLabel = new Label("❌ " + message);
        errLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-wrap-text: true;");
        errLabel.setMaxWidth(340);

        Label conseil = new Label("Verifiez votre cle API et votre connexion internet.");
        conseil.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 8px 25px; -fx-background-radius: 6px; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> stage.close());

        VBox errBox = new VBox(15, errLabel, conseil, btnFermer);
        errBox.setAlignment(Pos.CENTER);
        errBox.setPadding(new Insets(30));
        errBox.setStyle("-fx-background-color: " + FOND + ";");

        stage.setScene(new Scene(errBox, 420, 200));
    }

    // =====================================================
    // Emoji meteo selon le code OpenWeatherMap
    // =====================================================
    private static String getWeatherEmoji(String iconCode) {
        if (iconCode == null) return "🌡️";
        switch (iconCode.substring(0, 2)) {
            case "01": return "☀️";   // ciel clair
            case "02": return "⛅";   // quelques nuages
            case "03": return "☁️";   // nuageux
            case "04": return "☁️";   // couvert
            case "09": return "🌧️";  // averses
            case "10": return "🌦️";  // pluie
            case "11": return "⛈️";  // orage
            case "13": return "❄️";   // neige
            case "50": return "🌫️";  // brume
            default:   return "🌡️";
        }
    }

    private static String capitaliser(String texte) {
        if (texte == null || texte.isEmpty()) return texte;
        return Character.toUpperCase(texte.charAt(0)) + texte.substring(1);
    }
}
