package mandelbrot.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import mandelbrot.core.RenderParameters;
import mandelbrot.render.SequentialRenderer;

class MainTest {

    private static final int WIDTH = 8;
    private static final int HEIGHT = 6;
    private static final double XMIN = -2.0;
    private static final double XMAX = 0.5;
    private static final double YMIN = -1.0;
    private static final double YMAX = 1.0;
    private static final int MAX_ITERATIONS = 50;
    private static final int DEFAULT_MAX_ITERATIONS = 500;
    private static final int ALPHA_MASK = 0xFF000000;

    @TempDir
    Path tempDirectory;

    @ParameterizedTest
    @ValueSource(strings = {"sequential", "static", "dynamic", "hybrid"})
    void run_allModes_writesPixelsMatchingSequentialRenderer(String mode) throws IOException {
        Path output = tempDirectory.resolve(mode + ".png");

        Main.run(new String[]{
                "--mode", mode,
                "--width", String.valueOf(WIDTH),
                "--height", String.valueOf(HEIGHT),
                "--xmin", String.valueOf(XMIN),
                "--xmax", String.valueOf(XMAX),
                "--ymin", String.valueOf(YMIN),
                "--ymax", String.valueOf(YMAX),
                "--max-iterations", String.valueOf(MAX_ITERATIONS),
                "--threads", "3",
                "--block-height", "4",
                "--output", output.toString()
        });

        RenderParameters params = new RenderParameters(
                WIDTH, HEIGHT, XMIN, XMAX, YMIN, YMAX, MAX_ITERATIONS);
        assertArrayEquals(expectedPixels(params), readPixels(output));
    }

    @Test
    void run_omittedOptionalOptions_usesSequentialDefaults() throws IOException {
        Path output = tempDirectory.resolve("defaults.png");

        Main.run(new String[]{
                "--width", String.valueOf(WIDTH),
                "--height", String.valueOf(HEIGHT),
                "--output", output.toString()
        });

        RenderParameters params = new RenderParameters(
                WIDTH, HEIGHT, -2.5, 1.0, -1.25, 1.25, DEFAULT_MAX_ITERATIONS);
        assertArrayEquals(expectedPixels(params), readPixels(output));
    }

    @Test
    void run_unknownMode_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--mode", "fractal"}));
        assertTrue(exception.getMessage().contains("fractal"));
    }

    @Test
    void run_unknownOption_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--dpi", "72"}));
        assertTrue(exception.getMessage().contains("--dpi"));
    }

    @Test
    void run_missingOptionValue_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--width"}));
        assertTrue(exception.getMessage().contains("--width"));
    }

    @Test
    void run_invalidIntegerValue_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--width", "wide"}));
        assertTrue(exception.getMessage().contains("--width"));
        assertTrue(exception.getMessage().contains("wide"));
    }

    @Test
    void run_invalidDoubleValue_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--xmin", "left"}));
        assertTrue(exception.getMessage().contains("--xmin"));
        assertTrue(exception.getMessage().contains("left"));
    }

    @Test
    void run_invalidView_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--xmin", "1.0", "--xmax", "-1.0"}));
        assertTrue(exception.getMessage().contains("xmin"));
    }

    @Test
    void run_nonPositiveThreadsForStaticMode_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[]{"--mode", "static", "--threads", "0"}));
        assertTrue(exception.getMessage().contains("threads"));
    }

    private static int[] readPixels(Path output) throws IOException {
        BufferedImage image = ImageIO.read(output.toFile());
        assertNotNull(image);
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        for (int index = 0; index < pixels.length; index++) {
            pixels[index] |= ALPHA_MASK;
        }
        return pixels;
    }

    private static int[] expectedPixels(RenderParameters params) {
        int[] sequentialPixels = new SequentialRenderer().render(params, 1, 1);
        int[] expected = new int[sequentialPixels.length];
        for (int index = 0; index < sequentialPixels.length; index++) {
            expected[index] = sequentialPixels[index] | ALPHA_MASK;
        }
        return expected;
    }
}
