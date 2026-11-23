package mandelbrot.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BenchmarkConfigurationTest {

    @Test
    void fromArguments_noArguments_usesExperimentDefaults() {
        BenchmarkConfiguration configuration = BenchmarkConfiguration.fromArguments(new String[0]);
        assertEquals(List.of(1000, 2000, 4000), configuration.sizes());
        assertEquals(List.of(200, 500, 1000), configuration.iterationLimits());
        assertEquals(List.of(1, 2, 4, 8, 16), configuration.threadCounts());
        assertEquals(List.of(8, 32, 128), configuration.blockHeights());
        assertEquals(Path.of("results"), configuration.outputDirectory());
        assertEquals(1000, configuration.referenceSize());
        assertEquals(500, configuration.referenceIterations());
        assertEquals(16, configuration.referenceThreads());
        assertEquals(32, configuration.referenceBlockHeight());
    }

    @Test
    void fromArguments_customArguments_sortsAndDeduplicatesValues() {
        BenchmarkConfiguration configuration = BenchmarkConfiguration.fromArguments(new String[]{
                "--sizes=25, 10,25", "--iterations=50", "--threads=4,1", "--block-heights=3",
                "--output=build/benchmark output"
        });
        assertEquals(List.of(10, 25), configuration.sizes());
        assertEquals(List.of(50), configuration.iterationLimits());
        assertEquals(List.of(1, 4), configuration.threadCounts());
        assertEquals(List.of(3), configuration.blockHeights());
        assertEquals(Path.of("build/benchmark output"), configuration.outputDirectory());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "--unknown=1", "--sizes", "sizes=10", "--sizes=", "--sizes=0", "--sizes=-1",
            "--sizes=50000", "--sizes=10,", "--sizes=abc", "--iterations=0", "--threads=-2",
            "--block-heights=0", "--output= "
    })
    void fromArguments_invalidArgument_throwsIllegalArgumentException(String argument) {
        assertThrows(IllegalArgumentException.class,
                () -> BenchmarkConfiguration.fromArguments(new String[]{argument}));
    }

    @Test
    void fromArguments_duplicateOption_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> BenchmarkConfiguration.fromArguments(new String[]{"--sizes=10", "--sizes=20"}));
    }
}
