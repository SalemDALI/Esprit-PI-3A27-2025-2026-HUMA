package services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.function.Consumer;

public class MapService {

    private static final String LEAFLET_CSS =
            ".leaflet-pane,.leaflet-tile,.leaflet-marker-icon,.leaflet-marker-shadow," +
                    ".leaflet-tile-container,.leaflet-pane>svg,.leaflet-pane>canvas," +
                    ".leaflet-zoom-box,.leaflet-image-layer,.leaflet-layer{position:absolute;left:0;top:0}" +
                    ".leaflet-container{overflow:hidden}" +
                    ".leaflet-tile,.leaflet-marker-icon,.leaflet-marker-shadow{-webkit-user-select:none;-moz-user-select:none;user-select:none;-webkit-user-drag:none}" +
                    ".leaflet-tile::selection{background:0 0}" +
                    ".leaflet-safari .leaflet-tile{image-rendering:-webkit-optimize-contrast}" +
                    ".leaflet-safari .leaflet-tile-container{width:1600px;height:1600px;-webkit-transform-origin:0 0}" +
                    ".leaflet-marker-icon,.leaflet-marker-shadow{display:block}" +
                    ".leaflet-container img{padding:0;max-width:none!important}" +
                    ".leaflet-container img.leaflet-image-layer{max-width:15000px!important}" +
                    ".leaflet-tile{filter:inherit;visibility:hidden}" +
                    ".leaflet-tile-loaded{visibility:inherit}" +
                    ".leaflet-zoom-box{width:0;height:0;-moz-box-sizing:border-box;box-sizing:border-box;z-index:800}" +
                    ".leaflet-overlay-pane svg{-moz-user-select:none}" +
                    ".leaflet-pane{z-index:400}" +
                    ".leaflet-tile-pane{z-index:200}" +
                    ".leaflet-overlay-pane{z-index:400}" +
                    ".leaflet-shadow-pane{z-index:500}" +
                    ".leaflet-marker-pane{z-index:600}" +
                    ".leaflet-tooltip-pane{z-index:650}" +
                    ".leaflet-popup-pane{z-index:700}" +
                    ".leaflet-map-pane canvas{z-index:100}" +
                    ".leaflet-map-pane svg{z-index:200}" +
                    ".leaflet-control{position:relative;z-index:800;pointer-events:visiblePainted;pointer-events:auto}" +
                    ".leaflet-top,.leaflet-bottom{position:absolute;z-index:1000;pointer-events:none}" +
                    ".leaflet-top{top:0}.leaflet-right{right:0}.leaflet-bottom{bottom:0}.leaflet-left{left:0}" +
                    ".leaflet-control{float:left;clear:both}.leaflet-right .leaflet-control{float:right}" +
                    ".leaflet-top .leaflet-control{margin-top:10px}.leaflet-bottom .leaflet-control{margin-bottom:10px}" +
                    ".leaflet-left .leaflet-control{margin-left:10px}.leaflet-right .leaflet-control{margin-right:10px}" +
                    ".leaflet-fade-anim .leaflet-popup{opacity:0;-webkit-transition:opacity .2s linear;transition:opacity .2s linear}" +
                    ".leaflet-fade-anim .leaflet-map-pane .leaflet-popup{opacity:1}" +
                    ".leaflet-zoom-animated{-webkit-transform-origin:0 0;-ms-transform-origin:0 0;transform-origin:0 0}" +
                    ".leaflet-zoom-anim .leaflet-zoom-animated{will-change:transform;-webkit-transition:-webkit-transform .25s cubic-bezier(0,0,.25,1);transition:transform .25s cubic-bezier(0,0,.25,1)}" +
                    ".leaflet-zoom-anim .leaflet-tile,.leaflet-pan-anim .leaflet-tile{-webkit-transition:none;transition:none}" +
                    ".leaflet-zoom-anim .leaflet-zoom-animated.leaflet-zoom-hide{visibility:hidden}" +
                    ".leaflet-interactive{cursor:pointer}" +
                    ".leaflet-grab{cursor:-webkit-grab;cursor:grab}" +
                    ".leaflet-crosshair,.leaflet-crosshair .leaflet-interactive{cursor:crosshair}" +
                    ".leaflet-popup-pane,.leaflet-control{cursor:auto}" +
                    ".leaflet-dragging .leaflet-grab,.leaflet-dragging .leaflet-grab .leaflet-interactive,.leaflet-dragging .leaflet-marker-draggable{cursor:move;cursor:-webkit-grabbing;cursor:grabbing}" +
                    ".leaflet-marker-icon,.leaflet-marker-shadow,.leaflet-image-layer,.leaflet-pane>svg path,.leaflet-tile-container{pointer-events:none}" +
                    ".leaflet-marker-icon.leaflet-interactive,.leaflet-image-layer.leaflet-interactive,.leaflet-pane>svg path.leaflet-interactive{pointer-events:visiblePainted;pointer-events:auto}" +
                    ".leaflet-container{background:#ddd;outline-offset:1px;font-family:Helvetica Neue,Arial,Helvetica,sans-serif;font-size:12px;line-height:1.5}" +
                    ".leaflet-container a{color:#0078a8}.leaflet-container a.leaflet-active{outline:2px solid orange}" +
                    ".leaflet-zoom-box{border:2px dotted #38f;background:rgba(255,255,255,.5)}" +
                    ".leaflet-bar{box-shadow:0 1px 5px rgba(0,0,0,.65);border-radius:4px}" +
                    ".leaflet-bar a{background-color:#fff;border-bottom:1px solid #ccc;width:26px;height:26px;line-height:26px;display:block;text-align:center;text-decoration:none;color:black}" +
                    ".leaflet-bar a:hover,.leaflet-bar a:focus{background-color:#f4f4f4}" +
                    ".leaflet-bar a:first-child{border-top-left-radius:4px;border-top-right-radius:4px}" +
                    ".leaflet-bar a:last-child{border-bottom-left-radius:4px;border-bottom-right-radius:4px;border-bottom:none}" +
                    ".leaflet-bar a.leaflet-disabled{cursor:default;background-color:#f4f4f4;color:#bbb}" +
                    ".leaflet-touch .leaflet-bar a{width:30px;height:30px;line-height:30px}" +
                    ".leaflet-control-zoom-in,.leaflet-control-zoom-out{font:bold 18px 'Lucida Console',Monaco,monospace;text-indent:1px}" +
                    ".leaflet-control-attribution{background:#fff;background:rgba(255,255,255,.8);margin:0}" +
                    ".leaflet-control-attribution a{text-decoration:none}" +
                    ".leaflet-left .leaflet-control-scale{margin-left:5px}.leaflet-bottom .leaflet-control-scale{margin-bottom:5px}" +
                    ".leaflet-control-scale-line{border:2px solid #777;border-top:none;line-height:1.1;padding:2px 5px 1px;white-space:nowrap;box-sizing:border-box;background:#fff;background:rgba(255,255,255,.5)}" +
                    ".leaflet-control-scale-line:not(:first-child){border-top:2px solid #777;border-bottom:none;margin-top:-2px}" +
                    ".leaflet-touch .leaflet-control-attribution,.leaflet-touch .leaflet-control-layers,.leaflet-touch .leaflet-bar{box-shadow:none}" +
                    ".leaflet-touch .leaflet-control-layers,.leaflet-touch .leaflet-bar{border:2px solid rgba(0,0,0,.2);background-clip:padding-box}" +
                    ".leaflet-popup{position:absolute;text-align:center;margin-bottom:20px}" +
                    ".leaflet-popup-content-wrapper,.leaflet-popup-tip{background:#fff;color:#333;box-shadow:0 3px 14px rgba(0,0,0,.4)}" +
                    ".leaflet-popup-content-wrapper{padding:1px;text-align:left;border-radius:12px}" +
                    ".leaflet-popup-tip-container{width:40px;height:20px;position:absolute;left:50%;margin-left:-20px;overflow:hidden;pointer-events:none}" +
                    ".leaflet-popup-tip{width:17px;height:17px;padding:1px;margin:-10px auto 0;pointer-events:auto;-webkit-transform:rotate(45deg);transform:rotate(45deg)}" +
                    ".leaflet-popup-content-wrapper a{color:#0078a8}" +
                    ".leaflet-popup-content{margin:13px 24px 13px 20px;line-height:1.3;word-break:break-word}" +
                    ".leaflet-popup-content p{margin:17px 0 11px}" +
                    ".leaflet-popup-close-button{position:absolute;top:0;right:0;border:none;text-align:center;width:24px;height:24px;font:16px/24px Tahoma,Verdana,sans-serif;color:#757575;text-decoration:none;background:0 0}" +
                    ".leaflet-popup-close-button:hover{color:#585858}" +
                    ".leaflet-popup-scrolled{overflow:auto}" +
                    ".leaflet-div-icon{background:#fff;border:1px solid #666}" +
                    ".leaflet-tooltip{position:absolute;padding:6px;background-color:#fff;border:1px solid #fff;border-radius:3px;color:#222;white-space:nowrap;user-select:none;pointer-events:none;box-shadow:0 1px 3px rgba(0,0,0,.4)}" +
                    ".leaflet-container{cursor:grab}.leaflet-grab{cursor:grab}.leaflet-dragging .leaflet-grab{cursor:grabbing}";

