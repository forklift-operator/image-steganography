package bg.sofia.uni.fmi.mjt.steganography;

import bg.sofia.uni.fmi.mjt.steganography.algorithm.SteganoImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class Demo {
    public static BufferedImage loadImage(Path imagePath) {
        try {
            return ImageIO.read(imagePath.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to load image %s", imagePath.toString()), e);
        }
    }

    public static void saveImage(BufferedImage image, String outputDir, String name) {
        try {
            ImageIO.write(image, "png", new File(outputDir, name));
            System.out.println("Saved " + name + " to " + outputDir);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("While saving image %s", name), e);
        }
    }

    static void main() {
        SteganoImpl alg = new SteganoImpl();

        BufferedImage secret = loadImage(Path.of("dog.png"));
        BufferedImage cover = loadImage(Path.of("cat.png"));

        BufferedImage embedded = alg.embed(secret, cover);
        BufferedImage extracted = alg.extract(embedded);

        saveImage(embedded, ".", "./hidden.png");
        saveImage(extracted, ".", "./extracted.png");
    }
}