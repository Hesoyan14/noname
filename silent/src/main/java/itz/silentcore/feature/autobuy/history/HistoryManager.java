package itz.silentcore.feature.autobuy.history;

import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.feature.autobuy.manager.AutoBuyManager;
import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryManager implements IMinecraft {
    private static HistoryManager instance;
    private final List<PurchaseRecord> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 100;
    
    private static final Pattern SELLER_PATTERN = Pattern.compile("⚕.*:\\s*([\\w\\d_]+)", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\s*(\\d+(?:[,\\s.]\\d{3})*(?:\\.\\d{2})?)");

    private HistoryManager() {}

    public static HistoryManager getInstance() {
        if (instance == null) {
            instance = new HistoryManager();
        }
        return instance;
    }

    public void addPurchase(ItemStack item) {
        if (item == null || item.isEmpty()) return;

        String itemName = item.getName().getString();
        itemName = itemName.replaceAll("^\\s*-\\s*", "").replaceAll("\\s*-\\s*$", "").trim();
        
        int quantity = item.getCount();
        int price = extractPrice(item);
        String seller = extractSeller(item);

        PurchaseRecord record = new PurchaseRecord(item, itemName, quantity, price, seller);
        history.add(0, record);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
    }

    public void addPurchaseFromMessage(String itemName, int price) {
        if (itemName == null || itemName.isEmpty() || price <= 0) {
            return;
        }

        ItemStack foundItem = ItemStack.EMPTY;
        int quantity = 1;
        
        AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
        String cleanItemName = itemName.replaceAll("§[0-9a-fk-or]", "").trim();
        String cleanItemNameLower = cleanItemName.toLowerCase();
        
        for (AutoBuyableItem autoBuyItem : autoBuyManager.getAllItems()) {
            String displayName = autoBuyItem.getDisplayName().replaceAll("§[0-9a-fk-or]", "").trim();
            String displayNameLower = displayName.toLowerCase();
            String searchName = autoBuyItem.getSearchName() != null ? 
                autoBuyItem.getSearchName().replaceAll("§[0-9a-fk-or]", "").trim() : displayName;
            String searchNameLower = searchName.toLowerCase();
            
            if (displayNameLower.equals(cleanItemNameLower) || 
                searchNameLower.equals(cleanItemNameLower) ||
                cleanItemNameLower.contains(displayNameLower) ||
                displayNameLower.contains(cleanItemNameLower) ||
                cleanItemNameLower.contains(searchNameLower) ||
                searchNameLower.contains(cleanItemNameLower)) {
                try {
                    foundItem = autoBuyItem.createItemStack();
                    if (foundItem == null) {
                        foundItem = ItemStack.EMPTY;
                    }
                    quantity = foundItem.getCount();
                    if (quantity <= 0) quantity = 1;
                } catch (Exception e) {
                    foundItem = ItemStack.EMPTY;
                }
                break;
            }
        }

        String seller = findSellerInInventory(cleanItemName);
        String cleanItemNameForDisplay = cleanItemName.replaceAll("^\\s*-\\s*", "").replaceAll("\\s*-\\s*$", "").trim();

        PurchaseRecord record = new PurchaseRecord(foundItem, cleanItemNameForDisplay, quantity, price, seller);
        history.add(0, record);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
    }
    
    private String findSellerInInventory(String itemName) {
        if (mc.player == null) return "Неизвестно";
        
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            
            String stackName = stack.getName().getString().replaceAll("§[0-9a-fk-or]", "").trim();
            if (stackName.contains(itemName) || itemName.contains(stackName)) {
                String seller = extractSeller(stack);
                if (!seller.equals("Неизвестно")) {
                    return seller;
                }
            }
        }
        
        return "Неизвестно";
    }

    public List<PurchaseRecord> getHistory() {
        return new ArrayList<>(history);
    }

    public void clearHistory() {
        history.clear();
    }

    private int extractPrice(ItemStack stack) {
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            for (Text line : lore.lines()) {
                String text = line.getString();
                Matcher matcher = PRICE_PATTERN.matcher(text);
                String lastFoundPrice = null;
                while (matcher.find()) {
                    lastFoundPrice = matcher.group(1);
                }
                if (lastFoundPrice != null) {
                    try {
                        return Integer.parseInt(lastFoundPrice.replaceAll("[\\s,.]", ""));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return 0;
    }

    private String extractSeller(ItemStack stack) {
        if (stack == null) return "Неизвестно";
        
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore == null || lore.lines().isEmpty()) return "Неизвестно";
        
        for (Text line : lore.lines()) {
            String text = line.getString();
            
            Matcher matcher = SELLER_PATTERN.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            if (text.contains("Продавец:") || text.contains("Продaвeц:")) {
                String[] parts = text.split("Продавец:|Продaвeц:");
                if (parts.length > 1) {
                    String sellerPart = parts[1].trim();
                    sellerPart = sellerPart.replaceAll("§[0-9a-fk-or]", "");
                    sellerPart = sellerPart.replaceAll("[▍▶▎]", "").trim();
                    String[] words = sellerPart.split("\\s+");
                    if (words.length > 0 && !words[0].isEmpty()) {
                        return words[0];
                    }
                }
            }
        }
        
        return "Неизвестно";
    }
}
