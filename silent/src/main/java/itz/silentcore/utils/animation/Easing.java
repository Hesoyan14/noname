package itz.silentcore.utils.animation;

import itz.silentcore.utils.other.MathUtil;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
public interface Easing {

    Easing BAKEK = Easing.generate(0.45f, 1.45f, 0.49f, 1.15f);
    Easing BAKEK_SMALLER = Easing.generate(0.45f, 1.45f, 0.43f, 0.91f);
    Easing BAKEK_PAGES = Easing.generate(0.1f, 1.07f, 0.34f, 1.04f);
    Easing BAKEK_SIZE = Easing.generate(0.27f, 1.09f, 0.49f, 1.06f);
    Easing BAKEK_BACK = Easing.generate(0.62, -0.16, 0.8, 0.37);
    Easing BAKEK_MANY = Easing.generate(0.25, 1.07, 0.11, 1.1);

    Easing FIGMA_EASE_IN_OUT = Easing.generate(0.42, 0, 0.58, 1);

    Easing SMOOTH_STEP = (t, b, c, d) -> {
        float x = c * t / d + b;
        return (float) (-2 * Math.pow(x, 3) + (3 * Math.pow(x, 2)));
    };

    Easing BOTH_CUBIC = (t, b, c, d) -> {
        float x = c * t / d + b;
        return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
    };

    Easing LINEAR = (t, b, c, d) -> c * t / d + b;

    Easing QUAD_IN = (t, b, c, d) -> c * (t /= d) * t + b;

    Easing QUAD_OUT = (t, b, c, d) -> - c * (t /= d) * (t - 2) + b;

