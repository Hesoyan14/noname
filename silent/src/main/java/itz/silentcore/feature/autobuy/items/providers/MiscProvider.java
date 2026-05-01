package itz.silentcore.feature.autobuy.items.providers;

import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.feature.autobuy.items.SimpleAutoBuyItem;
import itz.silentcore.feature.autobuy.items.DefaultPrices;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class MiscProvider {
    public static List<AutoBuyableItem> getMisc() {
        List<AutoBuyableItem> misc = new ArrayList<>();
        
        // Базовые ресурсы
        misc.add(new SimpleAutoBuyItem("Алмаз", Items.DIAMOND, new ItemStack(Items.DIAMOND), DefaultPrices.getPrice("Алмаз")));
        misc.add(new SimpleAutoBuyItem("Алмазный блок", Items.DIAMOND_BLOCK, new ItemStack(Items.DIAMOND_BLOCK), DefaultPrices.getPrice("Алмазный блок")));
        misc.add(new SimpleAutoBuyItem("Незеритовый слиток", Items.NETHERITE_INGOT, new ItemStack(Items.NETHERITE_INGOT), DefaultPrices.getPrice("Незеритовый слиток")));
        misc.add(new SimpleAutoBuyItem("Незеритовый блок", Items.NETHERITE_BLOCK, new ItemStack(Items.NETHERITE_BLOCK), DefaultPrices.getPrice("Незеритовый блок")));
        misc.add(new SimpleAutoBuyItem("Незеритовое улучшение", Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), DefaultPrices.getPrice("Незеритовое улучшение")));
        misc.add(new SimpleAutoBuyItem("Золотой слиток", Items.GOLD_INGOT, new ItemStack(Items.GOLD_INGOT), DefaultPrices.getPrice("Золотой слиток")));
        misc.add(new SimpleAutoBuyItem("Блок золота", Items.GOLD_BLOCK, new ItemStack(Items.GOLD_BLOCK), DefaultPrices.getPrice("Блок золота")));
        misc.add(new SimpleAutoBuyItem("Железный слиток", Items.IRON_INGOT, new ItemStack(Items.IRON_INGOT), DefaultPrices.getPrice("Железный слиток")));
        misc.add(new SimpleAutoBuyItem("Железный блок", Items.IRON_BLOCK, new ItemStack(Items.IRON_BLOCK), DefaultPrices.getPrice("Железный блок")));
        
        // Еда
        misc.add(new SimpleAutoBuyItem("Золотое яблоко", Items.GOLDEN_APPLE, new ItemStack(Items.GOLDEN_APPLE), DefaultPrices.getPrice("Золотое яблоко")));
        misc.add(new SimpleAutoBuyItem("Зачарованное золотое яблоко", Items.ENCHANTED_GOLDEN_APPLE, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), DefaultPrices.getPrice("Зачарованное золотое яблоко")));
        misc.add(new SimpleAutoBuyItem("Яблоко", Items.APPLE, new ItemStack(Items.APPLE), DefaultPrices.getPrice("Яблоко")));
        misc.add(new SimpleAutoBuyItem("Торт", Items.CAKE, new ItemStack(Items.CAKE), DefaultPrices.getPrice("Торт")));
        
        // Особые предметы
        misc.add(new SimpleAutoBuyItem("Тотем бессмертия", Items.TOTEM_OF_UNDYING, new ItemStack(Items.TOTEM_OF_UNDYING), DefaultPrices.getPrice("Тотем бессмертия")));
        misc.add(new SimpleAutoBuyItem("Элитры", Items.ELYTRA, new ItemStack(Items.ELYTRA), DefaultPrices.getPrice("Элитры")));
        misc.add(new SimpleAutoBuyItem("Эндер жемчуг", Items.ENDER_PEARL, new ItemStack(Items.ENDER_PEARL), DefaultPrices.getPrice("Эндер жемчуг")));
        misc.add(new SimpleAutoBuyItem("Трезубец", Items.TRIDENT, new ItemStack(Items.TRIDENT), DefaultPrices.getPrice("Трезубец")));
        misc.add(new SimpleAutoBuyItem("Булава", Items.MACE, new ItemStack(Items.MACE), DefaultPrices.getPrice("Булава")));
        misc.add(new SimpleAutoBuyItem("Мешок", Items.BUNDLE, new ItemStack(Items.BUNDLE), DefaultPrices.getPrice("Мешок")));
        
        // Блоки и материалы
        misc.add(new SimpleAutoBuyItem("Обсидиан", Items.OBSIDIAN, new ItemStack(Items.OBSIDIAN), DefaultPrices.getPrice("Обсидиан")));
        misc.add(new SimpleAutoBuyItem("Динамит", Items.TNT, new ItemStack(Items.TNT), DefaultPrices.getPrice("Динамит")));
        misc.add(new SimpleAutoBuyItem("Маяк", Items.BEACON, new ItemStack(Items.BEACON), DefaultPrices.getPrice("Маяк")));
        misc.add(new SimpleAutoBuyItem("Спавнер", Items.SPAWNER, new ItemStack(Items.SPAWNER), DefaultPrices.getPrice("Спавнер")));
        misc.add(new SimpleAutoBuyItem("Шалкеровый ящик", Items.SHULKER_BOX, new ItemStack(Items.SHULKER_BOX), DefaultPrices.getPrice("Шалкеровый ящик")));
        
        // Опыт и зелья
        misc.add(new SimpleAutoBuyItem("Пузырёк опыта", Items.EXPERIENCE_BOTTLE, new ItemStack(Items.EXPERIENCE_BOTTLE), DefaultPrices.getPrice("Пузырёк опыта")));
        misc.add(new SimpleAutoBuyItem("Звезда Незера", Items.NETHER_STAR, new ItemStack(Items.NETHER_STAR), DefaultPrices.getPrice("Звезда Незера")));
        
        // Разное
        misc.add(new SimpleAutoBuyItem("Бирка", Items.NAME_TAG, new ItemStack(Items.NAME_TAG), DefaultPrices.getPrice("Бирка")));
        misc.add(new SimpleAutoBuyItem("Порох", Items.GUNPOWDER, new ItemStack(Items.GUNPOWDER), DefaultPrices.getPrice("Порох")));
        misc.add(new SimpleAutoBuyItem("Палка ифрита", Items.BLAZE_ROD, new ItemStack(Items.BLAZE_ROD), DefaultPrices.getPrice("Палка ифрита")));
        misc.add(new SimpleAutoBuyItem("Фейерверк", Items.FIREWORK_ROCKET, new ItemStack(Items.FIREWORK_ROCKET), DefaultPrices.getPrice("Фейерверк")));
        
        // Головы
        misc.add(new SimpleAutoBuyItem("Голова скелета", Items.SKELETON_SKULL, new ItemStack(Items.SKELETON_SKULL), DefaultPrices.getPrice("Голова скелета")));
        misc.add(new SimpleAutoBuyItem("Голова зомби", Items.ZOMBIE_HEAD, new ItemStack(Items.ZOMBIE_HEAD), DefaultPrices.getPrice("Голова зомби")));
        misc.add(new SimpleAutoBuyItem("Голова крипера", Items.CREEPER_HEAD, new ItemStack(Items.CREEPER_HEAD), DefaultPrices.getPrice("Голова крипера")));
        misc.add(new SimpleAutoBuyItem("Голова визер-скелета", Items.WITHER_SKELETON_SKULL, new ItemStack(Items.WITHER_SKELETON_SKULL), DefaultPrices.getPrice("Голова визер-скелета")));
        misc.add(new SimpleAutoBuyItem("Голова пиглина", Items.PIGLIN_HEAD, new ItemStack(Items.PIGLIN_HEAD), DefaultPrices.getPrice("Голова пиглина")));
        misc.add(new SimpleAutoBuyItem("Голова дракона", Items.DRAGON_HEAD, new ItemStack(Items.DRAGON_HEAD), DefaultPrices.getPrice("Голова дракона")));
        
        // Яйца спавна
        misc.add(new SimpleAutoBuyItem("Яйцо зомби-жителя", Items.ZOMBIE_VILLAGER_SPAWN_EGG, new ItemStack(Items.ZOMBIE_VILLAGER_SPAWN_EGG), DefaultPrices.getPrice("Яйцо зомби-жителя")));
        misc.add(new SimpleAutoBuyItem("Яйцо жителя", Items.VILLAGER_SPAWN_EGG, new ItemStack(Items.VILLAGER_SPAWN_EGG), DefaultPrices.getPrice("Яйцо жителя")));
        misc.add(new SimpleAutoBuyItem("Яйцо вихря", Items.BREEZE_SPAWN_EGG, new ItemStack(Items.BREEZE_SPAWN_EGG), DefaultPrices.getPrice("Яйцо вихря")));
        
        // Руды
        misc.add(new SimpleAutoBuyItem("Алмазная руда", Items.DIAMOND_ORE, new ItemStack(Items.DIAMOND_ORE), DefaultPrices.getPrice("Алмазная руда")));
        misc.add(new SimpleAutoBuyItem("Изумрудная руда", Items.EMERALD_ORE, new ItemStack(Items.EMERALD_ORE), DefaultPrices.getPrice("Изумрудная руда")));
        
        // Новые предметы 1.21
        misc.add(new SimpleAutoBuyItem("Ключ испытаний", Items.TRIAL_KEY, new ItemStack(Items.TRIAL_KEY), DefaultPrices.getPrice("Ключ испытаний")));
        misc.add(new SimpleAutoBuyItem("Зловещий ключ испытаний", Items.OMINOUS_TRIAL_KEY, new ItemStack(Items.OMINOUS_TRIAL_KEY), DefaultPrices.getPrice("Зловещий ключ испытаний")));
        misc.add(new SimpleAutoBuyItem("Заряд ветра", Items.WIND_CHARGE, new ItemStack(Items.WIND_CHARGE), DefaultPrices.getPrice("Заряд ветра")));
        misc.add(new SimpleAutoBuyItem("Стержень вихря", Items.BREEZE_ROD, new ItemStack(Items.BREEZE_ROD), DefaultPrices.getPrice("Стержень вихря")));
        
        return misc;
    }
}
