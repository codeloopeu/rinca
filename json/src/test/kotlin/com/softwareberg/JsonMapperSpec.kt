package com.softwareberg

import com.fasterxml.jackson.annotation.JsonProperty
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonMapperSpec {

    private data class HackerNews(val id: Int, val score: Int, val kids: List<Int> = emptyList(), val time: Int, val title: String, val text: String?, val url: String?, val type: String = "story")
    private data class Foo(@JsonProperty(value = "baz") val bar: String)

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
        assertThat(map["score"]).isEqualTo(61)
        assertThat(map["kids"]).isEqualTo(listOf(487171, 15, 234509, 454410, 82729))
        assertThat(map["title"]).isEqualTo("Y Combinator")
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
        assertThat(news.score).isEqualTo(61)
        assertThat(news.kids).containsExactly(487171, 15, 234509, 454410, 82729)
        assertThat(news.title).isEqualTo("Y Combinator")
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
        assertThat(news.score).isEqualTo(61)
        assertThat(news.kids).isEmpty()
        assertThat(news.title).isEqualTo("Y Combinator")
        assertThat(news.type).isEqualTo("story")
    }

    @Test
    fun `it should map json fields to domain class with different name mapping`() {
        // given
        val json = """{"baz": "foo"}"""
        // when
        val foo = jsonMapper.read<Foo>(json)
        // then
        assertThat(foo.bar).isEqualTo("foo")
    }

    @Test
    fun `it should map domain class to json`() {
        // given
        val hackerNews = HackerNews(2, 16, listOf(454411), 1160418628, "A Student's Guide to Startups", null, "http://www.paulgraham.com/mit.html", "story")
        // when
        val json = jsonMapper.write(hackerNews)
        // then
        assertThat(json).isEqualTo("""{"id":2,"score":16,"kids":[454411],"time":1160418628,"title":"A Student's Guide to Startups","text":null,"url":"http://www.paulgraham.com/mit.html","type":"story"}""")
    }

    @Test
    fun `it should map domain class to json with pretty print`() {
        // given
        val hackerNews = HackerNews(2, 16, listOf(454411), 1160418628, "A Student's Guide to Startups", null, "http://www.paulgraham.com/mit.html", "story")
        // when
        val json = jsonMapper.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hackerNews)
        // then
        assertThat(json).isEqualTo(
"""{
  "id" : 2,
  "score" : 16,
  "kids" : [ 454411 ],
  "time" : 1160418628,
  "title" : "A Student's Guide to Startups",
  "text" : null,
  "url" : "http://www.paulgraham.com/mit.html",
  "type" : "story"
}""")
    }
}
