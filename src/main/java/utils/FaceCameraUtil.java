package utils;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class FaceCameraUtil {

    /**
     * Ouvre la webcam, capture une seule image PNG et la retourne en bytes.
     * Retourne null si aucune webcam ou aucune image n'est disponible.
     */
    public static byte[] captureFaceImageOnce() throws Exception {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            return null;
        }
        webcam.open();
        try {
            BufferedImage image = webcam.getImage();
            if (image == null) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            baos.flush();
            return baos.toByteArray();
        } finally {
            webcam.close();
        }
    }
}

