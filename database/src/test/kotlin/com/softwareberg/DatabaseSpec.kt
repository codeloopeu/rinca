package com.softwareberg

import com.google.common.truth.Truth.assertThat
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
import org.junit.BeforeClass
import org.junit.Test

class DatabaseSpec {

    private data class Person(val id: Int, val name: String)

    companion object {

        private val databaseH2Server = Server.createTcpServer().start()
        private val dataSource = createLocalDataSource()
        private val database = Database(dataSource)

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
        prepareDatabase(
            deleteAll,
            insertTwoPeople
        )
        // when
        val nameA = database.findOne("SELECT name FROM people WHERE id = 1", nameExtractor)
        val nameB = database.findOne("SELECT name FROM people WHERE id = ?".paramsList(1), nameExtractor)
        val nameC = database.findOne("SELECT name FROM people WHERE id = :id".params("id" to 2), nameExtractor)
        // then
        assertThat(nameA).isEqualTo("Michal")
        assertThat(nameB).isEqualTo("Michal")
        assertThat(nameC).isEqualTo("Kasia")
    }

    @Test
    fun `it should map row to data class`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople
        )
        // when
        val person = database.findOne("SELECT id, name FROM people WHERE id = :id".params("id" to 2), { rs -> Person(rs.int("id"), rs.string("name")) })
        // then
        assertThat(person).isEqualTo(Person(2, "Kasia"))
    }

    @Test
    fun `it should map row to data class with external extractor`() {
        // given
        val personExtractor = createExtractor { rs -> Person(id = rs.int("id"), name = rs.string("name")) }
        prepareDatabase(
            deleteAll,
            insertTwoPeople
        )
        // when
        val person = database.findOne("SELECT id, name FROM people WHERE id = :id".params("id" to 2), personExtractor)
        // then
        assertThat(person).isEqualTo(Person(2, "Kasia"))
    }

    @Test
    fun `it should return null if person does not exsist`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople
        )
        // when
        val nonExsistingNameA = database.findOne("SELECT name FROM people WHERE id = 3", nameExtractor)
        val nonExsistingNameB = database.findOne("SELECT name FROM people WHERE id = ?".paramsList(3), nameExtractor)
        val nonExsistingNameC = database.findOne("SELECT name FROM people WHERE id = :id".params("id" to 3), nameExtractor)
        // then
        assertThat(nonExsistingNameA).isNull()
        assertThat(nonExsistingNameB).isNull()
        assertThat(nonExsistingNameC).isNull()
    }

    @Test
    fun `it should return list of people`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople
        )
        // when
        val people = database.findAll("SELECT name FROM people", nameExtractor)
        // then
        assertThat(people).containsExactly("Michal", "Kasia").inOrder()
    }

    @Test
    fun `it should return list of ids of people with name "Michal"`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople,
            insertPeopleHelper(listOf(3 to "Ania", 4 to "Michal", 7 to "Michal"))
        )
        // when
        val idsA = database.findAll("SELECT id FROM people WHERE name = 'Michal'", idExtractor)
        val idsB = database.findAll("SELECT id FROM people WHERE name = ?".paramsList("Michal"), idExtractor)
        val idsC = database.findAll("SELECT id FROM people WHERE name = :name".params("name" to "Michal"), idExtractor)
        // then
        assertThat(idsA).containsExactly(1, 4, 7).inOrder()
        assertThat(idsB).containsExactly(1, 4, 7).inOrder()
        assertThat(idsC).containsExactly(1, 4, 7).inOrder()
    }

    @Test
    fun `it should return list of ids of people with name "Ola"`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople,
            insertPeopleHelper(listOf(3 to "Ania", 4 to "Michal", 7 to "Michal"))
        )
        // when
        val ids = database.findAll("SELECT id FROM people WHERE name = :name".params("name" to "Ola"), idExtractor)
        // then
        assertThat(ids).isEmpty()
    }

    @Test
    fun `it should insert person`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople
        )
        // when
        database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 3, "name" to "Michal"))
        database.insert("INSERT INTO people (id, name) VALUES (?, ?)".paramsList(7, "Michal"))
        database.insert("INSERT INTO people (id, name) VALUES (8, 'Michal')")
        val ids = database.findAll("SELECT id FROM people WHERE name = ?".paramsList("Michal"), idExtractor)
        // then
        assertThat(ids).containsExactly(1, 3, 7, 8).inOrder()
    }

    @Test
    fun `it should update people`() {
        // given
        prepareDatabase(
            deleteAll,
            insertTwoPeople,
            insertPeopleHelper(listOf(3 to "Ania", 4 to "Michal", 7 to "Michal"))
        )
        // when
        database.update("UPDATE people SET name = 'Szymon' WHERE id = 1")
        database.update("UPDATE people SET name = 'Piotr' WHERE id = ?".paramsList(2))
        database.update("UPDATE people SET name = 'Zofia' WHERE id = :id".params("id" to 3))
        val people = findNamesOrderedById()
        // then
        assertThat(people).containsExactly("Szymon", "Piotr", "Zofia", "Michal", "Michal").inOrder()
    }

    @Test
    fun `it should commit transaction on success`() {
        // given
        prepareDatabase(
            deleteAll
        )
        // when
        database.transaction {
            database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 1, "name" to "Kasia"))
            database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 2, "name" to "Michal"))
        }
        val people = findNamesOrderedById()
        // then
        assertThat(people).containsExactly("Kasia", "Michal").inOrder()
    }

    @Test
    fun `it should rollback transaction on failure`() {
        // given
        prepareDatabase(
            deleteAll
        )
        // when
        database.transaction {
            database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 1, "name" to "Kasia"))
            database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 1, "name" to "Michal"))
        }
        val people = findNamesOrderedById()
        // then
        assertThat(people).isEmpty()
    }

    private fun findNamesOrderedById(): List<String> = database.findAll("SELECT name FROM people ORDER BY id", nameExtractor)

    private fun prepareDatabase(vararg operations: Operation) {
        val sequenceOfOperations = Operations.sequenceOf(operations.toList())
        DbSetup(DataSourceDestination(dataSource), sequenceOfOperations).launch()
    }

    private fun insertPeopleHelper(people: List<Pair<Int, String>>): Operation {
        return people.fold(insertInto("People").columns("id", "name"), { acc, e -> acc.values(e.first, e.second) }).build()
    }
}
