package itz.silentcore.feature.ui.screen.csgui.component.impl;

import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.Component;
import itz.silentcore.feature.ui.screen.csgui.component.impl.setting.TestSetting;
import itz.silentcore.feature.ui.screen.csgui.component.impl.setting.SliderSettingComponent;
import itz.silentcore.feature.ui.screen.csgui.component.impl.setting.BooleanSettingComponent;
import itz.silentcore.feature.ui.screen.csgui.component.impl.setting.ModeSettingComponent;
import itz.silentcore.feature.ui.screen.csgui.component.impl.setting.MultiBooleanSettingComponent;
import itz.silentcore.feature.module.api.setting.Setting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import itz.silentcore.utils.client.Keyboard;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ModuleComponent extends Component {
    public Module parent;
    public boolean expanded = false; // Изменено на false по умолчанию
    public Animation expand = new Animation(300, Easing.EXPO_OUT);
    public Animation enabledAlpha = new Animation(200, Easing.EXPO_OUT);
    public float descOffsetX = 0f;
    public boolean scrollForward = true;
    public long lastTimeNs = System.nanoTime();
    public float edgePauseTimer = 0f;
    public boolean canScroll = false;
    public ArrayList<SettingComponent> settings = new ArrayList<>();
    
    // Скролл для настроек
    private float scrollOffset = 0f;
    private float maxScroll = 0f;
    private float smoothScrollOffset = 0f;
    private static final float MAX_SETTINGS_HEIGHT = 150f; // Максимальная высота для настроек

    public ModuleComponent(float x, float y, Module module) {
        super(x, y);
        this.parent = module;
        try {
            for (Setting s : module.getSettings()) {
                if (s instanceof NumberSetting ns) {
                    settings.add(new SliderSettingComponent(x, y, ns));
                } else if (s instanceof BooleanSetting bs) {
                    settings.add(new BooleanSettingComponent(x, y, bs));
                } else if (s instanceof itz.silentcore.feature.module.api.setting.impl.ModeSetting ms) {
                    settings.add(new ModeSettingComponent(x, y, ms));
                } else if (s instanceof MultiBooleanSetting mbs) {
                    settings.add(new MultiBooleanSettingComponent(x, y, mbs));
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void render(RenderContext context) {
        expand.animate(expanded ? 1 : 0);
        expand.update();
        enabledAlpha.animate(parent.isEnabled() ? 1 : 0);
        enabledAlpha.update();

        float rectAlpha = (float) (0.8 + enabledAlpha.getValue() * 0.7);
        float textAlpha = (float) (0.5 + enabledAlpha.getValue() * 0.5);

        float totalSettingsHeight = 0f;
        for (SettingComponent s : settings) {
            if (s.getSetting().isVisible()) {
                totalSettingsHeight += s.getHeight();
            }
        }
        
        // Вычисляем максимальный скролл
        maxScroll = Math.max(0, totalSettingsHeight - MAX_SETTINGS_HEIGHT);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        // Плавный скролл
        smoothScrollOffset += (scrollOffset - smoothScrollOffset) * 0.3f;
        
        float displayHeight = Math.min(totalSettingsHeight, MAX_SETTINGS_HEIGHT);
        float expandedHeight = displayHeight * (float) expand.getValue();
        float totalHeight = 22f + expandedHeight;

        context.drawStyledRect(getX(), getY(), 101, totalHeight, 6, CsGui.alpha.getValue() * rectAlpha);
        context.drawText(parent.getName(), Fonts.sf_pro, getX() + 5f, getY() + 4f, 6.5f, 0.05f, ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue() * textAlpha)));
        String desc = parent.getInfo().description();

        float textX = getX() + 5f;
        float textY = getY() + 11.5f;

        long now = System.nanoTime();
        float dt = (now - lastTimeNs) / 1000000000f;
        lastTimeNs = now;

        float maxShift = Math.max(0f, Fonts.sf_pro.getWidth(desc, 5.5f) - 91);

        if (maxShift > 0.5f) {
            if (edgePauseTimer > 0f) {
                edgePauseTimer -= dt;
            } else if (canScroll) {
                if (scrollForward) {
                    descOffsetX += 30f * dt;
                    if (descOffsetX >= maxShift) {
                        descOffsetX = maxShift;
                        scrollForward = false;
                        edgePauseTimer = 0.8f;
                    }
                } else {
                    descOffsetX -= 30f * dt;
                    if (descOffsetX <= 0f) {
                        descOffsetX = 0f;
                        scrollForward = true;
                        edgePauseTimer = 0.8f;
                    }
                }
            }
        } else {
            descOffsetX = 0f;
            scrollForward = true;
            edgePauseTimer = 0f;
        }

        DrawContext dc = context.getContext();

        int sx = Math.round(getX() + 1);
        int sy = Math.round(getY());
        int sw = 99;
        int sh = 22;

        dc.enableScissor(sx, sy, sx + sw, sy + sh);
        context.drawText(desc, Fonts.sf_pro, textX - descOffsetX, textY, 5.5f,
                ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue() * textAlpha)));
        String bindLabel;
        if (CsGui.binding == parent) {
            bindLabel = "...";
        } else {
            int k = parent.getKey();
            boolean unbound = (k == GLFW.GLFW_KEY_UNKNOWN || k == -1 || k == -222);
            bindLabel = unbound ? "-" : Keyboard.getKeyName(k);
        }
        float bindSize = 6f;
        float bindWidth = Fonts.sf_pro.getWidth(bindLabel, bindSize);
        float bindX = getX() + 101f - 6f - bindWidth;
        float bindY = getY() + 6.5f;
        context.drawText(bindLabel, Fonts.sf_pro, bindX, bindY, bindSize,
                ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue() * textAlpha)));
        dc.disableScissor();

        if (expand.getValue() > 0f) {
            int clipH = Math.max(0, Math.round(expandedHeight));
            if (clipH > 0) {
                int sx2 = Math.round(getX());
                int sy2 = Math.round(getY() + 22f);
                int sw2 = 101;
                int sh2 = Math.max(0, clipH);

                DrawContext dc2 = context.getContext();
                dc2.enableScissor(sx2, sy2, sx2 + sw2, sy2 + sh2);

                if (sh2 > 0) {
                    ColorRGBA shade = ColorRGBA.of(0, 0, 0, (int) (40 * CsGui.alpha.getValue() * expand.getValue()));
                    float shadeX = getX();
                    float shadeY = getY() + 22f;
                    float shadeW = 101f;
                    float shadeH = sh2;
                    context.drawRect(shadeX, shadeY, shadeW, shadeH, 0f, 6f, 6f, 0f, shade);
                }

                ColorRGBA sep = ColorRGBA.of(0, 0, 0, (int) (140 * CsGui.alpha.getValue() * expand.getValue()));

                float settingY = getY() + 22f - smoothScrollOffset;
                int visibleCount = 0;
                for (int i = 0; i < settings.size(); i++) {
                    SettingComponent setting = settings.get(i);
                    if (!setting.getSetting().isVisible()) continue;
                    
                    setting.setY(settingY);
                    setting.setX(getX());
                    setting.render(context);
                    
                    if (i < settings.size() - 1) {
                        context.drawRect(getX() + 1f, settingY + setting.getHeight(), 99, 1, 0f, sep);
                    }
                    settingY += setting.getHeight();
                    visibleCount++;
                }

                // Рисуем скроллбар если нужен скролл
                if (maxScroll > 0 && totalSettingsHeight > MAX_SETTINGS_HEIGHT) {
                    float scrollbarHeight = Math.max(20, (displayHeight / totalSettingsHeight) * displayHeight);
                    float scrollPercent = smoothScrollOffset / maxScroll;
                    float scrollbarY = getY() + 22f + (scrollPercent * (displayHeight - scrollbarHeight));
                    
                    float scrollbarX = getX() + 101f - 4f;
                    float scrollbarWidth = 2f;
                    
                    // Фон скроллбара
                    context.drawRect(scrollbarX, getY() + 22f, scrollbarWidth, displayHeight, 1f, 
                        ColorRGBA.of(30, 30, 30, (int) (100 * CsGui.alpha.getValue() * expand.getValue())));
                    
                    // Сам скроллбар
                    context.drawRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 1f, 
                        ColorRGBA.of(100, 100, 100, (int) (180 * CsGui.alpha.getValue() * expand.getValue())));
                }

                dc2.disableScissor();
            }
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, 101, 22)) {
            switch (button) {
                case 0 -> parent.cToggle();
                case 1 -> expanded = !expanded;
                case 2 -> CsGui.binding = parent;
            }
        }

        if (expanded) {
            for (SettingComponent setting : settings) {
                setting.click(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void moved(double mouseX, double mouseY) {
        canScroll = isHovered(mouseX, mouseY, 101, 22);
        if (expanded) {
            for (SettingComponent setting : settings) {
                setting.moved(mouseX, mouseY);
            }
        }
    }

    @Override
    public void dragged(double mouseX, double mouseY, double dX, double dY, int button) {
        if (expanded) {
            for (SettingComponent setting : settings) {
                setting.dragged(mouseX, mouseY, dX, dY, button);
            }
        }
    }

    @Override
    public void mReleased(double mouseX, double mouseY, int button) {
        if (expanded) {
            for (SettingComponent setting : settings) {
                setting.mReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void key(int button) {
        if (expanded) {
            for (SettingComponent setting : settings) {
                setting.key(button);
            }
        }
    }

    @Override
    public void type(char chr) {
        if (expanded) {
            for (SettingComponent setting : settings) {
                setting.type(chr);
            }
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double amount) {
        if (expanded && isHovered(mouseX, mouseY, 101, getTotalHeight())) {
            scrollOffset -= (float) amount * 20f;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        }
    }

    public float getTotalHeight() {
        float height = 22;
        if (expand.getValue() > 0.01) {
            float totalSettingsHeight = 0f;
            for (SettingComponent setting : settings) {
                if (setting.getSetting().isVisible()) {
                    totalSettingsHeight += setting.getHeight();
                }
            }
            float displayHeight = Math.min(totalSettingsHeight, MAX_SETTINGS_HEIGHT);
            height += displayHeight * expand.getValue();
        }
        return height;
    }
}
