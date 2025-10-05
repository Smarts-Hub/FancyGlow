package dev.smartshub.fancyglow.plugin.service.notify;

import dev.smartshub.fancyglow.plugin.message.MessageParser;
import dev.smartshub.fancyglow.plugin.message.MessageRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class NotifyService {

    private final MessageParser parser;
    private final MessageRepository repository;

    public NotifyService(MessageParser parser, MessageRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    public void sendChat(Player player, String path) {
        if (player == null) return;
        String message = repository.getMessage(path);
        if (message == null || message.trim().isEmpty()) return;

        Component component = parser.parseWithPlayer(message, player);
        player.sendMessage(component);
    }

    public void sendChat(CommandSender sender, String path) {
        if (sender == null) return;
        String message = repository.getMessage(path);
        if (message == null || message.trim().isEmpty()) return;

        Component component = parser.parse(message);
        sender.sendMessage(component);
    }

    public void sendRawMessage(Player player, String message) {
        player.sendMessage(message);
    }

}

