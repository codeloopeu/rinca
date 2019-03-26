package com.softwareberg;

@FunctionalInterface
public interface RowSetExtractor<T> {
    T extract(RowSet row);
}
