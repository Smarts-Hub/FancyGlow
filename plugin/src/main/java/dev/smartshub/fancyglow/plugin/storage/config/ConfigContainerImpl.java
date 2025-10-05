package dev.smartshub.fancyglow.plugin.storage.config;

import dev.smartshub.fancyglow.api.config.ConfigContainer;
import dev.smartshub.fancyglow.api.config.ConfigException;
import dev.smartshub.fancyglow.api.config.ConfigType;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConfigContainerImpl implements ConfigContainer {
    
    private final String name;
    private final Configuration config;
    private final ConfigType type;
    
    public ConfigContainerImpl(String name, Configuration config, ConfigType type) {
        this.name = name;
        this.config = config;
        this.type = type;
    }
    
    @Override
    public Optional<String> getString(String path) {
        String value = config.getString(path);
        return value != null ? Optional.of(value) : Optional.empty();
    }
    
    @Override
    public Optional<Integer> getInt(String path) {
        return hasPath(path) ? Optional.of(config.getInt(path)) : Optional.empty();
    }
    
    @Override
    public Optional<Boolean> getBoolean(String path) {
        return hasPath(path) ? Optional.of(config.getBoolean(path)) : Optional.empty();
    }
    
    @Override
    public Optional<Double> getDouble(String path) {
        return hasPath(path) ? Optional.of(config.getDouble(path)) : Optional.empty();
    }
    
    @Override
    public Optional<Long> getLong(String path) {
        return hasPath(path) ? Optional.of(config.getLong(path)) : Optional.empty();
    }
    
    @Override
    public Optional<List<String>> getStringList(String path) {
        List<String> value = config.getStringList(path);
        return !value.isEmpty() ? Optional.of(value) : Optional.empty();
    }
    
    @Override
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    @Override
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    @Override
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    @Override
    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    
    @Override
    public List<String> getStringList(String path, List<String> defaultValue) {
        List<String> value = config.getStringList(path);
        return !value.isEmpty() ? value : defaultValue;
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        return config.getConfigurationSection(path);
    }

    @Override
    public boolean hasPath(String path) {
        return config.contains(path);
    }
    
    @Override
    public Set<String> getKeys(String path) {
        return config.getConfigurationSection(path) != null 
            ? config.getConfigurationSection(path).getKeys(false)
            : Set.of();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameWithoutExtension() {
        return name.replace(".yml", "").replace(".json", "");
    }
    
    @Override
    public ConfigType getType() {
        return type;
    }
    
    @Override
    public boolean isLoaded() {
        return config != null;
    }
    
    @Override
    public void requirePath(String path) throws ConfigException {
        if (!hasPath(path)) {
            throw new ConfigException("Required path not found: " + path + " in config: " + name);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRequired(String path, Class<T> type) throws ConfigException {
        requirePath(path);
        
        Object value = config.get(path);
        if (value == null) {
            throw new ConfigException("Required value is null at path: " + path);
        }
        
        if (!type.isInstance(value)) {
            throw new ConfigException("Value at path " + path + " is not of type " + type.getSimpleName());
        }
        
        return (T) value;
    }

    public Configuration getInternalConfiguration() {
        return this.config;
    }

    public File getFile() {
        return new File("plugins/FancyGlow/glows/", name);
    }

}
