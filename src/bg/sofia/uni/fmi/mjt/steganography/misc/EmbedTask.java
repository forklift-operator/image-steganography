package bg.sofia.uni.fmi.mjt.steganography.misc;

import java.awt.image.BufferedImage;

public record EmbedTask(
        BufferedImage cover,
        BufferedImage secret,
        String coverName,
        String outputPath,
        boolean isPoison
) {

    public static EmbedTask poisonPill() {
        return new EmbedTask(null, null, null, null, true);
    }

    public EmbedTask {

        if (!isPoison &&
                (cover == null
                        || secret == null
                        || coverName == null
                        || coverName.isBlank()
                        || outputPath == null
                        || outputPath.isBlank())) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

    }

}
