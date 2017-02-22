# XML - XML for Kotlin

## Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:xml:-SNAPSHOT'
}
```

## Examples

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
assertEquals("<foo><id>2</id><bar>2</bar><bar>4</bar><bar>5</bar></foo>", xml)
assertEquals(2, fooB.id)
assertEquals(listOf(2, 4, 5), fooB.bars)
```
