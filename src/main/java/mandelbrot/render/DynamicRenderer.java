package mandelbrot.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import mandelbrot.core.Mandelbrot;
import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;

public final class DynamicRenderer implements Renderer {

    @Override
    public int[] render(RenderParameters params, int threads, int blockHeight) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be positive: " + threads);
        }
        if (blockHeight <= 0) {
            throw new IllegalArgumentException("blockHeight must be positive: " + blockHeight);
        }

        int height = params.height();
        int[] pixels = new int[params.width() * height];
        int blockCount = 1 + (height - 1) / blockHeight;
        int workerCount = Math.min(threads, blockCount);
        AtomicInteger nextBlock = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        try {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int worker = 0; worker < workerCount; worker++) {
                tasks.add(() -> {
                    renderBlocks(pixels, params, blockHeight, blockCount, nextBlock);
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
            pool.close();
        }
        return pixels;
    }

    private static void renderBlocks(int[] pixels, RenderParameters params, int blockHeight,
                                     int blockCount, AtomicInteger nextBlock) {
        int width = params.width();
        int block;
        while (!Thread.currentThread().isInterrupted() && (block = nextBlock.getAndIncrement()) < blockCount) {
            int fromRow = block * blockHeight;
            int toRow = fromRow + Math.min(blockHeight, params.height() - fromRow);
            for (int y = fromRow; y < toRow; y++) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = Mandelbrot.pixelColor(x, y, params);
                }
            }
        }
    }
}
