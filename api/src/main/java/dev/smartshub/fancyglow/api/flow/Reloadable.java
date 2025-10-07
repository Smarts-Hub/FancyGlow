package dev.smartshub.fancyglow.api.flow;

public interface Reloadable {
    void reload();
    default void shutdown() {}
}