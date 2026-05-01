package itz.silentcore.feature.autobuy.items.providers;

import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.feature.autobuy.items.SimpleAutoBuyItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class WeaponProvider {
    public static List<AutoBuyableItem> getWeapons() {
        List<AutoBuyableItem> weapons = new ArrayList<>();
        
        // Мечи
        weapons.add(new SimpleAutoBuyItem("Незеритовый меч", Items.NETHERITE_SWORD, new ItemStack(Items.NETHERITE_SWORD), 150000));
        weapons.add(new SimpleAutoBuyItem("Алмазный меч", Items.DIAMOND_SWORD, new ItemStack(Items.DIAMOND_SWORD), 75000));
        
        // Инструменты
        weapons.add(new SimpleAutoBuyItem("Незеритовая кирка", Items.NETHERITE_PICKAXE, new ItemStack(Items.NETHERITE_PICKAXE), 150000));
        weapons.add(new SimpleAutoBuyItem("Незеритовый топор", Items.NETHERITE_AXE, new ItemStack(Items.NETHERITE_AXE), 150000));
        weapons.add(new SimpleAutoBuyItem("Незеритовая лопата", Items.NETHERITE_SHOVEL, new ItemStack(Items.NETHERITE_SHOVEL), 100000));
        weapons.add(new SimpleAutoBuyItem("Незеритовая мотыга", Items.NETHERITE_HOE, new ItemStack(Items.NETHERITE_HOE), 100000));
        
        weapons.add(new SimpleAutoBuyItem("Алмазная кирка", Items.DIAMOND_PICKAXE, new ItemStack(Items.DIAMOND_PICKAXE), 75000));
        weapons.add(new SimpleAutoBuyItem("Алмазный топор", Items.DIAMOND_AXE, new ItemStack(Items.DIAMOND_AXE), 75000));
        weapons.add(new SimpleAutoBuyItem("Алмазная лопата", Items.DIAMOND_SHOVEL, new ItemStack(Items.DIAMOND_SHOVEL), 50000));
        
        // Дальнобойное оружие
        weapons.add(new SimpleAutoBuyItem("Лук", Items.BOW, new ItemStack(Items.BOW), 10000));
        weapons.add(new SimpleAutoBuyItem("Арбалет", Items.CROSSBOW, new ItemStack(Items.CROSSBOW), 15000));
        weapons.add(new SimpleAutoBuyItem("Трезубец", Items.TRIDENT, new ItemStack(Items.TRIDENT), 100000));
        weapons.add(new SimpleAutoBuyItem("Булава", Items.MACE, new ItemStack(Items.MACE), 200000));
        
        return weapons;
    }
}
