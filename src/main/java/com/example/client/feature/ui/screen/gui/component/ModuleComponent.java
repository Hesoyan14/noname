package com.example.client.feature.ui.screen.gui.component;

import com.example.client.feature.module.api.Module;
import com.example.client.feature.ui.screen.gui.ClientGui;
import com.example.client.utils.animation.Animation;
import com.example.client.utils.animation.Easing;
import com.example.client.utils.render.ColorRGBA;
import com.example.client.utils.render.Fonts;
import com.example.client.utils.render.RenderContext;

public class ModuleComponent extends Component {

    public static final float H = 36f;   // высота карточки
    private float width = 96f;           // ширина — задаётся снаружи через setWidth()

    private final Module module;
    private final Animation enableAnim = new Animation(200, Easing.EXPO_OUT);
    private final Animation hoverAnim  = new Animation(150, Easing.EXPO_OUT);
    private boolean hovered = false;

    public ModuleComponent(float x, float y, Module module) {
        super(x, y);
        this.module = module;
    }

    public void setWidth(float w) { this.width = w; }

    @Override
    public void render(RenderContext context) {
        enableAnim.animate(module.isEnabled() ? 1f : 0f);
        enableAnim.update();
        hoverAnim.animate(hovered ? 1f : 0f);
        hoverAnim.update();

        float a   = ClientGui.alpha.getValue();
        float ena = enableAnim.getValue();
        float hov = hoverAnim.getValue();

        // ── Фон карточки ──────────────────────────────────────────────────────
        context.drawStyledRect(x, y, width, H, 6f, a * (0.8f + ena * 0.4f));

        // Подсветка при наведении
        if (hov > 0.01f) {
            context.drawRect(x, y, width, H, 6f,
                    ColorRGBA.of(255, 255, 255, (int) (18 * a * hov)));
        }

        // Акцентная полоска слева при включённом модуле
        if (ena > 0.01f) {
            context.drawRect(x + 1f, y + 5f, 2f, H - 10f, 1f,
                    ColorRGBA.of(120, 180, 255, (int) (255 * a * ena)));
        }

        float textAlpha = 0.5f + ena * 0.5f;

        // ── Название модуля ───────────────────────────────────────────────────
        context.drawText(module.getName(), Fonts.sf_pro(),
                x + 8f, y + 8f,
                6.5f, 0.05f,
                ColorRGBA.of(255, 255, 255, (int) (255 * a * textAlpha)));

        // ── Описание ──────────────────────────────────────────────────────────
        context.drawText(module.getInfo().description(), Fonts.sf_pro(),
                x + 8f, y + 18f,
                5f, 0.01f,
                ColorRGBA.of(200, 200, 200, (int) (255 * a * textAlpha * 0.5f)));
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, width, H) && button == 0) {
            module.cToggle();
        }
    }

    @Override
    public void moved(double mouseX, double mouseY) {
        hovered = isHovered(mouseX, mouseY, width, H);
    }

    public Module getModule() { return module; }
}
