package com.softwareberg

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp

/*
https://github.com/andrewoma/kwery/blob/master/core/src/main/kotlin/com/github/andrewoma/kwery/core/Row.kt
 */
open class Row(private val resultSet: ResultSet) {
    fun obj(name: String): Any = requireNotNull(resultSet.getObject(name), name)
    fun objectOrNull(name: String): Any? = resultSet.getObject(name)

    fun boolean(name: String): Boolean = requireNotNull(resultSet.getBoolean(name), name)
    fun booleanValue(name: String): Boolean = boolean(name)
    fun booleanOrNull(name: String): Boolean? = valueOrNull(resultSet.getBoolean(name))

    fun byte(name: String): Byte = requireNotNull(resultSet.getByte(name), name)
    fun byteValue(name: String): Byte = byte(name)
    fun byteOrNull(name: String): Byte? = valueOrNull(resultSet.getByte(name))

    fun short(name: String): Short = requireNotNull(resultSet.getShort(name), name)
    fun shortValue(name: String): Short = short(name)
    fun shortOrNull(name: String): Short? = valueOrNull(resultSet.getShort(name))

    fun int(name: String): Int = requireNotNull(resultSet.getInt(name), name)
    fun intValue(name: String): Int = int(name)
    fun intOrNull(name: String): Int? = valueOrNull(resultSet.getInt(name))

    fun long(name: String): Long = requireNotNull(resultSet.getLong(name), name)
    fun longValue(name: String): Long = long(name)
    fun longOrNull(name: String): Long? = valueOrNull(resultSet.getLong(name))

    fun float(name: String): Float = requireNotNull(resultSet.getFloat(name), name)
    fun floatValue(name: String): Float = float(name)
    fun floatOrNull(name: String): Float? = valueOrNull(resultSet.getFloat(name))

    fun double(name: String): Double = requireNotNull(resultSet.getDouble(name), name)
    fun doubleValue(name: String): Double = double(name)
    fun doubleOrNull(name: String): Double? = valueOrNull(resultSet.getDouble(name))

    fun bigDecimal(name: String): BigDecimal = resultSet.getBigDecimal(name)
    fun bigDecimalOrNull(name: String): BigDecimal? = resultSet.getBigDecimal(name)

    fun string(name: String): String = resultSet.getString(name)
    fun stringOrNull(name: String): String? = resultSet.getString(name)

    fun bytes(name: String): ByteArray = resultSet.getBytes(name)
    fun bytesOrNull(name: String): ByteArray? = resultSet.getBytes(name)

    fun timestamp(name: String): Timestamp = resultSet.getTimestamp(name)
    fun timestampOrNull(name: String): Timestamp? = resultSet.getTimestamp(name)

    fun time(name: String): Time = resultSet.getTime(name)
    fun timeOrNull(name: String): Time? = resultSet.getTime(name)

    fun date(name: String): Date = resultSet.getDate(name)
    fun dateOrNull(name: String): Date? = resultSet.getDate(name)

    fun clob(name: String): Clob = resultSet.getClob(name)
    fun clobOrNull(name: String): Clob? = resultSet.getClob(name)

    fun blob(name: String): Blob = resultSet.getBlob(name)
    fun blobOrNull(name: String): Blob? = resultSet.getBlob(name)

    fun characterStream(name: String): Reader = resultSet.getCharacterStream(name)
    fun characterStreamOrNull(name: String): Reader? = resultSet.getCharacterStream(name)

    fun binaryStream(name: String): InputStream = resultSet.getBinaryStream(name)
    fun binaryStreamOrNull(name: String): InputStream? = resultSet.getBinaryStream(name)

    @Suppress("UNCHECKED_CAST")
    fun <T> array(name: String): List<T> {
        val value = resultSet.getArray(name)
        return if (resultSet.wasNull()) emptyList() else (value.array as Array<Any>).toList() as List<T>
    }

    private fun <T : Any> valueOrNull(value: T): T? = if (resultSet.wasNull()) null else value

    private fun <T : Any> requireNotNull(value: T?, name: String): T {
        require(!resultSet.wasNull()) { "Unexpected null for column '$name'" }
        return value!!
    }
}
