package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.HandledScreenEvent;
import itz.silentcore.feature.event.impl.PacketEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.ColorSetting;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.utils.auction.PriceParser;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.ColorUtils;
import itz.silentcore.utils.render.draw.RectangleRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.Slot;

import java.util.Comparator;
import java.util.List;

@ModuleAnnotation(name = "AuctionHelper", category = Category.RENDER, description = "Highlights best auction deals")
public class AuctionHelper extends Module implements IMinecraft {
    
    private final PriceParser auctionPriceParser = new PriceParser();
    private Slot cheapestSlot, costEffectiveSlot;
    private int tickDelay = 0;
    
    private final ColorSetting cheapestColor = new ColorSetting("Cheapest Color",
            new ColorRGBA(75, 255, 75, 255),
            () -> false,
            () -> new ColorRGBA(75, 255, 75, 255)
    );
    
    private final ColorSetting costEffectiveColor = new ColorSetting("Cost Effective Color",
            new ColorRGBA(255, 75, 75, 255),
            () -> false,
            () -> new ColorRGBA(255, 75, 75, 255)
    );
    
    @Subscribe
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket) {
            tickDelay = 1;
        }
    }
    
    @Subscribe
    public void onTick(TickEvent event) {
        if (tickDelay > 0) {
            tickDelay--;
            if (tickDelay == 0 && mc.currentScreen instanceof GenericContainerScreen screen) {
                cheapestSlot = findSlotWithLowestPrice(screen.getScreenHandler().slots);
                costEffectiveSlot = findSlotWithBestPricePerItem(screen.getScreenHandler().slots);
            }
        }
    }
    
    @Subscribe
    public void onHandledScreen(HandledScreenEvent event) {
        DrawContext context = event.getDrawContext();
        MatrixStack matrix = context.getMatrices();
        
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            int offsetX = (screen.width - event.getBackgroundWidth()) / 2;
            int offsetY = (screen.height - event.getBackgroundHeight()) / 2;
            
            int cheapItemColor = getBlinkingColor(cheapestColor.getIntColor());
            int costEffectiveItemColor = getBlinkingColor(costEffectiveColor.getIntColor());
            
            matrix.push();
            matrix.translate(offsetX, offsetY, 0);
            
            if (cheapestSlot != costEffectiveSlot) {
                highlightSlot(matrix, cheapestSlot, cheapItemColor);
            }
            highlightSlot(matrix, costEffectiveSlot, costEffectiveItemColor);
            
            matrix.pop();
        }
    }
    
    private int getBlinkingColor(int color) {
        float alpha = (float) Math.abs(Math.sin((double) System.currentTimeMillis() / 10 * Math.PI / 180));
        return ColorUtils.multiplyAlpha(color, alpha);
    }
    
    private Slot findSlotWithLowestPrice(List<Slot> slots) {
        return slots.stream()
                .filter(this::hasValidPrice)
                .min(Comparator.comparingInt(slot -> auctionPriceParser.getPrice(slot.getStack())))
                .orElse(null);
    }
    
    private Slot findSlotWithBestPricePerItem(List<Slot> slots) {
        return slots.stream()
                .filter(this::isValidMultiItemSlot)
                .min(Comparator.comparingInt(slot -> auctionPriceParser.getPrice(slot.getStack()) / slot.getStack().getCount()))
                .orElse(null);
    }
    
    private boolean hasValidPrice(Slot slot) {
        return auctionPriceParser.getPrice(slot.getStack()) >= 0;
    }
    
    private boolean isValidMultiItemSlot(Slot slot) {
        return hasValidPrice(slot) && slot.getStack().getCount() > 1;
    }
    
    private void highlightSlot(MatrixStack matrix, Slot slot, int color) {
        if (slot != null) {
            RectangleRenderer.draw(
                    matrix.peek().getPositionMatrix(),
                    slot.x,
                    slot.y,
                    0,
                    16,
                    16,
                    2,
                    2,
                    2,
                    2,
                    color,
                    color,
                    color,
                    color,
                    0.6f
            );
        }
    }
}
