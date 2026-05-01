package code.essence.display.screens.autobuy;

import code.essence.common.animation.Easy.Direction;
import code.essence.common.animation.Easy.EaseBackIn;
import code.essence.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import code.essence.Essence;
import code.essence.features.impl.render.Hud;
import code.essence.features.module.setting.implement.BooleanSetting;
import code.essence.features.module.setting.implement.SliderSettings;
import code.essence.utils.display.color.ColorAssist;
import code.essence.utils.display.render.font.Fonts;
import code.essence.utils.display.render.geometry.Render2D;
import code.essence.utils.display.interfaces.QuickImports;
import code.essence.utils.display.scissor.ScissorAssist;
import code.essence.utils.display.render.shape.ShapeProperties;
import code.essence.utils.display.render.post.KawaseBlur;
import code.essence.utils.display.render.shape.implement.Rectangle;
import code.essence.utils.math.calc.Calculate;
import code.essence.utils.theme.ThemeManager;
import code.essence.utils.animation.AnimationHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Map;
import java.util.function.Function;

import static code.essence.utils.display.render.font.Fonts.Type.SuisseIntlSemiBold;


public class AutoBuySettingsPanelRender implements QuickImports {
    private final Rectangle rectangle = new Rectangle();
    private final EaseBackIn animation;
    
    
    private float panelX, panelY, panelWidth, panelHeight;
    
    
    private final Function<AutoBuyableItem, BooleanSetting> getParserSetting;
    private final Function<AutoBuyableItem, SliderSettings> getDurabilitySetting;
    private final Map<AutoBuyableItem, Float> sliderScrollPosition;
    
    public AutoBuySettingsPanelRender(
            EaseBackIn animation,
            Function<AutoBuyableItem, BooleanSetting> getParserSetting,
            Function<AutoBuyableItem, SliderSettings> getDurabilitySetting,
            Map<AutoBuyableItem, Float> sliderScrollPosition) {
        this.animation = animation;
        this.getParserSetting = getParserSetting;
        this.getDurabilitySetting = getDurabilitySetting;
        this.sliderScrollPosition = sliderScrollPosition;
    }
    
    
    public boolean render(
            DrawContext context,
            MatrixStack matrix,
            int mouseX,
            int mouseY,
            float delta,
            AutoBuyableItem settingsItem,
            AutoBuyableItem settingsAnimatedItem,
            float mainGuiX,
            float mainGuiY,
            float mainGuiWidth,
            float mainGuiHeight) {
        
        float animationProgress = Math.min(1.0f, Math.max(0.0f, (float) animation.getOutput()));
        
        
        if (settingsItem == null && animation.finished(Direction.BACKWARDS)) {
            return true; 
        }
        
        
        AutoBuyableItem renderItem = settingsItem != null ? settingsItem : settingsAnimatedItem;
        if (renderItem == null) {
            return false;
        }
        
        
        panelWidth = mainGuiWidth / 2.2f;
        
        
        ItemStack itemStack = renderItem.createItemStack();
        boolean hasDurability = itemStack != null && itemStack.isDamageable();
        
        
        panelHeight = hasDurability ? mainGuiHeight / 3.8f : mainGuiHeight / 5.5f;
        
        
        float spacing = 10f;
        float basePanelX = mainGuiX + mainGuiWidth + spacing;
        panelY = mainGuiY;
        panelX = basePanelX;
        
        
        float centerX = panelX + panelWidth / 2f;
        float centerY = panelY + panelHeight / 2f;
        matrix.push();
        matrix.translate(centerX, centerY, 0);
        matrix.scale(animationProgress, animationProgress, 1);
        matrix.translate(-centerX, -centerY, 0);


        if (Hud.blur.isValue()) {
            Render2D.rectangleWithMask(matrix.peek().getPositionMatrix(), panelX, panelY, panelWidth, panelHeight, 6f,0, KawaseBlur.INSTANCE.fbos.getFirst().getColorAttachment());

        }
        
        
        rectangle.render(ShapeProperties.create(matrix, panelX, panelY, panelWidth, panelHeight)
                .round(6)
                .color(ThemeManager.BackgroundGui.getColor())
                .build());
        
        
        renderCategoryHeader(context, matrix, animationProgress);
        
        
        renderSettings(context, matrix, mouseX, mouseY, delta, renderItem, animationProgress, hasDurability, itemStack);
        
        matrix.pop(); 
        
        return false; 
    }
    
