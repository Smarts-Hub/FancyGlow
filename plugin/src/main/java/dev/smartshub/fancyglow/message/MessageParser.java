package dev.smartshub.fancyglow.message;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class MessageParser {

    private final MiniMessage miniMessage;
    private final boolean placeholderAPIAvailable;

    public MessageParser() {
        this.miniMessage = MiniMessage.builder()
                .postProcessor(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .build();
        this.placeholderAPIAvailable = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if (!placeholderAPIAvailable) {
            Bukkit.getLogger().info("[FancyGlow] PlaceholderAPI not available - placeholders will not be processed");
        }
    }

    public Component parseWithPlayer(String message, Player player) {
        if (message == null || message.trim().isEmpty()) {
            return Component.empty();
        }

        String processedMessage = placeholderAPIAvailable
                ? PlaceholderAPI.setPlaceholders(player, message)
                : message;
        return miniMessage.deserialize(processedMessage);
    }

    public Component parse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Component.empty();
        }

        return miniMessage.deserialize(message);
    }

    public List<Component> parseListWithPlayer(List<String> messages, Player player) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        return messages.stream()
                .filter(message -> message != null && !message.trim().isEmpty())
                .map(message -> parseWithPlayer(message, player))
                .collect(Collectors.toList());
    }

    public List<Component> parseList(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        return messages.stream()
                .filter(message -> message != null && !message.trim().isEmpty())
                .map(this::parse)
                .collect(Collectors.toList());
    }

    public String toString(Component component) {
        if (component == null) {
            return "";
        }
        return miniMessage.serialize(component);
    }
}
