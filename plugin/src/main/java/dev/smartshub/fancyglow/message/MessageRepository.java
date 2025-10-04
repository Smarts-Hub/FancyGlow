package dev.smartshub.fancyglow.message;

import dev.smartshub.fancyglow.config.ConfigContainer;
import dev.smartshub.fancyglow.config.ConfigType;
import dev.smartshub.fancyglow.service.config.ConfigService;

import java.util.List;

public class MessageRepository {

    private final ConfigContainer messages;

    public MessageRepository(ConfigService configService) {
        this.messages = configService.provide(ConfigType.MESSAGES);
    }

    public String getMessage(String path) {
        return messages.getString(path, "<red>Message not found: " + path);
    }

    public List<String> getMessageList(String path) {
        return messages.getStringList(path, List.of("<gray>Empty message list: " + path));
    }

}