    private void renderCategoryHeader(DrawContext context, MatrixStack matrix, float animationProgress) {
        float categoryWidth = panelWidth - 10;
        float categoryX = panelX + 5;
        float categoryY = panelY + 4;
        
        matrix.push();
        matrix.translate(0, 0, 200);
        
        int categoryBgColor = Calculate.applyOpacity(ColorAssist.getClientColor(1f), (int)(255 * animationProgress));
        rectangle.render(ShapeProperties.create(matrix, categoryX, categoryY, categoryWidth, 22)
                .round(4f)
                .softness(4)
                .color(categoryBgColor, categoryBgColor,
                        Calculate.applyOpacity(ColorAssist.getClientColor2(1f), (int)(255 * animationProgress)),
                        Calculate.applyOpacity(ColorAssist.getClientColor2(1f), (int)(255 * animationProgress)))
                .build());
        
        rectangle.render(ShapeProperties.create(matrix, categoryX - 4, categoryY - 4, categoryWidth + 8, 30)
                .round(8f)
                .softness(4)
                .color(Calculate.applyOpacity(new Color(255, 255, 255, 5).getRGB(), (int)(255 * animationProgress)))
                .build());
        
        int categoryBgColor2 = Calculate.applyOpacity(ColorAssist.getClientColor(0.1f), (int)(255 * animationProgress));
        rectangle.render(ShapeProperties.create(matrix, categoryX - 3, categoryY - 3, categoryWidth + 6, 28)
                .round(7f)
                .softness(3)
                .color(categoryBgColor2, categoryBgColor2,
                        Calculate.applyOpacity(ColorAssist.getClientColor2(0.1f), (int)(255 * animationProgress)),
                        Calculate.applyOpacity(ColorAssist.getClientColor2(0.1f), (int)(255 * animationProgress)))
                .build());
        
        rectangle.render(ShapeProperties.create(matrix, categoryX - 2, categoryY - 2, categoryWidth + 4, 26)
                .round(6f)
                .softness(2)
                .color(categoryBgColor2, categoryBgColor2,
                        Calculate.applyOpacity(ColorAssist.getClientColor2(0.1f), (int)(255 * animationProgress)),
                        Calculate.applyOpacity(ColorAssist.getClientColor2(0.1f), (int)(255 * animationProgress)))
                .build());
        
        int titleColor = Calculate.applyOpacity(ThemeManager.textColor.getColor(), (int)(255 * animationProgress));
        Fonts.getSize(18, SuisseIntlSemiBold).drawString(context.getMatrices(), "Настройки", categoryX + 5, categoryY + 8, titleColor);
        
        matrix.pop();
    }
    
    private void renderSettings(
            DrawContext context,
            MatrixStack matrix,
            int mouseX,
            int mouseY,
            float delta,
            AutoBuyableItem renderItem,
            float animationProgress,
            boolean hasDurability,
            ItemStack itemStack) {
        
        float categoryY = panelY + 4;
        float settingsStartY = categoryY + 22 + 8;
        float settingsX = panelX + 5;
        float settingsY = settingsStartY;
        float settingsWidth = panelWidth - 10;
        float settingSpacing = 10;
        
        
        int settingsAlpha = animationProgress < 1.0f ? (int)(255 * animationProgress) : 255;
        
        
        BooleanSetting parserSetting = getParserSetting.apply(renderItem);
        renderParserCheckbox(matrix, settingsX, settingsY, settingsWidth, parserSetting, renderItem, settingsAlpha);
        
        
        if (hasDurability && itemStack != null && itemStack.isDamageable()) {
            settingsY += 15 + settingSpacing;
            SliderSettings durabilitySetting = getDurabilitySetting.apply(renderItem);
            renderDurabilitySlider(matrix, mouseX, mouseY, delta, settingsX, settingsY, settingsWidth, durabilitySetting, renderItem, settingsAlpha);
        }
    }
    
