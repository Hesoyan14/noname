package itz.silentcore.feature.module.impl.combat;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

@ModuleAnnotation(name = "AutoTotem", category = Category.COMBAT, description = "Автоматически экипирует тотем")
public class AutoTotem extends Module {

    private final NumberSetting healthThreshold = new NumberSetting("Порог здоровья", 4.5f, 1.0f, 20.0f, 0.5f);
    private final BooleanSetting fallCheck = new BooleanSetting("Проверка падения", true);
    
    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        
        // Проверка нужно ли экипировать тотем
        if (!shouldEquipTotem()) return;
        
        // Если тотем уже в оффхенде - ничего не делаем
        if (isTotemInOffhand()) return;
        
        // Ищем тотем в инвентаре
        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;
        
        // Экипируем тотем
        equipTotem(totemSlot);
    }

    private boolean shouldEquipTotem() {
        float health = mc.player.getHealth();
        
        // Проверка здоровья
        if (health <= healthThreshold.getCurrent()) return true;
        
        // Проверка падения
        if (fallCheck.isEnabled() && mc.player.fallDistance > 10) return true;
        
        return false;
    }

    private boolean isTotemInOffhand() {
        ItemStack offhandStack = mc.player.getOffHandStack();
        return offhandStack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    private int findTotemSlot() {
        // Проверяем хотбар
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
        // Проверяем остальной инвентарь
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
        return -1;
    }

    private void equipTotem(int slot) {
        if (mc.interactionManager == null || mc.player.playerScreenHandler == null) return;
        
        // Конвертируем слот для серверного инвентаря
        int serverSlot = slot;
        if (slot >= 0 && slot <= 8) {
            serverSlot = slot + 36;
        }
        
        // Свапаем с оффхендом (слот 45)
        mc.interactionManager.clickSlot(
            mc.player.playerScreenHandler.syncId,
            serverSlot,
            40,
            SlotActionType.SWAP,
            mc.player
        );
    }
}
