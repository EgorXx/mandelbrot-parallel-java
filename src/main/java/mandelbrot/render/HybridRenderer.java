package mandelbrot.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mandelbrot.core.Mandelbrot;
import mandelbrot.core.RenderParameters;
import mandelbrot.core.Renderer;

public final class HybridRenderer implements Renderer {

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

        List<ConcurrentLinkedDeque<Integer>> localQueues = createLocalQueues(blockCount, workerCount);

        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        try {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int worker = 0; worker < workerCount; worker++) {
                int workerIndex = worker;
                tasks.add(() -> {
                    renderBlocks(pixels, params, blockHeight, localQueues, workerIndex);
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

    private static List<ConcurrentLinkedDeque<Integer>> createLocalQueues(int blockCount, int workerCount) {
        List<ConcurrentLinkedDeque<Integer>> localQueues = new ArrayList<>(workerCount);
        for (int worker = 0; worker < workerCount; worker++) {
            localQueues.add(new ConcurrentLinkedDeque<>());
        }
        for (int block = 0; block < blockCount; block++) {
            localQueues.get(block % workerCount).addLast(block);
        }
        return localQueues;
    }

    private static void renderBlocks(int[] pixels, RenderParameters params, int blockHeight,
                                     List<ConcurrentLinkedDeque<Integer>> localQueues, int workerIndex) {
        int block;
        while (!Thread.currentThread().isInterrupted() && (block = takeBlock(localQueues, workerIndex)) >= 0) {
            renderBlock(pixels, params, blockHeight, block);
        }
    }

    private static int takeBlock(List<ConcurrentLinkedDeque<Integer>> localQueues, int workerIndex) {
        Integer block = localQueues.get(workerIndex).pollFirst();
        if (block != null) {
            return block;
        }
        for (int offset = 1; offset < localQueues.size(); offset++) {
            int victimIndex = (workerIndex + offset) % localQueues.size();
            block = localQueues.get(victimIndex).pollLast();
            if (block != null) {
                return block;
            }
        }
        return -1;
    }

    private static void renderBlock(int[] pixels, RenderParameters params, int blockHeight, int block) {
        int width = params.width();
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
