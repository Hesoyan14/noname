package com.example.client.feature.ui.screen.gui;

import com.example.client.feature.module.api.Category;
import com.example.client.feature.ui.screen.gui.component.CategoryComponent;
import com.example.client.feature.ui.screen.gui.component.Component;
import com.example.client.utils.animation.Animation;
import com.example.client.utils.animation.Easing;
import com.example.client.utils.client.IMinecraft;
import com.example.client.utils.render.ColorRGBA;
import com.example.client.utils.render.Fonts;
import com.example.client.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClientGui extends Screen implements IMinecraft {

    // Размер GUI (398x254 — как в SilentCore)
    public static final float GUI_W = 398f;
    public static final float GUI_H = 254f;

    // Анимации открытия
    public static final Animation open  = new Animation(400, Easing.EXPO_OUT);
    public static final Animation alpha = new Animation(300, Easing.EXPO_OUT);

    // Текущая выбранная категория
    public static Category currentCategory = Category.COMBAT;

    private final List<Component> components = new ArrayList<>();
    private final RenderContext renderContext = new RenderContext(null);

    public ClientGui() {
        super(Text.of("NoName GUI"));

        // Создаём компоненты категорий — Y начинается с 39 (18 + 21)
        float categoryY = 18f;
        for (Category cat : Category.values()) {
            categoryY += 21f;
            components.add(new CategoryComponent(14f, categoryY, cat));
        }

        open.setStartValue(0.2f);
        open.animate(1f);
        alpha.animate(1f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float ticks) {
        renderContext.setContext(context);
        open.update();
        alpha.update();

        // Закрываем если анимация закрытия завершена
        if (open.getValue() <= 0.21f && open.isFinished()) {
            super.close();
            return;
        }

        MatrixStack ms = context.getMatrices();
        ms.push();

        // Центрируем GUI и применяем scale-анимацию открытия
        float cx = context.getScaledWindowWidth() / 2f;
        float cy = context.getScaledWindowHeight() / 2f;
        ms.translate(cx, cy, 0);
        float scale = Math.max(0.0001f, open.getValue());
        ms.scale(scale, scale, 1f);
        ms.translate(-GUI_W / 2f, -GUI_H / 2f, 0);

        // Трансформируем координаты мыши в локальные
        float lmx = (float) ((mouseX - cx) / scale + GUI_W / 2f);
        float lmy = (float) ((mouseY - cy) / scale + GUI_H / 2f);
        renderContext.setMouseX(lmx);
        renderContext.setMouseY(lmy);

        float a = alpha.getValue();

        // --- Фон с блюром ---
        renderContext.drawBlur(0, 0, GUI_W, GUI_H, 8f, 128f,
                ColorRGBA.of(30, 30, 30, (int) (255 * a)));

        // --- Левая панель (категории) ---
        renderContext.drawStyledRect(5f, 5f, 78f, 244.5f, 6f, a);

        // --- Правая панель (модули) ---
        renderContext.drawStyledRect(87f, 5f, 306.5f, 24f, 6f, a);

        // --- Логотип "i" (иконка клиента) ---
        renderContext.drawText("i", Fonts.icons, 31f, 11f, 24f,
                ColorRGBA.of(120, 180, 255, (int) (255 * a)));

        // --- Название текущей категории в шапке ---
        renderContext.drawText("Y", Fonts.icons, 97.5f, 12.8f, 9f,
                ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * a)));
        renderContext.drawText(currentCategory.getName(), Fonts.sf_pro, 107.5f, 13f, 6.5f,
                ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * a)));

        // --- Нижняя панель (профиль) ---
        renderContext.drawStyledRect(9.5f, 224.5f, 69f, 21f, 4f, a);
        renderContext.drawRect(14.5f, 229.5f, 12f, 12f, 5.5f,
                ColorRGBA.of(255, 255, 255, (int) (255 * a)));
        renderContext.drawText("NoName", Fonts.sf_pro, 28f, 227.5f, 7f, 0.05f,
                ColorRGBA.of(255, 255, 255, (int) (255 * a)));
        renderContext.drawText("v1.0.0", Fonts.sf_pro, 28f, 235f, 6f, 0.01f,
                ColorRGBA.of(255, 255, 255, (int) (255 * a * 0.6f)));

        // --- Компоненты категорий ---
        components.forEach(c -> c.render(renderContext));

        ms.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float[] m = transformMouse(mouseX, mouseY);
        components.forEach(c -> c.click(m[0], m[1], button));
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        float[] m = transformMouse(mouseX, mouseY);
        components.forEach(c -> c.scroll(m[0], m[1], vAmount));
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        components.forEach(c -> c.moved(mouseX, mouseY));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            open.animate(0.2f);
            alpha.animate(0f);
        }
        return false;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        components.clear();
        super.close();
    }

    private float[] transformMouse(double mouseX, double mouseY) {
        float scale = Math.max(0.0001f, open.getValue());
        float lx = (float) ((mouseX - this.width / 2f) / scale + GUI_W / 2f);
        float ly = (float) ((mouseY - this.height / 2f) / scale + GUI_H / 2f);
        return new float[]{lx, ly};
    }
}
