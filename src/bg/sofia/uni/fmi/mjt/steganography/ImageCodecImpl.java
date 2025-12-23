package bg.sofia.uni.fmi.mjt.steganography;

import bg.sofia.uni.fmi.mjt.steganography.algorithm.SteganoImpl;
import bg.sofia.uni.fmi.mjt.steganography.misc.EmbedTask;
import bg.sofia.uni.fmi.mjt.steganography.worker.embeder.EmbedConsumer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ImageCodecImpl implements ImageCodec {
    private final SteganoImpl algorithm = new SteganoImpl();
    private final int EMBED_QUEUE_CAP = 5;
    private final int NUMBER_OF_EMBED_CONSUMERS = 2;

    @Override
    public void embedPNGImages(String coverSourceDirectory, String secretSourceDirectory, String outputDirectory) {

        if (coverSourceDirectory == null || secretSourceDirectory == null || outputDirectory == null) {
            throw new IllegalArgumentException("Cover, secret and output directories cannot be null");
        }

        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdir();
        }

        BlockingQueue<EmbedTask> queue = new ArrayBlockingQueue<>(EMBED_QUEUE_CAP);

        for (int i = 0; i < NUMBER_OF_EMBED_CONSUMERS; i++) {
            Thread consumer = new Thread(new EmbedConsumer(queue, algorithm));
            consumer.start();
        }

        Path coverPath = Path.of(coverSourceDirectory);
        Path secretPath = Path.of(secretSourceDirectory);

        try (DirectoryStream<Path> coverStream = Files.newDirectoryStream(coverPath);
             DirectoryStream<Path> secretStream = Files.newDirectoryStream(secretPath)) {

            Iterator<Path> coverIterator = coverStream.iterator();
            Iterator<Path> secretIterator = secretStream.iterator();

            while (coverIterator.hasNext() && secretIterator.hasNext()) {
                Path currentCover = coverIterator.next();
                Path currentSecret = secretIterator.next();

                System.out.println(currentCover.toString() + " " + currentSecret.toString());

                BufferedImage coverImg = loadImage(currentCover);
                BufferedImage secretImg = loadImage(currentSecret);

                checkImages(coverImg, secretImg);

                queue.put(new EmbedTask(
                        coverImg,
                        secretImg,
                        getFileNameNoExtension(currentCover),
                        outputDirectory,
                        false
                ));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < NUMBER_OF_EMBED_CONSUMERS; i++) {
            try {
                queue.put(EmbedTask.poisonPill());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void extractPNGImages(String sourceDirectory, String outputDirectory) {

    }

    private BufferedImage loadImage(Path imagePath) {
        try {
            return ImageIO.read(imagePath.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to load image %s", imagePath.toString()), e);
        }
    }

    private String getFileNameNoExtension(Path pathToFile) {
        String fileString = pathToFile.getFileName().toString();
        int endIndex = fileString.lastIndexOf('.');
        return fileString.substring(0, endIndex);
    }

    private void checkImages(BufferedImage cover, BufferedImage secret) {
        int cSize = cover.getWidth() * cover.getHeight();
        int sSize = secret.getWidth() * secret.getHeight();

        if (cSize < sSize + 8) {
            throw new IllegalArgumentException("The cover must be 8 pixels larger than the secret");
        }
    }

}
