package hhitt.fancyglow.utils;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageUtils {

    private static BukkitAudiences adventure;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

    /**
     * Parses a message with the mini message format
     *
     * @param message Messsage to parse.
     *
     * @return Component message with mini message format.
     */
    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Parse message with mini message format.
     *
     * @param message Message to parse to.
     *
     * @return Parsed message with mini message format.
     */
    public static @NotNull String miniMessageParse(String message) {
        return LEGACY_COMPONENT_SERIALIZER.serialize(parse(message));
    }

    /**
     * Sends a parsed message to player as a component.
     *
     * @param player  Player to send a message to.
     * @param message Message to send.
     */
    public static void miniMessageSender(Player player, String message) {
        adventure.player(player).sendMessage(parse(message));
    }

    /**
     * Sets adventure variable instance.
     *
     * @param adventure BukkitAudience instance to set to.
     */
    public static void setAdventure(BukkitAudiences adventure) {
        MessageUtils.adventure = adventure;
    }
}
