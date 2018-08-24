package com.softwareberg

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.transaction.TransactionDefinition.ISOLATION_DEFAULT
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

typealias Extractor<T> = (Row) -> T

fun <T> createExtractor(extractor: Extractor<T>): Extractor<T> = extractor

class SqlStatement(val sql: String, val params: Array<out Any?>) {
    companion object {
        @JvmStatic
        fun create(sql: String, vararg params: Any?): SqlStatement = SqlStatement(sql, params)
    }
}

fun String.paramsList(vararg params: Any?): SqlStatement = SqlStatement(this, params)

class NamedSqlStatement(val sql: String, val params: Map<String, Any?>) {
    companion object {
        @JvmStatic
        fun create(sql: String, vararg params: Param): NamedSqlStatement = NamedSqlStatement(sql, params.map { it.name to it.value }.toMap())
    }

    data class Param(val name: String, val value: Any?) {
        companion object {
            @JvmStatic
            fun of(name: String, value: Any?): Param = Param(name, value)
        }
    }
}

fun String.params(vararg params: Pair<String, Any?>): NamedSqlStatement = NamedSqlStatement(this, params.toMap())

class Database(private val dataSource: DataSource) {

    fun <T> findOne(stmt: SqlStatement, extractor: Extractor<T>): T? {
        return JdbcTemplate(dataSource).query(stmt.sql, stmt.params, ResultSetExtractor<T> { rs ->
            if (rs.next()) {
                extractor(Row(rs))
            } else {
                null
            }
        })
    }

    fun <T> findOne(stmt: NamedSqlStatement, extractor: Extractor<T>): T? {
        return NamedParameterJdbcTemplate(dataSource).query(stmt.sql, stmt.params, ResultSetExtractor<T> { rs ->
            if (rs.next()) {
                extractor(Row(rs))
            } else {
                null
            }
        })
    }

    fun <T> findOne(sql: String, extractor: Extractor<T>): T? = findOne(sql.paramsList(), extractor)

    fun <T> findAll(stmt: SqlStatement, extractor: Extractor<T>): List<T> {
        return JdbcTemplate(dataSource).query(stmt.sql, stmt.params, { rs, _ -> extractor(Row(rs)) })
    }

    fun <T> findAll(stmt: NamedSqlStatement, extractor: Extractor<T>): List<T> {
        return NamedParameterJdbcTemplate(dataSource).query(stmt.sql, stmt.params, { rs, _ -> extractor(Row(rs)) })
    }

    fun <T> findAll(sql: String, extractor: Extractor<T>): List<T> = findAll(sql.paramsList(), extractor)

    fun update(stmt: SqlStatement): Int {
        return JdbcTemplate(dataSource).update { c -> prepareStatementNoGeneratedKeys(c, stmt) }
    }

    fun update(stmt: NamedSqlStatement): Int {
        return NamedParameterJdbcTemplate(dataSource).update(stmt.sql, stmt.params)
    }

    fun update(sql: String): Int {
        return JdbcTemplate(dataSource).update(sql)
    }

    fun insert(stmt: SqlStatement): Map<String, Any?> {
        val keyHolder = GeneratedKeyHolder()
        JdbcTemplate(dataSource).update({ c -> prepareStatementGeneratedKeys(c, stmt) }, keyHolder)
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

    fun transaction(isolationLevel: Int = ISOLATION_DEFAULT, operations: () -> Unit) {
        val transactionManager = DataSourceTransactionManager(dataSource)
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isolationLevel = isolationLevel
        transactionTemplate.execute { status ->
            try {
                operations()
            } catch (e: Exception) {
                status.setRollbackOnly()
                throw e
            }
        }
    }

    fun transactionManual(isolationLevel: Int = ISOLATION_DEFAULT, operations: (status: TransactionStatus) -> Unit) {
        val transactionManager = DataSourceTransactionManager(dataSource)
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isolationLevel = isolationLevel
        transactionTemplate.execute { operations(it) }
    }

    private fun prepareStatementNoGeneratedKeys(c: Connection, stmt: SqlStatement): PreparedStatement {
        return prepareStatement(c, stmt, PreparedStatement.NO_GENERATED_KEYS)
    }

    private fun prepareStatementGeneratedKeys(c: Connection, stmt: SqlStatement): PreparedStatement {
        return prepareStatement(c, stmt, PreparedStatement.RETURN_GENERATED_KEYS)
    }

    private fun prepareStatement(c: Connection, stmt: SqlStatement, autoGeneratedKeys: Int): PreparedStatement {
        return stmt.params.withIndex().fold(c.prepareStatement(stmt.sql, autoGeneratedKeys), { ps, param -> ps.setObject(param.index + 1, param.value); ps })
    }
}
