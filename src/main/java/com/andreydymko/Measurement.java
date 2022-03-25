package com.andreydymko;

public class Measurement {
    private int threadId;
    private int allocations;
    private double allocatedTotal;
    private long millis;

    public Measurement(int threadId, int allocations, double allocatedTotal, long startTime) {
        this.threadId = threadId;
        this.allocations = allocations;
        this.allocatedTotal = allocatedTotal;
        this.millis = System.nanoTime() - startTime;
    }

    public int getThreadId() {
        return threadId;
    }

    public int getAllocations() {
        return allocations;
    }

    public double getAllocatedTotal() {
        return allocatedTotal;
    }

    public long getMillis() {
        return millis;
    }
}