    // =====================================================================
    // AFFICHER une localisation existante
    // =====================================================================
    public static void afficherCarte(String localisation) {
        try {
            Stage mapStage = new Stage();
            mapStage.setTitle("Localisation : " + localisation);

            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36");

            VBox.setVgrow(webView, Priority.ALWAYS);
            webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Label infoLabel = new Label("Formation a : " + localisation);
            infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #2c3e50; -fx-padding: 10px; -fx-background-color: #ecf0f1;");
            infoLabel.setMaxWidth(Double.MAX_VALUE);
            infoLabel.setAlignment(Pos.CENTER);

            VBox root = new VBox(webView, infoLabel);
            VBox.setVgrow(webView, Priority.ALWAYS);

            Scene scene = new Scene(root, 960, 680);
            mapStage.setScene(scene);
            mapStage.show();

            Platform.runLater(() -> {
                int mapH = (int)(scene.getHeight() - 50);
                engine.loadContent(buildHtml(localisation, false, 960, mapH));

                PauseTransition p1 = new PauseTransition(Duration.millis(800));
                p1.setOnFinished(e -> callInvalidateSize(engine));
                p1.play();

                PauseTransition p2 = new PauseTransition(Duration.millis(1500));
                p2.setOnFinished(e -> callInvalidateSize(engine));
                p2.play();
            });

        } catch (Exception e) {
            System.err.println("Erreur Maps : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // CHOISIR une localisation sur la carte
    // =====================================================================
    public static void choisirLocalisation(Consumer<String> onLocalisationChoisie) {
        Stage mapStage = new Stage();
        mapStage.setTitle("Choisir une localisation");
        mapStage.initModality(Modality.APPLICATION_MODAL);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36");

        VBox.setVgrow(webView, Priority.ALWAYS);
        webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        TextField villeField = new TextField();
        villeField.setPromptText("Cliquez sur la carte...");
        villeField.setEditable(false);
        villeField.setStyle("-fx-padding: 8px; -fx-font-size: 13px; " +
                "-fx-background-radius: 6px; -fx-border-color: #3498db; -fx-border-width: 2px;");
        HBox.setHgrow(villeField, Priority.ALWAYS);

        Button btnConfirmer = new Button("Confirmer");
        btnConfirmer.setStyle("-fx-background-color: #50C878; -fx-text-fill: white; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnConfirmer.setDisable(true);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> mapStage.close());

        engine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null
                    && !newTitle.isEmpty()
                    && !newTitle.equalsIgnoreCase("about:blank")
                    && !newTitle.toLowerCase().contains("leaflet")
                    && !newTitle.startsWith("http")
                    && !newTitle.startsWith("www")
                    && newTitle.length() > 1) {
                Platform.runLater(() -> {
                    villeField.setText(newTitle);
                    btnConfirmer.setDisable(false);
                });
            }
        });

        btnConfirmer.setOnAction(e -> {
            String ville = villeField.getText().trim();
            if (!ville.isEmpty()) {
                onLocalisationChoisie.accept(ville);
                mapStage.close();
            }
        });

        Label info = new Label("Cliquez sur la carte pour selectionner une ville");
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 8px 12px;");
        info.setMaxWidth(Double.MAX_VALUE);

        HBox controls = new HBox(10, villeField, btnConfirmer, btnAnnuler);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: #f8f9fa;");

        VBox root = new VBox(info, webView, controls);
        VBox.setVgrow(webView, Priority.ALWAYS);

        Scene scene = new Scene(root, 960, 700);
        mapStage.setScene(scene);
        mapStage.show();

        Platform.runLater(() -> {
            int mapH = (int)(scene.getHeight() - 35 - 58);
            engine.loadContent(buildHtml(null, true, 960, mapH));

            PauseTransition p1 = new PauseTransition(Duration.millis(800));
            p1.setOnFinished(e -> callInvalidateSize(engine));
            p1.play();

            PauseTransition p2 = new PauseTransition(Duration.millis(1500));
            p2.setOnFinished(e -> callInvalidateSize(engine));
            p2.play();
        });
    }

    private static void callInvalidateSize(WebEngine engine) {
        try {
            engine.executeScript("if(window._map){ window._map.invalidateSize(true); }");
        } catch (Exception ex) {
            // ignore
        }
    }

    // =====================================================================
    // Construction du HTML
    // =====================================================================
    private static String buildHtml(String localisation, boolean clickMode, int w, int h) {
        String mapH = h + "px";
        String mapW = w + "px";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>");
        sb.append("<meta charset='utf-8'/>");
        sb.append("<style>").append(LEAFLET_CSS).append("</style>");
        sb.append("<style>");
        sb.append("* { margin:0; padding:0; }");
        sb.append("html, body { width:").append(mapW).append("; height:").append(mapH).append("; overflow:hidden; background:#e8e8e8; }");
        sb.append("#map { width:").append(mapW).append("; height:").append(mapH).append("; }");
        sb.append("</style>");
        sb.append("<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>");
        sb.append("</head><body>");
        sb.append("<div id='map'></div>");
        sb.append("<script>");

        sb.append("window.addEventListener('load', function() {");
        sb.append("  var map = L.map('map', { preferCanvas: true });");
        sb.append("  window._map = map;");

        // =====================================================================
        // ICONE PERSONNALISEE : Pin SVG professionnel couleur emeraude #50C878
        // =====================================================================
        sb.append("  var svg = '<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"40\" height=\"52\" viewBox=\"0 0 40 52\">'");
        sb.append("    + '<defs>'");
        sb.append("    + '<filter id=\"sh\" x=\"-30%\" y=\"-20%\" width=\"160%\" height=\"160%\">'");
        sb.append("    + '<feDropShadow dx=\"0\" dy=\"3\" stdDeviation=\"3\" flood-color=\"rgba(0,0,0,0.35)\"/>'");
        sb.append("    + '</filter>'");
        sb.append("    + '<radialGradient id=\"gr\" cx=\"38%\" cy=\"32%\" r=\"65%\">'");
        sb.append("    + '<stop offset=\"0%\" stop-color=\"#80e8a8\"/>'");
        sb.append("    + '<stop offset=\"100%\" stop-color=\"#27a85a\"/>'");
        sb.append("    + '</radialGradient>'");
        sb.append("    + '</defs>'");
        sb.append("    + '<path d=\"M20 1C9.5 1 1 9.5 1 20c0 14.6 19 31 19 31S39 34.6 39 20C39 9.5 30.5 1 20 1z\"'");
        sb.append("    + ' fill=\"url(#gr)\" filter=\"url(#sh)\"/>'");
        sb.append("    + '<circle cx=\"20\" cy=\"20\" r=\"10\" fill=\"rgba(255,255,255,0.92)\"/>'");
        sb.append("    + '<circle cx=\"20\" cy=\"20\" r=\"6\" fill=\"#27a85a\"/>'");
        sb.append("    + '<circle cx=\"20\" cy=\"20\" r=\"3\" fill=\"white\"/>'");
        sb.append("    + '</svg>';");
        sb.append("  var iconUrl = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);");
        sb.append("  var customIcon = L.icon({");
        sb.append("    iconUrl: iconUrl,");
        sb.append("    iconSize:    [40, 52],");
        sb.append("    iconAnchor:  [20, 52],");
        sb.append("    popupAnchor: [0, -54]");
        sb.append("  });");
        // =====================================================================

        sb.append("  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {");
        sb.append("    maxZoom: 19,");
        sb.append("    attribution: '&copy; OpenStreetMap contributors'");
        sb.append("  }).addTo(map);");

        if (clickMode) {
            sb.append("  map.setView([33.8869, 9.5375], 6);");
            sb.append("  map.on('load', function(){ map.invalidateSize(true); });");
            sb.append("  setTimeout(function(){ map.invalidateSize(true); }, 500);");

            sb.append("  var marker = null;");
            sb.append("  map.on('click', function(e) {");
            sb.append("    var lat = e.latlng.lat.toFixed(6), lng = e.latlng.lng.toFixed(6);");
            sb.append("    fetch('https://nominatim.openstreetmap.org/reverse?lat='+lat+'&lon='+lng+'&format=json&accept-language=fr')");
            sb.append("      .then(function(r){ return r.json(); })");
            sb.append("      .then(function(d){");
            sb.append("        var a = d.address||{};");
            sb.append("        var city = a.city||a.town||a.village||a.municipality||a.county||a.state||'Lieu inconnu';");
            sb.append("        document.title = city;");
            sb.append("        if(marker) map.removeLayer(marker);");
            sb.append("        marker = L.marker([lat, lng], {icon: customIcon})");
            sb.append("                  .addTo(map)");
            sb.append("                  .bindPopup('<div style=\"font-family:Arial;font-size:13px;font-weight:bold;color:#2c3e50;padding:4px 6px;\">'+city+'</div>')");
            sb.append("                  .openPopup();");
            sb.append("      }).catch(function(){ document.title = lat+', '+lng; });");
            sb.append("  });");
        } else {
            String locUrl = (localisation != null ? localisation : "Tunis").replace(" ", "+") + "+Tunisie";
            String locLabel = localisation != null
                    ? localisation.replace("'", "\\'").replace("\"", "\\\"")
                    : "Tunis";

            sb.append("  map.setView([33.8869, 9.5375], 6);");
            sb.append("  setTimeout(function(){ map.invalidateSize(true); }, 500);");

            sb.append("  fetch('https://nominatim.openstreetmap.org/search?q=").append(locUrl)
                    .append("&format=json&limit=1&accept-language=fr&countrycodes=tn')");
            sb.append("    .then(function(r){ return r.json(); })");
            sb.append("    .then(function(d){");
            sb.append("      if(d && d.length > 0){");
            sb.append("        var lat = parseFloat(d[0].lat), lon = parseFloat(d[0].lon);");
            sb.append("        map.setView([lat, lon], 13);");
            sb.append("        map.invalidateSize(true);");
            sb.append("        L.marker([lat, lon], {icon: customIcon})");
            sb.append("          .addTo(map)");
            sb.append("          .bindPopup('<div style=\"font-family:Arial;font-size:13px;font-weight:bold;color:#2c3e50;padding:4px 6px;\">").append(locLabel).append("</div>')");
            sb.append("          .openPopup();");
            sb.append("      }");
            sb.append("    }).catch(function(err){ console.error(err); });");
        }

        sb.append("});");
        sb.append("</script></body></html>");
        return sb.toString();
    }
}