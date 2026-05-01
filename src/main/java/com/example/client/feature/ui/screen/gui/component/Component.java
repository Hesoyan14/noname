package com.example.client.feature.ui.screen.gui.component;

import com.example.client.utils.render.RenderContext;

public abstract class Component {
    protected float x, y;

    public Component(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public abstract void render(RenderContext context);

    public void click(double mouseX, double mouseY, int button) {}
    public void scroll(double mouseX, double mouseY, double amount) {}
    public void moved(double mouseX, double mouseY) {}
    public void dragged(double mouseX, double mouseY, double dX, double dY, int button) {}
    public void mReleased(double mouseX, double mouseY, int button) {}
    public void key(int keyCode) {}

    protected boolean isHovered(double mx, double my, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
