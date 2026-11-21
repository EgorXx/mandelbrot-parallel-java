package mandelbrot.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import mandelbrot.core.RenderParameters;

class DynamicRendererTest {

    private final SequentialRenderer sequentialRenderer = new SequentialRenderer();
    private final DynamicRenderer dynamicRenderer = new DynamicRenderer();

    @ParameterizedTest
    @CsvSource({
            "19, 12, 3, 4, 100",
            "19, 13, 3, 4, 100",
            "19, 13, 1, 4, 100",
            "19, 7, 16, 3, 100",
            "1, 1, 8, 1, 100",
            "19, 1, 4, 3, 100",
            "1, 13, 3, 4, 100",
            "19, 13, 4, 1, 100",
            "19, 13, 4, 13, 100",
            "19, 13, 4, 20, 100",
            "19, 13, 4, 2147483647, 100",
            "19, 13, 2147483647, 4, 100",
            "19, 13, 3, 4, 1",
            "19, 13, 3, 4, 500"
    })
    void render_validParameters_matchesSequential(int width, int height, int threads,
                                                 int blockHeight, int maxIterations) {
        RenderParameters params = params(width, height, maxIterations);
        assertArrayEquals(
                sequentialRenderer.render(params, 1, 1),
                dynamicRenderer.render(params, threads, blockHeight));
    }

    @Test
    void render_repeatedCalls_matchesSequential() {
        RenderParameters params = params(61, 47, 200);
        int[] expected = sequentialRenderer.render(params, 1, 1);
        for (int iteration = 0; iteration < 20; iteration++) {
            assertArrayEquals(expected, dynamicRenderer.render(params, 4, 3));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void render_nonPositiveThreads_throwsIllegalArgumentException(int threads) {
        RenderParameters params = params(9, 7, 100);
        assertThrows(IllegalArgumentException.class, () -> dynamicRenderer.render(params, threads, 2));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void render_nonPositiveBlockHeight_throwsIllegalArgumentException(int blockHeight) {
        RenderParameters params = params(9, 7, 100);
        assertThrows(IllegalArgumentException.class, () -> dynamicRenderer.render(params, 3, blockHeight));
    }

    @Test
    void render_interruptedCaller_throwsAndPreservesInterruptFlag() {
        RenderParameters params = params(19, 13, 100);
        Thread.currentThread().interrupt();
        try {
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class, () -> dynamicRenderer.render(params, 3, 4));
            assertInstanceOf(InterruptedException.class, exception.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private static RenderParameters params(int width, int height, int maxIterations) {
        return new RenderParameters(width, height, -2.5, 1.0, -1.25, 1.25, maxIterations);
    }
}
