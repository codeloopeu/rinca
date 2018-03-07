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
    compile 'com.github.softwareberg.rinca:database:0.7.1'
}
```

### Sample

```kotlin
val db = Database(dataSource)
val personName = db.findOne("SELECT id, name FROM people WHERE id = 1", { rs -> rs.string("name") })
println("person name: personName")
```

## [HTTP Client](https://github.com/softwareberg/rinca/tree/master/httpclient)

### Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:httpclient:0.7.1'
}
```

### Sample

```kotlin
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
    compile 'com.github.softwareberg.rinca:json:0.7.1'
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

## [XML](https://github.com/softwareberg/rinca/tree/master/xml)

### Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:xml:0.7.1'
}
```

### Sample

```kotlin
@JacksonXmlRootElement(localName = "foo")
private class Foo {
    var id: Int = 0
    @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("bar") var bars: MutableList<Int> = mutableListOf()

    @JsonSetter("bar")
    fun addBar(bar: Int) {
        this.bars.add(bar)
    }
}

// given
val fooA = Foo()
fooA.id = 2
fooA.bars = mutableListOf(2, 4, 5)
// when
val xml = xmlMapper.write(fooA)
val fooB = xmlMapper.read<Foo>(xml)
// then
assertThat(xml).isEqualTo("<foo><id>2</id><bar>2</bar><bar>4</bar><bar>5</bar></foo>")
assertThat(fooB.id).isEqualTo(2)
assertThat(fooB.bars).containsExactly(2, 4, 5).inOrder()
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
