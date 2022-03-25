package com.andreydymko;

public class Main {

    public static void main(String[] args) {
        if (args.length < 4 || 6 < args.length) {
            throw new RuntimeException("This benchmark accepts only 6 arguments: " +
                    "total MB to allocate,\n" +
                    "a step between the measurements (in MB),\n" +
                    "a flag to turn ON/OFF the println, " +
                    "the test type (parallel or normal),\n" +
                    "directory on where to save results (optional),\n" +
                    "and time to pause before closing the application (in sec, optional)\n" +
                    "For example:\n" +
                    "1024 128 true parallel myDir 5");
        }

        double total = Double.parseDouble(args[0]);
        double step = Double.parseDouble(args[1]);
        boolean isPrintEnabled = Boolean.parseBoolean(args[2]);
        String testType = args[3];

        TestEngine testEngine;
        if (args.length >= 5) {
            String dir = args[4];
            testEngine = new TestEngine(dir, total, step, isPrintEnabled, new MemoryRandom(123));
        } else {
            testEngine = new TestEngine(total, step, isPrintEnabled, new MemoryRandom(123));
        }

        switch (testType) {
            case "parallel":
                testEngine.initTestsParallel();
                break;
            case "normal":
                testEngine.initTests();
                break;
            default:
                System.out.printf("Unsupported test type \"%s\", please use one of this: sequential, normal.\n", testType);
                return;
        }

        if (isPrintEnabled) {
            System.out.println("Test is done.");
        }

        if (args.length == 6) {
            long timeToPause = Long.parseLong(args[5]);
            if (isPrintEnabled) {
                System.out.println("Pausing...");
            }
            try {
                Thread.sleep(timeToPause * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isPrintEnabled) {
            System.out.println("Exiting");
        }
    }
}
