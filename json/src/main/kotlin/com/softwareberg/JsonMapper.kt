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
        return objectMapper.readValue(json, T::class.java)
    }

    fun write(obj: Any): String = objectMapper.writeValueAsString(obj)
}
