package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.api.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DrawContext.class)
public abstract class TextMixin {

    @ModifyVariable(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), ordinal = 0)
    private String modifyStringText(String text) {
        try {
            if (!isAntiFTEnabled() || text == null) {
                return text;
            }

            String modified = replaceAntiFTText(text);
            return modified != null ? modified : text;
        } catch (Exception e) {
            return text;
        }
    }

    @ModifyVariable(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I", at = @At("HEAD"), ordinal = 0)
    private Text modifyTextObject(Text text) {
        try {
            if (!isAntiFTEnabled() || text == null) {
                return text;
            }

            String textString = text.getString();
            String modified = replaceAntiFTText(textString);

            if (!textString.equals(modified)) {
                return Text.literal(modified);
            }
            return text;
        } catch (Exception e) {
            return text;
        }
    }

    @Unique
    private boolean isAntiFTEnabled() {
        try {
            Module antiFT = SilentCore.getInstance().moduleManager.getModule("AntiFT");
            return antiFT != null && antiFT.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    @Unique
    private String replaceAntiFTText(String text) {
        try {
            if (text == null) {
                return null;
            }
            text = text.replace("funtime", "SilentCore");
            text = text.replace("Funtime", "SilentCore");
            text = text.replace("FUNTIME", "silentcore");
            text = text.replace("mc.funtime.su", "silentcoredlc.lol");
            text = text.replace("MC.FUNTIME.SU", "silentcoreDLC.LOL");
            return text;
        } catch (Exception e) {
            return text;
        }
    }
}
