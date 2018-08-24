package com.softwareberg

import com.ninja_squad.dbsetup.DbSetup
import com.ninja_squad.dbsetup.Operations
import com.ninja_squad.dbsetup.Operations.deleteAllFrom
import com.ninja_squad.dbsetup.Operations.insertInto
import com.ninja_squad.dbsetup.destination.DataSourceDestination
import com.ninja_squad.dbsetup.operation.Operation
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.h2.tools.Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import javax.sql.DataSource

class DatabaseSpec {

    private data class Person(val id: Int, val name: String?)

    companion object {

        private val databaseH2Server = Server.createTcpServer().start()
        private val dataSource = initH2()
        private val database = Database(dataSource)

        private fun initMySql(): HikariDataSource {
            return initDatabse("mysql")
        }

        private fun initPostgreSql(): HikariDataSource {
            return initDatabse("postgresql")
        }

        private fun initH2(): HikariDataSource {
            return initDatabse("h2")
        }

        private fun initDatabse(databaseName: String): HikariDataSource {
            val dataSource = createLocalDataSource(databaseName)
            cleanAndMigrate(databaseName, dataSource)
            return dataSource
        }

        @AfterAll
        @JvmStatic
        fun `it should close database`() {
            dataSource.close()
            databaseH2Server.stop()
        }

        private fun createLocalDataSource(databaseName: String): HikariDataSource {
            val config = HikariConfig("/db/datasource.$databaseName.properties")
            return HikariDataSource(config)
        }

        private fun cleanAndMigrate(databaseName: String, dataSource: DataSource) {
            val flyway = Flyway()
            flyway.dataSource = dataSource
            flyway.configure(mapOf("flyway.locations" to "db/migration/$databaseName"))
            flyway.clean()
            flyway.migrate()
        }
    }

    private val deleteAll = deleteAllFrom("people")
    private val nameExtractor: Extractor<String?> = { rs -> rs.stringOrNull("name") }
    private val idExtractor: Extractor<Int> = { rs -> rs.int("id") }
    private val personExtractor: Extractor<Person> = { rs -> Person(rs.int("id"), rs.stringOrNull("name")) }

