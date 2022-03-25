package com.andreydymko;

public class MemoryEater {
    public int[][] arrays;
    public int[] remainings;

    public MemoryEater(long size) {
        if (size == 0) {
            return;
        }
        int toAllocate = ((int) (size / Integer.MAX_VALUE)) + 1;
        if (toAllocate == 1) {
            remainings = new int[(int) size];
        } else {
            arrays = new int[toAllocate][Integer.MAX_VALUE];
            int i = 0;
            int remainingSize = (int) (size % Integer.MAX_VALUE);
            for (long remaining = size - remainingSize; remaining > 0 ; remaining -= Integer.MAX_VALUE, i++) {
                arrays[i] = new int[Integer.MAX_VALUE];
            }
            remainings = new int[remainingSize];
        }
    }

    public static MemoryEater alloc(double sizeInMb) {
        long sizeInInts = (long) (sizeInMb * 1024.0 * 1024.0 / 4.0);
        return new MemoryEater(sizeInInts);
    }
}
