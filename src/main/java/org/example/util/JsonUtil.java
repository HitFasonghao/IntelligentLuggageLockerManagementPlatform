package org.example.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;

import io.micrometer.common.util.StringUtils;
import org.example.constants.CodecConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author fasonghao
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final String EMPTY = "";


    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        if (clazz == null || json == null) {
            return null;
        }

        try {
            return CodecConstants.OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("error transfer to object , param {}, obj {}", json, clazz.getTypeName(), e);
        }
        return null;
    }

    public static <T> T toObject(String json, JavaType clazz) {
        if (clazz == null || StringUtils.isBlank(json)) {
            return null;
        }

        T result = null;
        try {
            result = CodecConstants.OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("error transfer to object , param {}, obj {}", json, clazz.getTypeName(), e);
        }
        return result;
    }

    public static <T> T[] toArray(String json, Class<T[]> arrayClass) {
        if (json == null || arrayClass == null) {
            return null;
        }

        try {
            return CodecConstants.OBJECT_MAPPER.readValue(json, arrayClass);
        } catch (IOException e) {
            logger.error("Error converting JSON to array", e);
        }
        return null;
    }


    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        String result = EMPTY;
        try {
            result = CodecConstants.OBJECT_MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("error transfer to json , param {}", obj, e);
        }

        return result;
    }

    public static JsonNode readTree(String obj) {
        if (obj == null) {
            return null;
        }

        JsonNode result = null;
        try {
            result = CodecConstants.OBJECT_MAPPER.readTree(obj);
        } catch (IOException e) {
            logger.error("error transfer to json , param {}", obj, e);
        }

        return result;
    }
}