package dev.smartshub.fancyglow.api.builder.serializer;

public interface Serializer<T, K> {
    T serialize(K k);
}