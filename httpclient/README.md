# HTTP Client - HTTP Client for Kotlin

## Install

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.softwareberg.rinca:httpclient:-SNAPSHOT'
}
```

## Examples

```kotlin
import com.softwareberg.HttpMethod.GET
import com.softwareberg.*

val httpClient = SimpleHttpClient.create()
val (statusCode, headers, body) = httpClient.execute(HttpRequest(GET, "http://urlecho.appspot.com/echo?body=HelloWorld")).join()
println("statusCode: $statusCode")
println("headers: $headers")
println("body: $body")
```

### Links

* [Asynchronous Http and WebSocket Client library for Java](https://github.com/AsyncHttpClient/async-http-client)
