package mandelbrot.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mandelbrot.core.Mandelbrot;
import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;

public final class StaticRenderer implements Renderer {

    @Override
    public int[] render(RenderParameters params, int threads, int blockHeight) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be positive: " + threads);
        }

        int width = params.width();
        int height = params.height();
        int[] pixels = new int[width * height];

        int workerCount = Math.min(threads, height);
        int rowsPerThread = (height + workerCount - 1) / workerCount;

        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int worker = 0; worker < workerCount; worker++) {
                int fromRow = worker * rowsPerThread;
                int toRow = Math.min(height, fromRow + rowsPerThread);
                tasks.add(() -> {
                    renderRows(pixels, params, fromRow, toRow);
                    return null;
                });
            }
            for (Future<Void> future : pool.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while rendering", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Rendering failed", e.getCause());
        } finally {
            pool.shutdownNow();
        }
        return pixels;
    }

    private static void renderRows(int[] pixels, RenderParameters params, int fromRow, int toRow) {
        int width = params.width();
        for (int y = fromRow; y < toRow; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = Mandelbrot.pixelColor(x, y, params);
            }
        }
    }
}
