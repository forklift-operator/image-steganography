package bg.sofia.uni.fmi.mjt.steganography.algorithm;

import java.awt.image.BufferedImage;

public class SteganoImpl implements SteganographyAlgorithm {

    @Override
    public BufferedImage embed(BufferedImage secret, BufferedImage cover) {

        if (cover == null || secret == null) {
            throw new IllegalArgumentException("One of the images is null");
        }

        int cSize = cover.getHeight() * cover.getWidth();
        int sSize = secret.getHeight() * secret.getWidth();

        if (cSize < sSize + 8) {
            throw new IllegalArgumentException("The cover must be 8 pixels larger than the secret");
        }

        BufferedImage result = new BufferedImage(cover.getWidth(), cover.getHeight(), BufferedImage.TYPE_INT_RGB);

        int sWidth = secret.getWidth();
        int sHeight = secret.getHeight();

        int coverIndex = 0;

        while (true) {
            int cx = coverIndex % cover.getWidth();
            int cy = coverIndex / cover.getWidth();

            if (cx > cover.getWidth() || cy > cover.getHeight() || coverIndex == 8) {
                break;
            }

            int coverPixel = cover.getRGB(cx, cy);

            if (coverIndex < 4) {
                // embedPixel width
                int shift = (12 - (coverIndex + 1) * 3);

                embedPixel(sWidth, shift, coverPixel, result, cx, cy);

                coverIndex++;
                continue;
            }

            if (coverIndex < 8) {
                //embedPixel height
                int shift = (12 - (coverIndex - 4 + 1) * 3);

                embedPixel(sHeight, shift, coverPixel, result, cx, cy);

                coverIndex++;
                continue;
            }

            coverIndex++;
        }

        for (int sy = 0; sy < sHeight; sy++) {
            for (int sx = 0; sx < sWidth; sx++) {
                int cx = coverIndex % cover.getWidth();
                int cy = coverIndex / cover.getWidth();

                int coverPixel = cover.getRGB(cx, cy);

                //embedPixel secret
                int sPixel = secret.getRGB(sx, sy);

                int r = (sPixel >> 16) & 0xff;
                int g = (sPixel >> 8) & 0xff;
                int b = sPixel & 0xff;
                int avg = (r + g + b) / 3;

                embedPixel(avg, 5, coverPixel, result, cx, cy);

                coverIndex++;
            }
        }

        while (true) {
            int cx = coverIndex % cover.getWidth();
            int cy = coverIndex / cover.getWidth();

            if (cx >= cover.getWidth() || cy >= cover.getHeight()) {
                break;
            }

            result.setRGB(cx, cy, cover.getRGB(cx, cy));
            coverIndex++;
        }

        return result;

    }

    private static void embedPixel(int sourcePixel, int shift, int destinationPixel, BufferedImage result, int x, int y) {

        int cleanLSB = 0xfe; //1111 1110
        int cleanPixelMask = cleanLSB << 16
                | cleanLSB << 8
                | cleanLSB;

        int threeBits = sourcePixel >> shift & 0b111;

        int resultPixel = (destinationPixel & cleanPixelMask)
                | (threeBits >> 2 & 0b1) << 16
                | (threeBits >> 1 & 0b1) << 8
                | (threeBits & 0b1);

        result.setRGB(x, y, resultPixel);

    }

    @Override
    public BufferedImage extract(BufferedImage source) {

        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        BufferedImage result;

        int width = 0;
        int height = 0;

        int sourceIndex = 0;
        while (true) {
            int cx = sourceIndex % source.getWidth();
            int cy = sourceIndex / source.getWidth();

            if (cx >= source.getWidth() || cy >= source.getHeight() || sourceIndex == 8) {
                break;
            }

            int pixel = source.getRGB(cx, cy);
            int fromR = pixel >> 16 & 1;
            int fromG = pixel >> 8 & 1;
            int fromB = pixel & 1;
            int block = fromR << 2
                    | fromG << 1
                    | fromB;

            if (sourceIndex < 4) {
                width = width | block << (9 - sourceIndex * 3);

                sourceIndex++;
                continue;
            }

            if (sourceIndex < 8) {
                height = height | block << (9 - (sourceIndex - 4) * 3);
            }

            sourceIndex++;
        }

        result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int cx = sourceIndex % source.getWidth();
                int cy = sourceIndex / source.getWidth();

                int pixel = source.getRGB(cx, cy);
                int fromR = pixel >> 16 & 1;
                int fromG = pixel >> 8 & 1;
                int fromB = pixel & 1;
                int block = fromR << 2
                        | fromG << 1
                        | fromB;

                int value = block << 5;

                int resultPixel =
                        value << 16
                                | value << 8
                                | value;

                result.setRGB(x, y, resultPixel);

                sourceIndex++;
            }
        }

        return result;
    }
}
