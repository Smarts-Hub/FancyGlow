package dev.smartshub.fancyglow.builder.serializer;

public interface Serializer<T, K> {
    T serialize(K k);
}