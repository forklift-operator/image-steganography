package bg.sofia.uni.fmi.mjt.steganography;

public interface ImageCodec {

    /**
     * Iterates over all files from @coverSourceDirectory and @secretSourceDirectory in a lexicographic order and
     * picks up image ones - those with extensions png. Embeds all the images from @secretSourceDirectory into the
     * corresponding images from @coverSourceDirectory. Both directories are guaranteed to have the same number of
     * images and the secret image will always be embeddable in the cover image.
     * The resulting images are saved with the name of the corresponding cover image into the @outputDirectory.
     */
    void embedPNGImages(String coverSourceDirectory, String secretSourceDirectory, String outputDirectory);

    /***
     * Iterates over all files from @sourceDirectory and picks up image ones - those with extensions png.
     * Extracts from them the embedded images and saves the in the @outputDirectory with the same name as
     * the corresponding image from the @sourceDirectory.
     *
     * @param sourceDirectory the source directory containing the embedded images
     * @param outputDirectory the output directory in which the extracted images are saved
     */
    void extractPNGImages(String sourceDirectory, String outputDirectory);

}