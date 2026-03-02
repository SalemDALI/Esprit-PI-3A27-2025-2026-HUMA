package services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiService {

    // =====================================================
    // Colle ta cle OpenRouter ici (commence par sk-or-...)
    // Obtenir sur : https://openrouter.ai/keys
    // =====================================================
    private static final String API_KEY = "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";
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";

    private static final String EMERAUDE   = "#50C878";
    private static final String ANTHRACITE = "#303030";
    private static final String FOND       = "#F5F5F5";
    private static final String CORAIL     = "#FF7F50";

    public static void genererDescription(String sujet, String type, String formateur,
                                          int duree, String localisation,
                                          java.util.function.Consumer<String> onDescriptionGeneree) {
        Stage stage = new Stage();
        stage.setTitle("IA - Description de la Formation");
        stage.setResizable(false);

        Label logoLabel = new Label("HUMA");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sousTitre = new Label("Assistant IA — Gemini via OpenRouter");
        sousTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75);");
        VBox logoBox = new VBox(2, logoLabel, sousTitre);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: " + ANTHRACITE + ";");
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        header.getChildren().add(logoBox);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: " + EMERAUDE + ";");
        progress.setPrefSize(50, 50);

        Label loadingLabel = new Label("Generation en cours pour : " + sujet);
        loadingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        VBox loadingBox = new VBox(15, progress, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));

        TextArea descArea = new TextArea();
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(8);
        descArea.setStyle(
                "-fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                        "-fx-border-color: " + EMERAUDE + "; -fx-border-width: 2px;" +
                        "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px;"
        );
        descArea.setVisible(false);

        Label infoLabel = new Label("Description generee par IA");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + EMERAUDE + "; -fx-font-weight: bold;");
        infoLabel.setVisible(false);

        Button btnUtiliser = new Button("Utiliser cette description");
        btnUtiliser.setStyle(
                "-fx-background-color: " + EMERAUDE + "; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8px 18px;" +
                        "-fx-background-radius: 8px; -fx-cursor: hand;"
        );
        btnUtiliser.setVisible(false);

        Button btnRegenerer = new Button("Regenerer");
        btnRegenerer.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-padding: 8px 18px;" +
                        "-fx-background-radius: 8px; -fx-cursor: hand;"
        );
        btnRegenerer.setVisible(false);

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle(
                "-fx-background-color: " + CORAIL + "; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-padding: 8px 18px;" +
                        "-fx-background-radius: 8px; -fx-cursor: hand;"
        );
        btnFermer.setOnAction(e -> stage.close());

        HBox btnBox = new HBox(10, btnUtiliser, btnRegenerer, btnFermer);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 15, 0));
        btnBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1px 0 0 0;");

        VBox body = new VBox(10, loadingBox, infoLabel, descArea);
        body.setPadding(new Insets(15, 20, 10, 20));
        body.setStyle("-fx-background-color: " + FOND + ";");

        VBox root = new VBox(0, header, body, btnBox);
        root.setStyle("-fx-background-color: " + FOND + ";");

        Scene scene = new Scene(root, 520, 420);
        stage.setScene(scene);
        stage.show();

        Thread thread = new Thread(() -> {
            String description = appelAPI(sujet, type, formateur, duree, localisation);
            Platform.runLater(() -> {
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);

                if (description != null && !description.isEmpty()) {
                    descArea.setText(description);
                    descArea.setVisible(true);
                    infoLabel.setVisible(true);
                    btnUtiliser.setVisible(true);
                    btnRegenerer.setVisible(true);

                    btnUtiliser.setOnAction(e -> {
                        if (onDescriptionGeneree != null) onDescriptionGeneree.accept(description);
                        stage.close();
                    });
                    btnRegenerer.setOnAction(e -> {
                        stage.close();
                        genererDescription(sujet, type, formateur, duree, localisation, onDescriptionGeneree);
                    });
                } else {
                    Label errLabel = new Label(
                            "Impossible de generer la description.\n" +
                                    "Verifiez votre cle sur : https://openrouter.ai/keys"
                    );
                    errLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-wrap-text: true;");
                    errLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                    body.getChildren().add(errLabel);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static String appelAPI(String sujet, String type, String formateur,
                                   int duree, String localisation) {
        try {
            String prompt = "Tu es un expert en formation professionnelle. " +
                    "Genere une description professionnelle (3-4 phrases) en francais pour : " +
                    "Sujet=" + sujet + ", Type=" + type + ", Formateur=" + formateur +
                    ", Duree=" + duree + " jours, Lieu=" + localisation +
                    ". Reponds uniquement avec la description, sans titre.";

            // ✅ Modele gratuit correct sur OpenRouter
            String requestBody = "{"
                    + "\"model\": \"mistralai/mistral-7b-instruct:free\","
                    + "\"messages\": [{"
                    + "\"role\": \"user\","
                    + "\"content\": \"" + prompt.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
                    + "}]"
                    + "}";

            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("HTTP-Referer", "https://huma-formation.tn");
            conn.setRequestProperty("X-Title", "HUMA Formation");
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("=== OpenRouter response: " + responseCode + " ===");

            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }

                String json = response.toString();
                System.out.println("=== JSON debut: " + json.substring(0, Math.min(300, json.length())) + " ===");

                int contentStart = json.indexOf("\"content\":\"") + 11;
                if (contentStart > 11) {
                    int idx = contentStart;
                    StringBuilder sb = new StringBuilder();
                    while (idx < json.length()) {
                        char c = json.charAt(idx);
                        if (c == '\\' && idx + 1 < json.length()) {
                            char next = json.charAt(idx + 1);
                            if (next == 'n')  { sb.append('\n'); idx += 2; continue; }
                            if (next == '"')  { sb.append('"');  idx += 2; continue; }
                            if (next == '\\') { sb.append('\\'); idx += 2; continue; }
                        }
                        if (c == '"') break;
                        sb.append(c);
                        idx++;
                    }
                    return sb.toString().trim();
                }
                return null;

            } else {
                StringBuilder errBody = new StringBuilder();
                try {
                    BufferedReader errReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) errBody.append(errLine);
                    errReader.close();
                } catch (Exception ignored) {}
                System.err.println("=== ERREUR " + responseCode + " : " + errBody + " ===");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Erreur reseau : " + e.getMessage());
            return null;
        }
    }
}