    private void renderParserCheckbox(
            MatrixStack matrix,
            float settingsX,
            float settingsY,
            float settingsWidth,
            BooleanSetting parserSetting,
            AutoBuyableItem renderItem,
            int settingsAlpha) {
        
        
        int textColor = Calculate.applyOpacity(ThemeManager.textColor.getColor(), settingsAlpha);
        Fonts.getSize(15, SuisseIntlSemiBold).drawString(matrix, parserSetting.getName(), settingsX + 5, settingsY + 5, textColor);
        
        
        float checkboxX = settingsX + settingsWidth - 12.5f;
        float checkboxY = settingsY + 3f;
        boolean checkboxState = parserSetting.isValue();
        
        String toggleAnimationKey = "autobuy_checkbox_" + System.identityHashCode(renderItem);
        float checkboxScale = AnimationHelper.getAnimationValue(toggleAnimationKey, 1f);
        
        if (checkboxState) {
            int checkboxColor1 = Calculate.applyOpacity(ColorAssist.getClientColor(), settingsAlpha);
            int checkboxColor2 = Calculate.applyOpacity(ColorAssist.getClientColor2(), settingsAlpha);
            rectangle.render(ShapeProperties.create(matrix, checkboxX, checkboxY, 7 * checkboxScale, 7 * checkboxScale)
                    .round(1.5f)
                    .color(checkboxColor1, checkboxColor2, checkboxColor1, checkboxColor2)
                    .build());
            Fonts.getSize(13, Fonts.Type.ESSENCE).drawString(matrix, "k", checkboxX + .2f, checkboxY + 2.9f, textColor);
        } else {
            int outlineColor = Calculate.applyOpacity(new Color(57, 57, 60, 255).getRGB(), settingsAlpha);
            int bgColor = Calculate.applyOpacity(new Color(32, 32, 35, 40).getRGB(), settingsAlpha);
            rectangle.render(ShapeProperties.create(matrix, checkboxX, checkboxY - 0.25, 7 * checkboxScale, 7 * checkboxScale)
                    .round(1.5f)
                    .softness(1)
                    .thickness(2)
                    .outlineColor(outlineColor)
                    .color(bgColor)
                    .build());
        }
    }
    
