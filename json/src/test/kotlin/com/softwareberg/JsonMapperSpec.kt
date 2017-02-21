package com.softwareberg

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonMapperSpec {

    private data class HackerNews(val id: Int, val score: Int, val kids: List<Int> = emptyList(), val time: Int, val title: String, val text: String?, val url: String?, val type: String = "story")

    private val jsonMapper = JsonMapper.create()

    @Test
    fun `it should map json to map`() {
        // givne
        val exampleHackerNews = """
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
        // when
        val map = jsonMapper.read<Map<String, Any>>(exampleHackerNews)
        // then
        assertEquals(61, map["score"])
        assertEquals(listOf(487171, 15, 234509, 454410, 82729), map["kids"])
        assertEquals("Y Combinator", map["title"])
    }

    @Test
    fun `it should map json to domain class`() {
        val exampleHackerNews = """
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
        // when
        val news = jsonMapper.read<HackerNews>(exampleHackerNews)
        // then
        assertEquals(61, news.score)
        assertEquals(listOf(487171, 15, 234509, 454410, 82729), news.kids)
        assertEquals("Y Combinator", news.title)
    }

    @Test
    fun `it should map json to domain class and set defaults`() {
        // given
        val exampleHackerNews = """
{
    "by": "pg",
    "descendants": 15,
    "id": 1,
    "score": 61,
    "time": 1160418111,
    "title": "Y Combinator",
    "url": "http://ycombinator.com"
}
"""
        // when
        val news = jsonMapper.read<HackerNews>(exampleHackerNews)
        // then
        assertEquals(61, news.score)
        assertEquals(emptyList<Int>(), news.kids)
        assertEquals("Y Combinator", news.title)
        assertEquals("story", news.type)
    }

    @Test
    fun `it should map json to domain class and ignore unknown fields`() {
        // given
        val exampleHackerNews = """
{
    "by": "pg",
    "descendants": 15,
    "id": 1,
    "score": 61,
    "time": 1160418111,
    "title": "Y Combinator",
    "url": "http://ycombinator.com",
    "tmp": "tmp"
}
"""
        // when
        val news = jsonMapper.read<HackerNews>(exampleHackerNews)
        // then
        assertEquals("Y Combinator", news.title)
    }

    @Test
    fun `it should map domain class to json`() {
        // given
        val hackerNews = HackerNews(2, 16, listOf(454411), 1160418628, "A Student's Guide to Startups", null, "http://www.paulgraham.com/mit.html", "story")
        // when
        val json = jsonMapper.write(hackerNews)
        // then
        assertEquals("""{"id":2,"score":16,"kids":[454411],"time":1160418628,"title":"A Student's Guide to Startups","text":null,"url":"http://www.paulgraham.com/mit.html","type":"story"}""", json)
    }

    @Test
    fun `it should map domain class to json with pretty print`() {
        // given
        val hackerNews = HackerNews(2, 16, listOf(454411), 1160418628, "A Student's Guide to Startups", null, "http://www.paulgraham.com/mit.html", "story")
        // when
        val json = jsonMapper.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hackerNews)
        // then
        assertEquals("""{
  "id" : 2,
  "score" : 16,
  "kids" : [ 454411 ],
  "time" : 1160418628,
  "title" : "A Student's Guide to Startups",
  "text" : null,
  "url" : "http://www.paulgraham.com/mit.html",
  "type" : "story"
}""", json)
    }

    @Test
    fun `dadfsdfit should map json to domain class`() {

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
        val news = jsonMapper.read<HackerNews>(hackerNews)
        println("score: ${news.score}") // score: 61
        println("kids: ${news.kids}") // kids: [487171, 15, 234509, 454410, 82729]
        println("title: ${news.title}") // title: Y Combinator
    }
}
