package bg.sofia.uni.fmi.mjt.steganography.algorithm;

import java.awt.image.BufferedImage;

public interface SteganographyAlgorithm {

    BufferedImage embed(BufferedImage cover, BufferedImage secret);

    BufferedImage extract(BufferedImage source);

}
