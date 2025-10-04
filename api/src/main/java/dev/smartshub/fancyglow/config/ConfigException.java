package dev.smartshub.fancyglow.config;

public class ConfigException extends RuntimeException {
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static class ConfigNotFoundException extends ConfigException {
        public ConfigNotFoundException(String configName) {
            super("Configuration not found: " + configName);
        }
    }
    
    public static class ConfigLoadException extends ConfigException {
        public ConfigLoadException(String configName, Throwable cause) {
            super("Failed to load configuration: " + configName, cause);
        }
    }
    
    public static class ConfigSaveException extends ConfigException {
        public ConfigSaveException(String configName, Throwable cause) {
            super("Failed to save configuration: " + configName, cause);
        }
    }
}