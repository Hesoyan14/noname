package itz.silentcore.feature.autobuy.items;

import itz.silentcore.feature.autobuy.settings.AutoBuyItemSettings;
import net.minecraft.item.ItemStack;

public interface AutoBuyableItem {
    String getDisplayName();
    ItemStack createItemStack();
    int getPrice();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    AutoBuyItemSettings getSettings();
    String getSearchName();
}
