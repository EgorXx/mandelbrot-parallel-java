package mandelbrot.benchmark;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

record BenchmarkConfiguration(List<Integer> sizes, List<Integer> iterationLimits,
                              List<Integer> threadCounts, List<Integer> blockHeights,
                              Path outputDirectory) {

    int referenceSize() {
        return sizes.getFirst();
    }

    int referenceIterations() {
        return iterationLimits.get(iterationLimits.size() / 2);
    }

    int referenceThreads() {
        return threadCounts.getLast();
    }

    int referenceBlockHeight() {
        return blockHeights.get(blockHeights.size() / 2);
    }

    static BenchmarkConfiguration fromArguments(String[] args) {
        Set<String> supportedOptions = Set.of("sizes", "iterations", "threads", "block-heights", "output");
        Map<String, String> options = new HashMap<>();
        for (String argument : args) {
            String[] parts = argument.split("=", 2);
            if (parts.length != 2 || !parts[0].startsWith("--") || parts[1].isBlank()) {
                throw new IllegalArgumentException("Expected --option=value: " + argument);
            }
            String option = parts[0].substring(2);
            if (!supportedOptions.contains(option)) {
                throw new IllegalArgumentException("Unknown benchmark option: " + option);
            }
            if (options.putIfAbsent(option, parts[1]) != null) {
                throw new IllegalArgumentException("Duplicate benchmark option: " + option);
            }
        }
        List<Integer> sizes = positiveIntegers(options.getOrDefault("sizes", "1000,2000,4000"));
        for (int size : sizes) {
            if ((long) size * size > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Image is too large for an int array: " + size);
            }
        }
        return new BenchmarkConfiguration(
                sizes,
                positiveIntegers(options.getOrDefault("iterations", "200,500,1000")),
                positiveIntegers(options.getOrDefault("threads", "1,2,4,8,16")),
                positiveIntegers(options.getOrDefault("block-heights", "8,32,128")),
                Path.of(options.getOrDefault("output", "results")));
    }

    private static List<Integer> positiveIntegers(String value) {
        List<Integer> values = Arrays.stream(value.split(",", -1))
                .map(String::trim)
                .map(Integer::parseInt)
                .distinct()
                .sorted()
                .toList();
        if (values.stream().anyMatch(number -> number <= 0)) {
            throw new IllegalArgumentException("Benchmark values must be positive: " + value);
        }
        return values;
    }
}
