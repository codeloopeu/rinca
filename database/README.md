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

val id = db.findOne("SELECT id FROM people WHERE name = :name".params("name" to "Michal"), { rs -> rs.getInt("id") })
val names = db.findAll("SELECT name FROM people", { rs -> rs.getString("name") })
val insertedId = db.insert("INSERT INTO people (id, name) VALUES (:id, :name)".params("id" to 3, "name" to "Michal"))["id"]
val rowsAffectedCount = db.update("UPDATE people SET name = 'Piotr' WHERE id = ?".paramsList(2))

println("id: $id") // id: 1
println("names: $names") // names: [Michal, Kasia]
println("insertedId: $insertedId") // insertedId: 3
println("rowsAffectedCount: $rowsAffectedCount") // rowsAffectedCount: 1
```

### Data class mapping

```kotlin
val db = Database(dataSource)
val personExtractor: Extractor<Person> = { rs -> Person(rs.getInt("id"), rs.getString("name")) }
val person = db.findOne("SELECT id, name FROM people WHERE id = 1", personExtractor)
println("person: $person") // Person(id=1, name=Michal)
```

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
