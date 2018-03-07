package com.softwareberg

import java.sql.Connection

class TransactionManager(private val connection: Connection) {

    fun commit() {
        connection.commit()
    }

    fun rollback() {
        connection.rollback()
    }
}
