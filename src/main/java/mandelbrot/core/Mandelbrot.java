package mandelbrot.core;

public final class Mandelbrot {

    private static final double ESCAPE_RADIUS_SQUARED = 4.0;
    private static final int CHANNEL_MAX = 255;

    private Mandelbrot() {
    }

    public static int pixelColor(int x, int y, RenderParameters params) {
        double cx = params.xmin() + (x + 0.5) * (params.xmax() - params.xmin()) / params.width();
        double cy = params.ymax() - (y + 0.5) * (params.ymax() - params.ymin()) / params.height();

        double zx = 0.0;
        double zy = 0.0;
        int iteration = 0;
        while (iteration < params.maxIterations() && zx * zx + zy * zy <= ESCAPE_RADIUS_SQUARED) {
            double nextZx = zx * zx - zy * zy + cx;
            zy = 2.0 * zx * zy + cy;
            zx = nextZx;
            iteration++;
        }

        if (zx * zx + zy * zy <= ESCAPE_RADIUS_SQUARED) {
            return 0;
        }
        return palette(iteration, params.maxIterations());
    }

    private static int palette(int iteration, int maxIterations) {
        double t = (double) iteration / maxIterations;
        int red = (int) (9.0 * (1 - t) * t * t * t * CHANNEL_MAX);
        int green = (int) (15.0 * (1 - t) * (1 - t) * t * t * CHANNEL_MAX);
        int blue = (int) (8.5 * (1 - t) * (1 - t) * (1 - t) * t * CHANNEL_MAX);
        return (red << 16) | (green << 8) | blue;
    }
}
