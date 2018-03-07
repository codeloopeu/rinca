# Database - JDBC for Kotlin

## Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:database:-SNAPSHOT'
}
```

## Examples

### Simple

```kotlin
val db = Database(dataSource)

val id = db.findOne("SELECT id FROM people WHERE name = :name".params("name" to "Michal"), { rs -> rs.int("id") })
val names = db.findAll("SELECT name FROM people", { rs -> rs.string("name") })
val insertedId = db.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 3, "name" to "Michal"))["id"]
val rowsAffectedCount = db.update("UPDATE people SET name = 'Piotr' WHERE id = ?".paramsList(2))

println("id: $id") // id: 1
println("names: $names") // names: [Michal, Kasia]
println("insertedId: $insertedId") // insertedId: 3
println("rowsAffectedCount: $rowsAffectedCount") // rowsAffectedCount: 1
```

### Data class mapping

```kotlin
data class Person(val id: Int, val name: String)
val personExtractor = createExtractor { rs -> Person(id = rs.int("id"), name = rs.string("name")) }

val db = Database(dataSource)
val person = db.findOne("SELECT id, name FROM people WHERE id = 1", personExtractor)
println("person: $person") // Person(id=1, name=Michal)
```

### Transactions

To start transaction you could use `database.transaction`. For example:

```kotlin
val db = Database(dataSource)
db.transaction {
    insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 1, "name" to "Kasia"))
                insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 2, "name" to "Michal"))
}
```

If all operation succeeded, then transaction is committed, otherwise rollback is executed.

Please note that during one transaction we must use the same connection. This is why, in the previous example, you should use `insert`, instead of `db.insert`. Under the hood, `transaction` block create new `com.softwareberg.Database` with `DataSource` that has only one connection. The connection is shared between operation and it has `autoCommit` set to false.

## Docker

```bash
docker run --name softwareberg-postgres-db -p 5432:5432 -e POSTGRES_USER=softwareberg -e POSTGRES_PASSWORD=softwareberg -d postgres:9.6
```

## Links

* https://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html
* https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
* http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/namedparam/NamedParameterJdbcTemplate.html
* http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html
* https://github.com/andrewoma/kwery
* http://dbsetup.ninja-squad.com/user-guide.html
