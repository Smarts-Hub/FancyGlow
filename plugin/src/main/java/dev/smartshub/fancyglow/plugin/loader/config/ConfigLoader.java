package dev.smartshub.fancyglow.plugin.loader.config;


import dev.smartshub.fancyglow.plugin.FancyGlow;
import dev.smartshub.fancyglow.api.config.ConfigContainer;
import dev.smartshub.fancyglow.api.config.ConfigException;
import dev.smartshub.fancyglow.api.config.ConfigType;
import dev.smartshub.fancyglow.plugin.storage.config.ConfigContainerImpl;
import dev.smartshub.fancyglow.plugin.storage.config.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigLoader {

    private final FancyGlow plugin;
    private final Map<String, Configuration> configCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastModified = new ConcurrentHashMap<>();
    private final boolean cacheEnabled;

    public ConfigLoader(FancyGlow plugin) {
        this(plugin, true);
    }

    public ConfigLoader(FancyGlow plugin, boolean cacheEnabled) {
        this.plugin = plugin;
        this.cacheEnabled = cacheEnabled;
    }

    public ConfigContainer load(String fileName, ConfigType type) {
        String fullPath = resolveFullPath(fileName, type);

        ensureParentFolderExists(fullPath);

        if (cacheEnabled && isCacheValid(fullPath)) {
            Configuration cachedConfig = configCache.get(fullPath);
            return new ConfigContainerImpl(extractFileName(fullPath), cachedConfig, type);
        }

        Configuration config = loadFromFile(fullPath);

        if (cacheEnabled) {
            configCache.put(fullPath, config);
            updateLastModified(fullPath);
        }

        return new ConfigContainerImpl(extractFileName(fullPath), config, type);
    }

    public ConfigContainer load(ConfigType type) {
        if (type.isFolder()) {
            throw new IllegalArgumentException("Cannot load a folder type directly. Use loadFromFolder() instead.");
        }
        return load(type.getDefaultPath(), type);
    }

    public void reload(String fileName, ConfigType type) {
        String fullPath = resolveFullPath(fileName, type);

        try {
            Configuration config = configCache.get(fullPath);
            if (config != null) {
                config.reload();
                updateLastModified(fullPath);
            } else {
                load(fileName, type);
            }
        } catch (Exception e) {
            throw new ConfigException.ConfigLoadException(fullPath, e);
        }
    }

    public void reload(ConfigType type) {
        if (type.isFolder()) {
            throw new IllegalArgumentException("Cannot reload a folder type directly.");
        }
        reload(type.getDefaultPath(), type);
    }

    public void save(String fileName) {
        Configuration config = configCache.get(fileName);

        if (config == null) {
            throw new ConfigException.ConfigNotFoundException(fileName);
        }

        try {
            config.saveFile();
            updateLastModified(fileName);
        } catch (Exception e) {
            throw new ConfigException.ConfigSaveException(fileName, e);
        }
    }

    public void save(ConfigType type) {
        if (type.isFolder()) {
            throw new IllegalArgumentException("Cannot save a folder type directly.");
        }
        save(type.getDefaultPath());
    }

    public Set<ConfigContainer> loadFromFolder(String folderPath, ConfigType type) {
        Set<ConfigContainer> containers = new HashSet<>();

        ensureFolderExists(folderPath);

        File folder = new File(plugin.getDataFolder(), folderPath);

        if (!folder.isDirectory()) {
            plugin.getLogger().warning("Path '" + folderPath + "' is not a directory.");
            return containers;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return containers;

        for (File file : files) {
            try {
                String fileName = file.getName();
                String fullPath = folderPath + "/" + fileName;

                Configuration config = new Configuration(plugin, file, fileName);
                config.load(file);

                if (cacheEnabled) {
                    configCache.put(fullPath, config);
                    lastModified.put(fullPath, file.lastModified());
                }

                containers.add(new ConfigContainerImpl(fileName, config, type));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load config: " + file.getName());
                e.printStackTrace();
            }
        }

        return containers;
    }

    public Set<ConfigContainer> loadFromFolder(ConfigType type) {
        if (!type.isFolder()) {
            throw new IllegalArgumentException("ConfigType must be a folder type to use this method.");
        }

        String folderPath = type.getDefaultPath();
        folderPath = folderPath.substring(0, folderPath.length() - 1);

        return loadFromFolder(folderPath, type);
    }

    public void initializeAllFolders() {
        Set<String> foldersToCreate = new HashSet<>();

        for (ConfigType type : ConfigType.values()) {
            String parentFolder = type.getParentFolder();
            if (parentFolder != null) {
                foldersToCreate.add(parentFolder);
            }

            if (type.isFolder()) {
                String folderName = type.getDefaultPath().substring(0, type.getDefaultPath().length() - 1);
                foldersToCreate.add(folderName);
            }
        }

        for (String folder : foldersToCreate) {
            ensureFolderExists(folder);
        }
    }

    public void ensureFolderExists(String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                throw new ConfigException("Cannot create folder: " + folderName);
            } else {
                plugin.getLogger().info("Created folder: " + folderName);
            }
        }
    }

    public void clearCache() {
        configCache.clear();
        lastModified.clear();
    }

    public void evictFromCache(String fileName) {
        configCache.remove(fileName);
        lastModified.remove(fileName);
    }

    public void evictFromCache(ConfigType type) {
        if (type.isFolder()) {
            String folderPath = type.getDefaultPath();
            configCache.entrySet().removeIf(entry -> entry.getKey().startsWith(folderPath));
            lastModified.entrySet().removeIf(entry -> entry.getKey().startsWith(folderPath));
        } else {
            evictFromCache(type.getDefaultPath());
        }
    }

    private Configuration loadFromFile(String fullPath) {
        File file = new File(plugin.getDataFolder(), fullPath);
        String fileName = extractFileName(fullPath);

        try {
            if (!file.exists()) {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                if (plugin.getResource(fullPath) != null) {
                    plugin.saveResource(fullPath, false);
                } else {
                    plugin.getLogger().warning("Resource '" + fullPath + "' not found in jar!");
                }
            }

            Configuration config = new Configuration(plugin, file, fileName);
            return config;
        } catch (Exception e) {
            throw new ConfigException.ConfigLoadException(fullPath, e);
        }
    }


    private String getResourceNameForPath(String fullPath) {
        for (ConfigType type : ConfigType.values()) {
            if (!type.isFolder() && type.getDefaultPath().equals(fullPath)) {
                return type.getResourceName();
            }
        }
        return null;
    }


    private boolean isCacheValid(String fullPath) {
        if (!configCache.containsKey(fullPath)) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), fullPath);
        Long cachedTime = lastModified.get(fullPath);

        return cachedTime != null && cachedTime >= file.lastModified();
    }

    private void updateLastModified(String fullPath) {
        File file = new File(plugin.getDataFolder(), fullPath);
        lastModified.put(fullPath, file.lastModified());
    }

    private String resolveFullPath(String fileName, ConfigType type) {
        if (fileName.contains("/")) {
            return normalizeFileName(fileName);
        }

        String parentFolder = type.getParentFolder();
        if (parentFolder != null) {
            return parentFolder + "/" + normalizeFileName(fileName);
        }

        return normalizeFileName(fileName);
    }

    private void ensureParentFolderExists(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentFolder = fullPath.substring(0, lastSlash);
            ensureFolderExists(parentFolder);
        }
    }

    private String extractFileName(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        return lastSlash >= 0 ? fullPath.substring(lastSlash + 1) : fullPath;
    }

    private String normalizeFileName(String name) {
        return name.endsWith(".yml") ? name : name + ".yml";
    }
}
