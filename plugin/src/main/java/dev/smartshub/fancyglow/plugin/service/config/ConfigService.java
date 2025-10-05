package dev.smartshub.fancyglow.plugin.service.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.smartshub.fancyglow.plugin.FancyGlow;
import dev.smartshub.fancyglow.api.config.ConfigContainer;
import dev.smartshub.fancyglow.api.config.ConfigType;
import dev.smartshub.fancyglow.plugin.loader.config.ConfigLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ConfigService {

    private static final String YML_EXTENSION = ".yml";

    private final ConfigLoader loader;
    private final FancyGlow plugin;

    public ConfigService(FancyGlow plugin) {
        this.plugin = plugin;
        this.loader = new ConfigLoader(plugin);
        initialize();
        copyDefaultGlowFiles();
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

        Stream.of(ConfigType.values())
                .filter(type -> !type.isFolder())
                .forEach(this::updateConfigFileFromType);
    }

    private void updateConfigFileFromType(ConfigType configType) {
        try {
            String resourcePath = configType.getDefaultPath();
            String fileName = configType.getResourceName();

            File folder = new File(plugin.getDataFolder(), configType.getParentFolder());
            folder.mkdirs();

            File configFile = new File(folder, fileName);
            InputStream defaultResource = plugin.getResource(resourcePath);

            if (defaultResource == null) {
                if (!configFile.exists()) {
                    plugin.getLogger().warning("Can't find default file for: " + resourcePath);
                }
                return;
            }

            YamlDocument config = createYamlDocument(configFile, defaultResource);

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

    private YamlDocument createYamlDocument(File configFile, InputStream defaultResource) throws IOException {
        return YamlDocument.create(
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

    private void copyDefaultGlowFiles() {
        try {
            Path jarPath = getJarPath();
            if (jarPath == null || !isJarFile(jarPath)) {
                return;
            }

            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                jarFile.stream()
                        .filter(this::isGlowFile)
                        .forEach(entry -> copyGlowFile(jarFile, entry));
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to copy default glow files: " + e.getMessage());
        }
    }

    private Path getJarPath() {
        try {
            return Path.of(plugin.getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isJarFile(Path path) {
        return Files.exists(path) && path.toString().endsWith(".jar");
    }

    private boolean isGlowFile(JarEntry entry) {
        String name = entry.getName();
        return !entry.isDirectory()
                && name.startsWith(ConfigType.GLOW_DEFINITION.getDefaultPath())
                && name.endsWith(YML_EXTENSION);
    }

    private void copyGlowFile(JarFile jarFile, JarEntry entry) {
        try {
            File outFile = new File(plugin.getDataFolder(), entry.getName());

            if (outFile.exists()) {
                return;
            }

            Files.createDirectories(outFile.getParentFile().toPath());

            try (InputStream in = jarFile.getInputStream(entry)) {
                Files.copy(in, outFile.toPath());
                plugin.getLogger().info("Created default glow file: " + entry.getName());
            }

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to copy glow file " + entry.getName() + ": " + e.getMessage());
        }
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