    private void renderDurabilitySlider(
            MatrixStack matrix,
            int mouseX,
            int mouseY,
            float delta,
            float settingsX,
            float settingsY,
            float settingsWidth,
            SliderSettings durabilitySetting,
            AutoBuyableItem renderItem,
            int settingsAlpha) {
        
        
        String value = String.valueOf((int) durabilitySetting.getValue());
        float valueWidth = Fonts.getSize(13, SuisseIntlSemiBold).getStringWidth(value);
        float valueX = settingsX + settingsWidth - valueWidth - 5;
        
        
        float nameStartX = settingsX + 5;
        float nameMaxWidth = valueX - nameStartX - 4;
        
        var nameFont = Fonts.getSize(15, SuisseIntlSemiBold);
        String name = durabilitySetting.getName();
        float nameWidth = nameFont.getStringWidth(name);
        
        
        float scrollPosition = sliderScrollPosition.getOrDefault(renderItem, 0f);
        int textColor = Calculate.applyOpacity(ThemeManager.textColor.getColor(), settingsAlpha);
        
        if (nameWidth > nameMaxWidth && nameMaxWidth > 0) {
            ScissorAssist scissorManager = Essence.getInstance().getScissorManager();
            
            String separation = "  ";
            String scrollingText = name + separation + name;
            float scrollingTextWidth = nameFont.getStringWidth(scrollingText);
            float separationWidth = nameFont.getStringWidth(separation);
            float scrollCycle = nameWidth + separationWidth;
            
            
            boolean isHovered = Calculate.isHovered(mouseX, mouseY, nameStartX, settingsY - 5, nameMaxWidth, 15);
            
            if (isHovered) {
                float scrollSpeed = 2.0f;
                scrollPosition += scrollSpeed * delta;
                scrollPosition = scrollPosition % scrollCycle;
                sliderScrollPosition.put(renderItem, scrollPosition);
            } else {
                scrollPosition = 0f;
                sliderScrollPosition.put(renderItem, 0f);
            }
            
            float textStartX = nameStartX - scrollPosition;
            float clipX = nameStartX;
            float clipY = settingsY - 15;
            float clipWidth = nameMaxWidth;
            float clipHeight = 20;
            
            if (clipWidth > 0 && clipHeight > 0) {
                scissorManager.push(matrix.peek().getPositionMatrix(), clipX, clipY, clipWidth, clipHeight);
                nameFont.drawString(matrix, scrollingText, textStartX, settingsY - 5, textColor);
                scissorManager.pop();
            }
        } else {
            scrollPosition = 0f;
            sliderScrollPosition.put(renderItem, 0f);
            if (nameWidth <= nameMaxWidth) {
                nameFont.drawString(matrix, name, nameStartX, settingsY - 5, textColor);
            }
        }
        
        
        int rainbowColor1 = Calculate.applyOpacity(ColorAssist.getClientColor(), settingsAlpha);
        int rainbowColor2 = Calculate.applyOpacity(ColorAssist.getClientColor2(), settingsAlpha);
        Fonts.getSize(13, SuisseIntlSemiBold).drawRainbowString(matrix, value, valueX, settingsY - 4.5, rainbowColor1, rainbowColor2);
        
        
        float sliderY = settingsY + 12;
        float sliderWidth = settingsWidth - 10;
        float sliderHeight = 3;
        
        int sliderBgColor = Calculate.applyOpacity(new Color(40, 40, 45, 255).getRGB(), settingsAlpha);
        rectangle.render(ShapeProperties.create(matrix, settingsX + 5, sliderY - 6, sliderWidth, sliderHeight)
                .round(2)
                .color(sliderBgColor)
                .build());
        
        float progress = (durabilitySetting.getValue() - durabilitySetting.getMin()) / (durabilitySetting.getMax() - durabilitySetting.getMin());
        float progressWidth = MathHelper.clamp(sliderWidth * progress, 0, sliderWidth);
        
        
        long time = System.currentTimeMillis();
        float rainbowOffset = (time % 2000) / 2000.0f;
        
        int baseColor1 = ColorAssist.getClientColor();
        int baseColor2 = ColorAssist.getClientColor2();
        
        
        int animatedColor1 = ColorAssist.interpolateColor(baseColor1, baseColor2, rainbowOffset);
        int animatedColor2 = ColorAssist.interpolateColor(baseColor2, baseColor1, rainbowOffset);
        
        
        int animatedColor1Alpha = Calculate.applyOpacity(animatedColor1, settingsAlpha);
        int animatedColor2Alpha = Calculate.applyOpacity(animatedColor2, settingsAlpha);
        
        rectangle.render(ShapeProperties.create(matrix, settingsX + 5, sliderY - 6, progressWidth, sliderHeight)
                .round(2)
                .color(animatedColor1Alpha, animatedColor2Alpha, animatedColor1Alpha, animatedColor2Alpha)
                .build());
        
        
        float sliderHandleX = MathHelper.clamp(settingsX + 5 + progressWidth - 3, settingsX + 5, settingsX + 5 + sliderWidth - 3);
        rectangle.render(ShapeProperties.create(matrix, sliderHandleX, sliderY - 7.2f, 5, 5)
                .round(3)
                .color(textColor)
                .build());
    }
    
    
    public float getPanelX() { return panelX; }
    public float getPanelY() { return panelY; }
    public float getPanelWidth() { return panelWidth; }
    public float getPanelHeight() { return panelHeight; }
}

