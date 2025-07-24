package hhitt.fancyglow.listeners;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.inventory.CreatingInventory;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.utils.MessageHandler;
import hhitt.fancyglow.utils.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HeadClickListener implements Listener {

    private final FancyGlow plugin;
    private final GlowManager glowManager;
    private final MessageHandler messageHandler;

    public HeadClickListener(FancyGlow plugin) {
        this.plugin = plugin;
        this.glowManager = plugin.getGlowManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    @EventHandler
    public void onPlayerClickHead(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !(clickedInventory.getHolder() instanceof CreatingInventory)) return;

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() != Material.PLAYER_HEAD) return;

        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        int rainbowSlot = plugin.getConfiguration().getInt("Inventory.Rainbow.Slot", 39);
        int flashingSlot = plugin.getConfiguration().getInt("Inventory.Flashing.Slot", 40);
        int statusSlot = plugin.getConfiguration().getInt("Inventory.Status.Slot", 41);
        // Rainbow Slot
        if (rainbowSlot != -1 && event.getSlot() == rainbowSlot) {
            if (!player.hasPermission("fancyglow.rainbow")) {
                messageHandler.sendMessage(player, Messages.NO_PERMISSION);
                return;
            }

            // Disables combined modes if set on config.
            if (!plugin.getConfiguration().getBoolean("Flash_Rainbow") && glowManager.isFlashingTaskActive(player)) {
                messageHandler.sendMessage(player, Messages.FLASHING_WITH_RAINBOW);
                return;
            }

            // Toggle rainbow mode
            boolean toggled = glowManager.toggleMulticolorGlow(player);
            messageHandler.sendMessage(player, toggled ? Messages.ENABLE_RAINBOW : Messages.DISABLE_RAINBOW);
        }
        // Flashing Slot
        else if (flashingSlot != -1 && event.getSlot() == flashingSlot) {
            if (!player.hasPermission("fancyglow.flashing")) {
                messageHandler.sendMessage(player, Messages.NO_PERMISSION);
                return;
            }

            // Disables combined modes if set on config.
            if (!plugin.getConfiguration().getBoolean("Flash_Rainbow") && glowManager.isMulticolorTaskActive(player)) {
                messageHandler.sendMessage(player, Messages.FLASHING_WITH_RAINBOW);
                return;
            }

            // Toggle flashing mode
            boolean toggled = glowManager.toggleFlashingGlow(player);
            messageHandler.sendMessage(player, toggled ? Messages.ENABLE_FLASHING : Messages.DISABLE_GLOW);
        }
        // Status Slot
        else if (event.getSlot() == statusSlot) {
            glowManager.removeGlow(player);
            messageHandler.sendMessage(player, Messages.DISABLE_GLOW);
        }
    }
}