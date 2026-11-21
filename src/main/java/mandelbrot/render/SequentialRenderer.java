package mandelbrot.render;

import mandelbrot.core.Mandelbrot;
import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;

public final class SequentialRenderer implements Renderer {

    @Override
    public int[] render(RenderParameters params, int threads, int blockHeight) {
        int width = params.width();
        int[] pixels = new int[width * params.height()];
        for (int y = 0; y < params.height(); y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = Mandelbrot.pixelColor(x, y, params);
            }
        }
        return pixels;
    }
}
