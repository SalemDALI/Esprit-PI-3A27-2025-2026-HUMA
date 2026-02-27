package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class CvFileStorageUtil {

    private CvFileStorageUtil() {
    }

    public static String savePdfCv(Path sourcePdfPath) throws IOException {
        if (sourcePdfPath == null) {
            throw new IllegalArgumentException("Aucun fichier selectionne");
        }

        String fileName = sourcePdfPath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Le fichier doit etre un PDF");
        }

        Path uploadDir = Path.of("uploads", "cv");
        Files.createDirectories(uploadDir);

        String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String finalName = System.currentTimeMillis() + "_" + safeName;

        Path targetPath = uploadDir.resolve(finalName);
        Files.copy(sourcePdfPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toAbsolutePath().normalize().toString().replace("\\", "/");
    }

    public static String savePdfCv(Path sourcePdfPath, int candidatId) throws IOException {
        if (sourcePdfPath == null) {
            throw new IllegalArgumentException("Aucun fichier selectionne");
        }
        if (candidatId <= 0) {
            throw new IllegalArgumentException("Id candidat invalide");
        }

        String fileName = sourcePdfPath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Le fichier doit etre un PDF");
        }

        Path uploadDir = Path.of("uploads", "cv");
        Files.createDirectories(uploadDir);

        Path targetPath = uploadDir.resolve(candidatId + ".pdf");
        Files.copy(sourcePdfPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toAbsolutePath().normalize().toString().replace("\\", "/");
    }
}
