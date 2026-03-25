# Image Steganography

Hide one PNG image inside another PNG image, then extract it back later.

This project is a Java implementation of basic image steganography with a concurrent producer-consumer pipeline. It works on directories of images rather than a single file at a time. The core algorithm stores a secret image inside the least significant bits of a cover image and reconstructs it later as a grayscale image.

## What it does

The embed flow reads images from a cover directory and a secret directory, pairs them in iteration order, and writes the resulting embedded images to an output directory. The extract flow reads embedded images from a directory and restores the hidden image into another output directory.

## How it works

### 1. Metadata is stored in the first 8 pixels

Before writing the secret image data, the algorithm stores the secret image dimensions in the first 8 pixels of the cover image:

- first 4 pixels store the secret width
- next 4 pixels store the secret height

Each of those pixels contributes 3 bits, so width and height are each stored in 12 bits. During extraction, the first 8 pixels are read back to recover the secret image dimensions.

### 2. One secret pixel is encoded into one cover pixel

After the metadata, the algorithm walks through the secret image pixel by pixel. For each secret pixel:

- it reads the RGB values
- computes their average
- keeps only 3 bits from that grayscale value
- places those 3 bits into the least significant bit of the cover pixel's red, green, and blue channels

That means each secret pixel is compressed to a 3-bit grayscale representation, so the extracted result is not full color. It is a lower quality grayscale version of the original secret image.

### 3. The rest of the cover image is copied unchanged

Once all secret pixels are embedded, the remaining cover pixels are copied directly into the result image without modification. 

### 4. Extraction reverses the process

During extraction, the algorithm:

- reads width and height from the first 8 pixels
- allocates a new image of that size
- reads 3 hidden bits from each following pixel
- shifts them back into a grayscale intensity
- rebuilds the secret image pixel by pixel

The extracted image is written as grayscale by repeating the same value in R, G, and B.

## Capacity rules

A cover image must be large enough to hold:

- 8 pixels for metadata
- 1 pixel for every secret pixel

So the cover image must contain at least:

`secretWidth * secretHeight + 8`

pixels in total.

If not, the code throws an `IllegalArgumentException`. This check is performed both in the codec layer and in the algorithm implementation. 

## Concurrency model

The directory-level operations are implemented with a producer-consumer design:

- the main codec class scans directories and creates tasks
- tasks are pushed into a bounded blocking queue
- a fixed thread pool consumes those tasks
- poison-pill tasks are used to stop the workers cleanly

There are separate task records and worker classes for embedding and extraction. Queue capacity is 5 for both pipelines.

## Project structure

```text
src/
└── bg/sofia/uni/fmi/mjt/steganography
    ├── Demo.java
    ├── ImageCodec.java
    ├── ImageCodecImpl.java
    ├── algorithm
    │   ├── SteganoImpl.java
    │   └── SteganographyAlgorithm.java
    ├── misc
    │   ├── EmbedTask.java
    │   └── ExtractTask.java
    └── worker
        ├── EmbedConsumer.java
        └── ExtractConsumer.java
