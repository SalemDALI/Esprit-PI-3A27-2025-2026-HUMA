package controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import services.PublicationService;
import utils.Session;
import models.User;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class chatbotController {

    @FXML private WebView chatbotWebView;

    private final PublicationService pubService = new PublicationService();
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private WebEngine engine;

    private final List<ObjectNode> history = new ArrayList<>();

    /*// ✅ Clé Gemini GRATUITE — obtenir sur https://aistudio.google.com/apikey
    private static final String API_KEY = "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";*/

    private static final String SYSTEM_PROMPT =
            "Tu es un assistant RH pour l'application HUMA, spécialisé UNIQUEMENT dans la gestion des publications internes.\n\n" +
                    "Pour chaque demande d'action, réponds avec :\n" +
                    "1. Un bloc JSON entre ```action et ```\n" +
                    "2. Un message naturel en français\n\n" +
                    "Types d'actions :\n" +
                    "- CREATE  → créer une publication\n" +
                    "- UPDATE  → modifier (besoin de l'id)\n" +
                    "- DELETE  → supprimer (besoin de l'id)\n" +
                    "- LIST    → lister les publications\n" +
                    "- IMPROVE → améliorer le contenu\n" +
                    "- NONE    → question sans action\n\n" +
                    "Formats JSON :\n" +
                    "```action\n{\"type\":\"CREATE\",\"data\":{\"titre\":\"...\",\"contenu\":\"...\"}}\n```\n" +
                    "```action\n{\"type\":\"UPDATE\",\"data\":{\"id\":1,\"titre\":\"...\",\"contenu\":\"...\"}}\n```\n" +
                    "```action\n{\"type\":\"DELETE\",\"data\":{\"id\":1}}\n```\n" +
                    "```action\n{\"type\":\"LIST\",\"data\":{}}\n```\n\n" +
                    "Règles :\n" +
                    "- Réponds TOUJOURS en français, professionnel et bienveillant\n" +
                    "- Si l'id manque pour UPDATE/DELETE, demande-le poliment\n" +
                    "- Si on parle d'autre chose que publications, redirige\n";

    // ── Bridge Java exposé au JavaScript ─────────────────
    public class JavaBridge {

        public void sendToAI(String userMessage) {
            new Thread(() -> {
                try {
                    String pubsJson = getPublicationsJson();
                    String contextMsg = userMessage + "\n\n[Publications en base : " + pubsJson + "]";

                    ObjectNode userNode = mapper.createObjectNode();
                    userNode.put("role", "user");
                    userNode.put("content", contextMsg);
                    history.add(userNode);

                    String response = callGeminiAPI();

                    String action = extractAction(response);
                    String cleanMsg = cleanResponse(response);
                    String actionResult = "null";
                    if (action != null) {
                        actionResult = executeAction(action);
                    }

                    ObjectNode assistantNode = mapper.createObjectNode();
                    assistantNode.put("role", "model");
                    assistantNode.put("content", response);
                    history.add(assistantNode);

                    final String finalMsg = cleanMsg
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("\n", "\\n")
                            .replace("\r", "");
                    final String finalResult = actionResult;
                    final String finalAction = action != null ? action.replace("'", "\\'") : "null";

                    Platform.runLater(() ->
                            engine.executeScript(
                                    "receiveFromJava('" + finalMsg + "', " +
                                            finalResult + ", '" + finalAction + "')"
                            )
                    );

                } catch (Exception e) {
                    Platform.runLater(() ->
                            engine.executeScript(
                                    "receiveFromJava('❌ Erreur : " + e.getMessage() + "', null, null)"
                            )
                    );
                }
            }).start();
        }

        public String getPublications() { return getPublicationsJson(); }

        public String createPublication(String titre, String contenu) {
            try {
                int id = pubService.addPublicationAndGetId(titre, contenu);
                return id > 0
                        ? "{\"success\":true,\"id\":" + id + ",\"message\":\"Publication #" + id + " créée\"}"
                        : "{\"success\":false,\"message\":\"Échec création\"}";
            } catch (Exception e) {
                return "{\"success\":false,\"message\":\"" + esc(e.getMessage()) + "\"}";
            }
        }

        public String updatePublication(int id, String titre, String contenu) {
            try {
                User u = Session.getUser();
                boolean ok = pubService.updatePublication(id, u != null ? u.getId() : 0, true, titre, contenu);
                return ok
                        ? "{\"success\":true,\"message\":\"Publication #" + id + " modifiée\"}"
                        : "{\"success\":false,\"message\":\"Modification impossible\"}";
            } catch (Exception e) {
                return "{\"success\":false,\"message\":\"" + esc(e.getMessage()) + "\"}";
            }
        }

        public String deletePublication(int id) {
            try {
                boolean ok = pubService.deleteById(id);
                return ok
                        ? "{\"success\":true,\"message\":\"Publication #" + id + " supprimée\"}"
                        : "{\"success\":false,\"message\":\"Suppression impossible\"}";
            } catch (Exception e) {
                return "{\"success\":false,\"message\":\"" + esc(e.getMessage()) + "\"}";
            }
        }

        private String esc(String s) {
            if (s == null) return "";
            return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
        }
    }

    private String callGeminiAPI() throws Exception {
        ArrayNode messages = mapper.createArrayNode();

        // System message
        ObjectNode sysMsg = mapper.createObjectNode();
        sysMsg.put("role", "system");
        sysMsg.put("content", SYSTEM_PROMPT);
        messages.add(sysMsg);

        // Historique
        for (ObjectNode msg : history) {
            ObjectNode m = mapper.createObjectNode();
            String role = msg.path("role").asText();
            m.put("role", role.equals("model") ? "assistant" : role);
            m.put("content", msg.path("content").asText());
            messages.add(m);
        }

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("max_tokens", 1000);
        body.set("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(response.body());

        if (json.has("error")) {
            return "❌ Erreur : " + json.path("error").path("message").asText();
        }

        return json.path("choices").get(0)
                .path("message").path("content")
                .asText("Désolé, réponse vide.");
    }




    // ── Helpers ───────────────────────────────────────────
    private String extractAction(String text) {
        int start = text.indexOf("```action");
        if (start == -1) return null;
        int end = text.indexOf("```", start + 9);
        if (end == -1) return null;
        return text.substring(start + 9, end).trim();
    }

    private String cleanResponse(String text) {
        return text.replaceAll("```action[\\s\\S]*?```", "").trim();
    }

    private String executeAction(String actionJson) {
        try {
            JsonNode node = mapper.readTree(actionJson);
            String type = node.path("type").asText();
            JsonNode data = node.path("data");
            JavaBridge bridge = new JavaBridge();
            switch (type) {
                case "CREATE": return bridge.createPublication(data.path("titre").asText(""), data.path("contenu").asText(""));
                case "UPDATE": return bridge.updatePublication(data.path("id").asInt(), data.path("titre").asText(""), data.path("contenu").asText(""));
                case "DELETE": return bridge.deletePublication(data.path("id").asInt());
                case "LIST":   return "{\"success\":true,\"pubs\":" + getPublicationsJson() + "}";
                default:       return "null";
            }
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getPublicationsJson() {
        try {
            var pubs = pubService.getAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < pubs.size(); i++) {
                var p = pubs.get(i);
                sb.append("{")
                        .append("\"id\":").append(p.getId()).append(",")
                        .append("\"titre\":\"").append(esc(p.getTitre())).append("\",")
                        .append("\"contenu\":\"").append(esc(p.getContenu())).append("\",")
                        .append("\"auteur\":\"").append(esc(p.getAuteur())).append("\",")
                        .append("\"date\":\"").append(p.getDatePublication() != null ? p.getDatePublication().toString() : "")
                        .append("\"}");
                if (i < pubs.size() - 1) sb.append(",");
            }
            return sb.append("]").toString();
        } catch (Exception e) { return "[]"; }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }

    // ── Initialize ────────────────────────────────────────
    @FXML
    public void initialize() {
        engine = chatbotWebView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", new JavaBridge());
            }
        });
        URL url = getClass().getResource("/html/chatbot.html");
        if (url != null) {
            engine.load(url.toExternalForm());
        } else {
            engine.loadContent("<h2 style='font-family:sans-serif;padding:40px'>❌ chatbot.html introuvable dans resources/html/</h2>");
        }
    }

    // ── Navigation ────────────────────────────────────────
    @FXML public void openDashboard(ActionEvent e)     { go(e, "/fxml/recrutement/dashboard.fxml"); }
    @FXML public void openRecrutement(ActionEvent e)   { go(e, "/fxml/recrutement/recrutement.fxml"); }
    @FXML public void openConges(ActionEvent e)        { go(e, "/fxml/congesAbsences/Conges.fxml"); }
    @FXML public void openAbsences(ActionEvent e)      { go(e, "/fxml/congesAbsences/absences.fxml"); }
    @FXML public void openCommunication(ActionEvent e) { go(e, "/fxml/communication.fxml"); }
    @FXML public void openFormations(ActionEvent e)    { go(e, "/fxml/formation/formation.fxml"); }
    @FXML public void openFeedback(ActionEvent e)      { go(e, "/fxml/feedback/feedback.fxml"); }
    @FXML public void openParametres(ActionEvent e)    { go(e, "/fxml/parametres.fxml"); }

    @FXML
    public void logout(ActionEvent e) {
        Session.clear();
        try {
            Stage s = (Stage)((Node)e.getSource()).getScene().getWindow();
            s.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            s.show();
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void go(ActionEvent e, String fxml) {
        try {
            Stage s = (Stage)((Node)e.getSource()).getScene().getWindow();
            s.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxml))));
            s.show();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}