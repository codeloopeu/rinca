package com.softwareberg;

@FunctionalInterface
public interface RowProcessor {
    void process(Row row);
}
