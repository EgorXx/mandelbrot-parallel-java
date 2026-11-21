package mandelbrot.core;

public interface Renderer {

    int[] render(RenderParameters params, int threads, int blockHeight);
}
