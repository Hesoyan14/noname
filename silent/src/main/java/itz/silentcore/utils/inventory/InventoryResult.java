package itz.silentcore.utils.inventory;

public record InventoryResult(boolean found, int slot) {
    public static InventoryResult notFound() {
        return new InventoryResult(false, -1);
    }

    public static InventoryResult of(int slot) {
        return new InventoryResult(true, slot);
    }
}
