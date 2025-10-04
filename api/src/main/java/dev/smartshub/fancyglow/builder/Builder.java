package dev.smartshub.fancyglow.builder;

public interface Builder<T, K> {
    T build(K k);
}
