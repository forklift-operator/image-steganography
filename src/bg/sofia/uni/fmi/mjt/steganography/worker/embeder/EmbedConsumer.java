package bg.sofia.uni.fmi.mjt.steganography.worker.embeder;

import bg.sofia.uni.fmi.mjt.steganography.algorithm.SteganographyAlgorithm;
import bg.sofia.uni.fmi.mjt.steganography.misc.EmbedTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.BlockingQueue;

public class EmbedConsumer implements Runnable {
    private final BlockingQueue<EmbedTask> imageTasks;
    private final SteganographyAlgorithm embedAlgorithm;


    public EmbedConsumer(BlockingQueue<EmbedTask> imageTasks, SteganographyAlgorithm embedAlgorithm) {
        if (imageTasks == null) {
            throw new IllegalArgumentException("Image tasks cannot be null");
        }

        if (embedAlgorithm == null) {
            throw new IllegalArgumentException("Embedding algorithm cannot be null");
        }

        this.embedAlgorithm = embedAlgorithm;
        this.imageTasks = imageTasks;
    }


    public void saveImage(BufferedImage image, String outputDir, String name) {
        try {
            ImageIO.write(image, "png", new File(outputDir, name));
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("While saving image %s", name), e);
        }
    }

    @Override
    public void run() {

        try {
            while (true) {
                EmbedTask task = imageTasks.take();
                if (task.isPoison()) {
                    break;
                }
                consume(task);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while taking next elem from the queue", e);
        }

    }

    void consume(EmbedTask task) {
        BufferedImage embedded = embedAlgorithm.embed(task.cover(), task.secret());
        saveImage(embedded, task.outputPath(), task.coverName() + ".png");
    }
}
