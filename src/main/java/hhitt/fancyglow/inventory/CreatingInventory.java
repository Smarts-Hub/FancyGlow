package hhitt.fancyglow.inventory;

import dev.dejvokep.boostedyaml.YamlDocument;
import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import hhitt.fancyglow.utils.ColorUtils;
import hhitt.fancyglow.utils.HeadUtils;
import hhitt.fancyglow.utils.MessageHandler;
import hhitt.fancyglow.utils.MessageUtils;
import hhitt.fancyglow.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DataFlowIssue")
public class CreatingInventory implements InventoryHolder {

    private static final String DEFAULT_RAINBOW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI0OTMyYmI5NDlkMGM2NTcxN2IxMjFjOGNkOWEyMWI2OWU4NmMwZjdlMzQyMWFlOWI4YzY0ZDhiOTkwZWI2MCJ9fX0";
    private static final String DEFAULT_FLASHING_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFjNTgxYThiNTk3NjkyYjViOTRkM2I4YmViOWM1MmY1Njk5OWQ2MmY3Mzk1NjY4ZmFjNTdhYzk1MmZlNGRjNCJ9fX0=";

    private final YamlDocument config;
    private final Inventory inventory;
    private final MessageHandler messageHandler;
    private final PlayerGlowManager playerGlowManager;

    public CreatingInventory(FancyGlow plugin) {
        this.config = plugin.getConfiguration();
        this.messageHandler = plugin.getMessageHandler();
        this.playerGlowManager = plugin.getPlayerGlowManager();
        this.inventory = plugin.getServer().createInventory(this, 45, messageHandler.getMessage(Messages.INVENTORY_TITLE));
    }

    public void setupContent() {
        // Set fill items.
        setFiller();

        // Set color items.
        int i = 9;
        ItemStack colorItem;
        LeatherArmorMeta colorMeta;
        // Preferably we'll want to avoid us the object-alloc that involves the #getMessages method.
        List<String> colorLoreMessage = messageHandler.getMessages(Messages.COLOR_LORE);
        for (ChatColor availableColor : GlowManager.COLORS_ARRAY) {
            colorItem = new ItemStack(Material.LEATHER_CHESTPLATE);
            colorMeta = (LeatherArmorMeta) colorItem.getItemMeta();

            colorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
            colorMeta.setLore(colorLoreMessage);
            // Might cause IllegalArgumentException if message doesn't exist.
            Messages colorMessage = Messages.valueOf(availableColor.name().toUpperCase() + "_NAME");
            colorMeta.setDisplayName(messageHandler.getMessage(colorMessage));
            colorMeta.setColor(ColorUtils.getArmorColorFromChatColor(availableColor));
            colorItem.setItemMeta(colorMeta);

            // Skip slot 18.
            if (i == 18) {
                i++;
            }
            inventory.setItem(i++, colorItem);
        }

        setRainbowItem();
    }

    private void prepareForPlayer(final Player player) {
        setFlashingItem(player);
        setPlayerStatusItem(player);
    }

    public void openInventory(Player player) {
        prepareForPlayer(player);
        player.openInventory(inventory);
    }

    private void setFiller() {
        // Return if filler is not enabled.
        if (!config.getBoolean("Inventory.Filler.Enabled")) return;

        // Define filler-material.
        Material material = Material.getMaterial(config.getString("Inventory.Filler.Material", "GRAY_STAINED_GLASS_PANE"));
        if (material == null) return;

        ItemStack fill = new ItemStack(material);
        ItemMeta fillMeta = fill.getItemMeta();
        fillMeta.setDisplayName(messageHandler.getMessage(Messages.FILLER_NAME));
        fill.setItemMeta(fillMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, fill);
        }
    }

    private void setFlashingItem(final Player player) {
        // Clear flashing item since plugin no longer
        int slot = config.getInt("Inventory.Flashing.Slot", 40);
        if (slot == -1) {
            return;
        }

        inventory.setItem(slot, null);

        // Returns if player doesn't have any team or isn't glowing.
        if (playerGlowManager.findPlayerTeam(player) == null && !player.isGlowing()) return;

        // Flashing head
        ItemStack flashingHead = HeadUtils.getCustomSkull(config.getString("Inventory.Flashing.Texture", DEFAULT_FLASHING_TEXTURE));
        ItemMeta flashingHeadMeta = flashingHead.getItemMeta();

        flashingHeadMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        flashingHeadMeta.setDisplayName(messageHandler.getMessage(Messages.FLASHING_HEAD_NAME));
        flashingHeadMeta.setLore(messageHandler.getMessages(Messages.FLASHING_HEAD_LORE));
        flashingHead.setItemMeta(flashingHeadMeta);

        inventory.setItem(slot, flashingHead);
    }

    private void setRainbowItem() {
        int slot = config.getInt("Inventory.Rainbow.Slot", 39);
        if (slot == -1) {
            return;
        }

        // Rainbow head
        ItemStack rainbowHead = HeadUtils.getCustomSkull(config.getString("Inventory.Rainbow.Texture", DEFAULT_RAINBOW_TEXTURE));
        ItemMeta rainbowHeadMeta = rainbowHead.getItemMeta();

        rainbowHeadMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rainbowHeadMeta.setDisplayName(messageHandler.getMessage(Messages.RAINBOW_HEAD_NAME));
        rainbowHeadMeta.setLore(messageHandler.getMessages(Messages.RAINBOW_HEAD_LORE));
        rainbowHead.setItemMeta(rainbowHeadMeta);

        inventory.setItem(slot, rainbowHead);
    }

    private void setPlayerStatusItem(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(messageHandler.getMessage(Messages.HEAD_NAME));

        String mode = playerGlowManager.getPlayerGlowingMode(player);
        if (mode.equals("NONE")) {
            meta.setLore(messageHandler.getMessages(Messages.HEAD_LORE_CLICK));
        } else {
            List<String> parsedMessage = new ArrayList<>();
            messageHandler.sendMessageBuilder(player, Messages.HEAD_LORE_SELECTED)
                    .placeholder("%mode%", mode)
                    .parseList()
                    .forEach(line -> parsedMessage.add(MessageUtils.miniMessageParse(line)));
            meta.setLore(parsedMessage);
        }

        meta.setOwningPlayer(player);
        head.setItemMeta(meta);

        inventory.setItem(config.getInt("Inventory.Status.Slot", 41), head);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
