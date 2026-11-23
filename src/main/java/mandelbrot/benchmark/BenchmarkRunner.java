package mandelbrot.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;
import mandelbrot.render.DynamicRenderer;
import mandelbrot.render.HybridRenderer;
import mandelbrot.render.SequentialRenderer;
import mandelbrot.render.StaticRenderer;

public final class BenchmarkRunner {

    private static final int WARMUP_RUNS = 2;
    private static final int MEASUREMENT_RUNS = 5;
    private static volatile int pixelChecksum;

    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws IOException {
        BenchmarkConfiguration configuration = BenchmarkConfiguration.fromArguments(args);
        Files.createDirectories(configuration.outputDirectory());
        try (CsvWriter writer = new CsvWriter(configuration.outputDirectory().resolve("benchmarks.csv"))) {
            for (int size : configuration.sizes()) {
                for (int maxIterations : configuration.iterationLimits()) {
                    benchmarkScene(configuration, writer, size, maxIterations);
                }
            }
        }
        System.out.println("Results: " + configuration.outputDirectory().toAbsolutePath());
    }

    private static void benchmarkScene(BenchmarkConfiguration configuration, CsvWriter writer,
                                       int size, int maxIterations) throws IOException {
        RenderParameters params = new RenderParameters(size, size, -2.5, 1.0, -1.25, 1.25, maxIterations);
        long sequentialTime = measure(new SequentialRenderer(), params, 1, 1);
        writeResult(writer, "sequential", params, 1, 0, sequentialTime, sequentialTime);
        boolean referenceScene = size == configuration.referenceSize()
                && maxIterations == configuration.referenceIterations();
        List<Integer> threadCounts = referenceScene
                ? configuration.threadCounts() : List.of(configuration.referenceThreads());
        List<Integer> blockHeights = referenceScene
                ? configuration.blockHeights() : List.of(configuration.referenceBlockHeight());
        for (int threads : threadCounts) {
            long staticTime = measure(new StaticRenderer(), params, threads, 1);
            writeResult(writer, "static", params, threads, 0, staticTime, sequentialTime);
            for (int blockHeight : blockHeights) {
                long dynamicTime = measure(new DynamicRenderer(), params, threads, blockHeight);
                writeResult(writer, "dynamic", params, threads, blockHeight, dynamicTime, sequentialTime);
                long hybridTime = measure(new HybridRenderer(), params, threads, blockHeight);
                writeResult(writer, "hybrid", params, threads, blockHeight, hybridTime, sequentialTime);
            }
        }
    }

    private static long measure(Renderer renderer, RenderParameters params, int threads, int blockHeight) {
        for (int run = 0; run < WARMUP_RUNS; run++) {
            consume(renderer.render(params, threads, blockHeight));
        }
        long[] times = new long[MEASUREMENT_RUNS];
        for (int run = 0; run < times.length; run++) {
            long start = System.nanoTime();
            int[] pixels = renderer.render(params, threads, blockHeight);
            times[run] = System.nanoTime() - start;
            consume(pixels);
        }
        return median(times);
    }

    static long median(long[] values) {
        if (values.length == 0 || values.length % 2 == 0) {
            throw new IllegalArgumentException("Expected a non-empty odd number of measurements");
        }
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    private static void consume(int[] pixels) {
        pixelChecksum = Arrays.hashCode(pixels);
    }

    private static void writeResult(CsvWriter writer, String algorithm, RenderParameters params,
                                     int threads, int blockHeight, long timeNanos,
                                     long sequentialTime) throws IOException {
        BenchmarkResult result = BenchmarkResult.fromMeasurement(
                algorithm, params, threads, blockHeight, timeNanos, sequentialTime);
        writer.write(result);
        System.out.printf(Locale.ROOT, "%s %dx%d iterations=%d threads=%d block=%d: %.3f ms%n",
                algorithm, params.width(), params.height(), params.maxIterations(), threads,
                blockHeight, timeNanos / 1_000_000.0);
    }

}
