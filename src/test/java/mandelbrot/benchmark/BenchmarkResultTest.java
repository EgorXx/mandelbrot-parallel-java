package mandelbrot.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import mandelbrot.core.RenderParameters;

class BenchmarkResultTest {

    private final RenderParameters params = new RenderParameters(10, 20, -2.5, 1.0, -1.25, 1.25, 100);

    @Test
    void fromMeasurement_parallelRenderer_calculatesSpeedupAndEfficiency() {
        BenchmarkResult result = BenchmarkResult.fromMeasurement("dynamic", params, 4, 3, 20, 60);
        assertEquals(3.0, result.speedup());
        assertEquals(0.75, result.efficiency());
        assertEquals(10, result.width());
        assertEquals(20, result.height());
        assertEquals(100, result.maxIterations());
    }

    @Test
    void fromMeasurement_sequentialRenderer_returnsUnitSpeedupAndEfficiency() {
        BenchmarkResult result = BenchmarkResult.fromMeasurement("sequential", params, 1, 0, 60, 60);
        assertEquals(1.0, result.speedup());
        assertEquals(1.0, result.efficiency());
    }

    @Test
    void fromMeasurement_nonPositiveTimeOrThreads_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> BenchmarkResult.fromMeasurement("static", params, 4, 0, 0, 60));
        assertThrows(IllegalArgumentException.class,
                () -> BenchmarkResult.fromMeasurement("static", params, 4, 0, 20, -1));
        assertThrows(IllegalArgumentException.class,
                () -> BenchmarkResult.fromMeasurement("static", params, 0, 0, 20, 60));
    }
}
