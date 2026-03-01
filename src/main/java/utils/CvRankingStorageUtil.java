package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.CandidateScoringResult;
import models.OffreEmploi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CvRankingStorageUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private CvRankingStorageUtil() {
    }

    public static String saveRankingAsJson(OffreEmploi offre, List<CandidateScoringResult> ranking) throws IOException {
        Path outDir = Path.of("uploads");
        Files.createDirectories(outDir);

        String offreId = offre == null ? "0" : String.valueOf(offre.getId());
        Path outFile = outDir.resolve("offre_" + offreId + "_cv.json");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("generated_at", LocalDateTime.now().toString());
        payload.put("offre_id", offre == null ? null : offre.getId());
        payload.put("offre_titre", offre == null ? "" : offre.getTitre());
        payload.put("offre_departement", offre == null ? "" : offre.getDepartement());
        payload.put("total_candidates", ranking == null ? 0 : ranking.size());
        payload.put("ranking", ranking);

        MAPPER.writeValue(outFile.toFile(), payload);
        return outFile.toString().replace("\\", "/");
    }
}
