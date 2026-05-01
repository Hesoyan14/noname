package itz.silentcore.feature.autobuy.items.providers;

import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.feature.autobuy.items.SimpleAutoBuyItem;
import itz.silentcore.feature.autobuy.items.DefaultPrices;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class ArmorProvider {
    public static List<AutoBuyableItem> getArmor() {
        List<AutoBuyableItem> armor = new ArrayList<>();
        
        // Незеритовая броня
        armor.add(new SimpleAutoBuyItem("Незеритовый шлем", Items.NETHERITE_HELMET, new ItemStack(Items.NETHERITE_HELMET), 100000));
        armor.add(new SimpleAutoBuyItem("Незеритовый нагрудник", Items.NETHERITE_CHESTPLATE, new ItemStack(Items.NETHERITE_CHESTPLATE), 100000));
        armor.add(new SimpleAutoBuyItem("Незеритовые поножи", Items.NETHERITE_LEGGINGS, new ItemStack(Items.NETHERITE_LEGGINGS), 100000));
        armor.add(new SimpleAutoBuyItem("Незеритовые ботинки", Items.NETHERITE_BOOTS, new ItemStack(Items.NETHERITE_BOOTS), 100000));
        
        // Алмазная броня
        armor.add(new SimpleAutoBuyItem("Алмазный шлем", Items.DIAMOND_HELMET, new ItemStack(Items.DIAMOND_HELMET), 50000));
        armor.add(new SimpleAutoBuyItem("Алмазный нагрудник", Items.DIAMOND_CHESTPLATE, new ItemStack(Items.DIAMOND_CHESTPLATE), 50000));
        armor.add(new SimpleAutoBuyItem("Алмазные поножи", Items.DIAMOND_LEGGINGS, new ItemStack(Items.DIAMOND_LEGGINGS), 50000));
        armor.add(new SimpleAutoBuyItem("Алмазные ботинки", Items.DIAMOND_BOOTS, new ItemStack(Items.DIAMOND_BOOTS), 50000));
        
        return armor;
    }
}
