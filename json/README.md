# JSON - JSON for Kotlin

## Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:json:0.11.0'
}
```

## Examples

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
