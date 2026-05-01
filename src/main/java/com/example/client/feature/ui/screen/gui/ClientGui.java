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

    // ── Размеры GUI ──────────────────────────────────────────────────────────
    public static final float GUI_W = 398f;
    public static final float GUI_H = 254f;

    // Левая панель
    private static final float LEFT_X      = 5f;
    private static final float LEFT_Y      = 5f;
    private static final float LEFT_W      = 78f;
    private static final float LEFT_H      = 244f;

    // Правая панель (шапка)
    private static final float HEADER_X    = 87f;
    private static final float HEADER_Y    = 5f;
    private static final float HEADER_W    = 306f;
    private static final float HEADER_H    = 28f;

    // Область модулей (под шапкой)
    public static final float MOD_AREA_X   = 87f;
    public static final float MOD_AREA_Y   = 37f;   // HEADER_Y + HEADER_H + 4
    public static final float MOD_AREA_W   = 306f;
    public static final float MOD_AREA_H   = 212f;  // до низа GUI с отступом

    // Нижняя панель профиля
    private static final float PROFILE_X   = 9.5f;
    private static final float PROFILE_Y   = 220f;
    private static final float PROFILE_W   = 69f;
    private static final float PROFILE_H   = 24f;

    // Логотип — центр левой панели по X, отступ сверху
    private static final float LOGO_X      = 14f;   // левый край текста
    private static final float LOGO_Y      = 12f;
    private static final float LOGO_SIZE   = 18f;

    // Категории — начало Y
    private static final float CAT_START_Y = 46f;
    private static final float CAT_STEP    = 26f;

    // ── Анимации ─────────────────────────────────────────────────────────────
    public static final Animation open  = new Animation(400, Easing.EXPO_OUT);
    public static final Animation alpha = new Animation(300, Easing.EXPO_OUT);

    public static Category currentCategory = Category.COMBAT;

    private final List<Component> components = new ArrayList<>();
    private final RenderContext renderContext = new RenderContext(null);

    public ClientGui() {
        super(Text.of("NoName GUI"));

        float y = CAT_START_Y;
        for (Category cat : Category.values()) {
            components.add(new CategoryComponent(LEFT_X + 4f, y, cat));
            y += CAT_STEP;
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

        if (open.getValue() <= 0.21f && open.isFinished()) {
            super.close();
            return;
        }

        MatrixStack ms = context.getMatrices();
        ms.push();

        float cx = context.getScaledWindowWidth() / 2f;
        float cy = context.getScaledWindowHeight() / 2f;
        ms.translate(cx, cy, 0);
        float scale = Math.max(0.0001f, open.getValue());
        ms.scale(scale, scale, 1f);
        ms.translate(-GUI_W / 2f, -GUI_H / 2f, 0);

        float lmx = (float) ((mouseX - cx) / scale + GUI_W / 2f);
        float lmy = (float) ((mouseY - cy) / scale + GUI_H / 2f);
        renderContext.setMouseX(lmx);
        renderContext.setMouseY(lmy);

        float a = alpha.getValue();

        // ── Блюр всего фона GUI ───────────────────────────────────────────────
        renderContext.drawBlur(0, 0, GUI_W, GUI_H, 10f, 8f,
                ColorRGBA.of(255, 255, 255, (int) (255 * a)));

        // ── Тёмный полупрозрачный фон поверх блюра ───────────────────────────
        renderContext.drawRect(0, 0, GUI_W, GUI_H, 10f,
                ColorRGBA.of(12, 12, 12, (int) (160 * a)));

        // ── Левая панель ──────────────────────────────────────────────────────
        renderContext.drawStyledRect(LEFT_X, LEFT_Y, LEFT_W, LEFT_H, 8f, a);

        // ── Логотип "//" ──────────────────────────────────────────────────────
        // Центрируем иконку по ширине левой панели
        float logoTextW = Fonts.icons().getWidth("i", LOGO_SIZE);
        float logoX = LEFT_X + (LEFT_W - logoTextW) / 2f;
        renderContext.drawText("i", Fonts.icons(), logoX, LOGO_Y, LOGO_SIZE,
                ColorRGBA.of(120, 180, 255, (int) (255 * a)));

        // ── Разделитель под логотипом ─────────────────────────────────────────
        renderContext.drawRect(LEFT_X + 8f, CAT_START_Y - 6f, LEFT_W - 16f, 0.5f, 0f,
                ColorRGBA.of(255, 255, 255, (int) (25 * a)));

        // ── Шапка правой панели ───────────────────────────────────────────────
        renderContext.drawStyledRect(HEADER_X, HEADER_Y, HEADER_W, HEADER_H, 8f, a);

        // Иконка категории в шапке
        float headerIconY = HEADER_Y + (HEADER_H - 9f) / 2f - 1f;
        renderContext.drawText(currentCategory.getIcon(), Fonts.icons(),
                HEADER_X + 10f, headerIconY, 9f, 0.04f,
                ColorRGBA.of(233, 233, 233, (int) (255 * 0.55 * a)));

        // Название категории в шапке
        float headerTextY = HEADER_Y + (HEADER_H - 6.5f) / 2f;
        renderContext.drawText(currentCategory.getName(), Fonts.sf_pro(),
                HEADER_X + 23f, headerTextY, 6.5f, 0.04f,
                ColorRGBA.of(233, 233, 233, (int) (255 * 0.55 * a)));

        // ── Нижняя панель профиля ─────────────────────────────────────────────
        renderContext.drawStyledRect(PROFILE_X, PROFILE_Y, PROFILE_W, PROFILE_H, 6f, a);

        // Аватар (круглый прямоугольник)
        float avatarSize = 14f;
        float avatarX = PROFILE_X + 6f;
        float avatarY = PROFILE_Y + (PROFILE_H - avatarSize) / 2f;
        renderContext.drawRect(avatarX, avatarY, avatarSize, avatarSize, avatarSize / 2f,
                ColorRGBA.of(120, 180, 255, (int) (200 * a)));

        // Имя и версия — вертикально по центру панели
        float nameX = avatarX + avatarSize + 5f;
        float nameY = PROFILE_Y + PROFILE_H / 2f - 6f;
        renderContext.drawText("NoName", Fonts.sf_pro(), nameX, nameY, 6.5f, 0.05f,
                ColorRGBA.of(255, 255, 255, (int) (255 * a)));
        renderContext.drawText("v1.0.0", Fonts.sf_pro(), nameX, nameY + 8f, 5.5f, 0.01f,
                ColorRGBA.of(255, 255, 255, (int) (255 * 0.45 * a)));

        // ── Компоненты категорий ──────────────────────────────────────────────
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
