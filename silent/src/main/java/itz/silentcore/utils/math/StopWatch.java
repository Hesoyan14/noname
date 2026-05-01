package itz.silentcore.utils.math;

public class StopWatch {
    private long startTime;

    public StopWatch() {
        reset();
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    public boolean finished(long delay) {
        return System.currentTimeMillis() - startTime >= delay;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
