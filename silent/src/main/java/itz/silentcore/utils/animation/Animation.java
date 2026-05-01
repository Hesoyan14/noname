package itz.silentcore.utils.animation;

import lombok.Setter;

public class Animation {
    private float value;
    @Setter
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
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;

        if (elapsed >= duration) {
            value = target;
            return value;
        }

        float t = (float) elapsed;
        float b = startValue;
        float c = target - startValue;
        float d = (float) duration;

        value = easing.ease(t, b, c, d);
        return value;
    }

    public boolean isFinished() {
        return Math.abs(value - target) < 0.01f;
    }

    public void update() {
        getValue();
    }

    public void animate(float to) {
        setTarget(to);
    }

    public void reset(float value) {
        this.value = value;
        this.startValue = value;
        this.target = value;
        this.startTime = System.currentTimeMillis();
    }
}
