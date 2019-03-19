package com.softwareberg;

@FunctionalInterface
public interface Extractor<T> {
    T extract(Row row);
}
