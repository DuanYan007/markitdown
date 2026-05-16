package com.markitdown.cli;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Shared sequential and parallel execution helpers for CLI batch conversion flows.
 */
final class BatchConversionRunner {

    private BatchConversionRunner() {
    }

    static BatchResult runSequential(
            List<String> files,
            ProgressListener progressListener,
            FileProcessor processor,
            SuccessListener successListener,
            ErrorListener errorListener
    ) {
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < files.size(); i++) {
            String inputFile = files.get(i);
            progressListener.onProgress(i + 1, files.size(), inputFile);
            try {
                processor.process(inputFile);
                successCount++;
                successListener.onSuccess(inputFile);
            } catch (Exception e) {
                errorCount++;
                errorListener.onError(inputFile, e);
            }
        }

        return new BatchResult(successCount, errorCount);
    }

    static BatchResult runParallel(
            List<String> files,
            int poolSize,
            ProgressListener progressListener,
            FileProcessor processor,
            SuccessListener successListener,
            ErrorListener errorListener
    ) {
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger processed = new AtomicInteger(0);

        try {
            List<CompletableFuture<Void>> futures = files.stream()
                    .map(inputFile -> CompletableFuture.runAsync(() -> {
                        try {
                            int current = processed.incrementAndGet();
                            progressListener.onProgress(current, files.size(), inputFile);
                            processor.process(inputFile);
                            successCount.incrementAndGet();
                            successListener.onSuccess(inputFile);
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            errorListener.onError(inputFile, e);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return new BatchResult(successCount.get(), errorCount.get());
    }

    @FunctionalInterface
    interface FileProcessor {
        void process(String inputFile) throws Exception;
    }

    @FunctionalInterface
    interface SuccessListener {
        void onSuccess(String inputFile);
    }

    @FunctionalInterface
    interface ErrorListener {
        void onError(String inputFile, Exception error);
    }

    @FunctionalInterface
    interface ProgressListener {
        ProgressListener NO_OP = (current, total, inputFile) -> { };

        void onProgress(int current, int total, String inputFile);
    }

    static final class BatchResult {
        private final int successCount;
        private final int errorCount;

        BatchResult(int successCount, int errorCount) {
            this.successCount = successCount;
            this.errorCount = errorCount;
        }

        int successCount() {
            return successCount;
        }

        int errorCount() {
            return errorCount;
        }
    }
}
