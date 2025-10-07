package dev.smartshub.fancyglow.plugin.storage.config;

import dev.smartshub.fancyglow.api.flow.Reloadable;
import dev.smartshub.fancyglow.plugin.FancyGlow;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configuration extends YamlConfiguration implements Reloadable {

    private final File file;
    private final FancyGlow plugin;

    public Configuration(FancyGlow plugin, File file, String fileName) {
        this.plugin = plugin;

        if (file == null) {
            this.file = new File(plugin.getDataFolder(), fileName.endsWith(".yml") ? fileName : fileName + ".yml");
        } else {
            if (file.isDirectory()) {
                this.file = new File(file, fileName.endsWith(".yml") ? fileName : fileName + ".yml");
            } else {
                this.file = file;
            }
        }

        if (this.file.getParentFile() != null) {
            this.file.getParentFile().mkdirs();
        }

        loadFile();
    }


    private void loadFile() {
        try {
            this.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveFile() {
        try {
            this.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        try {
            loadFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

