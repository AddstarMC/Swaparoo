package au.com.addstar.swaparoo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConvertGui implements InventoryHolder {
    public static final int SLOT_CONVERT = 49;
    private static final int SLOT_LOCK = 10;
    private static final int SLOT_PLAYER = 47;
    private static final int SLOT_GEMS = 51;
    private static final int[] LEVEL_SLOTS = {
            11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    public static final int LEVEL_COUNT = LEVEL_SLOTS.length;

    private static final String STARGEM = "<bold><yellow>Star<gold>Gem</gold></yellow></bold>";
    private static final String STARGEMS = "<bold><yellow>Star<gold>Gems</gold></yellow></bold>";

    private final SwaparooPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final ConvertManager.ConvertData data;

    public ConvertGui(SwaparooPlugin plugin, Player player, ConvertManager.ConvertData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
        this.inventory = Bukkit.createInventory(this, 54,
                plugin.getMM().deserialize("<dark_gray>Convert Points to </dark_gray>" + STARGEMS));

        ItemStack filler = filler();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        renderLock();
        renderLevels();
        renderPlayer();
        renderConvert();
        renderGems();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public ConvertManager.ConvertData getData() {
        return data;
    }

    ItemStack filler() {
        return item(Material.BLACK_STAINED_GLASS_PANE, 1, " ", List.of());
    }

    String summaryLine() {
        return "<green>Convert <aqua>" + (data.stats().points() - data.remainder()) + " <green>points to <aqua>"
                + data.gemsToReceive() + " " + STARGEMS;
    }

    private void renderLock() {
        if (data.currentTier() == null) {
            inventory.setItem(SLOT_LOCK, item(Material.BARRIER, 1, "<red>✘ Locked", List.of(lifetimeLine(), levelUpLine())));
        } else {
            inventory.setItem(SLOT_LOCK, item(Material.LIME_STAINED_GLASS_PANE, 1, "<green>✔ Unlocked", List.of("<green>Point conversion is unlocked")));
        }
    }

    private void renderLevels() {
        DataManager.ConversionTier current = data.currentTier();
        DataManager.ConversionTier next = data.nextTier();
        int currentLevel = current != null ? current.level() : 0;
        for (DataManager.ConversionTier tier : data.tiers()) {
            int level = tier.level();
            if (level < 1 || level > LEVEL_COUNT) {
                continue;
            }

            ItemStack item;
            if (level > currentLevel) {
                // Only the next level to unlock shows progress
                item = item(Material.RED_STAINED_GLASS_PANE, level, "<red>✘ Locked",
                        tier.equals(next) ? List.of(lifetimeLine(), levelUpLine()) : List.of());
            } else if (level == currentLevel) {
                item = glow(item(Material.EMERALD, level, "<green>Level " + level + " <gray>(current)",
                        List.of(rateLine(tier), levelUpLine())));
            } else {
                item = item(Material.LIME_STAINED_GLASS_PANE, level, "<green>Level " + level, List.of(rateLine(tier)));
            }
            inventory.setItem(LEVEL_SLOTS[level - 1], item);
        }
    }

    private void renderPlayer() {
        DataManager.ConversionTier current = data.currentTier();
        DataManager.UnscramblePlayer stats = data.stats();
        List<String> lore = List.of(
                "<green>Level: <aqua>" + (current != null ? current.level() : "None"),
                "<green>Wins: <aqua>" + stats.wins(),
                "<green>Points: <aqua>" + stats.points(),
                "<green>Lifetime points: <aqua>" + stats.totalPoints(),
                current != null ? "<green>Rate: " + rateLine(current) : "<green>Rate: <red>Locked");

        ItemStack head = item(Material.PLAYER_HEAD, 1, "<aqua>" + player.getName(), lore);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        head.setItemMeta(meta);
        inventory.setItem(SLOT_PLAYER, head);
    }

    private void renderConvert() {
        if (!data.canConvert()) {
            inventory.setItem(SLOT_CONVERT, item(Material.REDSTONE_BLOCK, 1, "<red>Cannot convert", cannotConvertLore()));
            return;
        }

        inventory.setItem(SLOT_CONVERT, item(Material.EMERALD_BLOCK, 1, summaryLine(), List.of("<green>✔ <yellow>Click to convert")));
    }

    private List<String> cannotConvertLore() {
        DataManager.ConversionTier current = data.currentTier();
        if (current == null) {
            return List.of("<yellow>Reach Level 1 to unlock conversion");
        }
        return List.of(
                "<yellow>You need at least <aqua>" + data.requiredPoints(current) + " <yellow>points to convert",
                "<yellow>You have <aqua>" + data.stats().points() + " <yellow>Unscramble " + (data.stats().points() == 1 ? "Point" : "Points"));
    }

    private void renderGems() {
        inventory.setItem(SLOT_GEMS, item(Material.NETHER_STAR, 1, STARGEMS,
                List.of("<green>Balance: <aqua>" + data.gems() + " " + STARGEMS)));
    }

    private String rateLine(DataManager.ConversionTier tier) {
        return "<aqua>" + tier.pointsPerGem() + " <green>Points = <aqua>1 " + STARGEM;
    }

    private String lifetimeLine() {
        return "<green>Lifetime points: <aqua>" + data.stats().totalPoints();
    }

    private String levelUpLine() {
        DataManager.ConversionTier next = data.nextTier();
        if (next == null) {
            return "<gold>Max level";
        }
        int needed = next.minTotalPoints() - data.stats().totalPoints();
        return "<yellow>Next level in <aqua>" + needed + " <yellow>lifetime " + (needed == 1 ? "point" : "points");
    }

    ItemStack item(Material material, int amount, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.getMM().deserialize("<!italic>" + name));
        meta.lore(lore.stream().map(line -> plugin.getMM().deserialize("<!italic>" + line)).toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack glow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
