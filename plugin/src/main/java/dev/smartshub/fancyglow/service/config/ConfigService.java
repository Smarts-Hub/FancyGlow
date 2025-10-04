package dev.smartshub.fancyglow.service.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.smartshub.fancyglow.FancyGlow;
import dev.smartshub.fancyglow.config.ConfigContainer;
import dev.smartshub.fancyglow.config.ConfigType;
import dev.smartshub.fancyglow.loader.config.ConfigLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Level;

public class ConfigService {

    private final ConfigLoader loader;
    private final FancyGlow plugin;

    public ConfigService(FancyGlow plugin) {
        this.plugin = plugin;
        this.loader = new ConfigLoader(plugin);
        initialize();
    }

    public void initialize() {
        updateConfigsIfNeeded();

        loader.initializeAllFolders();

        provide(ConfigType.DATABASE);
        provide(ConfigType.MESSAGES);
        provide(ConfigType.SETTINGS);
    }
    private void updateConfigsIfNeeded() {
        plugin.getDataFolder().mkdirs();

        for (ConfigType configType : ConfigType.values()) {
            if (!configType.isFolder()) {
                updateConfigFileFromType(configType);
            }
        }

    }

    private void updateConfigFileFromType(ConfigType configType) {
        if (configType.isFolder()) return;

        try {
            String resourcePath = configType.getDefaultPath();
            String fileName = configType.getResourceName();

            File folder = new File(plugin.getDataFolder(), configType.getParentFolder());
            if (!folder.exists()) folder.mkdirs();

            File configFile = new File(folder, fileName);
            InputStream defaultResource = plugin.getResource(resourcePath);

            if (defaultResource == null) {
                if (!configFile.exists()) {
                    plugin.getLogger().warning("Can't found default file for: " + resourcePath);
                }
                return;
            }

            YamlDocument config = YamlDocument.create(
                    configFile,
                    defaultResource,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.builder()
                            .setEncoding(DumperSettings.Encoding.UNICODE)
                            .build(),
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .setKeepAll(true)
                            .build()
            );

            if (config.update()) {
                plugin.getLogger().info("Updated configuration: " + fileName);
                config.save();
            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating config: " + configType.getResourceName(), e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error processing: " + configType.getResourceName(), e);
        }
    }


    public ConfigContainer provide(ConfigType type) {
        if (type.isFolder()) {
            loader.loadFromFolder(type);
        }
        return loader.load(type);
    }

    public ConfigContainer provide(String customPath, ConfigType type) {
        return loader.load(customPath, type);
    }

    public Set<ConfigContainer> provideAllGlows() {
        return loader.loadFromFolder(ConfigType.GLOW_DEFINITION);
    }

    public void reload(ConfigType type) {
        if (type.isFolder()) {
            loader.evictFromCache(type);
        } else {
            loader.reload(type);
        }
    }

    public void save(ConfigType type) {
        if (type.isFolder()) {
            throw new IllegalArgumentException("Cannot save folder types directly.");
        }
        loader.save(type);
    }

    public void clearCache() {
        loader.clearCache();
    }

    public void reloadAll() {
        updateConfigsIfNeeded();

        reload(ConfigType.DATABASE);
        reload(ConfigType.MESSAGES);
        reload(ConfigType.SETTINGS);
    }
}