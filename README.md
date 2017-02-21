# Rinca

[![Build Status](https://travis-ci.org/softwareberg/rinca.svg?branch=master)](https://travis-ci.org/softwareberg/rinca)
[![Coverage Status](https://codecov.io/github/softwareberg/rinca/badge.svg?branch=master)](https://codecov.io/github/softwareberg/rinca?branch=master)
[![JitPack](https://jitpack.io/v/softwareberg/rinca.svg)](https://jitpack.io/#softwareberg/rinca)

## [Database](https://github.com/softwareberg/rinca/tree/master/database)

### Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:database:0.3.+'
}
```

### Sample

```kotlin
val db = Database(dataSource)
val personExtractor: Extractor<Person> = { rs -> Person(rs.getInt("id"), rs.getString("name")) }
val person = db.findOne("SELECT id, name FROM people WHERE id = 1", personExtractor)
println("person: $person") // Person(id=1, name=Michal)
```

## [HTTP Client](https://github.com/softwareberg/rinca/tree/master/httpclient)

### Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:httpclient:0.3.+'
}
```

### Sample

```kotlin
import com.softwareberg.HttpMethod.GET
import com.softwareberg.*

val httpClient = SimpleHttpClient.create()
val (statusCode, headers, body) = httpClient.execute(HttpRequest(GET, "http://urlecho.appspot.com/echo?body=HelloWorld")).join()
println("statusCode: $statusCode")
println("headers: $headers")
println("body: $body")
```

## [JSON](https://github.com/softwareberg/rinca/tree/master/json)

### Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:json:0.3.+'
}
```

### Sample

```kotlin
val hackerNews = """
{
    "by": "pg",
    "descendants": 15,
    "id": 1,
    "kids": [
        487171,
        15,
        234509,
        454410,
        82729
    ],
    "score": 61,
    "time": 1160418111,
    "title": "Y Combinator",
    "type": "story",
    "url": "http://ycombinator.com"
}
"""

data class HackerNews(val id: Int, val score: Int, val kids: List<Int> = emptyList(), val time: Int, val title: String, val text: String?, val url: String?, val type: String = "story")

val jsonMapper = JsonMapper.create()
val news = jsonMapper.read<HackerNews>(hackerNews)

println("score: ${news.score}") // score: 61
println("kids: ${news.kids}") // kids: [487171, 15, 234509, 454410, 82729]
println("title: ${news.title}") // title: Y Combinator
```

## Building

### Build

```bash
gradle
```

### Code coverage

```bash
gradle jacocoTestReport
open build/reports/jacoco/test/html/index.html
```
