package itz.silentcore.utils.inventory;

import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class InventoryToolkit implements IMinecraft {
    
    public static InventoryResult findItemInHotBar(Item item) {
        if (mc.player == null) return InventoryResult.notFound();
        
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return InventoryResult.of(i);
            }
        }
        return InventoryResult.notFound();
    }

    public static InventoryResult findItemInInventory(Item item) {
        if (mc.player == null) return InventoryResult.notFound();
        
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return InventoryResult.of(i);
            }
        }
        return InventoryResult.notFound();
    }

    public static void switchTo(int slot) {
        if (mc.player == null) return;
        mc.player.getInventory().selectedSlot = slot;
    }

    public static void clickSlot(int slot, int button, SlotActionType actionType) {
        if (mc.player == null || mc.interactionManager == null) return;
        
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            slot,
            button,
            actionType,
            mc.player
        );
    }

    public static void sendPacket(PlayerInteractItemC2SPacket packet) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }
}