    Easing QUAD_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t + b;
        return - c / 2 * ((-- t) * (t - 2) - 1) + b;
    };

    Easing CUBIC_IN = (t, b, c, d) -> c * (t /= d) * t * t + b;

    Easing CUBIC_OUT = (t, b, c, d) -> c * ((t = t / d - 1) * t * t + 1) + b;

    Easing CUBIC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t * t + b;
        return c / 2 * ((t -= 2) * t * t + 2) + b;
    };

    Easing QUARTIC_IN = (t, b, c, d) -> c * (t /= d) * t * t * t + b;

    Easing QUARTIC_OUT = (t, b, c, d) -> - c * ((t = t / d - 1) * t * t * t - 1) + b;

    Easing QUARTIC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t * t * t + b;
        return - c / 2 * ((t -= 2) * t * t * t - 2) + b;
    };

    Easing QUINTIC_IN = (t, b, c, d) -> c * (t /= d) * t * t * t * t + b;

    Easing QUINTIC_OUT = (t, b, c, d) -> c * ((t = t / d - 1) * t * t * t * t + 1) + b;

    Easing QUINTIC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t * t * t * t + b;
        return c / 2 * ((t -= 2) * t * t * t * t + 2) + b;
    };

    Easing SINE_IN = (t, b, c, d) -> - c * (float) MathUtil.cos(t / d * (Math.PI / 2)) + c + b;

    Easing SINE_OUT = (t, b, c, d) -> c * (float) MathUtil.sin(t / d * (Math.PI / 2)) + b;

    Easing SINE_IN_OUT = (t, b, c, d) -> - c / 2 * ((float) MathUtil.cos(Math.PI * t / d) - 1) + b;

    Easing EXPO_IN = (t, b, c, d) -> (t == 0) ? b : c * (float) Math.pow(2, 10 * (t / d - 1)) + b;

    Easing EXPO_OUT = (t, b, c, d) -> (t == d) ? b + c : c * (- (float) Math.pow(2, - 10 * t / d) + 1) + b;

    Easing EXPO_IN_OUT = (t, b, c, d) -> {
        if (t == 0) return b;
        if (t == d) return b + c;
        if ((t /= d / 2) < 1) return c / 2 * (float) Math.pow(2, 10 * (t - 1)) + b;
        return c / 2 * (- (float) Math.pow(2, - 10 * -- t) + 2) + b;
    };

    Easing CIRC_IN = (t, b, c, d) -> - c * ((float) Math.sqrt(1 - (t /= d) * t) - 1) + b;

    Easing CIRC_OUT = (t, b, c, d) -> c * (float) Math.sqrt(1 - (t = t / d - 1) * t) + b;

    Easing CIRC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return - c / 2 * ((float) Math.sqrt(1 - t * t) - 1) + b;
        return c / 2 * ((float) Math.sqrt(1 - (t -= 2) * t) + 1) + b;
    };

    Elastic ELASTIC_IN = new ElasticIn();

    Elastic ELASTIC_OUT = new ElasticOut();

    Elastic ELASTIC_IN_OUT = new ElasticInOut();

    Back BACK_IN = new BackIn();

    Back BACK_OUT = new BackOut();

    Back BACK_IN_OUT = new BackInOut();

    Easing BOUNCE_OUT = (t, b, c, d) -> {
        if ((t /= d) < (1 / 2.75f)) {
            return c * (7.5625f * t * t) + b;
        } else if (t < (2 / 2.75f)) {
            return c * (7.5625f * (t -= (1.5f / 2.75f)) * t + .75f) + b;
        } else if (t < (2.5f / 2.75f)) {
            return c * (7.5625f * (t -= (2.25f / 2.75f)) * t + .9375f) + b;
        } else {
            return c * (7.5625f * (t -= (2.625f / 2.75f)) * t + .984375f) + b;
        }
    };

    Easing BOUNCE_IN = (t, b, c, d) -> c - Easing.BOUNCE_OUT.ease(d - t, 0, c, d) + b;

    Easing BOUNCE_IN_OUT = (t, b, c, d) -> {
        if (t < d / 2) return Easing.BOUNCE_IN.ease(t * 2, 0, c, d) * .5f + b;
        return Easing.BOUNCE_OUT.ease(t * 2 - d, 0, c, d) * .5f + c * .5f + b;
    };

    static Easing generate(double x1, double y1, double x2, double y2) {
        return new Easing() {
            @Override
            public float ease(float t, float b, float c, float d) {
                if (d <= 0 || t <= 0) return b;
                if (t >= d) return b + c;

                float progress = t / d;
                float tBez = solveTBez((float) x1, (float) x2, progress);
                float y = bezierY(tBez, (float) y1, (float) y2);

                return b + c * y;
            }

            private float solveTBez(float x1, float x2, float progress) {
                float t = progress;
                final int MAX_ITERATIONS = 8;
                final float EPSILON = 1e-5f;

                for (int i = 0; i < MAX_ITERATIONS; i++) {
                    float x = bezierX(t, x1, x2);
                    float dx = bezierDX(t, x1, x2);

                    if (Math.abs(x - progress) < EPSILON) break;
                    if (Math.abs(dx) < 1e-6f) break;

                    t -= (x - progress) / dx;
                    t = Math.max(0, Math.min(1, t));
                }

                return t;
            }

            private float bezierX(float t, float x1, float x2) {
                return 3 * (1 - t) * (1 - t) * t * x1
                        + 3 * (1 - t) * t * t * x2
                        + t * t * t;
            }

            private float bezierDX(float t, float x1, float x2) {
                return 3 * ((1 - t) * (1 - 3 * t) * x1
                        + (2 * t - 3 * t * t) * x2)
                        + 3 * t * t;
            }

            private float bezierY(float t, float y1, float y2) {
                return 3 * (1 - t) * (1 - t) * t * y1
                        + 3 * (1 - t) * t * t * y2
                        + t * t * t;
            }
        };
    }

    float ease(float t, float b, float c, float d);

    @Setter
    @Getter
    abstract class Elastic implements Easing {
        private float amplitude;
        private float period;

        public Elastic(float amplitude, float period) {
            this.amplitude = amplitude;
            this.period = period;
        }

        public Elastic() {
            this(- 1f, 0f);
        }
    }

    class ElasticIn extends Elastic {
        public ElasticIn(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticIn() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s = 0;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4;
            } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            return - (a * (float) Math.pow(2, 10 * (t -= 1)) * (float) MathUtil.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        }
    }

    class ElasticOut extends Elastic {
        public ElasticOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s = 0;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4;
            } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            return a * (float) Math.pow(2, - 10 * t) * (float) MathUtil.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
        }
    }

    class ElasticInOut extends Elastic {
        public ElasticInOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticInOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d / 2) == 2) return b + c;
            if (p == 0) p = d * (.3f * 1.5f);
            float s = 0;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4f;
            } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            if (t < 1)
                return - .5f * (a * (float) Math.pow(2, 10 * (t -= 1)) * (float) MathUtil.sin((t * d - s) * (2 * Math.PI) / p)) + b;
            return a * (float) Math.pow(2, - 10 * (t -= 1)) * (float) MathUtil.sin((t * d - s) * (2 * Math.PI) / p) * .5f + c + b;
        }
    }

    @Setter
    @Getter
    abstract class Back implements Easing {
        public static final float DEFAULT_OVERSHOOT = 1.70158f;

        private float overshoot;

        public Back() {
            this(DEFAULT_OVERSHOOT);
        }

        public Back(float overshoot) {
            this.overshoot = overshoot;
        }
    }

    class BackIn extends Back {
        public BackIn() {
            super();
        }

        public BackIn(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            return c * (t /= d) * t * ((s + 1) * t - s) + b;
        }
    }

    class BackOut extends Back {
        public BackOut() {
            super();
        }

        public BackOut(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
        }
    }

    class BackInOut extends Back {
        public BackInOut() {
            super();
        }

        public BackInOut(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            if ((t /= d / 2) < 1) return c / 2 * (t * t * (((s *= 1.525F) + 1) * t - s)) + b;
            return c / 2 * ((t -= 2) * t * (((s *= 1.525F) + 1) * t + s) + 2) + b;
        }
    }
}
