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
    private final Category category;
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private final Animation hoverAnim = new Animation(150, Easing.EXPO_OUT);
    private boolean hovered = false;

    // Размеры кнопки категории в левой панели
    public static final float W = 78f;
    public static final float H = 23f;

    // Область модулей: начало X=87, Y=33, ширина=306.5, высота=216.5
    private static final float MOD_AREA_X = 87f;
    private static final float MOD_AREA_Y = 33f;
    private static final float MOD_AREA_W = 306.5f;
    private static final float MOD_AREA_H = 216.5f;
    private static final float MOD_COL_W = 103f;
    private static final int   MOD_COLS   = 3;

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
        float alpha = ClientGui.alpha.getValue();

        // Фон кнопки категории
        if (active) {
            context.drawRect(x, y, W, H, 6f,
                    ColorRGBA.of(120, 180, 255, (int) (255 * 0.15 * alpha)));
        } else if (hoverAnim.getValue() > 0.01f) {
            context.drawRect(x, y, W, H, 6f,
                    ColorRGBA.of(255, 255, 255, (int) (255 * 0.05 * alpha * hoverAnim.getValue())));
        }

        // Цвет иконки и текста
        ColorRGBA iconColor = active
                ? ColorRGBA.of(120, 180, 255, (int) (255 * 0.9 * alpha))
                : ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * alpha));

        // Иконка категории
        context.drawText(category.getIcon(), Fonts.icons, x + 1f, y + 1f, 11f, 0.04f, iconColor);
        // Название категории
        context.drawText(category.getName(), Fonts.sf_pro, x + 15f, y + 2.5f, 6.5f, 0.04f, iconColor);

        // Рендер модулей если эта категория активна
        if (active) {
            context.getContext().enableScissor(
                    Math.round(MOD_AREA_X), Math.round(MOD_AREA_Y),
                    Math.round(MOD_AREA_X + MOD_AREA_W), Math.round(MOD_AREA_Y + MOD_AREA_H));

            // Раскладываем модули по колонкам
            float[] colY = new float[MOD_COLS];
            for (int c = 0; c < MOD_COLS; c++) {
                colY[c] = MOD_AREA_Y;
            }

            for (int i = 0; i < moduleComponents.size(); i++) {
                ModuleComponent mc = moduleComponents.get(i);
                int col = i % MOD_COLS;
                float mx = MOD_AREA_X + col * MOD_COL_W + 2f;
                mc.setX(mx);
                mc.setY(colY[col]);
                mc.render(context);
                colY[col] += ModuleComponent.H + 2f;
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
