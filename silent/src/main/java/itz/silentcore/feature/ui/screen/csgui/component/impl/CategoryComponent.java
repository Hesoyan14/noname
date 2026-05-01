package itz.silentcore.feature.ui.screen.csgui.component.impl;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.Component;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;

import java.util.ArrayList;

public class CategoryComponent extends Component {
    @Getter
    private final Category parent;
    private final Animation animation;
    public final ArrayList<ModuleComponent> components = new ArrayList<>();

    public CategoryComponent(float x, float y, Category parent) {
        super(x, y);
        this.parent = parent;
        this.animation = new Animation(250, Easing.EXPO_OUT);
        float tempX = -16, tempY = 33;
        for (Module module : SilentCore.getInstance().moduleManager.getModules(parent)) {
            tempX += 103;
            if (tempX > 380) {
                tempX = 103 - 16;
                tempY += 24;
            }
            components.add(new ModuleComponent(tempX, tempY, module));
        }
    }

    @Override
    public void render(RenderContext context) {
        animation.update();
        
        boolean isActive = CsGui.category == parent;
        
        // Получаем цвет темы
        ColorRGBA themeColor = itz.silentcore.feature.theme.ThemeManager.getInstance().getPrimaryColorRGBA();
        
        // Фон для активной категории - более яркий
        ColorRGBA back = isActive ? 
            ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) ((((255 * 0.15) * animation.getValue())) * CsGui.alpha.getValue())) : 
            ColorRGBA.of(233, 233, 233, (int) (255 * 0));
        context.drawRect(getX(), getY(), 78, 23, 6, back);
        
        // Цвет иконки и текста - цветной если активна, серый если нет
        ColorRGBA iconTextColor = isActive ?
            ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) (255 * 0.8 * CsGui.alpha.getValue())) :
            ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * CsGui.alpha.getValue()));
        
        context.drawText(getParent().getIcon(), Fonts.icons, getX() + 1, getY() + 1f, 11, 0.04f, iconTextColor);
        context.drawText(getParent().getName(), Fonts.sf_pro, getX() + 15, getY() + 2.5f, 6.5f, 0.04f, iconTextColor);

        if (CsGui.category == parent) {
            // Включаем scissor для области модулей
            int sx = Math.round(87);
            int sy = Math.round(33);
            int sw = Math.round(306.5f);
            int sh = Math.round(216.5f); // 254 (высота GUI) - 33 (начало) - 4.5 (отступ снизу)
            
            context.getContext().enableScissor(sx, sy, sx + sw, sy + sh);
            
            float startX = 103 - 16;
            float spacingX = 103f;
            int columns = 3;
            float[] colX = new float[columns];
            float[] colY = new float[columns];
            for (int c = 0; c < columns; c++) {
                colX[c] = startX + c * spacingX;
                colY[c] = 33f;
            }

            for (int i = 0; i < components.size(); i++) {
                ModuleComponent moduleComponent = components.get(i);
                int col = i % columns;
                moduleComponent.setX(colX[col]);
                moduleComponent.setY(colY[col]);
                moduleComponent.render(context);
                colY[col] += moduleComponent.getTotalHeight() + 2f;
            }
            
            context.getContext().disableScissor();
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (mouseX >= getX() && mouseX <= getX() + 78 && mouseY >= getY() && mouseY <= getY() + 23) {
            if (button == 0) CsGui.category = parent;
        }
        if (CsGui.category == parent) components.forEach(c -> c.click(mouseX, mouseY, button));
    }

    @Override
    public void key(int button) {
        if (CsGui.category == parent) components.forEach(moduleComponent -> moduleComponent.key(button));
    }

    @Override
    public void type(char chr) {
        if (CsGui.category == parent) components.forEach(moduleComponent -> moduleComponent.type(chr));
    }

    @Override
    public void dragged(double mouseX, double mouseY, double dX, double dY, int button) {
        if (CsGui.category == parent) components.forEach(moduleComponent -> moduleComponent.dragged(mouseX, mouseY, dX, dY, button));
    }

    @Override
    public void moved(double mouseX, double mouseY) {
        if (CsGui.category == parent) components.forEach(moduleComponent -> moduleComponent.moved(mouseX, mouseY));
    }

    @Override
    public void mReleased(double mouseX, double mouseY, int button) {
        if (CsGui.category == parent) components.forEach(moduleComponent -> moduleComponent.mReleased(mouseX, mouseY, button));
    }

    @Override
    public void scroll(double mouseX, double mouseY, double amount) {
        if (CsGui.category == parent) components.forEach(moduleComponent -> moduleComponent.scroll(mouseX, mouseY, amount));
    }
}
