package au.com.addstar.swaparoo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class ConvertListener implements Listener {
    private final SwaparooPlugin plugin;

    public ConvertListener(SwaparooPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!isConvertMenu(holder)) {
            return;
        }

        // The menus are display only, so no click may move an item
        event.setCancelled(true);
        if (!(event.isLeftClick() || event.isRightClick()) || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (holder instanceof ConvertGui gui && slot == ConvertGui.SLOT_CONVERT) {
            plugin.getCM().handleConvertClick(player, gui);
        } else if (holder instanceof ConvertConfirmGui gui && slot == ConvertConfirmGui.SLOT_CONFIRM) {
            plugin.getCM().handleConfirmClick(player, gui);
        } else if (holder instanceof ConvertConfirmGui gui && slot == ConvertConfirmGui.SLOT_CANCEL) {
            plugin.getCM().handleCancelClick(player, gui);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isConvertMenu(event.getInventory().getHolder())) {
            event.setCancelled(true);
        }
    }

    private boolean isConvertMenu(InventoryHolder holder) {
        return holder instanceof ConvertGui || holder instanceof ConvertConfirmGui;
    }
}
