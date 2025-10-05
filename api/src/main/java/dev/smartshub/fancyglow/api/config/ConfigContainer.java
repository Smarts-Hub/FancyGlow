package dev.smartshub.fancyglow.api.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConfigContainer {

    Optional<String> getString(String path);
    Optional<Integer> getInt(String path);
    Optional<Boolean> getBoolean(String path);
    Optional<Double> getDouble(String path);
    Optional<Long> getLong(String path);
    Optional<List<String>> getStringList(String path);

    String getString(String path, String defaultValue);
    int getInt(String path, int defaultValue);
    boolean getBoolean(String path, boolean defaultValue);
    double getDouble(String path, double defaultValue);
    long getLong(String path, long defaultValue);
    List<String> getStringList(String path, List<String> defaultValue);

    ConfigurationSection getConfigurationSection(String path);
    boolean hasPath(String path);
    Set<String> getKeys(String path);
    String getName();
    String getNameWithoutExtension();
    ConfigType getType();
    boolean isLoaded();

    void requirePath(String path) throws ConfigException;
    <T> T getRequired(String path, Class<T> type) throws ConfigException;
}