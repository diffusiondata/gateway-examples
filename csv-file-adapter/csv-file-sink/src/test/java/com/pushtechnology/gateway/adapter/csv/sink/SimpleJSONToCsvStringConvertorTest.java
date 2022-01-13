package com.pushtechnology.gateway.adapter.csv.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.gateway.framework.exceptions.PayloadConversionException;

/**
 * Tests for {@link SimpleJSONToCsvStringConvertor}.
 *
 * @author ndhougoda-hamal
 */
class SimpleJSONToCsvStringConvertorTest {

    private static final String SIMPLE_JSON_OBJECT_TEST_DATA = "{\n" +
        "  \"name\": \"Sam\",\n" +
        "  \"profession\": \"Engineer\",\n" +
        "  \"age\": 100\n" +
        "}";

    private static final String SIMPLE_JSON_ARRAY_TEST_DATA = "[\n" +
        "    {\n" +
        "      \"user\": \"Sam\",\n" +
        "      \"membership\": \"Platinum\",\n" +
        "      \"age\": 100\n" +
        "    },\n" +
        "    {\n" +
        "      \"user\": \"Mark\",\n" +
        "      \"membership\": \"Classic\",\n" +
        "      \"salary\": 190\n" +
        "    }\n" +
        "  ]";

    private static final String COMPLEX_JSON_OBJECT_TEST_DATA = "{\n" +
        "    \"users\": [\n" +
        "      {\n" +
        "        \"name\": \"Sam\",\n" +
        "        \"age\": \"100\"\n" +
        "      }\n" +
        "    ]\n" +
        "  }";

    private static final String COMPLEX_JSON_ARRAY_TEST_DATA = "[\n" +
        "    [\n" +
        "      {\n" +
        "        \"name\": \"Sam\"\n" +
        "      }\n" +
        "    ]\n" +
        "  ]";

    private final SimpleJSONToCsvStringConvertor simpleJsonToCsvStringConvertor =
        new SimpleJSONToCsvStringConvertor();

    @Test
    void testFromDiffusionTypeForObjectJsonData() throws PayloadConversionException {
        String convertedValue =
            simpleJsonToCsvStringConvertor.fromDiffusionType(
                Diffusion.dataTypes().json().fromJsonString(SIMPLE_JSON_OBJECT_TEST_DATA));

        assertEquals("name,profession,age\n" +
            "Sam,Engineer,100\n", convertedValue);
    }

    @Test
    void testFromDiffusionTypeForArrayJsonData() throws PayloadConversionException {
        String convertedValue =
            simpleJsonToCsvStringConvertor.fromDiffusionType(
                Diffusion.dataTypes().json().fromJsonString(SIMPLE_JSON_ARRAY_TEST_DATA));

        assertEquals("user,membership,age,salary\n" +
            "Sam,Platinum,100,\n" +
            "Mark,Classic,,190\n", convertedValue);
    }

    @Test
    void testFromDiffusionTypeForComplexObjectJsonData() {
        PayloadConversionException exception =
            assertThrows(PayloadConversionException.class,
                () -> simpleJsonToCsvStringConvertor.fromDiffusionType(
                    Diffusion.dataTypes().json().fromJsonString(COMPLEX_JSON_OBJECT_TEST_DATA)));

        assertEquals(
            "Failed to map content of passed JSON data to CSV string",
            exception.getMessage());
    }

    @Test
    void testFromDiffusionTypeForComplexArrayJsonData() {
        PayloadConversionException exception =
            assertThrows(PayloadConversionException.class,
                () -> simpleJsonToCsvStringConvertor.fromDiffusionType(
                    Diffusion.dataTypes().json().fromJsonString(COMPLEX_JSON_ARRAY_TEST_DATA)));

        assertEquals(
            "This convertor can convert only simple JSON objects and JSON " +
                "array containing simple JSON objects",
            exception.getMessage());
    }

    void testGetNameAndToString() {
        assertEquals("$JSON_to_CSV_STRING", simpleJsonToCsvStringConvertor.getName());
        assertEquals("$JSON_to_CSV_STRING", simpleJsonToCsvStringConvertor.toString());
    }
}