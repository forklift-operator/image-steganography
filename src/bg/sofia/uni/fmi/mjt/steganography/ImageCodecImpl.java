package bg.sofia.uni.fmi.mjt.steganography;

import bg.sofia.uni.fmi.mjt.steganography.algorithm.SteganoImpl;

public class ImageCodecImpl implements ImageCodec{
    SteganoImpl algorithm = new SteganoImpl();

    @Override
    public void embedPNGImages(String coverSourceDirectory, String secretSourceDirectory, String outputDirectory) {

    }

    @Override
    public void extractPNGImages(String sourceDirectory, String outputDirectory) {

    }
}
