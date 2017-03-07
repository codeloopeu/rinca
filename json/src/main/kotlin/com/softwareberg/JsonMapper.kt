package com.softwareberg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class JsonMapper(val objectMapper: ObjectMapper) {

    companion object {

        private val objectMapper = createObjectMapper()

        fun create(): JsonMapper = JsonMapper(objectMapper)

        private fun createObjectMapper(): ObjectMapper {
            val objectMapper = ObjectMapper().registerModule(KotlinModule())
            objectMapper.findAndRegisterModules()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }
    }

    inline fun <reified T> read(json: String): T {
        return read(json, T::class.java)
    }

    fun <T> read(json: String, valueType: Class<T>): T {
        return objectMapper.readValue(json, valueType)
    }

    fun write(obj: Any): String = objectMapper.writeValueAsString(obj)
}
