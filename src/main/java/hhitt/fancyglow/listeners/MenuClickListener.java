package hhitt.fancyglow.listeners;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.inventory.CreatingInventory;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import hhitt.fancyglow.utils.ColorUtils;
import hhitt.fancyglow.utils.MessageHandler;
import hhitt.fancyglow.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class MenuClickListener implements Listener {
    private final GlowManager glowManager;
    private final MessageHandler messageHandler;
    private final PlayerGlowManager playerGlowManager;

    public MenuClickListener(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
        this.messageHandler = plugin.getMessageHandler();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Cancel every click while the menu is open, not only clicks inside it: shift-clicking, double-click
        // collecting or number-key swapping from the player's own inventory used to move items into the menu.
        if (!(e.getView().getTopInventory().getHolder() instanceof CreatingInventory)) return;

        e.setCancelled(true);

        Inventory inventoryClicked = e.getClickedInventory();
        if (inventoryClicked == null || !(inventoryClicked.getHolder() instanceof CreatingInventory)) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.LEATHER_CHESTPLATE) return;

        // The clicked-item already is of leather-type, why should we check if also it has an item-meta?
        // It won´t be null.
        ChatColor color = ColorUtils.getColorFromArmorColor(((LeatherArmorMeta) clickedItem.getItemMeta()).getColor());
        if (color == null) return;

        Player player = (Player) e.getWhoClicked();
        player.closeInventory();

        // Same rule as '/glow color': the menu used to let players pick a color in disabled worlds.
        if (glowManager.isDeniedWorld(player.getWorld().getName())) {
            messageHandler.sendMessage(player, Messages.DISABLED_WORLD);
            return;
        }

        if (!(glowManager.hasGlowPermission(player, color) || player.hasPermission("fancyglow.all_colors"))) {
            messageHandler.sendMessage(player, Messages.NO_PERMISSION);
            return;
        }

        if (!glowManager.isMulticolorTaskActive(player) && playerGlowManager.getPlayerGlowColorName(player).equalsIgnoreCase(color.name())) {
            messageHandler.sendMessage(player, Messages.COLOR_ALREADY_SELECTED);
            return;
        }

        glowManager.setGlow(player, color);
        messageHandler.sendMessage(player, Messages.ENABLE_GLOW);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        // Dragging items across the menu's slots would otherwise place them into it.
        if (e.getView().getTopInventory().getHolder() instanceof CreatingInventory) {
            e.setCancelled(true);
        }
    }
}
