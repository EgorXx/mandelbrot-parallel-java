package mandelbrot.cli;

import java.io.IOException;
import java.nio.file.Path;

import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;
import mandelbrot.render.DynamicRenderer;
import mandelbrot.render.HybridRenderer;
import mandelbrot.render.SequentialRenderer;
import mandelbrot.render.StaticRenderer;

public final class Main {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final double DEFAULT_XMIN = -2.5;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = -1.25;
    private static final double DEFAULT_YMAX = 1.25;
    private static final int DEFAULT_MAX_ITERATIONS = 500;
    private static final int DEFAULT_BLOCK_HEIGHT = 64;
    private static final String DEFAULT_MODE = "sequential";
    private static final Path DEFAULT_OUTPUT = Path.of("fractal.png");

    private Main() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    static void run(String[] args) throws IOException {
        Options options = Options.parse(args);
        RenderParameters params = options.params();
        Renderer renderer = createRenderer(options.mode());
        int[] pixels = renderer.render(params, options.threads(), options.blockHeight());
        PngWriter.write(pixels, params.width(), params.height(), options.output());
    }

    private static Renderer createRenderer(String mode) {
        return switch (mode) {
            case "sequential" -> new SequentialRenderer();
            case "static" -> new StaticRenderer();
            case "dynamic" -> new DynamicRenderer();
            case "hybrid" -> new HybridRenderer();
            default -> throw new IllegalArgumentException(
                    "Unknown mode: " + mode + ". Expected one of: sequential, static, dynamic, hybrid");
        };
    }

    private record Options(
            RenderParameters params,
            String mode,
            int threads,
            int blockHeight,
            Path output
    ) {
        private static Options parse(String[] args) {
            int width = DEFAULT_WIDTH;
            int height = DEFAULT_HEIGHT;
            double xmin = DEFAULT_XMIN;
            double xmax = DEFAULT_XMAX;
            double ymin = DEFAULT_YMIN;
            double ymax = DEFAULT_YMAX;
            int maxIterations = DEFAULT_MAX_ITERATIONS;
            int threads = Runtime.getRuntime().availableProcessors();
            int blockHeight = DEFAULT_BLOCK_HEIGHT;
            String mode = DEFAULT_MODE;
            Path output = DEFAULT_OUTPUT;

            for (int index = 0; index < args.length; index += 2) {
                String option = args[index];
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for option: " + option);
                }
                String value = args[index + 1];
                switch (option) {
                    case "--mode" -> mode = value;
                    case "--width" -> width = parseInteger(option, value);
                    case "--height" -> height = parseInteger(option, value);
                    case "--xmin" -> xmin = parseDouble(option, value);
                    case "--xmax" -> xmax = parseDouble(option, value);
                    case "--ymin" -> ymin = parseDouble(option, value);
                    case "--ymax" -> ymax = parseDouble(option, value);
                    case "--max-iterations" -> maxIterations = parseInteger(option, value);
                    case "--threads" -> threads = parseInteger(option, value);
                    case "--block-height" -> blockHeight = parseInteger(option, value);
                    case "--output" -> output = Path.of(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + option);
                }
            }

            return new Options(
                    new RenderParameters(width, height, xmin, xmax, ymin, ymax, maxIterations),
                    mode,
                    threads,
                    blockHeight,
                    output
            );
        }

        private static int parseInteger(String option, String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer for " + option + ": " + value, e);
            }
        }

        private static double parseDouble(String option, String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number for " + option + ": " + value, e);
            }
        }
    }
}
