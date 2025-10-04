package dev.smartshub.fancyglow.builder.mapper;

public interface Mapper<T, K> {
    T map(K k);
}
