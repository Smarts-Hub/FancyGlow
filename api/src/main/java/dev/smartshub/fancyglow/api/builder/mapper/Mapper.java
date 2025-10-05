package dev.smartshub.fancyglow.api.builder.mapper;

public interface Mapper<T, K> {
    T map(K k);
}
