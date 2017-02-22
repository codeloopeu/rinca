package com.softwareberg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.xml.bind.annotation.XmlAttribute

class XmlMapperSpec {

    private val xmlMapper = XmlMapper.create()

    @JacksonXmlRootElement(localName = "news")
    private class HackerNews {
        @XmlAttribute var id: Int = 0
        @XmlAttribute var score: Int = 0
        @JacksonXmlElementWrapper(localName = "kids", useWrapping = true) @JsonProperty("kid") var kids: List<Int> = emptyList()
        @JacksonXmlProperty(isAttribute = true, localName = "timestamp") var time: Int = 0
        var title: String = ""
        var text: String? = null
        var url: String? = null
        @XmlAttribute var type: String = "story"
    }

    @JacksonXmlRootElement(localName = "foo")
    private class Foo {
        var id: Int = 0
        @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("bar") var bars: MutableList<Int> = mutableListOf()

        @JsonSetter("bar")
        fun addBar(bar: Int) {
            this.bars.add(bar)
        }
    }

    @Test
    fun `it should map xml to map`() {
        // givne
        val exampleHackerNews = """
<news id="1" score="61" timestamp="1160418111" type="story" by="pg">
  <kids>
    <kid>487171</kid>
    <kid>15</kid>
    <kid>234509</kid>
    <kid>454410</kid>
    <kid>82729</kid>
  </kids>
  <title>Y Combinator</title>
  <url>http://ycombinator.com</url>
</news>
"""
        // when
        val map = xmlMapper.read<Map<String, Any>>(exampleHackerNews)
        // then
        assertEquals("61", map["score"])
        assertEquals("{kid=82729}", map["kids"].toString())
        assertEquals("Y Combinator", map["title"])
    }

    @Test
    fun `it should map xml to domain class`() {
        val exampleHackerNews = """
<news id="1" score="61" timestamp="1160418111" type="story" by="pg">
  <kids>
    <kid>487171</kid>
    <kid>15</kid>
    <kid>234509</kid>
    <kid>454410</kid>
    <kid>82729</kid>
  </kids>
  <title>Y Combinator</title>
  <url>http://ycombinator.com</url>
</news>
"""        // when
        val news = xmlMapper.read<HackerNews>(exampleHackerNews)
        // then
        assertEquals(61, news.score)
        assertEquals(listOf(487171, 15, 234509, 454410, 82729), news.kids)
        assertEquals("Y Combinator", news.title)
        assertEquals(null, news.text)
    }

    @Test
    fun `it should serialize and deserialize xml without wrapping`() {
        // given
        val fooA = Foo()
        fooA.id = 2
        fooA.bars = mutableListOf(2, 4, 5)
        // when
        val xml = xmlMapper.write(fooA)
        val fooB = xmlMapper.read<Foo>(xml)
        // then
        assertEquals("<foo><id>2</id><bar>2</bar><bar>4</bar><bar>5</bar></foo>", xml)
        assertEquals(2, fooB.id)
        assertEquals(listOf(2, 4, 5), fooB.bars)
    }

    @Test
    fun `it should deserialize xml without wrapping with element in the middle`() {
        // given
        val xml = "<foo><bar>2</bar><id>2</id><bar>4</bar><bar>5</bar></foo>"
        // when
        val foo = xmlMapper.read<Foo>(xml)
        // then
        assertEquals(2, foo.id)
        assertEquals(listOf(2, 4, 5), foo.bars)
    }

    @Test
    fun `it should map domain class to xml`() {
        // given
        val hackerNews = HackerNews()
        hackerNews.id = 2
        hackerNews.score = 16
        hackerNews.kids = listOf(454411, 44423)
        hackerNews.time = 1160418628
        hackerNews.title = "A Student's Guide to Startups"
        hackerNews.url = "http://www.paulgraham.com/mit.html"
        // when
        val xml = xmlMapper.write(hackerNews)
        // then
        assertEquals("""<news id="2" score="16" type="story" timestamp="1160418628"><title>A Student's Guide to Startups</title><text/><url>http://www.paulgraham.com/mit.html</url><kids><kid>454411</kid><kid>44423</kid></kids></news>""", xml)
    }

    @Test
    fun `it should map domain class to xml with pretty print`() {
        // given
        val hackerNews = HackerNews()
        hackerNews.id = 2
        hackerNews.score = 16
        hackerNews.kids = listOf(454411, 44423)
        hackerNews.time = 1160418628
        hackerNews.title = "A Student's Guide to Startups"
        hackerNews.url = "http://www.paulgraham.com/mit.html"
        // when
        val xml = xmlMapper.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hackerNews)
        // then
        assertEquals("""<news id="2" score="16" type="story" timestamp="1160418628">
  <title>A Student's Guide to Startups</title>
  <text/>
  <url>http://www.paulgraham.com/mit.html</url>
  <kids>
    <kid>454411</kid>
    <kid>44423</kid>
  </kids>
</news>
""", xml)
    }


}
