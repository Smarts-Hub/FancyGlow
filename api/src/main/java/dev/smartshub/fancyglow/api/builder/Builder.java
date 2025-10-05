package dev.smartshub.fancyglow.api.builder;

public interface Builder<T, K> {
    T build(K k);
}
