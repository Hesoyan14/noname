package itz.silentcore.feature.autobuy.items;

import itz.silentcore.feature.autobuy.items.providers.ArmorProvider;
import itz.silentcore.feature.autobuy.items.providers.MiscProvider;
import itz.silentcore.feature.autobuy.items.providers.WeaponProvider;
import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    private static List<AutoBuyableItem> allItems = null;

    public static List<AutoBuyableItem> getAllItems() {
        if (allItems == null) {
            allItems = new ArrayList<>();
            allItems.addAll(getArmor());
            allItems.addAll(getWeapons());
            allItems.addAll(getMisc());
        }
        return allItems;
    }

    public static List<AutoBuyableItem> getArmor() {
        return ArmorProvider.getArmor();
    }

    public static List<AutoBuyableItem> getWeapons() {
        return WeaponProvider.getWeapons();
    }

    public static List<AutoBuyableItem> getMisc() {
        return MiscProvider.getMisc();
    }
}
