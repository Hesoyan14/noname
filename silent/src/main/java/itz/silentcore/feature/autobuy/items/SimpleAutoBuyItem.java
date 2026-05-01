package itz.silentcore.feature.autobuy.items;

import itz.silentcore.feature.autobuy.settings.AutoBuyItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SimpleAutoBuyItem implements AutoBuyableItem {
    private final String displayName;
    private final String searchName;
    private final ItemStack reference;
    private final Item material;
    private final int price;
    private final AutoBuyItemSettings settings;
    private boolean enabled;

    public SimpleAutoBuyItem(String displayName, Item material, ItemStack reference, int price) {
        this(displayName, displayName, material, reference, price);
    }

    public SimpleAutoBuyItem(String displayName, String searchName, Item material, ItemStack reference, int price) {
        this.displayName = displayName;
        this.searchName = searchName;
        this.material = material;
        this.reference = reference;
        this.price = price;
        this.enabled = false;
        this.settings = new AutoBuyItemSettings(price, material, displayName);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSearchName() {
        return searchName;
    }

    @Override
    public ItemStack createItemStack() {
        return reference.copy();
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public AutoBuyItemSettings getSettings() {
        return settings;
    }
}
