package controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Publication;
import models.User;
import services.PublicationService;
import utils.Session;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class chatbotController {

    @FXML private VBox messagesBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextArea txtInput;
    @FXML private Button btnSend;

    private static final String API_KEY = "gsk_XXXX";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PublicationService pubService = new PublicationService();
    private final ObjectMapper mapper           = new ObjectMapper();
    private final HttpClient httpClient         = HttpClient.newHttpClient();
    private final List<ObjectNode> history      = new ArrayList<>();
    private HBox typingRow;

    private static final String SYSTEM_PROMPT =
            "Tu es un assistant RH pour l'application HUMA, specialise UNIQUEMENT dans la gestion des publications internes.\n\n" +
                    "Pour chaque demande d'action, reponds avec :\n" +
                    "1. Un bloc JSON entre ```action et ```\n" +
                    "2. Un message naturel en francais\n\n" +
                    "Types d'actions :\n" +
                    "- CREATE  -> creer une publication\n" +
                    "- UPDATE  -> modifier (besoin de l'id)\n" +
                    "- DELETE  -> supprimer (besoin de l'id)\n" +
                    "- LIST    -> lister les publications\n" +
                    "- IMPROVE -> ameliorer le contenu\n" +
                    "- NONE    -> question sans action\n\n" +
                    "Formats JSON :\n" +
                    "```action\n{\"type\":\"CREATE\",\"data\":{\"titre\":\"...\",\"contenu\":\"...\"}}\n```\n" +
                    "```action\n{\"type\":\"UPDATE\",\"data\":{\"id\":1,\"titre\":\"...\",\"contenu\":\"...\"}}\n```\n" +
                    "```action\n{\"type\":\"DELETE\",\"data\":{\"id\":1}}\n```\n" +
                    "```action\n{\"type\":\"LIST\",\"data\":{}}\n```\n" +
                    "```action\n{\"type\":\"IMPROVE\",\"data\":{\"id\":1,\"contenu_ameliore\":\"...\"}}\n```\n\n" +
                    "Regles :\n" +
                    "- Reponds TOUJOURS en francais, professionnel et bienveillant\n" +
                    "- Si l'id manque pour UPDATE/DELETE, demande-le poliment\n" +
                    "- Si on parle d'autre chose que publications, redirige poliment\n";

    // ══════════════════════════════════════════════════════
    // INITIALIZE
    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        addBotMessage(
                "Bonjour ! Je suis votre assistant pour la gestion des publications RH.\n\n" +
                        "Je peux vous aider a :\n" +
                        "  Creer une nouvelle publication\n" +
                        "  Modifier une publication existante\n" +
                        "  Supprimer une publication\n" +
                        "  Lister toutes les publications\n" +
                        "  Ameliorer le contenu d'une publication\n\n" +
                        "Que souhaitez-vous faire ?"
        );

        txtInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                send();
            }
        });
    }

    // ══════════════════════════════════════════════════════
    // BOUTONS RAPIDES
    // ══════════════════════════════════════════════════════
    @FXML public void quickVoirTout() {
        txtInput.setText("Liste toutes les publications");
        send();
    }
    @FXML public void quickCreer() {
        txtInput.setText("Cree une publication pour annoncer la reunion de demain a 10h en salle B");
        send();
    }
    @FXML public void quickModifier() {
        txtInput.setText("Modifie la publication ");
        txtInput.requestFocus();
        txtInput.positionCaret(txtInput.getText().length());
    }
    @FXML public void quickSupprimer() {
        txtInput.setText("Supprime la publication ");
        txtInput.requestFocus();
        txtInput.positionCaret(txtInput.getText().length());
    }
    @FXML public void quickAmeliorer() {
        txtInput.setText("Ameliore le contenu de la publication ");
        txtInput.requestFocus();
        txtInput.positionCaret(txtInput.getText().length());
    }

    // ══════════════════════════════════════════════════════
    // ENVOYER MESSAGE
    // ══════════════════════════════════════════════════════
    @FXML
    public void send() {
        String text = txtInput.getText() == null ? "" : txtInput.getText().trim();
        if (text.isBlank()) return;

        txtInput.clear();
        btnSend.setDisable(true);
        addUserMessage(text);
        addTypingIndicator();

        // Contexte publications ajouté au message
        String pubsJson   = getPublicationsJson();
        String contextMsg = text + "\n\n[Publications en base : " + pubsJson + "]";

        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("role", "user");
        userNode.put("content", contextMsg);
        history.add(userNode);

        new Thread(() -> {
            try {
                String response = callGroqAPI();

                String action   = extractAction(response);
                String cleanMsg = cleanResponse(response);

                ObjectNode assistantNode = mapper.createObjectNode();
                assistantNode.put("role", "assistant");
                assistantNode.put("content", response);
                history.add(assistantNode);

                Platform.runLater(() -> {
                    removeTypingIndicator();
                    if (!cleanMsg.isBlank()) addBotMessage(cleanMsg);
                    if (action != null) executeAndDisplay(action);
                    btnSend.setDisable(false);
                    scrollToBottom();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    removeTypingIndicator();
                    addBotMessage("Erreur de connexion : " + e.getMessage());
                    btnSend.setDisable(false);
                });
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════
    // APPEL GROQ API
    // ══════════════════════════════════════════════════════
    private String callGroqAPI() throws Exception {
        ArrayNode messages = mapper.createArrayNode();

        // System message
        ObjectNode sysMsg = mapper.createObjectNode();
        sysMsg.put("role", "system");
        sysMsg.put("content", SYSTEM_PROMPT);
        messages.add(sysMsg);

        // Historique
        for (ObjectNode msg : history) {
            ObjectNode m = mapper.createObjectNode();
            m.put("role", msg.path("role").asText());
            m.put("content", msg.path("content").asText());
            messages.add(m);
        }

        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", 1000);
        body.set("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = mapper.readTree(response.body());

        if (json.has("error"))
            throw new Exception(json.path("error").path("message").asText("Erreur API Groq"));

        return json.path("choices").get(0)
                .path("message").path("content")
                .asText("Desole, reponse vide.");
    }

    // ══════════════════════════════════════════════════════
    // EXECUTER ACTION ET AFFICHER RESULTAT
    // ══════════════════════════════════════════════════════
    private void executeAndDisplay(String actionJson) {
        try {
            JsonNode node = mapper.readTree(actionJson);
            String type   = node.path("type").asText();
            JsonNode data = node.path("data");

            switch (type) {
                case "CREATE" -> {
                    String titre   = data.path("titre").asText("");
                    String contenu = data.path("contenu").asText("");
                    if (!titre.isBlank() && !contenu.isBlank()) {
                        int id = pubService.addPublicationAndGetId(titre, contenu);
                        if (id > 0)
                            addSuccessCard("Publication creee", "#" + id + " — " + titre);
                        else
                            addErrorCard("Echec creation", "Erreur base de donnees.");
                    }
                }
                case "UPDATE" -> {
                    int id         = data.path("id").asInt(-1);
                    String titre   = data.path("titre").asText("");
                    String contenu = data.path("contenu").asText("");
                    if (id > 0) {
                        User u  = Session.getUser();
                        boolean ok = pubService.updatePublication(
                                id, u != null ? u.getId() : 0, true, titre, contenu);
                        if (ok) addSuccessCard("Publication modifiee", "#" + id + " — " + titre);
                        else    addErrorCard("Modification impossible", "ID #" + id + " introuvable.");
                    }
                }
                case "DELETE" -> {
                    int id = data.path("id").asInt(-1);
                    if (id > 0) {
                        boolean ok = pubService.deleteById(id);
                        if (ok) addSuccessCard("Publication supprimee", "ID #" + id);
                        else    addErrorCard("Suppression impossible", "ID #" + id + " introuvable.");
                    }
                }
                case "LIST" -> {
                    List<Publication> pubs = pubService.getAll();
                    if (pubs.isEmpty())
                        addBotMessage("Aucune publication pour le moment.");
                    else
                        for (Publication p : pubs) addPublicationCard(p);
                }
                case "IMPROVE" -> {
                    int id            = data.path("id").asInt(-1);
                    String newContenu = data.path("contenu_ameliore").asText("");
                    if (id > 0 && !newContenu.isBlank()) {
                        Publication pub = pubService.getAll().stream()
                                .filter(p -> p.getId() == id).findFirst().orElse(null);
                        if (pub != null) {
                            User u  = Session.getUser();
                            boolean ok = pubService.updatePublication(
                                    id, u != null ? u.getId() : 0, true, pub.getTitre(), newContenu);
                            if (ok) addSuccessCard("Contenu ameliore", pub.getTitre());
                            else    addErrorCard("Amelioration echouee", "ID #" + id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            addErrorCard("Erreur action", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════
    private String getPublicationsJson() {
        try {
            List<Publication> pubs = pubService.getAll();
            if (pubs.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < pubs.size(); i++) {
                Publication p = pubs.get(i);
                String c = p.getContenu() != null ?
                        p.getContenu().substring(0, Math.min(60, p.getContenu().length())) : "";
                sb.append("{\"id\":").append(p.getId())
                        .append(",\"titre\":\"").append(esc(p.getTitre())).append("\"")
                        .append(",\"auteur\":\"").append(esc(p.getAuteur())).append("\"")
                        .append(",\"contenu\":\"").append(esc(c)).append("\"")
                        .append(",\"date\":\"").append(
                                p.getDatePublication() != null ?
                                        p.getDatePublication().format(DATE_FMT) : "")
                        .append("\"}");
                if (i < pubs.size() - 1) sb.append(",");
            }
            return sb.append("]").toString();
        } catch (Exception e) { return "[]"; }
    }

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

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // ══════════════════════════════════════════════════════
    // COMPOSANTS VISUELS
    // ══════════════════════════════════════════════════════
    private void addUserMessage(String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_RIGHT);

        Label avatar = makeAvatar("RH", "#1f2937");

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(520);
        bubble.setStyle(
                "-fx-background-color:linear-gradient(to bottom right,#1f2937,#374151);" +
                        "-fx-text-fill:white;-fx-font-size:13px;-fx-padding:12 16;" +
                        "-fx-background-radius:18 4 18 18;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),6,0,0,2);"
        );

        row.getChildren().addAll(bubble, avatar);
        messagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label avatar = makeAvatar("IA", "#20c997");

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(560);
        bubble.setStyle(
                "-fx-background-color:white;-fx-text-fill:#1f2937;-fx-font-size:13px;" +
                        "-fx-padding:12 16;-fx-background-radius:4 18 18 18;" +
                        "-fx-border-color:#e8edf4;-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);"
        );

        row.getChildren().addAll(avatar, bubble);
        messagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void addPublicationCard(Publication pub) {
        VBox card = new VBox(4);
        VBox.setMargin(card, new Insets(2, 0, 2, 44));
        card.setStyle(
                "-fx-background-color:#f8fafc;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-left-color:#20c997;" +
                        "-fx-border-width:1 1 1 4;" +
                        "-fx-border-radius:0 10 10 0;" +
                        "-fx-background-radius:0 10 10 0;" +
                        "-fx-padding:10 14;"
        );

        Label titre = new Label("#" + pub.getId() + " — " + pub.getTitre());
        titre.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        String dateStr = pub.getDatePublication() != null ?
                pub.getDatePublication().format(DATE_FMT) : "";
        Label meta = new Label(pub.getAuteur() + "  ·  " + dateStr);
        meta.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");

        String contenuCourt = pub.getContenu() != null ?
                (pub.getContenu().length() > 90 ?
                        pub.getContenu().substring(0, 90) + "..." : pub.getContenu()) : "";
        Label contenu = new Label(contenuCourt);
        contenu.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        contenu.setWrapText(true);

        card.getChildren().addAll(titre, meta, contenu);
        messagesBox.getChildren().add(card);
    }

    private void addSuccessCard(String title, String detail) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(card, new Insets(2, 0, 2, 44));
        card.setStyle(
                "-fx-background-color:#d1fae5;-fx-border-color:#6ee7b7;" +
                        "-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;" +
                        "-fx-padding:10 16;"
        );

        Label icon = new Label("OK");
        icon.setStyle(
                "-fx-background-color:#059669;-fx-text-fill:white;" +
                        "-fx-font-size:10px;-fx-font-weight:bold;" +
                        "-fx-padding:3 8;-fx-background-radius:6;"
        );

        VBox info = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#065f46;");
        Label d = new Label(detail);
        d.setStyle("-fx-font-size:11px;-fx-text-fill:#047857;");
        info.getChildren().addAll(t, d);

        card.getChildren().addAll(icon, info);
        messagesBox.getChildren().add(card);
    }

    private void addErrorCard(String title, String detail) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(card, new Insets(2, 0, 2, 44));
        card.setStyle(
                "-fx-background-color:#fee2e2;-fx-border-color:#fca5a5;" +
                        "-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;" +
                        "-fx-padding:10 16;"
        );

        Label icon = new Label("ERR");
        icon.setStyle(
                "-fx-background-color:#dc2626;-fx-text-fill:white;" +
                        "-fx-font-size:10px;-fx-font-weight:bold;" +
                        "-fx-padding:3 8;-fx-background-radius:6;"
        );

        VBox info = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#991b1b;");
        Label d = new Label(detail);
        d.setStyle("-fx-font-size:11px;-fx-text-fill:#b91c1c;");
        info.getChildren().addAll(t, d);

        card.getChildren().addAll(icon, info);
        messagesBox.getChildren().add(card);
    }

    private Label makeAvatar(String text, String color) {
        Label av = new Label(text);
        av.setStyle(
                "-fx-background-color:" + color + ";-fx-text-fill:white;" +
                        "-fx-font-size:10px;-fx-font-weight:bold;" +
                        "-fx-min-width:34;-fx-min-height:34;-fx-max-width:34;-fx-max-height:34;" +
                        "-fx-background-radius:50;-fx-alignment:center;"
        );
        return av;
    }

    private void addTypingIndicator() {
        typingRow = new HBox(10);
        typingRow.setAlignment(Pos.TOP_LEFT);

        Label avatar = makeAvatar("IA", "#20c997");

        Label typing = new Label("En train d'ecrire...");
        typing.setStyle(
                "-fx-background-color:white;-fx-text-fill:#9ca3af;" +
                        "-fx-font-size:12px;-fx-font-style:italic;" +
                        "-fx-padding:10 16;-fx-background-radius:4 18 18 18;" +
                        "-fx-border-color:#e8edf4;-fx-border-width:1;"
        );

        typingRow.getChildren().addAll(avatar, typing);
        messagesBox.getChildren().add(typingRow);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        if (typingRow != null) {
            messagesBox.getChildren().remove(typingRow);
            typingRow = null;
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ══════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════
    @FXML public void openDashboard(ActionEvent e)     { navigateTo(e, "/fxml/recrutement/dashboard.fxml"); }
    @FXML public void openRecrutement(ActionEvent e)   { navigateTo(e, "/fxml/recrutement/recrutement.fxml"); }
    @FXML public void openConges(ActionEvent e)        { navigateTo(e, "/fxml/congesAbsences/Conges.fxml"); }
    @FXML public void openAbsences(ActionEvent e)      { navigateTo(e, "/fxml/congesAbsences/absences.fxml"); }
    @FXML public void openCommunication(ActionEvent e) { navigateTo(e, "/fxml/communication.fxml"); }
    @FXML public void openFormations(ActionEvent e)    { navigateTo(e, "/fxml/formation/formation.fxml"); }
    @FXML public void openFeedback(ActionEvent e)      { navigateTo(e, "/fxml/feedback/feedback.fxml"); }
    @FXML public void openChatbot(ActionEvent e)       { navigateTo(e, "/fxml/chatbot.fxml"); }
    @FXML public void openParametres(ActionEvent e)    { navigateTo(e, "/fxml/parametres.fxml"); }

    @FXML public void logout(ActionEvent e) {
        Session.clear();
        navigateTo(e, "/fxml/login.fxml");
    }

    private void navigateTo(ActionEvent event, String fxml) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxml))));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}