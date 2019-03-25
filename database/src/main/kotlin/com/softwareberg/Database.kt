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

typealias ExtractorFn<T> = (Row) -> T

fun <T> createExtractor(extractor: ExtractorFn<T>): Extractor<T> = Extractor { extractor(it) }

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
        fun create(sql: String, vararg params: Param): NamedSqlStatement = create(sql, Params(params.toList()))

        @JvmStatic
        fun create(sql: String, params: List<Param>): NamedSqlStatement = create(sql, Params(params))

        @JvmStatic
        fun create(sql: String, params: Params): NamedSqlStatement = NamedSqlStatement(sql, params.params.map { it.name to it.value }.toMap())
    }

    data class Param(val name: String, val value: Any?) {
        companion object {
            @JvmStatic
            fun of(name: String, value: Any?): Param = Param(name, value)
        }
    }

    data class Params(val params: List<Param>) {
        companion object {
            @JvmStatic
            fun of(k1: String, v1: Any?): Params = Params(listOf(Param(k1, v1)))
            fun of(k1: String, v1: Any?, k2: String, v2: Any?): Params = Params(listOf(Param(k1, v1), Param(k2, v2)))
            fun of(k1: String, v1: Any?, k2: String, v2: Any?, k3: String, v3: Any?): Params = Params(listOf(Param(k1, v1), Param(k2, v2), Param(k3, v3)))
            fun of(k1: String, v1: Any?, k2: String, v2: Any?, k3: String, v3: Any?, k4: String, v4: Any?): Params = Params(listOf(Param(k1, v1), Param(k2, v2), Param(k3, v3), Param(k4, v4)))
            fun of(k1: String, v1: Any?, k2: String, v2: Any?, k3: String, v3: Any?, k4: String, v4: Any?, k5: String, v5: Any?): Params = Params(listOf(Param(k1, v1), Param(k2, v2), Param(k3, v3), Param(k4, v4), Param(k5, v5)))
            fun of(k1: String, v1: Any?, k2: String, v2: Any?, k3: String, v3: Any?, k4: String, v4: Any?, k5: String, v5: Any?, k6: String, v6: Any?): Params = Params(listOf(Param(k1, v1), Param(k2, v2), Param(k3, v3), Param(k4, v4), Param(k5, v5), Param(k6, v6)))
            fun of(k1: String, v1: Any?, k2: String, v2: Any?, k3: String, v3: Any?, k4: String, v4: Any?, k5: String, v5: Any?, k6: String, v6: Any?, k7: String, v7: Any?): Params = Params(listOf(Param(k1, v1), Param(k2, v2), Param(k3, v3), Param(k4, v4), Param(k5, v5), Param(k6, v6), Param(k7, v7)))
        }
    }
}

fun String.params(vararg params: Pair<String, Any?>): NamedSqlStatement = NamedSqlStatement(this, params.toMap())

class Database(private val dataSource: DataSource) {

    fun <T> findOne(stmt: SqlStatement, extractor: Extractor<T>): T? {
        return JdbcTemplate(dataSource).query(stmt.sql, stmt.params, ResultSetExtractor<T> { rs ->
            if (rs.next()) {
                extractor.extract(Row(rs))
            } else {
                null
            }
        })
    }

    fun <T> findOne(stmt: NamedSqlStatement, extractor: Extractor<T>): T? {
        return NamedParameterJdbcTemplate(dataSource).query(stmt.sql, stmt.params, ResultSetExtractor<T> { rs ->
            if (rs.next()) {
                extractor.extract(Row(rs))
            } else {
                null
            }
        })
    }

    fun <T> findOne(sql: String, extractor: Extractor<T>): T? = findOne(sql.paramsList(), extractor)

    fun <T> findAll(stmt: SqlStatement, extractor: Extractor<T>): List<T> {
        return JdbcTemplate(dataSource).query(stmt.sql, stmt.params) { rs, _ -> extractor.extract(Row(rs)) }
    }

    fun <T> findAll(stmt: NamedSqlStatement, extractor: Extractor<T>): List<T> {
        return NamedParameterJdbcTemplate(dataSource).query(stmt.sql, stmt.params) { rs, _ -> extractor.extract(Row(rs)) }
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

    @JvmOverloads
    fun transaction(isolationLevel: Int = ISOLATION_DEFAULT, operations: Transaction) {
        val transactionManager = DataSourceTransactionManager(dataSource)
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isolationLevel = isolationLevel
        transactionTemplate.execute { status ->
            try {
                operations.exec()
            } catch (e: Exception) {
                status.setRollbackOnly()
                throw e
            }
        }
    }

    @JvmOverloads
    fun transactionManual(isolationLevel: Int = ISOLATION_DEFAULT, operations: TransactionWithStatus) {
        val transactionManager = DataSourceTransactionManager(dataSource)
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isolationLevel = isolationLevel
        transactionTemplate.execute { operations.exec(it) }
    }

    private fun prepareStatementNoGeneratedKeys(c: Connection, stmt: SqlStatement): PreparedStatement {
        return prepareStatement(c, stmt, PreparedStatement.NO_GENERATED_KEYS)
    }

    private fun prepareStatementGeneratedKeys(c: Connection, stmt: SqlStatement): PreparedStatement {
        return prepareStatement(c, stmt, PreparedStatement.RETURN_GENERATED_KEYS)
    }

    private fun prepareStatement(c: Connection, stmt: SqlStatement, autoGeneratedKeys: Int): PreparedStatement {
        return stmt.params.withIndex().fold(c.prepareStatement(stmt.sql, autoGeneratedKeys)) { ps, param -> ps.setObject(param.index + 1, param.value); ps }
    }
}

fun <T> Database.findOne(stmt: SqlStatement, extractor: ExtractorFn<T>): T? = this.findOne(stmt, createExtractor(extractor))
fun <T> Database.findOne(stmt: NamedSqlStatement, extractor: ExtractorFn<T>): T? = this.findOne(stmt, createExtractor(extractor))
fun <T> Database.findOne(sql: String, extractor: ExtractorFn<T>): T? = this.findOne(sql, createExtractor(extractor))
fun <T> Database.findAll(stmt: SqlStatement, extractor: ExtractorFn<T>): List<T> = this.findAll(stmt, createExtractor(extractor))
fun <T> Database.findAll(stmt: NamedSqlStatement, extractor: ExtractorFn<T>): List<T> = this.findAll(stmt, createExtractor(extractor))
fun <T> Database.findAll(sql: String, extractor: ExtractorFn<T>): List<T> = this.findAll(sql, createExtractor(extractor))
fun Database.transaction(isolationLevel: Int = ISOLATION_DEFAULT, operations: () -> Unit) = this.transaction(isolationLevel, Transaction { operations() })
fun Database.transactionManual(isolationLevel: Int = ISOLATION_DEFAULT, operations: (TransactionStatus) -> Unit) = this.transactionManual(isolationLevel, TransactionWithStatus { operations(it) })
