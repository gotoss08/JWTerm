package com.jwterm.utils;

public class Timer {

    private long startTime;
    private long elapsedTime;

    public void start() {
        startTime = System.nanoTime();
    }

    public long stop() {
        elapsedTime = System.nanoTime() - startTime;
        return elapsedTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public double getElapsedTimeMs() {
        return elapsedTime / 1_000_000.0f;
    }

}
