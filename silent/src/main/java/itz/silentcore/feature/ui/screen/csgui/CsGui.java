package itz.silentcore.feature.ui.screen.csgui;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.ui.screen.csgui.component.Component;
import itz.silentcore.feature.ui.screen.csgui.component.impl.CategoryComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.kotopushka.compiler.sdk.classes.Profile;

import java.util.ArrayList;

public class CsGui extends Screen implements IMinecraft {
    public static Animation open = new Animation(400, Easing.EXPO_OUT);
    public static Animation alpha = new Animation(300, Easing.EXPO_OUT);
    public static Category category = Category.COMBAT;
    public ArrayList<Component> components = new ArrayList<>();
    public static Module binding;

    public CsGui() {
        super(Text.of("CsGui"));
        int categoryY = 18;
        for (Category category1 : Category.values()) {
            categoryY += 21;
            // Используем специальный компонент для категории THEMES
            if (category1 == Category.THEMES) {
                components.add(new itz.silentcore.feature.ui.screen.csgui.component.impl.ThemeCategoryComponent(14, categoryY, category1));
            } else if (category1 == Category.AUTOBUY) {
                components.add(new itz.silentcore.feature.ui.screen.csgui.component.impl.AutoBuyCategoryComponent(14, categoryY, category1));
            } else {
                components.add(new CategoryComponent(14, categoryY, category1));
            }
        }

        open.setStartValue(0.2f);
        open.animate(1);
        alpha.animate(1);
    }

    RenderContext renderContext = new RenderContext(null);
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float ticks) {
        renderContext.setContext(context);
        open.update();
        alpha.update();

        MatrixStack ms = context.getMatrices();
        ms.push();

        float centerX = context.getScaledWindowWidth() / 2f;
        float centerY = context.getScaledWindowHeight() / 2f;

        ms.translate(centerX, centerY, 0);
        float scale = Math.max(0.0001f, open.getValue());
        ms.scale(scale, scale, 1);
        ms.translate(-398 / 2f, -254 / 2f, 0);

        float transformedMouseX = (mouseX - centerX) / scale + 398 / 2f;
        float transformedMouseY = (mouseY - centerY) / scale + 254 / 2f;

        renderContext.setMouseX(transformedMouseX);
        renderContext.setMouseY(transformedMouseY);

        if (open.getValue() == 0.2f) {
            super.close();
        }

        float width1 = 24 + Fonts.sf_pro.getWidth(category.getName(), 6.5f);
        long activeCount = SilentCore.getInstance().moduleManager.getActiveModules().stream().filter(m -> m.getCategory() == category).count();
        long totalCount = SilentCore.getInstance().moduleManager.getModules().stream().filter(m -> m.getCategory() == category).count();
        float width = 24 + Fonts.sf_pro.getWidth(String.format("%s/%s", activeCount, totalCount), 6.5f);
        renderContext.drawBlur(0, 0, 398, 254, 8, 128, ColorRGBA.of(55, 55, 55, (int) (255 * alpha.getValue())));
        renderContext.drawStyledRect(5, 5, 78, 244.5f, 6, alpha.getValue());
        renderContext.drawStyledRect(87, 5, 306.5f, 24, 6, alpha.getValue());
        renderContext.drawStyledRect(91.5f, 9, width1, 16, 3, alpha.getValue());
        renderContext.drawStyledRect(94.5f + width1, 9, width - 12, 16, 3, alpha.getValue());
        renderContext.drawText(String.format("%s/%s", activeCount, totalCount), Fonts.sf_pro, 92f + width1 + width / 4, 13f, 6.5f, ColorRGBA.of(233, 233, 233, (int) ((255 * 0.45) * alpha.getValue())));
        renderContext.drawText("Y", Fonts.icons, 97.5f, 12.8f, 9, ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * alpha.getValue())));
        renderContext.drawText(category.getName(), Fonts.sf_pro, 107.5f, 13f, 6.5f, ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * alpha.getValue())));
        
        // Логотип с цветом темы
        ColorRGBA logoColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
        renderContext.drawText("i", Fonts.icons, 31f, 11f, 24, 
            ColorRGBA.of(logoColor.getR(), logoColor.getG(), logoColor.getB(), (int) (255 * alpha.getValue())));
        renderContext.drawStyledRect(9.5f, 224.5f, 69, 21, 4, alpha.getValue());
        renderContext.drawRect(14.5f, 229.5f, 12, 12, 5.5f, ColorRGBA.of(255, 255, 255, (int) (255 * alpha.getValue())));
        renderContext.drawText(Profile.getUsername(), Fonts.sf_pro, 28, 227.5f, 7, 0.05f, ColorRGBA.of(255, 255, 255, (int) (255 * alpha.getValue())));
        renderContext.drawText("UID: " + Profile.getUid(), Fonts.sf_pro, 28, 235, 6, 0.01f, ColorRGBA.of(255, 255, 255, (int) (255 * alpha.getValue())));
        renderContext.drawText("T", Fonts.icons, 47.5f, 232.25f, 7, 0.001f, ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * alpha.getValue())));
        renderContext.drawText("d", Fonts.icons, 57.5f, 232.25f, 7, 0.001f, ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * alpha.getValue())));
        renderContext.drawText("b", Fonts.icons, 67.5f, 232.25f, 7, 0.001f, ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * alpha.getValue())));

        components.forEach(component -> component.render(renderContext));
        ms.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float[] m = transformMouse(mouseX, mouseY);
        components.forEach(c -> c.click(m[0], m[1], button));
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && binding == null) {
            open.animate(0.2f);
            alpha.animate(0);
        }

        if (binding != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                binding = null;
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                binding.setKey(-222);
            }

            else if (binding != null) {
                binding.setKey(keyCode);
            }

            binding = null;
        }

        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        components.clear();
        super.close();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        components.forEach(component -> component.moved(mouseX, mouseY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float[] m = transformMouse(mouseX, mouseY);
        components.forEach(c -> c.scroll(m[0], m[1], verticalAmount));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    float[] transformMouse(double mouseX, double mouseY) {
        float scale = Math.max(0.0001f, open.getValue());
        float localX = (float) ((mouseX - this.width / 2f) / scale + 398 / 2f);
        float localY = (float) ((mouseY - this.height / 2f) / scale + 254 / 2f);
        return new float[]{localX, localY};
    }
}