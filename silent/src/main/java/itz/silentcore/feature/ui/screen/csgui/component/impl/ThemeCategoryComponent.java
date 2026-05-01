package itz.silentcore.feature.ui.screen.csgui.component.impl;

import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.theme.Theme;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.Component;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ThemeCategoryComponent extends Component {
    @Getter
    private final Category parent;
    private final Animation animation;
    private final List<ThemeBox> themeBoxes = new ArrayList<>();
    private static final int BOX_SIZE = 60;
    private static final int BOX_SPACING = 15;
    
    public ThemeCategoryComponent(float x, float y, Category parent) {
        super(x, y);
        this.parent = parent;
        this.animation = new Animation(250, Easing.EXPO_OUT);
        
        // Создаем квадратики для каждой темы
        List<Theme> themes = ThemeManager.getInstance().getThemes();
        for (int i = 0; i < themes.size(); i++) {
            float boxX = 103 + (i % 4) * (BOX_SIZE + BOX_SPACING);
            float boxY = 50 + (i / 4) * (BOX_SIZE + BOX_SPACING + 20);
            themeBoxes.add(new ThemeBox(boxX, boxY, themes.get(i)));
        }
    }

    @Override
    public void render(RenderContext context) {
        animation.update();
        
        boolean isActive = CsGui.category == parent;
        
        // Получаем цвет темы
        ColorRGBA themeColor = ThemeManager.getInstance().getPrimaryColorRGBA();
        
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

        // Рисуем темы если категория активна
        if (CsGui.category == parent) {
            for (ThemeBox box : themeBoxes) {
                box.render(context);
            }
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (mouseX >= getX() && mouseX <= getX() + 78 && mouseY >= getY() && mouseY <= getY() + 23) {
            if (button == 0) CsGui.category = parent;
        }
        
        if (CsGui.category == parent && button == 0) {
            for (ThemeBox box : themeBoxes) {
                box.click(mouseX, mouseY);
            }
        }
    }

    private static class ThemeBox {
        private final float x, y;
        private final Theme theme;
        private final ThemeManager themeManager = ThemeManager.getInstance();
        
        public ThemeBox(float x, float y, Theme theme) {
            this.x = x;
            this.y = y;
            this.theme = theme;
        }
        
        public void render(RenderContext context) {
            boolean isSelected = themeManager.getCurrentTheme() == theme;
            
            // Рисуем квадратик с цветом темы
            context.drawRect(x, y, BOX_SIZE, BOX_SIZE, 8, 
                new ColorRGBA(theme.getPrimaryColor()));
            
            // Рисуем обводку если выбрана
            if (isSelected) {
                context.drawBorder(x - 2, y - 2, BOX_SIZE + 4, BOX_SIZE + 4, 
                    8, 2, ColorRGBA.of(255, 255, 255, 255));
            }
            
            // Рисуем название темы под квадратиком (центрируем вручную)
            String name = theme.getName();
            float textWidth = Fonts.sf_pro.getWidth(name, 6f);
            context.drawText(name, Fonts.sf_pro, 
                x + (BOX_SIZE - textWidth) / 2f, y + BOX_SIZE + 5, 
                6f, 0.04f, 
                ColorRGBA.of(233, 233, 233, (int) (255 * 0.7 * CsGui.alpha.getValue())));
        }
        
        public void click(double mouseX, double mouseY) {
            if (mouseX >= x && mouseX <= x + BOX_SIZE && 
                mouseY >= y && mouseY <= y + BOX_SIZE) {
                themeManager.setTheme(theme);
            }
        }
    }

    @Override
    public void key(int button) {}

    @Override
    public void type(char chr) {}

    @Override
    public void dragged(double mouseX, double mouseY, double dX, double dY, int button) {}

    @Override
    public void moved(double mouseX, double mouseY) {}

    @Override
    public void mReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void scroll(double mouseX, double mouseY, double amount) {}
}
