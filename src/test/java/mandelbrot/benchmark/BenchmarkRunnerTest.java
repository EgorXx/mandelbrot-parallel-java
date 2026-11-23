package mandelbrot.benchmark;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BenchmarkRunnerTest {

    @Test
    void median_unsortedMeasurements_returnsMiddleValueWithoutChangingInput() {
        long[] values = {70, 10, 90, 30, 50};
        assertEquals(50, BenchmarkRunner.median(values));
        assertArrayEquals(new long[]{70, 10, 90, 30, 50}, values);
    }

    @Test
    void median_singleMeasurement_returnsValue() {
        assertEquals(12, BenchmarkRunner.median(new long[]{12}));
    }

    @Test
    void median_emptyOrEvenMeasurementCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> BenchmarkRunner.median(new long[0]));
        assertThrows(IllegalArgumentException.class, () -> BenchmarkRunner.median(new long[]{10, 20}));
    }
}
