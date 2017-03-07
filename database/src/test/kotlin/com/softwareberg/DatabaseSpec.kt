package com.softwareberg

import com.ninja_squad.dbsetup.DbSetup
import com.ninja_squad.dbsetup.Operations
import com.ninja_squad.dbsetup.Operations.deleteAllFrom
import com.ninja_squad.dbsetup.Operations.insertInto
import com.ninja_squad.dbsetup.destination.DataSourceDestination
import com.ninja_squad.dbsetup.operation.Operation
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.h2.tools.Server
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.BeforeClass
import org.junit.Test

class DatabaseSpec {

    private data class Person(val id: Int, val name: String)

    companion object {

        private val databaseH2Server = Server.createTcpServer().start()
        private val dataSource = createLocalDataSource()

        @BeforeClass
        @JvmStatic
        fun `it should init database`() {
            val flyway = Flyway()
            flyway.dataSource = dataSource
            flyway.clean()
            flyway.migrate()
        }

        @AfterClass
        @JvmStatic
        fun `it should close database`() {
            dataSource.close()
            databaseH2Server.stop()
        }

        private fun createLocalDataSource(): HikariDataSource {
            val config = HikariConfig("/db/datasource.h2.properties")
            return HikariDataSource(config)
        }
    }

    private val deleteAll = deleteAllFrom("People")
    private val insertTwoPeople = insertInto("People").columns("id", "name").values(1, "Michal").values(2, "Kasia").build()
    private val nameExtractor: Extractor<String> = { rs -> rs.string("name") }
    private val idExtractor: Extractor<Int> = { rs -> rs.int("id") }

    @Test
    fun `it should find people by id`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople))
        // when
        val nameA = Database(dataSource).findOne("SELECT name FROM people WHERE id = 1", nameExtractor)
        val nameB = Database(dataSource).findOne("SELECT name FROM people WHERE id = ?".paramsList(1), nameExtractor)
        val nameC = Database(dataSource).findOne("SELECT name FROM people WHERE id = :id".params("id" to 2), nameExtractor)
        // then
        assertEquals("Michal", nameA)
        assertEquals("Michal", nameB)
        assertEquals("Kasia", nameC)
    }

    @Test
    fun `it should map row to data class`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople))
        // when
        val person = Database(dataSource).findOne("SELECT id, name FROM people WHERE id = :id".params("id" to 2), { rs -> Person(rs.int("id"), rs.string("name")) })
        // then
        assertEquals(Person(2, "Kasia"), person)
    }

    @Test
    fun `it should return null if person does not exsist`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople))
        // when
        val nonExsistingNameA = Database(dataSource).findOne("SELECT name FROM people WHERE id = 3", nameExtractor)
        val nonExsistingNameB = Database(dataSource).findOne("SELECT name FROM people WHERE id = ?".paramsList(3), nameExtractor)
        val nonExsistingNameC = Database(dataSource).findOne("SELECT name FROM people WHERE id = :id".params("id" to 3), nameExtractor)
        // then
        assertNull(nonExsistingNameA)
        assertNull(nonExsistingNameB)
        assertNull(nonExsistingNameC)
    }

    @Test
    fun `it should return list of people`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople))
        // when
        val people = Database(dataSource).findAll("SELECT name FROM people", nameExtractor)
        // then
        assertEquals(listOf("Michal", "Kasia"), people)
    }

    @Test
    fun `it should return list of ids of people with name "Michal"`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople, insertPeopleHelper(listOf(3 to "Ania", 4 to "Michal", 7 to "Michal"))))
        // when
        val idsA = Database(dataSource).findAll("SELECT id FROM people WHERE name = 'Michal'", idExtractor)
        val idsB = Database(dataSource).findAll("SELECT id FROM people WHERE name = ?".paramsList("Michal"), idExtractor)
        val idsC = Database(dataSource).findAll("SELECT id FROM people WHERE name = :name".params("name" to "Michal"), idExtractor)
        // then
        assertEquals(listOf(1, 4, 7), idsA)
        assertEquals(listOf(1, 4, 7), idsB)
        assertEquals(listOf(1, 4, 7), idsC)
    }

    @Test
    fun `it should return list of ids of people with name "Ola"`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople, insertPeopleHelper(listOf(3 to "Ania", 4 to "Michal", 7 to "Michal"))))
        // when
        val ids = Database(dataSource).findAll("SELECT id FROM people WHERE name = :name".params("name" to "Ola"), idExtractor)
        // then
        assertEquals(emptyList<Int>(), ids)
    }

    @Test
    fun `it should insert person`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople))
        // when
        Database(dataSource).insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 3, "name" to "Michal"))
        Database(dataSource).insert("INSERT INTO people (id, name) VALUES (?, ?)".paramsList(7, "Michal"))
        Database(dataSource).insert("INSERT INTO people (id, name) VALUES (8, 'Michal')")
        val ids = Database(dataSource).findAll("SELECT id FROM people WHERE name = ?".paramsList("Michal"), idExtractor)
        // then
        assertEquals(listOf(1, 3, 7, 8), ids)
    }

    @Test
    fun `it should update people`() {
        // given
        prepareDatabase(Operations.sequenceOf(deleteAll, insertTwoPeople, insertPeopleHelper(listOf(3 to "Ania", 4 to "Michal", 7 to "Michal"))))
        // when
        Database(dataSource).update("UPDATE people SET name = 'Szymon' WHERE id = 1")
        Database(dataSource).update("UPDATE people SET name = 'Piotr' WHERE id = ?".paramsList(2))
        Database(dataSource).update("UPDATE people SET name = 'Zofia' WHERE id = :id".params("id" to 3))
        val people = Database(dataSource).findAll("SELECT name FROM people ORDER BY id", nameExtractor)
        // then
        assertEquals(listOf("Szymon", "Piotr", "Zofia", "Michal", "Michal"), people)
    }

    private fun prepareDatabase(operation: Operation) {
        DbSetup(DataSourceDestination(dataSource), operation).launch()
    }

    private fun insertPeopleHelper(people: List<Pair<Int, String>>): Operation {
        return people.fold(insertInto("People").columns("id", "name"), { acc, e -> acc.values(e.first, e.second) }).build()
    }
}
