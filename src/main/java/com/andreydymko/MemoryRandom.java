package com.andreydymko;

import java.util.Random;

public class MemoryRandom extends Random {
    public MemoryRandom(int seed) {
        super(seed);
    }

    @Override
    public double nextDouble() {
        return realisticMemoryCurve(super.nextDouble());
    }

    private static double realisticMemoryCurve(double x) {
        if (x >= 1.0) {
            return 1.0;
        }
        if (x <= 0) {
            return 0;
        }
        if (x >= 0.9995) {
            return x*10.0;
        }
        if (x > 0.9951) {
            return 150*(x - 0.995);
            //return Math.pow(x, 512) + 0.008;
        } else {
            return x*0.009;
        }
    }
}
