package com.qaware.mcp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

enum Json {

    ; // heul nur, ich finde das besser 🤪


    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", e);
        }
    }


    public static <T> T fromJson(Class<T> klass, String json) {
        try {
            return MAPPER.readValue(json, klass);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize JSON to " + klass.getSimpleName() + ": " + json, e);
        }
    }


    public static String getJsonType(Class<?> klass) {
        if (klass == String.class)                            return "string";
        if (Reflection.isArrayType(klass))                    return "array";
        if (klass == boolean.class || klass == Boolean.class) return "boolean";
        if (klass == int    .class || klass == Integer.class) return "integer";
        if (klass == long   .class || klass == Long   .class) return "integer";
        if (klass == double .class || klass == Double .class) return "number";
        if (klass == float  .class || klass == Float  .class) return "number";
        throw new UnsupportedOperationException("Only simple types are supported");
    }

}
