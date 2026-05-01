package itz.silentcore.utils.inventory;

import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

import java.util.List;

public class InventoryTask implements IMinecraft {
    
    public static void swapAndUse(Item item) {
        if (mc.player == null) return;
        
        InventoryResult result = InventoryToolkit.findItemInHotBar(item);
        if (result.found()) {
            int savedSlot = mc.player.getInventory().selectedSlot;
            InventoryToolkit.switchTo(result.slot());
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            InventoryToolkit.switchTo(savedSlot);
        }
    }

    public static Slot getSlot(Item item) {
        if (mc.player == null) return null;
        
        for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot.getStack().getItem() == item) {
                return slot;
            }
        }
        return null;
    }

    public static Slot getSlot(List<Item> items) {
        if (mc.player == null) return null;
        
        for (Item item : items) {
            Slot slot = getSlot(item);
            if (slot != null) return slot;
        }
        return null;
    }

    public static void moveItem(Slot slot, int targetSlot, boolean quickMove, boolean instant) {
        if (mc.player == null || mc.interactionManager == null || slot == null) return;
        
        if (quickMove) {
            mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slot.id,
                0,
                SlotActionType.QUICK_MOVE,
                mc.player
            );
        } else {
            InventoryToolkit.clickSlot(slot.id, targetSlot, SlotActionType.SWAP);
        }
    }
}
