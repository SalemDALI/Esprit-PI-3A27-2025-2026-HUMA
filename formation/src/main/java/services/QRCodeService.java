package services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import model.Formation;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class QRCodeService {

    // Charte graphique HUMA
    private static final String EMERAUDE    = "#50C878";
    private static final String ANTHRACITE  = "#303030";
    private static final String FOND        = "#F5F5F5";
    private static final String CORAIL      = "#FF7F50";
    private static final String BLANC       = "#FFFFFF";
    private static final String GRIS_TEXTE  = "#7f8c8d";

    public static void genererEtAfficherQRCode(Formation formation) {
        try {
            Stage stage = new Stage();
            stage.setTitle("QR Code - " + formation.getSujet());
            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(false);

            // ===== Generation QR Code iCalendar =====
            String icsContent = genererICS(formation);

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(icsContent, BarcodeFormat.QR_CODE, 420, 420, hints);

            // Rendu QR avec couleurs charte
            int size = 420;
            BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, size, size);
            // Modules en couleur anthracite
            g2d.setColor(new java.awt.Color(0x30, 0x30, 0x30));
            int matrixSize = bitMatrix.getWidth();
            float moduleSize = (float) size / matrixSize;
            for (int x = 0; x < matrixSize; x++) {
                for (int y = 0; y < matrixSize; y++) {
                    if (bitMatrix.get(x, y)) {
                        int px = Math.round(x * moduleSize);
                        int py = Math.round(y * moduleSize);
                        int pw = Math.round((x + 1) * moduleSize) - px;
                        int ph = Math.round((y + 1) * moduleSize) - py;
                        g2d.fillRoundRect(px, py, pw, ph, 2, 2);
                    }
                }
            }
            g2d.dispose();

            WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            ImageView imageView = new ImageView(fxImage);
            imageView.setFitWidth(260);
            imageView.setFitHeight(260);

            // ===================================================
            // HEADER : bande verte avec logo et titre
            // ===================================================
            Label logoLabel = new Label("HUMA");
            logoLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; " +
                    "-fx-text-fill: white; -fx-font-family: 'Segoe UI';");

            Label sousTitre = new Label("Gestion des Formations");
            sousTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");

            VBox logoBox = new VBox(2, logoLabel, sousTitre);
            logoBox.setAlignment(Pos.CENTER_LEFT);

            // Icone calendrier a droite
            Label calIcon = new Label("📅");
            calIcon.setStyle("-fx-font-size: 28px;");

            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(18, 25, 18, 25));
            header.setSpacing(10);
            header.setStyle("-fx-background-color: " + ANTHRACITE + ";");
            HBox.setHgrow(logoBox, Priority.ALWAYS);
            header.getChildren().addAll(logoBox, calIcon);

            // ===================================================
            // BODY GAUCHE : QR Code avec cadre
            // ===================================================
            // Cadre emeraude autour du QR
            VBox qrWrapper = new VBox(imageView);
            qrWrapper.setAlignment(Pos.CENTER);
            qrWrapper.setPadding(new Insets(12));
            qrWrapper.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: " + EMERAUDE + ";" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 12px;" +
                            "-fx-background-radius: 12px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(80,200,120,0.3), 15, 0, 0, 4);"
            );

            // Badge "iCalendar"
            Label badgeIcs = new Label("📅  Format iCalendar");
            badgeIcs.setStyle(
                    "-fx-background-color: " + EMERAUDE + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 5px 14px;" +
                            "-fx-background-radius: 20px;"
            );

            Label scanMsg = new Label("Scannez pour ajouter au calendrier");
            scanMsg.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRIS_TEXTE + ";");

            VBox leftPanel = new VBox(14, qrWrapper, badgeIcs, scanMsg);
            leftPanel.setAlignment(Pos.CENTER);
            leftPanel.setPadding(new Insets(25, 20, 25, 25));

            // ===================================================
            // BODY DROITE : infos formation
            // ===================================================

            // Titre formation
            Label nomFormation = new Label(formation.getSujet());
            nomFormation.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            nomFormation.setStyle("-fx-text-fill: " + ANTHRACITE + ";");
            nomFormation.setWrapText(true);
            nomFormation.setMaxWidth(230);

            // Badge type
            String couleurType;
            String typeText = formation.getType() != null ? formation.getType() : "";
            if (typeText.toLowerCase().contains("presentiel") || typeText.toLowerCase().contains("présentiel")) {
                couleurType = EMERAUDE;
            } else if (typeText.toLowerCase().contains("ligne")) {
                couleurType = "#3498db";
            } else {
                couleurType = "#9b59b6";
            }
            Label typeBadge = new Label(typeText);
            typeBadge.setStyle(
                    "-fx-background-color: " + couleurType + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4px 14px;" +
                            "-fx-background-radius: 20px;"
            );

            // Separateur
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #e0e0e0;");

            // Infos avec icones
            String date  = formation.getDateDebut() != null ? formation.getDateDebut().toString() : "N/A";
            String duree = formation.getDuree() + " jours";
            String lieu  = formation.getLocalisation() != null ? formation.getLocalisation() : "N/A";

            VBox infosBox = new VBox(10,
                    infoRow("👤", "Formateur",  formation.getFormateur()),
                    infoRow("📅", "Date debut", date),
                    infoRow("⏱️", "Duree",      duree),
                    infoRow("📍", "Lieu",        lieu)
            );

            // Note compatible
            Label compatLabel = new Label("✓ Google Calendar   ✓ iPhone   ✓ Outlook");
            compatLabel.setStyle(
                    "-fx-font-size: 10px;" +
                            "-fx-text-fill: " + EMERAUDE + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8px 12px;" +
                            "-fx-background-color: #e8f8ef;" +
                            "-fx-background-radius: 8px;"
            );

            VBox rightPanel = new VBox(12,
                    nomFormation,
                    typeBadge,
                    sep,
                    infosBox,
                    compatLabel
            );
            rightPanel.setAlignment(Pos.TOP_LEFT);
            rightPanel.setPadding(new Insets(25, 25, 25, 10));
            rightPanel.setMinWidth(260);

            // ===================================================
            // BODY : gauche + droite
            // ===================================================
            HBox body = new HBox(0, leftPanel, rightPanel);
            body.setStyle("-fx-background-color: " + FOND + ";");
            body.setAlignment(Pos.CENTER);

            // ===================================================
            // FOOTER : bouton fermer
            // ===================================================
            Button btnFermer = new Button("Fermer");
            btnFermer.setStyle(
                    "-fx-background-color: " + CORAIL + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 10px 35px;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;"
            );
            btnFermer.setOnAction(e -> stage.close());
            btnFermer.setOnMouseEntered(e ->
                    btnFermer.setStyle(btnFermer.getStyle().replace(CORAIL, "#e06030")));
            btnFermer.setOnMouseExited(e ->
                    btnFermer.setStyle(btnFermer.getStyle().replace("#e06030", CORAIL)));

            HBox footer = new HBox(btnFermer);
            footer.setAlignment(Pos.CENTER);
            footer.setPadding(new Insets(14, 0, 18, 0));
            footer.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1px 0 0 0;"
            );

            // ===================================================
            // LAYOUT FINAL
            // ===================================================
            VBox root = new VBox(0, header, body, footer);
            root.setStyle("-fx-background-color: " + FOND + ";");

            Scene scene = new Scene(root, 660, 440);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur QR Code : " + e.getMessage());
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur QR Code");
            alert.setHeaderText("Impossible de generer le QR Code");
            alert.setContentText("Erreur : " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ===================================================
    // Ligne info avec icone + label + valeur
    // ===================================================
    private static HBox infoRow(String icone, String cle, String valeur) {
        Label ico = new Label(icone);
        ico.setStyle("-fx-font-size: 14px;");
        ico.setMinWidth(24);

        Label cleLabel = new Label(cle + " :");
        cleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-min-width: 80px;");

        Label valLabel = new Label(valeur != null ? valeur : "N/A");
        valLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        valLabel.setWrapText(true);
        valLabel.setMaxWidth(150);

        HBox row = new HBox(8, ico, cleLabel, valLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ===================================================
    // Generateur ICS (iCalendar standard RFC 5545)
    // ===================================================
    private static String genererICS(Formation formation) {
        String sujet     = nettoyer(formation.getSujet());
        String formateur = nettoyer(formation.getFormateur());
        String lieu      = nettoyer(formation.getLocalisation());
        String type      = nettoyer(formation.getType());

        LocalDate dateDebut = formation.getDateDebut() != null
                ? formation.getDateDebut().toLocalDate()
                : LocalDate.now();
        LocalDate dateFin = dateDebut.plusDays(formation.getDuree());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dtStart = dateDebut.format(fmt);
        String dtEnd   = dateFin.format(fmt);
        String uid     = "formation-" + formation.getId() + "@huma-app";

        String description = "Formation: " + sujet +
                "\\nFormateur: " + formateur +
                "\\nType: " + type +
                "\\nDuree: " + formation.getDuree() + " jours" +
                "\\nLieu: " + lieu;

        return "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//HUMA Formation App//FR\r\n" +
                "CALSCALE:GREGORIAN\r\n" +
                "METHOD:PUBLISH\r\n" +
                "BEGIN:VEVENT\r\n" +
                "UID:" + uid + "\r\n" +
                "DTSTART;VALUE=DATE:" + dtStart + "\r\n" +
                "DTEND;VALUE=DATE:" + dtEnd + "\r\n" +
                "SUMMARY:" + sujet + "\r\n" +
                "DESCRIPTION:" + description + "\r\n" +
                "LOCATION:" + lieu + "\r\n" +
                "ORGANIZER;CN=" + formateur + ":mailto:formation@huma.tn\r\n" +
                "STATUS:CONFIRMED\r\n" +
                "TRANSP:OPAQUE\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR";
    }

    private static String nettoyer(String texte) {
        if (texte == null) return "";
        return texte
                .replace("é","e").replace("è","e").replace("ê","e").replace("ë","e")
                .replace("É","E").replace("È","E").replace("Ê","E").replace("Ë","E")
                .replace("à","a").replace("â","a").replace("ä","a")
                .replace("À","A").replace("Â","A").replace("Ä","A")
                .replace("î","i").replace("ï","i").replace("Î","I").replace("Ï","I")
                .replace("ô","o").replace("ö","o").replace("Ô","O").replace("Ö","O")
                .replace("ù","u").replace("û","u").replace("ü","u")
                .replace("Ù","U").replace("Û","U").replace("Ü","U")
                .replace("ç","c").replace("Ç","C")
                .replace("œ","oe").replace("Œ","OE");
    }
}