package com.softwareberg

import java.sql.ResultSet

/*
https://github.com/andrewoma/kwery/blob/master/core/src/main/kotlin/com/github/andrewoma/kwery/core/Row.kt
 */
class RowSet(private val resultSet: ResultSet) : Row(resultSet) {
    fun next(): Boolean = resultSet.next()
}
