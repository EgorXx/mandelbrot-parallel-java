package mandelbrot.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import mandelbrot.core.Mandelbrot;
import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;

class RendererEquivalenceTest {

    static Stream<Arguments> allRenderers() {
        return Stream.of(
                Arguments.of(new SequentialRenderer()),
                Arguments.of(new StaticRenderer()),
                Arguments.of(new DynamicRenderer()),
                Arguments.of(new HybridRenderer()));
    }

    @ParameterizedTest
    @MethodSource("allRenderers")
    void render_heightNotDivisibleByThreads_matchesReference(Renderer renderer) {
        RenderParameters params = params(37, 23, 200);
        assertMatchesReference(renderer, params, 4, 3);
    }

    @ParameterizedTest
    @MethodSource("allRenderers")
    void render_lastBlockIncomplete_matchesReference(Renderer renderer) {
        RenderParameters params = params(32, 20, 300);
        assertMatchesReference(renderer, params, 3, 7);
    }

    @ParameterizedTest
    @MethodSource("allRenderers")
    void render_singleThread_matchesReference(Renderer renderer) {
        RenderParameters params = params(31, 17, 150);
        assertMatchesReference(renderer, params, 1, 5);
    }

    @ParameterizedTest
    @MethodSource("allRenderers")
    void render_manyThreads_matchesReference(Renderer renderer) {
        RenderParameters params = params(41, 29, 250);
        assertMatchesReference(renderer, params, 16, 2);
    }

    @ParameterizedTest
    @MethodSource("allRenderers")
    void render_smallImage_matchesReference(Renderer renderer) {
        RenderParameters params = params(3, 2, 50);
        assertMatchesReference(renderer, params, 8, 1);
    }

    @ParameterizedTest
    @MethodSource("allRenderers")
    void render_differentIterationLimits_matchesReference(Renderer renderer) {
        assertMatchesReference(renderer, params(25, 19, 50), 4, 4);
        assertMatchesReference(renderer, params(25, 19, 200), 4, 4);
        assertMatchesReference(renderer, params(25, 19, 800), 4, 4);
    }

    @Test
    void dynamic_repeatedRuns_matchReference() {
        RenderParameters params = params(45, 31, 400);
        DynamicRenderer renderer = new DynamicRenderer();
        int[] expected = referencePixels(params);
        for (int run = 0; run < 10; run++) {
            assertArrayEquals(expected, renderer.render(params, 6, 3));
        }
    }

    @Test
    void hybrid_repeatedRuns_matchReference() {
        RenderParameters params = params(45, 31, 400);
        HybridRenderer renderer = new HybridRenderer();
        int[] expected = referencePixels(params);
        for (int run = 0; run < 10; run++) {
            assertArrayEquals(expected, renderer.render(params, 6, 3));
        }
    }

    private static void assertMatchesReference(Renderer renderer, RenderParameters params,
                                               int threads, int blockHeight) {
        assertArrayEquals(referencePixels(params), renderer.render(params, threads, blockHeight));
    }

    private static int[] referencePixels(RenderParameters params) {
        int width = params.width();
        int[] pixels = new int[width * params.height()];
        for (int y = 0; y < params.height(); y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = Mandelbrot.pixelColor(x, y, params);
            }
        }
        return pixels;
    }

    private static RenderParameters params(int width, int height, int maxIterations) {
        return new RenderParameters(width, height, -2.5, 1.0, -1.25, 1.25, maxIterations);
    }
}
