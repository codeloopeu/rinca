package com.softwareberg;

import org.springframework.transaction.TransactionStatus;

@FunctionalInterface
public interface TransactionWithStatus {
    void exec(TransactionStatus status);
}
