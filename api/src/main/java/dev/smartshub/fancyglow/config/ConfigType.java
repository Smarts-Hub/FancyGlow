package dev.smartshub.fancyglow.config;

public enum ConfigType {

    DATABASE("configuration/database.yml", "database.yml", "configuration"),
    SETTINGS("configuration/settings.yml", "settings.yml", "configuration"),
    MESSAGES("lang/messages.yml", "messages.yml", "lang"),

    GLOW_DEFINITION("glows/", null, "glows");

    private final String defaultPath;
    private final String resourceName;
    private final String parentFolder;

    ConfigType(String defaultPath, String resourceName, String parentFolder) {
        this.defaultPath = defaultPath;
        this.resourceName = resourceName;
        this.parentFolder = parentFolder;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getParentFolder() {
        return parentFolder;
    }

    public boolean isFolder() {
        return defaultPath.endsWith("/");
    }
}
