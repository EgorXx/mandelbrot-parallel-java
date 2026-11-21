package mandelbrot.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import mandelbrot.core.Mandelbrot;
import mandelbrot.core.RenderParameters;

class SequentialRendererTest {

    private final SequentialRenderer renderer = new SequentialRenderer();

    @Test
    void render_returnsOnePixelPerImagePoint() {
        RenderParameters params = params(8, 7);
        int[] pixels = renderer.render(params, 4, 2);
        assertEquals(params.width() * params.height(), pixels.length);
    }

    @Test
    void render_matchesDirectPixelComputation() {
        RenderParameters params = params(8, 7);
        int[] expected = new int[params.width() * params.height()];
        for (int y = 0; y < params.height(); y++) {
            for (int x = 0; x < params.width(); x++) {
                expected[y * params.width() + x] = Mandelbrot.pixelColor(x, y, params);
            }
        }
        assertArrayEquals(expected, renderer.render(params, 4, 2));
    }

    @Test
    void render_windowInsideSet_isAllBlack() {
        RenderParameters params = new RenderParameters(8, 7, -0.1, 0.1, -0.1, 0.1, 100);
        for (int color : renderer.render(params, 1, 1)) {
            assertEquals(0, color);
        }
    }

    private static RenderParameters params(int width, int height) {
        return new RenderParameters(width, height, -2.5, 1.0, -1.25, 1.25, 100);
    }
}
