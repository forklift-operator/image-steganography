package bg.sofia.uni.fmi.mjt.steganography.worker;

import bg.sofia.uni.fmi.mjt.steganography.algorithm.SteganographyAlgorithm;
import bg.sofia.uni.fmi.mjt.steganography.misc.ExtractTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.BlockingQueue;

public class ExtractConsumer implements Runnable{
    private final BlockingQueue<ExtractTask> imageTasks;
    private final SteganographyAlgorithm algorithm;

    public ExtractConsumer(BlockingQueue<ExtractTask> imageTasks, SteganographyAlgorithm algorithm) {
        if (imageTasks == null) {
            throw new IllegalArgumentException("Image tasks queue cannot be null");
        }

        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }

        this.imageTasks = imageTasks;
        this.algorithm = algorithm;
    }


    @Override
    public void run() {
        try {
            while (true) {
                ExtractTask task = imageTasks.take();
                if (task.isPoison()) {
                    break;
                }
                consume(task);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void consume (ExtractTask task) {
        BufferedImage extracted = algorithm.extract(task.source());
        saveImage(extracted, task.outputPath(), "extracted-" + task.sourceName() + ".png");
    }

    private void saveImage(BufferedImage image, String outputDir, String name) {
        try {
            ImageIO.write(image, "png", new File(outputDir, name));
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("While saving image %s", name), e);
        }
    }

}
