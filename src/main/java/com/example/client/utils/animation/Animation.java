package com.example.client.utils.animation;

public class Animation {
    private float value;
    private float startValue;
    private float target;
    private long startTime;
    private final long duration;
    private final Easing easing;

    public Animation(long duration, Easing easing) {
        this.duration = duration;
        this.easing = easing;
        this.value = 0;
        this.startValue = 0;
        this.target = 0;
        this.startTime = System.currentTimeMillis();
    }

    public void setTarget(float target) {
        if (this.target != target) {
            this.startValue = this.value;
            this.target = target;
            this.startTime = System.currentTimeMillis();
        }
    }

    public float getValue() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= duration) {
            value = target;
            return value;
        }
        value = easing.ease(elapsed, startValue, target - startValue, duration);
        return value;
    }

    public void update() {
        getValue();
    }

    public void animate(float to) {
        setTarget(to);
    }

    public void setStartValue(float v) {
        this.startValue = v;
        this.value = v;
    }

    public void reset(float v) {
        this.value = v;
        this.startValue = v;
        this.target = v;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return Math.abs(value - target) < 0.01f;
    }
}
