package itz.silentcore.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import itz.silentcore.utils.client.ClientUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static itz.silentcore.utils.render.Fonts.sf_pro;

@Mixin(InGameHud.class)
public class HotbarMixin {

    private float smoothedSelectedSlot = 0f;
    private int lastSelectedSlot = 0;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        PlayerEntity player = mc.player;
        RenderContext renderContext = new RenderContext(context);

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        float slotWidth = 20f;
        float slotHeight = 20f;
        float gap = 3f;
        float radius = 3f;
        float textSize = 6f;

        float totalWidth = slotWidth * 9 + gap * 8;
        float startX = screenWidth / 2f - totalWidth / 2f;
        float startY = screenHeight - 25f;
        float paddingX = 4f;
        float paddingY = 4f;

        float bgX = startX - paddingX;
        float bgY = startY - paddingY;
        float bgWidth = totalWidth + paddingX * 2;
        float bgHeight = slotHeight + paddingY * 2;

        renderContext.drawBlur(bgX, bgY, bgWidth, bgHeight, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        renderContext.drawRect(bgX, bgY, bgWidth, bgHeight, radius, ColorRGBA.of(0, 0, 0, 166));
        renderContext.drawBorder(bgX, bgY, bgWidth, bgHeight, radius, 0.5f, ColorRGBA.of(0, 0, 0, 166));

        net.minecraft.entity.player.PlayerInventory inventory = player.getInventory();
        int selectedSlot = inventory.selectedSlot;

        float smoothing = 0.2f;
        smoothedSelectedSlot += (selectedSlot - smoothedSelectedSlot) * smoothing;

        float selectedSlotX = startX + smoothedSelectedSlot * (slotWidth + gap) - 2;
        float selectedSlotY = startY - 2;

        ColorRGBA themeColor = ClientUtility.getThemePrimaryColorRGBA();
        renderContext.drawBorder(selectedSlotX, selectedSlotY, slotWidth + 4, slotHeight + 4, radius, 1.0f, themeColor);

        for (int i = 0; i < 9; i++) {
            float slotX = startX + i * (slotWidth + gap);
            float slotY = startY;

            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                context.drawItem(itemStack, (int)slotX + 2, (int)slotY + 2);
            }

            if (itemStack.getCount() > 1) {
                String count = String.valueOf(itemStack.getCount());
                renderContext.drawText(count, sf_pro, slotX + 10f, slotY + 10f, 7.5f, ColorRGBA.of(255, 255, 255));
            }
        }

        float offHandSlotSize = 20f;
        float offHandSlotX = startX - offHandSlotSize - gap - paddingX * 2;
        float offHandSlotY = startY;

        float offHandBgX = offHandSlotX - paddingX;
        float offHandBgY = offHandSlotY - paddingY;
        float offHandBgWidth = offHandSlotSize + paddingX * 2;
        float offHandBgHeight = offHandSlotSize + paddingY * 2;

        renderContext.drawBlur(offHandBgX, offHandBgY, offHandBgWidth, offHandBgHeight, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        renderContext.drawRect(offHandBgX, offHandBgY, offHandBgWidth, offHandBgHeight, radius, ColorRGBA.of(0, 0, 0, 166));
        renderContext.drawBorder(offHandBgX, offHandBgY, offHandBgWidth, offHandBgHeight, radius, 0.5f, ColorRGBA.of(0, 0, 0, 166));

        ItemStack offHandStack = inventory.getStack(40);
        if (!offHandStack.isEmpty()) {
            context.drawItem(offHandStack, (int)offHandSlotX + 2, (int)offHandSlotY + 2);
        }

        if (offHandStack.getCount() > 1) {
            String count = String.valueOf(offHandStack.getCount());
            renderContext.drawText(count, sf_pro, offHandSlotX + 10f, offHandSlotY + 10f, 7.5f, ColorRGBA.of(255, 255, 255));
        }

        ci.cancel();
    }
}
