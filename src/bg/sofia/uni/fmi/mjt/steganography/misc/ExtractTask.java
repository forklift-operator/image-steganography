package bg.sofia.uni.fmi.mjt.steganography.misc;

import java.awt.image.BufferedImage;

public record ExtractTask(
        BufferedImage source,
        String sourceName,
        String outputPath,
        boolean isPoison
) {
    public static ExtractTask poisonPill() {
        return new ExtractTask(null, null, null, true);
    }

    public ExtractTask {
        if (!isPoison &&
                (source == null ||
                        sourceName == null ||
                        sourceName.isBlank() ||
                        outputPath == null ||
                        outputPath.isBlank())) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
    }
}
