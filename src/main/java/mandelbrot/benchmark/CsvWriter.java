package mandelbrot.benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class CsvWriter implements AutoCloseable {

    private final BufferedWriter writer;

    CsvWriter(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        writer.write("algorithm,width,height,max_iterations,threads,block_height,time_ms,speedup,efficiency\n");
    }

    void write(BenchmarkResult result) throws IOException {
        writer.write(String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%.6f,%.6f,%.6f\n",
                result.algorithm(), result.width(), result.height(), result.maxIterations(),
                result.threads(), result.blockHeight(), result.timeNanos() / 1_000_000.0,
                result.speedup(), result.efficiency()));
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
