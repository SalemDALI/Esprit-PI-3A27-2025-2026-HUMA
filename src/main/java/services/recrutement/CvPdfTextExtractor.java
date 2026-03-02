package services.recrutement;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CvPdfTextExtractor {

    public String extractText(String cvPath) throws IOException {
        if (cvPath == null || cvPath.isBlank()) {
            throw new IllegalArgumentException("Chemin CV vide.");
        }

        Path path = Path.of(cvPath);
        if (!Files.exists(path)) {
            throw new IOException("CV introuvable: " + cvPath);
        }

        try (PDDocument document = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
