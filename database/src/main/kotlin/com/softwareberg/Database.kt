package com.softwareberg

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

typealias Extractor<T> = (ResultSet) -> T

class SqlStatement(val sql: String, val params: Array<out Any>)

fun String.paramsList(vararg params: Any): SqlStatement = SqlStatement(this, params)

class NamedSqlStatement(val sql: String, val params: Map<String, Any>)

fun String.params(vararg params: Pair<String, Any>): NamedSqlStatement = NamedSqlStatement(this, params.toMap())

class Database(private val dataSource: DataSource) {

    fun <T> findOne(stmt: SqlStatement, extractor: Extractor<T>): T? {
        return JdbcTemplate(dataSource).query(stmt.sql, stmt.params, ResultSetExtractor<T> { rs ->
            if (rs.next()) {
                extractor(rs)
            } else {
                null
            }
        })
    }

    fun <T> findOne(stmt: NamedSqlStatement, extractor: Extractor<T>): T? {
        return NamedParameterJdbcTemplate(dataSource).query(stmt.sql, stmt.params, ResultSetExtractor<T> { rs ->
            if (rs.next()) {
                extractor(rs)
            } else {
                null
            }
        })
    }

    fun <T> findAll(stmt: SqlStatement, extractor: Extractor<T>): List<T> {
        return JdbcTemplate(dataSource).query(stmt.sql, stmt.params, { rs, _ -> extractor(rs) })
    }

    fun <T> findAll(stmt: NamedSqlStatement, extractor: Extractor<T>): List<T> {
        return NamedParameterJdbcTemplate(dataSource).query(stmt.sql, stmt.params, { rs, _ -> extractor(rs) })
    }

    fun <T> findOne(sql: String, extractor: Extractor<T>): T? = findOne(sql.paramsList(), extractor)

    fun <T> findAll(sql: String, extractor: Extractor<T>): List<T> = findAll(sql.paramsList(), extractor)

    fun update(stmt: SqlStatement): Int {
        return JdbcTemplate(dataSource).update { c -> prepareStatement(c, stmt) }
    }

    fun update(stmt: NamedSqlStatement): Int {
        return NamedParameterJdbcTemplate(dataSource).update(stmt.sql, stmt.params)
    }

    fun update(sql: String): Int {
        return JdbcTemplate(dataSource).update(sql)
    }

    fun insert(stmt: SqlStatement): Map<String, Any?> {
        val keyHolder = GeneratedKeyHolder()
        JdbcTemplate(dataSource).update({ c -> prepareStatement(c, stmt) }, keyHolder)
        return keyHolder.keys.orEmpty()
    }

    fun insert(stmt: NamedSqlStatement): Map<String, Any?> {
        val keyHolder = GeneratedKeyHolder()
        val sqlParameterSource = MapSqlParameterSource(stmt.params)
        NamedParameterJdbcTemplate(dataSource).update(stmt.sql, sqlParameterSource, keyHolder)
        return keyHolder.keys.orEmpty()
    }

    fun insert(sql: String): Map<String, Any?> {
        return insert(sql.paramsList())
    }

    private fun prepareStatement(c: Connection, stmt: SqlStatement): PreparedStatement {
        return stmt.params.withIndex().fold(c.prepareStatement(stmt.sql), { ps, param -> ps.setObject(param.index + 1, param.value); ps })
    }
}