package bg.sofia.uni.fmi.mjt.steganography;

import bg.sofia.uni.fmi.mjt.steganography.algorithm.SteganoImpl;
import bg.sofia.uni.fmi.mjt.steganography.misc.EmbedTask;
import bg.sofia.uni.fmi.mjt.steganography.misc.ExtractTask;
import bg.sofia.uni.fmi.mjt.steganography.worker.EmbedConsumer;
import bg.sofia.uni.fmi.mjt.steganography.worker.ExtractConsumer;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCodecImpl implements ImageCodec {
    private final SteganoImpl algorithm = new SteganoImpl();

    private static final int EMBED_QUEUE_CAP = 5;
    private static final int EXTRACT_QUEUE_CAP = 5;
    private static final int NUMBER_OF_CONSUMERS = Runtime.getRuntime().availableProcessors();

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

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_CONSUMERS);

        for (int i = 0; i < NUMBER_OF_CONSUMERS; i++) {
            executor.submit(new EmbedConsumer(queue, algorithm));
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

            for (int i = 0; i < NUMBER_OF_CONSUMERS; i++) {
                queue.put(EmbedTask.poisonPill());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();
    }

    @Override
    public void extractPNGImages(String sourceDirectory, String outputDirectory) {

        if (sourceDirectory == null || outputDirectory == null) {
            throw new IllegalArgumentException("Directories cannot be null");
        }

        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdir();
        }

        BlockingQueue<ExtractTask> queue = new ArrayBlockingQueue<>(EXTRACT_QUEUE_CAP);

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_CONSUMERS);

        for (int i = 0; i < NUMBER_OF_CONSUMERS; i++) {
            executor.submit(new ExtractConsumer(queue, algorithm));
        }

        Path sourcePath = Path.of(sourceDirectory);

        try (DirectoryStream<Path> sourceStream = Files.newDirectoryStream(sourcePath)) {

            Iterator<Path> sourceIterator = sourceStream.iterator();

            while (sourceIterator.hasNext()) {
                Path current = sourceIterator.next();

                BufferedImage currentImage = loadImage(current);

                queue.put(new ExtractTask(
                        currentImage,
                        getFileNameNoExtension(current),
                        outputDirectory,
                        false
                ));
            }

            for (int i = 0; i < NUMBER_OF_CONSUMERS; i++) {
                queue.put(ExtractTask.poisonPill());
            }

            executor.shutdown();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

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
