# Rinca

[![Build Status](https://travis-ci.org/softwareberg/rinca.svg?branch=master)](https://travis-ci.org/softwareberg/rinca)
[![Coverage Status](https://codecov.io/github/softwareberg/rinca/badge.svg?branch=master)](https://codecov.io/github/softwareberg/rinca?branch=master)
[![JitPack](https://jitpack.io/v/softwareberg/rinca.svg)](https://jitpack.io/#softwareberg/rinca)

## [Database](https://github.com/softwareberg/rinca/tree/master/database)

```kotlin
val db = Database(dataSource)
val personExtractor: Extractor<Person> = { rs -> Person(rs.getInt("id"), rs.getString("name")) }
val person = db.findOne("SELECT id, name FROM people WHERE id = 1", personExtractor)
println("person: $person") // Person(id=1, name=Michal)
```

## [HTTP Client](https://github.com/softwareberg/rinca/tree/master/httpclient)

```kotlin
import com.softwareberg.HttpMethod.GET
import com.softwareberg.*

val httpClient = SimpleHttpClient.create()
val (statusCode, headers, body) = httpClient.execute(HttpRequest(GET, "http://urlecho.appspot.com/echo?body=HelloWorld")).join()
println("statusCode: $statusCode")
println("headers: $headers")
println("body: $body")
```
