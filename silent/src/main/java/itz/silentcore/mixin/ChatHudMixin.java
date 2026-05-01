package itz.silentcore.mixin;

import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ObjectShare;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Shadow private int scrolledLines;
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow private boolean hasUnreadNewMessages;
    @Shadow @Final private MinecraftClient client;

    @Shadow private static double getMessageOpacityMultiplier(int age) { return 0; }
    @Shadow private boolean isChatHidden() { return false; }
    @Shadow private int getLineHeight() { return 0; }
    @Shadow private boolean isChatFocused() { return false; }
    @Shadow public int getVisibleLineCount() { return 0; }
    @Shadow public double getChatScale() { return 0; }
    @Shadow public int getWidth() { return 0; }

    @Unique private final Animation messageAnimation = createMessageAnimation();
    @Unique private final float fadeOffsetYScale = 0.8f;
    @Unique private int chatDisplacementY = 0;

    @Unique
    private static Animation createMessageAnimation() {
        Animation animation = new Animation(250, Easing.BAKEK_SIZE);
        animation.reset(1.0f);
        return animation;
    }

    @Unique
    private void calculateYOffset() {
        if (this.scrolledLines != 0) {
            chatDisplacementY = 0;
            return;
        }

        int lineHeight = this.getLineHeight();
        if (lineHeight <= 0) {
            chatDisplacementY = 0;
            return;
        }

        float maxDisplacement = lineHeight * fadeOffsetYScale;
        float progress = Math.min(Math.max(messageAnimation.getValue(), 0.0f), 1.0f);

        if (progress >= 0.999f) {
            chatDisplacementY = 0;
            return;
        }

        chatDisplacementY = (int) (maxDisplacement * (1.0f - progress));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        calculateYOffset();

        float raisedOffset = 0;
        ObjectShare share = FabricLoader.getInstance().getObjectShare();
        if (share.get("raised:hud") instanceof Integer distance) {
            raisedOffset -= distance;
        } else if (share.get("raised:distance") instanceof Integer distance) {
            raisedOffset -= distance;
        }

        context.getMatrices().translate(0, chatDisplacementY + raisedOffset, 0);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        float raisedOffset = 0;
        ObjectShare share = FabricLoader.getInstance().getObjectShare();
        if (share.get("raised:hud") instanceof Integer distance) {
            raisedOffset -= distance;
        } else if (share.get("raised:distance") instanceof Integer distance) {
            raisedOffset -= distance;
        }

        context.getMatrices().translate(0, -(chatDisplacementY + raisedOffset), 0);
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("TAIL"))
    private void addMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        messageAnimation.reset(0.0f);
        messageAnimation.animate(1.0f);
    }
}