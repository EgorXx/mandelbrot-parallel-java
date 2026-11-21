package mandelbrot.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import mandelbrot.core.RenderParameters;

class StaticRendererTest {

    private final SequentialRenderer sequentialRenderer = new SequentialRenderer();
    private final StaticRenderer staticRenderer = new StaticRenderer();

    @Test
    void render_matchesSequential_whenHeightNotDivisibleByThreads() {
        RenderParameters params = params(9, 7);
        assertArrayEquals(
                sequentialRenderer.render(params, 1, 1),
                staticRenderer.render(params, 3, 1));
    }

    @Test
    void render_matchesSequential_whenThreadsExceedRows() {
        RenderParameters params = params(9, 3);
        assertArrayEquals(
                sequentialRenderer.render(params, 1, 1),
                staticRenderer.render(params, 16, 1));
    }

    @Test
    void render_matchesSequential_withSingleThread() {
        RenderParameters params = params(9, 7);
        assertArrayEquals(
                sequentialRenderer.render(params, 1, 1),
                staticRenderer.render(params, 1, 1));
    }

    @Test
    void render_nonPositiveThreads_throwsIllegalArgumentException() {
        RenderParameters params = params(9, 7);
        assertThrows(IllegalArgumentException.class, () -> staticRenderer.render(params, 0, 1));
    }

    private static RenderParameters params(int width, int height) {
        return new RenderParameters(width, height, -2.5, 1.0, -1.25, 1.25, 100);
    }
}
