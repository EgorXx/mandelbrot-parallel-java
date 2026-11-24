package mandelbrot.cli;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public final class PngWriter {

    private PngWriter() {
    }

    public static void write(int[] pixels, int width, int height, Path output) throws IOException {
        int expectedLength = width * height;
        if (pixels.length != expectedLength) {
            throw new IllegalArgumentException(
                    "pixels length must be " + expectedLength + " but was " + pixels.length);
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        if (!ImageIO.write(image, "png", output.toFile())) {
            throw new IOException("No PNG writer available for " + output);
        }
    }
}
