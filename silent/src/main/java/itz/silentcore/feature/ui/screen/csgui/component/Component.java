package itz.silentcore.feature.ui.screen.csgui.component;

import itz.silentcore.utils.render.RenderContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Component {
    private float x, y;

    public void click(double mouseX, double mouseY, int button) {}
    public void key(int key) {}
    public void render(RenderContext context) {}
    public void close() {}
    public void dragged(double mouseX, double mouseY, double deltaX, double deltaY, int button) {}
    public void moved(double mouseX, double mouseY) {}
    public void kReleased(int key) {}
    public void mReleased(double mouseX, double mouseY, int button) {}
    public void type(char chr) {}
    public void scroll(double mouseX, double mouseY, double amount) {}

    public boolean isHovered(double mouseX, double mouseY, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
