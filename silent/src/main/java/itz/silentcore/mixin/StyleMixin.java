package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.impl.ThemeCommand;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin {

    @Inject(method = "getColor()Lnet/minecraft/text/TextColor;", at = @At("RETURN"), cancellable = true)
    private void modifyGetColor(CallbackInfoReturnable<TextColor> cir) {
        if (!RenderContext.isTabOrScoreboard() || !isAnParserEnabled()) {
            return;
        }

        TextColor originalColor = cir.getReturnValue();
        if (originalColor == null) {
            return;
        }

        int rgb = originalColor.getRgb();

        if (rgb != 0xFFFFFF
                && !originalColor.equals(TextColor.fromFormatting(Formatting.RESET))
                && rgb != 0x7F7F7F
                && rgb != 0x3F3F3F
                && !isFormattingExcluded(originalColor)) {
            int themeColor = getThemeColor1();
            cir.setReturnValue(TextColor.fromRgb(themeColor));
        }
    }

    @Inject(method = "getShadowColor()Ljava/lang/Integer;", at = @At("RETURN"), cancellable = true)
    private void modifyShadowColor(CallbackInfoReturnable<Integer> cir) {
        if (!RenderContext.isTabOrScoreboard() || !isAnParserEnabled()) {
            return;
        }

        int shadowColor = getThemeColor2();
        cir.setReturnValue(shadowColor);
    }

    @Unique
    private boolean isAnParserEnabled() {
        try {
            Module anParser = SilentCore.getInstance().moduleManager.getModule("AnParser");
            return anParser != null && anParser.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    @Unique
    private boolean isFormattingExcluded(TextColor color) {
        return color.equals(TextColor.fromFormatting(Formatting.OBFUSCATED))
                || color.equals(TextColor.fromFormatting(Formatting.BOLD))
                || color.equals(TextColor.fromFormatting(Formatting.ITALIC));
    }

    @Unique
    private int getThemeColor1() {
        try {
            String currentTheme = ClientUtility.getCurrentTheme();
            String[] colors = ThemeCommand.getThemeColors(currentTheme);
            if (colors != null && colors.length >= 1) {
                String hexColor = colors[0];
                if (hexColor.startsWith("#")) {
                    return Integer.parseInt(hexColor.substring(1), 16);
                } else {
                    return Integer.parseInt(hexColor, 16);
                }
            }
        } catch (Exception ignored) {
        }
        return 0xFF00FF;
    }

    @Unique
    private int getThemeColor2() {
        try {
            String currentTheme = ClientUtility.getCurrentTheme();
            String[] colors = ThemeCommand.getThemeColors(currentTheme);
            if (colors != null && colors.length >= 2) {
                String hexColor = colors[1];
                if (hexColor.startsWith("#")) {
                    return Integer.parseInt(hexColor.substring(1), 16);
                } else {
                    return Integer.parseInt(hexColor, 16);
                }
            } else if (colors != null && colors.length == 1) {
                String hexColor = colors[0];
                if (hexColor.startsWith("#")) {
                    return Integer.parseInt(hexColor.substring(1), 16);
                } else {
                    return Integer.parseInt(hexColor, 16);
                }
            }
        } catch (Exception ignored) {
        }
        return 0x000000;
    }
}