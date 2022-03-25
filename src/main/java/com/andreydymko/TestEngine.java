package com.andreydymko;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestEngine {
    private static final String SINGLE_VARYING_FILENAME = "SingleCoreVaryingSize.csv";
    private static final String MULTIPLE_VARYING_FILENAME = "MultipleCoresVaryingSize.csv";
    private String resDirectory;

    private double oldGenSize;
    private double mbToAlloc;
    private double step;
    private boolean isPrintEnabled;

    private Random localRandom;
    private CsvWriter csvWriter = new CsvWriter();
    private List<Double> randomSizes = new ArrayList<>();

    public TestEngine(String resDirectory, double mbToAlloc, double step, boolean isPrintEnabled, Random localRandom) {
        this.resDirectory = resDirectory;
        this.mbToAlloc = mbToAlloc;
        this.step = step;
        this.isPrintEnabled = isPrintEnabled;
        this.localRandom = localRandom;

        // old gen size in MB. One fourth of a total available memory (by arg "-Xmx")
        oldGenSize = Runtime.getRuntime().maxMemory()/(1024.0 * 1024.0 * 4.0);
        generateRandomSizes();
    }

    public TestEngine(double mbToAlloc, double step, boolean isPrintEnabled) {
        this("", mbToAlloc, step, isPrintEnabled, new Random());
    }

    public TestEngine(double mbToAlloc, double step, boolean isPrintEnabled, Random localRandom) {
        this("", mbToAlloc, step, isPrintEnabled, localRandom);
    }

    private void generateRandomSizes() {
        for (int i = 0; i < 10000; i++) {
            randomSizes.add(localRandom.nextDouble());
        }
    }

    private double getAvgOfRandom( ) {
        return randomSizes.stream()
                .mapToDouble(x -> x)
                .average()
                .orElse(0.0);
    }

    public void initTests() {
        final int sizesListSize = randomSizes.size();
        // 1.5 is for reserve
        final int oldGenSizeInElems = (int) ((oldGenSize / getAvgOfRandom()) * 1.5);
        List<MemoryEater> oldGen = new ArrayList<>(oldGenSizeInElems);

        double nextMeasure = mbToAlloc - step;
        double oldGenCounter = 0;
        double nextOldGenDrop = mbToAlloc - 4 * oldGenSize;
        double toAllocLeft = mbToAlloc;
        double chunkSize;
        int i;
        ConcurrentLinkedQueue<Measurement> measurements = new ConcurrentLinkedQueue<>();

        if (isPrintEnabled) {
            System.out.printf("Starting GarbageCollector benchmark " +
                    "with %f MB total allocations (%f MB step) in single-core mode...%n", mbToAlloc, step);
        }
        long benchmarkStart = System.nanoTime();

        measurements.offer(new Measurement(0, 0, mbToAlloc - toAllocLeft, benchmarkStart));
        MemoryEater memoryEater;
        for (i = 1; toAllocLeft > 0; i++) {
            chunkSize = randomSizes.get((i - 1) % sizesListSize);
            memoryEater = MemoryEater.alloc(chunkSize);
            toAllocLeft -= chunkSize;
            if (toAllocLeft <= nextMeasure) {
                measurements.offer(new Measurement(0, i, mbToAlloc - toAllocLeft, benchmarkStart));
                nextMeasure -= step;
            }
            if (toAllocLeft <= nextOldGenDrop) {
                oldGenCounter = 0;
                nextOldGenDrop = toAllocLeft - 4 * oldGenSize;
                oldGen.clear();
            } else {
                if (oldGenCounter < oldGenSize) {
                    oldGen.add(memoryEater);
                    oldGenCounter += chunkSize;
                }
            }
        }
        measurements.offer(new Measurement(0, i, mbToAlloc - toAllocLeft, benchmarkStart));

        // jvm may optimize arraylist, so we need this to avoid optimizations
        oldGen.forEach(x -> x = null);

        csvWriter.write(resDirectory, SINGLE_VARYING_FILENAME, measurements);
    }

    public void initTestsParallel() {
        final int threadNum = Runtime.getRuntime().availableProcessors();
        final double mbPerThread = mbToAlloc / threadNum;
        final int sizesListSize = randomSizes.size();

        final double oldGenSizeInMbForEachThread = oldGenSize / threadNum;
        // 1.5 is for reserve
        final int oldGenSizeInElems = (int) (((oldGenSizeInMbForEachThread / getAvgOfRandom()) * 1.5) / threadNum);

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        ConcurrentLinkedQueue<Measurement> measurements = new ConcurrentLinkedQueue<>();

        if (isPrintEnabled) {
            System.out.printf("Starting GarbageCollector benchmark " +
                    "with %f MB total allocations (%f MB step) in parallel mode...%n", mbToAlloc, step);
        }

        for (int threadId = 0; threadId < threadNum; threadId++) {
            final int finalThreadId = threadId;
            executor.submit(() -> {
                double nextMeasure = mbPerThread - step;
                double toAllocLeft = mbPerThread;
                double oldGenCounter = 0;
                double nextOldGenDrop = mbPerThread - 4 * oldGenSizeInMbForEachThread;
                double chunkSize;
                int i;

                List<MemoryEater> oldGen = new ArrayList<>(oldGenSizeInElems);

                final long benchmarkStart = System.nanoTime();
                measurements.offer(new Measurement(finalThreadId, 0, mbPerThread - toAllocLeft, benchmarkStart));
                MemoryEater memoryEater;
                for (i = 1; toAllocLeft > 0; i++) {
                    chunkSize = randomSizes.get((i - 1) % sizesListSize);
                    memoryEater = MemoryEater.alloc(chunkSize);
                    toAllocLeft -= chunkSize;
                    if (toAllocLeft <= nextMeasure) {
                        measurements.offer(new Measurement(finalThreadId, i, mbPerThread - toAllocLeft, benchmarkStart));
                        nextMeasure -= step;
                    }
                    if (toAllocLeft <= nextOldGenDrop) {
                        oldGenCounter = 0;
                        nextOldGenDrop = toAllocLeft - 4 * oldGenSizeInMbForEachThread;
                        oldGen.clear();
                    } else {
                        if (oldGenCounter < oldGenSizeInMbForEachThread) {
                            oldGen.add(memoryEater);
                            oldGenCounter += chunkSize;
                        }
                    }
                }
                measurements.offer(new Measurement(finalThreadId, i, mbPerThread - toAllocLeft, benchmarkStart));

                // jvm may optimize arraylist, so we need this to avoid optimizations
                oldGen.forEach(x -> x = null);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        csvWriter.write(resDirectory, MULTIPLE_VARYING_FILENAME, measurements);
    }
}
