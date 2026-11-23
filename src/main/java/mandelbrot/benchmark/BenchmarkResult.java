package mandelbrot.benchmark;

import mandelbrot.core.RenderParameters;

record BenchmarkResult(String algorithm, int width, int height, int maxIterations,
                       int threads, int blockHeight, long timeNanos, double speedup, double efficiency) {

    static BenchmarkResult fromMeasurement(String algorithm, RenderParameters params, int threads,
                                           int blockHeight, long timeNanos, long sequentialTimeNanos) {
        if (timeNanos <= 0 || sequentialTimeNanos <= 0) {
            throw new IllegalArgumentException("Measured times must be positive");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be positive: " + threads);
        }
        double speedup = (double) sequentialTimeNanos / timeNanos;
        return new BenchmarkResult(algorithm, params.width(), params.height(), params.maxIterations(),
                threads, blockHeight, timeNanos, speedup, speedup / threads);
    }
}
