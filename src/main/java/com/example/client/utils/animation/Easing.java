package com.example.client.utils.animation;

public interface Easing {

    Easing LINEAR = (t, b, c, d) -> c * t / d + b;

    Easing QUAD_OUT = (t, b, c, d) -> -c * (t /= d) * (t - 2) + b;

    Easing CUBIC_OUT = (t, b, c, d) -> c * ((t = t / d - 1) * t * t + 1) + b;

    Easing EXPO_OUT = (t, b, c, d) -> (t == d) ? b + c : c * (-(float) Math.pow(2, -10 * t / d) + 1) + b;

    Easing EXPO_IN = (t, b, c, d) -> (t == 0) ? b : c * (float) Math.pow(2, 10 * (t / d - 1)) + b;

    Easing SINE_OUT = (t, b, c, d) -> c * (float) Math.sin(t / d * (Math.PI / 2)) + b;

    Easing CIRC_OUT = (t, b, c, d) -> c * (float) Math.sqrt(1 - (t = t / d - 1) * t) + b;

    float ease(float t, float b, float c, float d);
}
