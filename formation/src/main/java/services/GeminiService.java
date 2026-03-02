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
    // CLE API GROQ - Gratuite, fonctionne en Tunisie
    // Obtenir sur : https://console.groq.com → API Keys
    // =====================================================
    private static final String API_KEY = "";
    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String EMERAUDE   = "#50C878";
    private static final String ANTHRACITE = "#303030";
    private static final String FOND       = "#F5F5F5";
    private static final String CORAIL     = "#FF7F50";

    public static void genererDescription(String sujet, String type, String formateur,
                                          int duree, String localisation,
                                          java.util.function.Consumer<String> onDescriptionGeneree) {
        Stage stage = new Stage();
        stage.setTitle("HUMA — Description IA");
        stage.setResizable(false);

        // ===== HEADER elegant =====
        Label logoLabel = new Label("HUMA");
        logoLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");

        Label aiTag = new Label("✦  Intelligence Artificielle");
        aiTag.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: " + EMERAUDE + ";" +
                        "-fx-background-color: rgba(80,200,120,0.15);" +
                        "-fx-padding: 3px 10px; -fx-background-radius: 20px;" +
                        "-fx-font-family: 'Segoe UI';"
        );

        Label sujetTag = new Label(sujet.toUpperCase());
        sujetTag.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.5);" +
                        "-fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1px;"
        );

        VBox logoBox = new VBox(4, logoLabel, aiTag, sujetTag);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(logoBox);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #1a1a1a, #303030);" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 3);"
        );

        // ===== META INFO BAR =====
        HBox metaBar = new HBox(20);
        metaBar.setPadding(new Insets(10, 25, 10, 25));
        metaBar.setAlignment(Pos.CENTER_LEFT);
        metaBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        metaBar.getChildren().addAll(
                metaChip("👤", formateur),
                metaChip("📍", localisation),
                metaChip("⏱", duree + " jours"),
                metaChip("📋", type)
        );

        // ===== LOADING =====
        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: " + EMERAUDE + ";");
        progress.setPrefSize(40, 40);

        Label loadingLabel = new Label("Génération de la description en cours...");
        loadingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888; -fx-font-family: 'Segoe UI';");

        Label loadingSubLabel = new Label("Powered by Groq LLaMA 3.1");
        loadingSubLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb; -fx-font-family: 'Segoe UI';");

        VBox loadingBox = new VBox(12, progress, loadingLabel, loadingSubLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));

        // ===== DESCRIPTION AREA =====
        Label descTitre = new Label("Description générée");
        descTitre.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + EMERAUDE + ";" +
                        "-fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1px;"
        );
        descTitre.setVisible(false);

        TextArea descArea = new TextArea();
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(7);
        descArea.setStyle(
                "-fx-font-size: 13.5px;" +
                        "-fx-font-family: 'Georgia', 'Segoe UI', serif;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e8e8e8;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 14px;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-line-spacing: 4px;"
        );
        descArea.setVisible(false);

        // ===== FOOTER BUTTONS =====
        Button btnRegenerer = new Button("↺   Régénérer");
        btnRegenerer.setStyle(
                "-fx-background-color: " + EMERAUDE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-padding: 10px 28px;" +
                        "-fx-background-radius: 25px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(80,200,120,0.4), 8, 0, 0, 2);"
        );
        btnRegenerer.setVisible(false);

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-background-radius: 25px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 25px;" +
                        "-fx-border-width: 1px;"
        );
        btnFermer.setOnAction(e -> stage.close());

        Label poweredBy = new Label("Powered by Groq LLaMA 3.1  •  HUMA RH");
        poweredBy.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb; -fx-font-family: 'Segoe UI';");

        HBox btnBox = new HBox(12, btnRegenerer, btnFermer);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15, 25, 8, 25));

        VBox footer = new VBox(6, btnBox, poweredBy);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 0, 18, 0));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1px 0 0 0;");

        // ===== BODY =====
        VBox body = new VBox(10, loadingBox, descTitre, descArea);
        body.setPadding(new Insets(20, 25, 15, 25));
        body.setStyle("-fx-background-color: white;");

        VBox root = new VBox(0, header, metaBar, body, footer);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 540, 460);
        stage.setScene(scene);
        stage.show();

        // ===== APPEL API =====
        Thread thread = new Thread(() -> {
            String description = appelAPI(sujet, type, formateur, duree, localisation);
            Platform.runLater(() -> {
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);

                if (description != null && !description.isEmpty()) {
                    // Nettoyer les guillemets si présents
                    String cleanDesc = description.replaceAll("^\"|\"$", "").trim();
                    descArea.setText(cleanDesc);
                    descArea.setVisible(true);
                    descTitre.setVisible(true);
                    btnRegenerer.setVisible(true);

                    btnRegenerer.setOnAction(e -> {
                        stage.close();
                        genererDescription(sujet, type, formateur, duree, localisation, onDescriptionGeneree);
                    });
                } else {
                    Label errLabel = new Label("Impossible de générer la description.\nVérifiez votre clé sur : https://console.groq.com");
                    errLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-wrap-text: true; -fx-font-family: 'Segoe UI';");
                    errLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                    body.getChildren().add(errLabel);
                    btnFermer.setVisible(true);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Helper : chip meta info
    private static javafx.scene.layout.HBox metaChip(String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 12px;");
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-font-family: 'Segoe UI';");
        javafx.scene.layout.HBox chip = new javafx.scene.layout.HBox(4, iconLabel, textLabel);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(3, 8, 3, 8));
        chip.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #ddd; -fx-border-radius: 12px; -fx-border-width: 1px;");
        return chip;
    }

    // =====================================================
    // APPEL API GROQ (format OpenAI compatible)
    // =====================================================
    private static String appelAPI(String sujet, String type, String formateur,
                                   int duree, String localisation) {
        try {
            String prompt = "Tu es un responsable RH expert en formation professionnelle. "
                    + "Redige une description concise et professionnelle (3 phrases maximum) "
                    + "pour une formation intitulee \"" + sujet + "\". "
                    + "La formation est de type " + type + ", animee par " + formateur
                    + ", d\'une duree de " + duree + " jours, a " + localisation + ". "
                    + "La description doit : 1) expliquer clairement le contenu et les objectifs de \"" + sujet + "\", "
                    + "2) mentionner les competences que les participants vont acquerir sur \"" + sujet + "\", "
                    + "3) etre motivante et professionnelle. "
                    + "IMPORTANT: parle uniquement du sujet \"" + sujet + "\", pas d\'outils generiques. "
                    + "Reponds uniquement avec la description, sans titre, sans introduction, sans liste.";

            String safePrompt = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            // Format JSON compatible OpenAI / Groq
            String requestBody = "{"
                    + "\"model\": \"llama-3.1-8b-instant\","
                    + "\"messages\": ["
                    + "  {\"role\": \"user\", \"content\": \"" + safePrompt + "\"}"
                    + "],"
                    + "\"max_tokens\": 300,"
                    + "\"temperature\": 0.7"
                    + "}";

            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("=== Groq response: " + responseCode + " ===");

            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }

                String json = response.toString();
                System.out.println("=== JSON debut: " + json.substring(0, Math.min(200, json.length())) + " ===");

                // Extraction : {"choices":[{"message":{"content":"..."}}]}
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
                try (BufferedReader errReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) errBody.append(errLine);
                }
                System.err.println("=== ERREUR " + responseCode + " : " + errBody + " ===");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Erreur reseau : " + e.getMessage());
            return null;
        }
    }
}