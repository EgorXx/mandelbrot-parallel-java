package mandelbrot.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvWriterTest {

    @TempDir
    Path directory;

    @Test
    void write_result_createsDirectoriesAndWritesCsvWithDecimalPoints() throws IOException {
        Path path = directory.resolve("nested/benchmarks.csv");
        Locale previousLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
        try (CsvWriter writer = new CsvWriter(path)) {
            writer.write(new BenchmarkResult("dynamic", 10, 20, 100, 4, 3, 1_500_000, 2.5, 0.625));
            assertEquals(2, Files.readAllLines(path, StandardCharsets.UTF_8).size());
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previousLocale);
        }
        assertEquals(List.of(
                "algorithm,width,height,max_iterations,threads,block_height,time_ms,speedup,efficiency",
                "dynamic,10,20,100,4,3,1.500000,2.500000,0.625000"
        ), Files.readAllLines(path, StandardCharsets.UTF_8));
        String content = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(content.endsWith("\n"));
        assertFalse(content.contains("\r"));
    }

    @Test
    void constructor_existingFile_replacesPreviousResults() throws IOException {
        Path path = directory.resolve("benchmarks.csv");
        Files.writeString(path, "old results\n", StandardCharsets.UTF_8);
        try (CsvWriter writer = new CsvWriter(path)) {
            writer.write(new BenchmarkResult("sequential", 10, 20, 100, 1, 0, 1_000_000, 1.0, 1.0));
        }
        assertFalse(Files.readString(path, StandardCharsets.UTF_8).contains("old results"));
    }
}
