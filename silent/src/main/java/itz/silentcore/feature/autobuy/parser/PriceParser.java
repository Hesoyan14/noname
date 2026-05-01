package itz.silentcore.feature.autobuy.parser;

import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсер цен для анализа аукциона и поиска выгодных предложений
 */
public class PriceParser implements IMinecraft {
    private static boolean enabled = false;
    private static int discountPercent = 10; // Процент скидки для поиска выгодных предложений
    private static String currentItemName = "";
    private static final Map<String, Integer> cheapestPrices = new HashMap<>();
    private static final Map<String, List<Integer>> priceHistory = new HashMap<>();
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void setEnabled(boolean value) {
        enabled = value;
    }
    
    public static int getDiscountPercent() {
        return discountPercent;
    }
    
    public static void setDiscountPercent(int percent) {
        discountPercent = Math.max(0, Math.min(100, percent));
    }
    
    public static String getCurrentItemName() {
        return currentItemName;
    }
    
    public static void setCurrentItemName(String name) {
        currentItemName = name;
    }
    
    /**
     * Анализирует слоты аукциона и находит самую дешевую цену за единицу товара
     */
    public static int findCheapestPerItem(List<Slot> slots, String itemName) {
        if (slots == null || itemName == null || itemName.isEmpty()) {
            return 0;
        }
        
        int cheapest = Integer.MAX_VALUE;
        
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            
            String stackName = stack.getName().getString();
            if (!stackName.contains(itemName)) continue;
            
            int price = extractPrice(stack);
            int count = stack.getCount();
            
            if (price > 0 && count > 0) {
                int pricePerItem = price / count;
                if (pricePerItem < cheapest) {
                    cheapest = pricePerItem;
                }
            }
        }
        
        return cheapest == Integer.MAX_VALUE ? 0 : cheapest;
    }
    
    /**
     * Извлекает цену из описания предмета
     */
    public static int extractPrice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        
        var lore = stack.get(net.minecraft.component.DataComponentTypes.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            for (Text line : lore.lines()) {
                String text = line.getString();
                
                // Ищем цену в разных форматах
                if (text.contains("$") || text.contains("Цена") || text.contains("Price")) {
                    try {
                        String priceStr = text.replaceAll("[^0-9]", "");
                        if (!priceStr.isEmpty()) {
                            return Integer.parseInt(priceStr);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Обновляет историю цен для предмета
     */
    public static void updatePriceHistory(String itemName, int price) {
        if (itemName == null || itemName.isEmpty() || price <= 0) return;
        
        List<Integer> history = priceHistory.computeIfAbsent(itemName, k -> new ArrayList<>());
        history.add(price);
        
        // Храним только последние 100 цен
        if (history.size() > 100) {
            history.remove(0);
        }
        
        // Обновляем самую дешевую цену
        int cheapest = history.stream().min(Integer::compare).orElse(price);
        cheapestPrices.put(itemName, cheapest);
    }
    
    /**
     * Получает среднюю цену для предмета
     */
    public static int getAveragePrice(String itemName) {
        List<Integer> history = priceHistory.get(itemName);
        if (history == null || history.isEmpty()) return 0;
        
        return (int) history.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
    
    /**
     * Получает самую дешевую цену для предмета
     */
    public static int getCheapestPrice(String itemName) {
        return cheapestPrices.getOrDefault(itemName, 0);
    }
    
    /**
     * Проверяет, является ли цена выгодной (ниже средней на заданный процент)
     */
    public static boolean isGoodDeal(String itemName, int price) {
        int average = getAveragePrice(itemName);
        if (average == 0) return false;
        
        int threshold = (int) (average * (100 - discountPercent) / 100.0);
        return price <= threshold;
    }
    
    /**
     * Анализирует слоты и находит выгодные предложения
     */
    public static List<PriceDeal> findGoodDeals(List<Slot> slots, List<AutoBuyableItem> enabledItems) {
        List<PriceDeal> deals = new ArrayList<>();
        
        if (slots == null || enabledItems == null) return deals;
        
        for (AutoBuyableItem item : enabledItems) {
            if (item == null || !item.isEnabled()) continue;
            
            String itemName = item.getDisplayName();
            if (itemName == null) continue;
            
            for (Slot slot : slots) {
                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) continue;
                
                String stackName = stack.getName().getString();
                if (!stackName.contains(itemName)) continue;
                
                int price = extractPrice(stack);
                if (price <= 0) continue;
                
                updatePriceHistory(itemName, price);
                
                if (isGoodDeal(itemName, price)) {
                    deals.add(new PriceDeal(slot, item, price, getAveragePrice(itemName)));
                }
            }
        }
        
        return deals;
    }
    
    /**
     * Очищает историю цен
     */
    public static void clearHistory() {
        priceHistory.clear();
        cheapestPrices.clear();
    }
    
    /**
     * Класс для хранения информации о выгодном предложении
     */
    public static class PriceDeal {
        public final Slot slot;
        public final AutoBuyableItem item;
        public final int currentPrice;
        public final int averagePrice;
        public final int savings;
        
        public PriceDeal(Slot slot, AutoBuyableItem item, int currentPrice, int averagePrice) {
            this.slot = slot;
            this.item = item;
            this.currentPrice = currentPrice;
            this.averagePrice = averagePrice;
            this.savings = averagePrice - currentPrice;
        }
        
        public int getSavingsPercent() {
            if (averagePrice == 0) return 0;
            return (int) ((savings / (double) averagePrice) * 100);
        }
    }
}
