package com.example.client.feature.ui.screen.gui.component;

import com.example.client.NoNameClient;
import com.example.client.feature.module.api.Category;
import com.example.client.feature.module.api.Module;
import com.example.client.feature.ui.screen.gui.ClientGui;
import com.example.client.utils.animation.Animation;
import com.example.client.utils.animation.Easing;
import com.example.client.utils.render.ColorRGBA;
import com.example.client.utils.render.Fonts;
import com.example.client.utils.render.RenderContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryComponent extends Component {

    // Размеры кнопки категории
    public static final float W = 70f;
    public static final float H = 22f;

    // Отступы сетки модулей
    private static final float MOD_PAD_X  = 4f;
    private static final float MOD_PAD_Y  = 4f;
    private static final float MOD_GAP    = 3f;
    private static final int   MOD_COLS   = 3;

    private final Category category;
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private final Animation hoverAnim = new Animation(150, Easing.EXPO_OUT);
    private boolean hovered = false;

    public CategoryComponent(float x, float y, Category category) {
        super(x, y);
        this.category = category;
        for (Module m : NoNameClient.getInstance().getModuleManager().getModules(category)) {
            moduleComponents.add(new ModuleComponent(0, 0, m));
        }
    }

    @Override
    public void render(RenderContext context) {
        hoverAnim.animate(hovered ? 1f : 0f);
        hoverAnim.update();

        boolean active = ClientGui.currentCategory == category;
        float a = ClientGui.alpha.getValue();

        // ── Фон кнопки ────────────────────────────────────────────────────────
        if (active) {
            context.drawRect(x, y, W, H, 6f,
                    ColorRGBA.of(120, 180, 255, (int) (255 * 0.18 * a)));
            // Акцентная полоска слева
            context.drawRect(x, y + 4f, 2.5f, H - 8f, 1.5f,
                    ColorRGBA.of(120, 180, 255, (int) (255 * 0.9 * a)));
        } else if (hoverAnim.getValue() > 0.01f) {
            context.drawRect(x, y, W, H, 6f,
                    ColorRGBA.of(255, 255, 255, (int) (255 * 0.06 * a * hoverAnim.getValue())));
        }

        // ── Иконка + название ─────────────────────────────────────────────────
        ColorRGBA iconColor = active
                ? ColorRGBA.of(120, 180, 255, (int) (255 * 0.95 * a))
                : ColorRGBA.of(200, 200, 200, (int) (255 * 0.45 * a));

        // Иконка — вертикально по центру кнопки
        float iconY = y + (H - 9f) / 2f - 1f;
        context.drawText(category.getIcon(), Fonts.icons(), x + 6f, iconY, 9f, 0.04f, iconColor);

        // Название — вертикально по центру
        float textY = y + (H - 6.5f) / 2f;
        context.drawText(category.getName(), Fonts.sf_pro(), x + 19f, textY, 6.5f, 0.04f, iconColor);

        // ── Модули (только для активной категории) ────────────────────────────
        if (active) {
            float areaX = ClientGui.MOD_AREA_X;
            float areaY = ClientGui.MOD_AREA_Y;
            float areaW = ClientGui.MOD_AREA_W;
            float areaH = ClientGui.MOD_AREA_H;

            context.getContext().enableScissor(
                    Math.round(areaX), Math.round(areaY),
                    Math.round(areaX + areaW), Math.round(areaY + areaH));

            // Ширина одной карточки с учётом отступов
            float colW = (areaW - MOD_PAD_X * 2f - MOD_GAP * (MOD_COLS - 1)) / MOD_COLS;

            float[] colY = new float[MOD_COLS];
            for (int c = 0; c < MOD_COLS; c++) {
                colY[c] = areaY + MOD_PAD_Y;
            }

            for (int i = 0; i < moduleComponents.size(); i++) {
                ModuleComponent mc = moduleComponents.get(i);
                int col = i % MOD_COLS;
                float mx = areaX + MOD_PAD_X + col * (colW + MOD_GAP);
                mc.setX(mx);
                mc.setY(colY[col]);
                mc.setWidth(colW);
                mc.render(context);
                colY[col] += ModuleComponent.H + MOD_GAP;
            }

            context.getContext().disableScissor();
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, W, H) && button == 0) {
            ClientGui.currentCategory = category;
        }
        if (ClientGui.currentCategory == category) {
            moduleComponents.forEach(mc -> mc.click(mouseX, mouseY, button));
        }
    }

    @Override
    public void moved(double mouseX, double mouseY) {
        hovered = isHovered(mouseX, mouseY, W, H);
        if (ClientGui.currentCategory == category) {
            moduleComponents.forEach(mc -> mc.moved(mouseX, mouseY));
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double amount) {
        if (ClientGui.currentCategory == category) {
            moduleComponents.forEach(mc -> mc.scroll(mouseX, mouseY, amount));
        }
    }
}
