package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PublicationValidator {

    // ── Constantes ──
    public static final int TITRE_MIN        = 5;
    public static final int TITRE_MAX        = 100;
    public static final int CONTENU_MIN      = 10;
    public static final int CONTENU_MAX      = 2000;
    public static final int COMMENTAIRE_MIN  = 2;
    public static final int COMMENTAIRE_MAX  = 500;

    // Mots interdits
    private static final List<String> MOTS_INTERDITS = List.of(
            "idiot", "imbecile", "nul", "stupide", "con", "connard",
            "merde", "putain", "salaud", "cretin", "abruti"
    );

    // Pattern URL
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://|www\\.)\\S+", Pattern.CASE_INSENSITIVE
    );

    // Pattern caractères spéciaux dangereux (XSS, injection)
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile(
            "[<>\"'%;()&+\\-]{3,}"
    );

    // Pattern HTML tags
    private static final Pattern HTML_PATTERN = Pattern.compile(
            "<[^>]+>", Pattern.CASE_INSENSITIVE
    );

    // Pattern que des espaces/chiffres/caractères spéciaux (pas de vraies lettres)
    private static final Pattern ONLY_SPECIAL = Pattern.compile(
            "^[^a-zA-ZÀ-ÿ]+$"
    );

    // ══════════════════════════════════════════
    // VALIDATION TITRE
    // ══════════════════════════════════════════
    public static List<String> validerTitre(String titre) {
        List<String> erreurs = new ArrayList<>();

        if (titre == null || titre.isBlank()) {
            erreurs.add("Le titre est obligatoire.");
            return erreurs;
        }

        String t = titre.trim();

        if (t.length() < TITRE_MIN)
            erreurs.add("Le titre doit contenir au moins " + TITRE_MIN + " caracteres.");

        if (t.length() > TITRE_MAX)
            erreurs.add("Le titre ne doit pas depasser " + TITRE_MAX + " caracteres.");

        if (ONLY_SPECIAL.matcher(t).matches())
            erreurs.add("Le titre doit contenir au moins une lettre.");

        if (HTML_PATTERN.matcher(t).find())
            erreurs.add("Le titre ne doit pas contenir de balises HTML.");

        if (SPECIAL_CHARS_PATTERN.matcher(t).find())
            erreurs.add("Le titre contient des caracteres speciaux non autorises.");

        if (URL_PATTERN.matcher(t).find())
            erreurs.add("Le titre ne doit pas contenir de lien URL.");

        String tLower = t.toLowerCase();
        for (String mot : MOTS_INTERDITS) {
            if (tLower.contains(mot)) {
                erreurs.add("Le titre contient un mot interdit.");
                break;
            }
        }

        return erreurs;
    }

    // ══════════════════════════════════════════
    // VALIDATION CONTENU PUBLICATION
    // ══════════════════════════════════════════
    public static List<String> validerContenuPublication(String contenu) {
        List<String> erreurs = new ArrayList<>();

        if (contenu == null || contenu.isBlank()) {
            erreurs.add("Le contenu est obligatoire.");
            return erreurs;
        }

        String c = contenu.trim();

        if (c.length() < CONTENU_MIN)
            erreurs.add("Le contenu doit contenir au moins " + CONTENU_MIN + " caracteres.");

        if (c.length() > CONTENU_MAX)
            erreurs.add("Le contenu ne doit pas depasser " + CONTENU_MAX + " caracteres.");

        if (ONLY_SPECIAL.matcher(c).matches())
            erreurs.add("Le contenu doit contenir au moins une lettre.");

        if (HTML_PATTERN.matcher(c).find())
            erreurs.add("Le contenu ne doit pas contenir de balises HTML.");

        if (SPECIAL_CHARS_PATTERN.matcher(c).find())
            erreurs.add("Le contenu contient des caracteres speciaux non autorises.");

        String cLower = c.toLowerCase();
        for (String mot : MOTS_INTERDITS) {
            if (cLower.contains(mot)) {
                erreurs.add("Le contenu contient un mot interdit.");
                break;
            }
        }

        return erreurs;
    }

    // ══════════════════════════════════════════
    // VALIDATION COMMENTAIRE
    // ══════════════════════════════════════════
    public static List<String> validerCommentaire(String commentaire) {
        List<String> erreurs = new ArrayList<>();

        if (commentaire == null || commentaire.isBlank()) {
            erreurs.add("Le commentaire est obligatoire.");
            return erreurs;
        }

        String c = commentaire.trim();

        if (c.length() < COMMENTAIRE_MIN)
            erreurs.add("Le commentaire doit contenir au moins " + COMMENTAIRE_MIN + " caracteres.");

        if (c.length() > COMMENTAIRE_MAX)
            erreurs.add("Le commentaire ne doit pas depasser " + COMMENTAIRE_MAX + " caracteres.");

        if (ONLY_SPECIAL.matcher(c).matches())
            erreurs.add("Le commentaire doit contenir au moins une lettre.");

        if (HTML_PATTERN.matcher(c).find())
            erreurs.add("Le commentaire ne doit pas contenir de balises HTML.");

        if (URL_PATTERN.matcher(c).find())
            erreurs.add("Le commentaire ne doit pas contenir de lien URL.");

        if (SPECIAL_CHARS_PATTERN.matcher(c).find())
            erreurs.add("Le commentaire contient des caracteres speciaux non autorises.");

        String cLower = c.toLowerCase();
        for (String mot : MOTS_INTERDITS) {
            if (cLower.contains(mot)) {
                erreurs.add("Le commentaire contient un mot interdit.");
                break;
            }
        }

        return erreurs;
    }

    // ══════════════════════════════════════════
    // METHODE GLOBALE : valider publication complète
    // ══════════════════════════════════════════
    public static List<String> validerPublication(String titre, String contenu) {
        List<String> erreurs = new ArrayList<>();
        erreurs.addAll(validerTitre(titre));
        erreurs.addAll(validerContenuPublication(contenu));
        return erreurs;
    }

    // ══════════════════════════════════════════
    // HELPER : formater les erreurs en string
    // ══════════════════════════════════════════
    public static String formaterErreurs(List<String> erreurs) {
        return String.join("\n", erreurs);
    }
}
