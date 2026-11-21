package mandelbrot.core;

public record RenderParameters(
        int width,
        int height,
        double xmin,
        double xmax,
        double ymin,
        double ymax,
        int maxIterations
) {
    public RenderParameters {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive: " + height);
        }
        if (!(xmin < xmax)) {
            throw new IllegalArgumentException("xmin must be less than xmax: " + xmin + " >= " + xmax);
        }
        if (!(ymin < ymax)) {
            throw new IllegalArgumentException("ymin must be less than ymax: " + ymin + " >= " + ymax);
        }
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive: " + maxIterations);
        }
    }
}
