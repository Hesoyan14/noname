package com.example.client.feature.ui.screen.gui.component;

import com.example.client.feature.module.api.Module;
import com.example.client.feature.ui.screen.gui.ClientGui;
import com.example.client.utils.animation.Animation;
import com.example.client.utils.animation.Easing;
import com.example.client.utils.render.ColorRGBA;
import com.example.client.utils.render.Fonts;
import com.example.client.utils.render.RenderContext;

public class ModuleComponent extends Component {
    private final Module module;
    private final Animation enableAnim = new Animation(200, Easing.EXPO_OUT);
    private final Animation hoverAnim = new Animation(150, Easing.EXPO_OUT);
    private boolean hovered = false;

    // Размеры карточки модуля
    public static final float W = 101f;
    public static final float H = 22f;

    public ModuleComponent(float x, float y, Module module) {
        super(x, y);
        this.module = module;
    }

    @Override
    public void render(RenderContext context) {
        enableAnim.animate(module.isEnabled() ? 1f : 0f);
        enableAnim.update();
        hoverAnim.animate(hovered ? 1f : 0f);
        hoverAnim.update();

        float alpha = ClientGui.alpha.getValue();
        float enabledV = enableAnim.getValue();
        float hoverV = hoverAnim.getValue();

        // Фон карточки — стеклянный прямоугольник
        float rectAlpha = (float) (0.8 + enabledV * 0.7);
        context.drawStyledRect(x, y, W, H, 6, alpha * rectAlpha);

        // Подсветка при включённом модуле — тонкая цветная полоска слева
        if (enabledV > 0.01f) {
            ColorRGBA accent = ColorRGBA.of(120, 180, 255, (int) (255 * alpha * enabledV));
            context.drawRect(x, y + 4f, 2f, H - 8f, 1f, accent);
        }

        // Подсветка при наведении
        if (hoverV > 0.01f) {
            context.drawRect(x, y, W, H, 6f,
                    ColorRGBA.of(255, 255, 255, (int) (15 * alpha * hoverV)));
        }

        float textAlpha = (float) (0.5 + enabledV * 0.5);

        // Название модуля
        context.drawText(module.getName(), Fonts.sf_pro,
                x + 7f, y + 4f, 6.5f, 0.05f,
                ColorRGBA.of(255, 255, 255, (int) (255 * alpha * textAlpha)));

        // Описание
        context.drawText(module.getInfo().description(), Fonts.sf_pro,
                x + 7f, y + 12f, 5.5f, 0.01f,
                ColorRGBA.of(255, 255, 255, (int) (255 * alpha * textAlpha * 0.5f)));
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, W, H)) {
            if (button == 0) module.cToggle();
        }
    }

    @Override
    public void moved(double mouseX, double mouseY) {
        hovered = isHovered(mouseX, mouseY, W, H);
    }

    public Module getModule() { return module; }
}
