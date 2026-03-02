package services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PexelsService {

    // =====================================================
    // Unsplash Access Key
    // =====================================================
    private static final String API_KEY  = "TXghlb9cpi86_S6KKCgoLqqi3CGqFeZfT1PFcVeZf08";
    private static final String BASE_URL = "https://api.unsplash.com/photos/random";

    private static final String EMERAUDE   = "#50C878";
    private static final String ANTHRACITE = "#303030";
    private static final String CORAIL     = "#FF7F50";

    // =====================================================
    // Methode utilisee par les cartes catalogue
    // =====================================================
    public static String[] fetchPhotoUrl(String sujet) {
        try {
            String query = URLEncoder.encode(traduire(sujet), StandardCharsets.UTF_8.toString());

            // &w=620&h=320&fit=crop → Unsplash recadre exactement aux dimensions voulues
            String urlStr = BASE_URL
                    + "?query=" + query
                    + "&orientation=landscape"
                    + "&content_filter=high"
                    + "&client_id=" + API_KEY;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Version", "v1");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int responseCode = conn.getResponseCode();
            System.out.println("=== Unsplash [" + sujet + "] -> " + responseCode + " ===");

            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }
                String json = response.toString();

                // Extraire l'URL "raw" et ajouter les parametres de recadrage Unsplash
                // raw = URL originale → on ajoute ?w=620&h=320&fit=crop&auto=format
                String rawKey = "\"raw\":\"";
                int start = json.indexOf(rawKey);
                if (start != -1) {
                    start += rawKey.length();
                    int end = json.indexOf("\"", start);
                    String rawUrl = json.substring(start, end)
                            .replace("\\u0026", "&")
                            .replace("\\u003D", "=");

                    // Ajouter parametres de crop exact 620x320
                    String photoUrl = rawUrl + "&w=620&h=320&fit=crop&crop=center&auto=format&q=80";

                    // Photographe
                    String photographer = "Unsplash";
                    String nameKey = "\"name\":\"";
                    int nameIdx = json.indexOf(nameKey);
                    if (nameIdx != -1) {
                        nameIdx += nameKey.length();
                        int nameEnd = json.indexOf("\"", nameIdx);
                        photographer = json.substring(nameIdx, nameEnd);
                    }

                    System.out.println("=== Unsplash photo OK pour: " + sujet + " ===");
                    return new String[]{photoUrl, photographer};
                }
            } else {
                try {
                    BufferedReader errReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder err = new StringBuilder();
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) err.append(errLine);
                    System.err.println("=== Unsplash ERREUR " + responseCode + ": " + err + " ===");
                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.err.println("=== Unsplash exception: " + e.getMessage() + " ===");
        }
        return fetchPhotoUrlFallback();
    }

    private static String[] fetchPhotoUrlFallback() {
        try {
            String urlStr = BASE_URL
                    + "?query=office+professional+workspace"
                    + "&orientation=landscape"
                    + "&client_id=" + API_KEY;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Version", "v1");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }
                String json = response.toString();
                String rawKey = "\"raw\":\"";
                int start = json.indexOf(rawKey);
                if (start != -1) {
                    start += rawKey.length();
                    int end = json.indexOf("\"", start);
                    String rawUrl = json.substring(start, end).replace("\\u0026", "&");
                    return new String[]{rawUrl + "&w=620&h=320&fit=crop&crop=center&auto=format&q=80", "Unsplash"};
                }
            }
        } catch (Exception e) {
            System.err.println("=== Unsplash fallback exception: " + e.getMessage() + " ===");
        }
        return null;
    }

    // Mots-cles precis pour des photos concretes et professionnelles
    private static String traduire(String sujet) {
        if (sujet == null) return "office desk professional";
        String s = sujet.toLowerCase();
        if (s.contains("java") || s.contains("programmation") || s.contains("code") || s.contains("informatique"))
            return "laptop code programming screen";
        if (s.contains("management") || s.contains("gestion"))
            return "business meeting whiteboard";
        if (s.contains("comptabilit") || s.contains("finance"))
            return "finance documents calculator desk";
        if (s.contains("marketing"))
            return "marketing presentation charts";
        if (s.contains("langue") || s.contains("anglais"))
            return "books open library study";
        if (s.contains("securit"))
            return "server room technology blue";
        if (s.contains("design") || s.contains("graphisme"))
            return "design tools tablet creative";
        if (s.contains("ressource") || s.contains("rh"))
            return "conference room office table";
        if (s.contains("leadership"))
            return "conference room modern office";
        if (s.contains("entreprise") || s.contains("business"))
            return "modern office building glass";
        if (s.contains("france"))
            return "paris eiffel tower architecture";
        if (s.contains("fdf") || s.contains("test"))
            return "office workspace laptop";
        // Recherche directe avec le sujet + mot professionnel
        return sujet + " professional training";
    }

    // =====================================================
    // Affiche une fenetre avec la photo
    // =====================================================
    public static void afficherPhoto(String sujet) {
        Stage stage = new Stage();
        stage.setTitle("HUMA — Photo de la Formation");
        stage.setResizable(false);

        Label logoLabel = new Label("HUMA");
        logoLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sousTitre = new Label("Illustration — Unsplash API");
        sousTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.6);");
        VBox logoBox = new VBox(3, logoLabel, sousTitre);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(logoBox);
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setStyle("-fx-background-color: " + ANTHRACITE + ";");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: " + EMERAUDE + ";");
        progress.setPrefSize(45, 45);
        Label loadingLabel = new Label("Recherche d'une illustration...");
        loadingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
        VBox loadingBox = new VBox(15, progress, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(60, 30, 60, 30));
        loadingBox.setStyle("-fx-background-color: white;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(520);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        Label photographerLabel = new Label("");
        photographerLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");
        VBox imageBox = new VBox(6, imageView, photographerLabel);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPadding(new Insets(15, 20, 10, 20));
        imageBox.setStyle("-fx-background-color: white;");
        imageBox.setVisible(false);

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: " + CORAIL + "; -fx-text-fill: white; -fx-padding: 10px 30px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> stage.close());

        HBox footer = new HBox(btnFermer);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 0, 15, 0));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1px 0 0 0;");

        VBox body = new VBox(0, loadingBox, imageBox);
        VBox root = new VBox(0, header, body, footer);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 560, 460);
        stage.setScene(scene);
        stage.show();

        Thread thread = new Thread(() -> {
            String[] result = fetchPhotoUrl(sujet);
            Platform.runLater(() -> {
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);
                if (result != null && result[0] != null) {
                    Image image = new Image(result[0], true);
                    imageView.setImage(image);
                    imageBox.setVisible(true);
                    photographerLabel.setText("Photo par " + result[1] + " via Unsplash");
                } else {
                    Label err = new Label("Aucune photo trouvee.");
                    err.setStyle("-fx-text-fill: #e74c3c;");
                    body.getChildren().add(err);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
}