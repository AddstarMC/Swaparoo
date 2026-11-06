package au.com.addstar.swaparoo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConvertConfirmGui implements InventoryHolder {
    public static final int SLOT_CONFIRM = 11;
    public static final int SLOT_CANCEL = 15;

    private final ConvertGui parent;
    private final Inventory inventory;
    private boolean busy = false;

    public ConvertConfirmGui(SwaparooPlugin plugin, ConvertGui parent) {
        this.parent = parent;
        this.inventory = Bukkit.createInventory(this, 27, plugin.getMM().deserialize("<dark_gray>Confirm Conversion"));

        ItemStack filler = parent.filler();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        inventory.setItem(SLOT_CONFIRM, parent.item(Material.EMERALD_BLOCK, 1, "<green>✔ Confirm", List.of(parent.summaryLine())));
        inventory.setItem(SLOT_CANCEL, parent.item(Material.REDSTONE_BLOCK, 1, "<red>✘ Cancel", List.of("<yellow>Go back without converting")));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public ConvertGui getParent() {
        return parent;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy() {
        busy = true;
    }
}
