package com.automation.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> T fromJson(String json, TypeReference<T> typeReference, String errorMessage) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }
}