    @Test
    fun `it should find people by id`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(1 to "Michal", 2 to "Kasia", 3 to "Zuzia")
        )

        // when
        val nameA = database.findOne("SELECT name FROM people WHERE id = 1", nameExtractor)
        val nameB = database.findOne("SELECT name FROM people WHERE id = ?".paramsList(2), nameExtractor)
        val nameC = database.findOne("SELECT name FROM people WHERE id = :id".params("id" to 3), nameExtractor)

        // then
        assertThat(nameA).isEqualTo("Michal")
        assertThat(nameB).isEqualTo("Kasia")
        assertThat(nameC).isEqualTo("Zuzia")
    }

    @Test
    fun `it should map row to data class with custom extractor`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(2 to "Kasia")
        )

        // when
        val person = database.findOne("SELECT id, name FROM people WHERE id = :id".params("id" to 2)) { rs -> Person(rs.int("id"), rs.string("name")) }

        // then
        assertThat(person).isEqualTo(Person(2, "Kasia"))
    }

    @Test
    fun `it should map row to data class`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(2 to "Kasia")
        )

        // when
        val person = database.findOne("SELECT id, name FROM people WHERE id = :id".params("id" to 2), personExtractor)

        // then
        assertThat(person).isEqualTo(Person(2, "Kasia"))
    }

    @Test
    fun `it should return null if person does not exist`() {
        // given
        prepareDatabase(
            deleteAll
        )

        // when
        val nonExistingNameA = database.findOne("SELECT name FROM people WHERE id = 3", nameExtractor)
        val nonExistingNameB = database.findOne("SELECT name FROM people WHERE id = ?".paramsList(3), nameExtractor)
        val nonExistingNameC = database.findOne("SELECT name FROM people WHERE id = :id".params("id" to 3), nameExtractor)

        // then
        assertThat(nonExistingNameA).isNull()
        assertThat(nonExistingNameB).isNull()
        assertThat(nonExistingNameC).isNull()
    }

    @Test
    fun `it should return list of people`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(1 to "Michal", 2 to "Kasia")
        )

        // when
        val people = database.findAll("SELECT name FROM people", nameExtractor)

        // then
        assertThat(people).containsExactly("Michal", "Kasia")
    }

    @Test
    fun `it should return list of ids of people with name "Michal"`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(3 to "Ania", 4 to "Michal", 7 to "Michal")
        )

        // when
        val idsA = database.findAll("SELECT id FROM people WHERE name = 'Michal'", idExtractor)
        val idsB = database.findAll("SELECT id FROM people WHERE name = ?".paramsList("Michal"), idExtractor)
        val idsC = database.findAll("SELECT id FROM people WHERE name = :name".params("name" to "Michal"), idExtractor)

        // then
        assertThat(idsA).containsExactly(4, 7)
        assertThat(idsB).containsExactly(4, 7)
        assertThat(idsC).containsExactly(4, 7)
    }

    @Test
    fun `it should return list of ids of people with name "Ola" (no result)`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(1 to "Michal", 2 to "Kasia")
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
            deleteAll
        )

        // when
        database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 3, "name" to "Michal"))
        database.insert("INSERT INTO people (id, name) VALUES (?, ?)".paramsList(7, "Kasia"))
        database.insert("INSERT INTO people (id, name) VALUES (8, 'Bob')")
        val people = database.findAll("SELECT id, name FROM people", personExtractor)

        // then
        assertThat(people).containsExactlyInAnyOrder(Person(3, "Michal"), Person(7, "Kasia"), Person(8, "Bob"))
    }

    @Test
    fun `it should insert person with nullable name`() {
        // given
        prepareDatabase(
            deleteAll
        )

        // when
        database.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 3, "name" to null))
        database.insert("INSERT INTO people (id, name) VALUES (?, ?)".paramsList(7, null))
        database.insert("INSERT INTO people (id, name) VALUES (8, NULL)")
        val names = database.findAll("SELECT id, name FROM people", nameExtractor)

        // then
        assertThat(names).containsExactly(null, null, null)
    }

    @Test
    fun `it should insert person and get id`() {
        // given
        prepareDatabase(
            deleteAll
        )

        // when
        val keys = database.insert("INSERT INTO people (name) VALUES (:name)".params("name" to "Michal"))
        val id = keys.toList().first().second.toString().toIntOrNull()

        // then
        assertThat(id).isNotNull()
    }

    @Test
    fun `it should update people`() {
        // given
        prepareDatabase(
            deleteAll,
            insertPeople(1 to "Ania", 2 to "Michal", 3 to "Michal", 4 to "Karol")
        )

        // when
        database.update("UPDATE people SET name = 'Szymon' WHERE id = 1")
        database.update("UPDATE people SET name = 'Piotr' WHERE id = ?".paramsList(2))
        database.update("UPDATE people SET name = 'Zofia' WHERE id = :id".params("id" to 3))
        val people = findNamesOrderedById()

        // then
        assertThat(people).containsExactly("Szymon", "Piotr", "Zofia", "Karol")
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
        assertThat(people).containsExactly("Kasia", "Michal")
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

    private fun findNamesOrderedById(): List<String> = database.findAll("SELECT name FROM people ORDER BY id", nameExtractor).filterNotNull()

    private fun prepareDatabase(vararg operations: Operation) {
        val sequenceOfOperations = Operations.sequenceOf(operations.toList())
        DbSetup(DataSourceDestination(dataSource), sequenceOfOperations).launch()
    }

    private fun insertPeople(vararg people: Pair<Int, String>): Operation {
        return people.fold(insertInto("people").columns("id", "name")) { acc, e -> acc.values(e.first, e.second) }.build()
    }
}
