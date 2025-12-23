package bg.sofia.uni.fmi.mjt.steganography.worker.embeder;

import bg.sofia.uni.fmi.mjt.steganography.misc.EmbedTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

public class EmbedProducer implements Runnable {
    private final BlockingQueue<EmbedTask> imageTasks;
    private final Path pathToCover;
    private final Path pathToSecret;
    private final String outputPath;

    public EmbedProducer(BlockingQueue<EmbedTask> imageTasks, Path pathToCover, Path pathToSecret, String outputPath) {
        if (imageTasks == null) {
            throw new IllegalArgumentException("Image tasks cannot be null");
        }

        if (pathToCover == null || pathToSecret == null) {
            throw new IllegalArgumentException("Path to images cannot be null");
        }

        if (outputPath == null || outputPath.isBlank()) {
            throw new IllegalArgumentException("Output path cannot be null");
        }

        this.outputPath = outputPath;
        this.imageTasks = imageTasks;
        this.pathToCover = pathToCover;
        this.pathToSecret = pathToSecret;
    }

    public static BufferedImage loadImage(Path imagePath) {
        try {
            return ImageIO.read(imagePath.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to load image %s", imagePath.toString()), e);
        }
    }

    @Override
    public void run() {

        BufferedImage cover = loadImage(pathToCover);
        BufferedImage secret = loadImage(pathToSecret);

        int cSize = cover.getWidth() * cover.getHeight();
        int sSize = secret.getWidth() * secret.getHeight();

        if (cSize < sSize + 8) {
            throw new IllegalArgumentException("The cover must be 8 pixels larger than the secret");
        }

        EmbedTask task = new EmbedTask(cover, secret, getFileNameNoExtension(pathToCover), outputPath, false);
        try {
            imageTasks.put(task);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error putting task in the queue", e);
        }

    }

    private String getFileNameNoExtension(Path pathToFile) {
        String fileString = pathToFile.getFileName().toString();
        int endIndex = fileString.lastIndexOf('.');
        return fileString.substring(0, endIndex - 1);
    }
}
