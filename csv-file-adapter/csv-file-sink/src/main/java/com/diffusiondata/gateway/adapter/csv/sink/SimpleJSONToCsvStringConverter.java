package com.diffusiondata.gateway.adapter.csv.sink;

import static com.diffusiondata.gateway.converters.CBORContext.CBOR_FACTORY;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import com.diffusiondata.gateway.framework.converters.PayloadConverter;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * A Payload converter to convert JSON data to CSV string.
 * <p>
 * This converter can only be used for JSON payload with simple JSON objects or
 * JSON array containing simple JSON objects. It uses fields of JSON object to
 * extract headers for the CSV data. If the data to convert is of type array,
 * all the items of array will be looped to extract the headers. Hence, this
 * converter is suggested to be used only for simple and small JSON payload.
 *
 * @author DiffusionData Ltd
 */
public final class SimpleJSONToCsvStringConverter
    implements PayloadConverter<JSON, String> {

    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper(CBOR_FACTORY);

    public static String getName() {
        return "JSON_to_CSV_STRING";
    }

    @Override
    public String convert(JSON jsonData)
        throws PayloadConversionException {

        final JsonNode jsonNode;
        try {
            jsonNode = OBJECT_MAPPER.readValue(jsonData.asInputStream(),
                JsonNode.class);
        }
        catch (IOException ex) {
            throw new PayloadConversionException(
                "Failed to process passed JSON data", ex);
        }

        final Builder csvSchemaBuilder = CsvSchema.builder();

        final Set<String> headers = createHeaders(jsonNode);

        headers.forEach(csvSchemaBuilder::addColumn);

        final CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            new CsvMapper()
                .writerFor(JsonNode.class)
                .with(csvSchema)
                .writeValue(outputStream, jsonNode);
            return outputStream.toString();
        }
        catch (IOException ex) {
            throw new PayloadConversionException(
                "Failed to map content of passed JSON data to CSV string",
                ex);
        }
    }

    private Set<String> createHeaders(final JsonNode jsonNode)
        throws PayloadConversionException {
        final Set<String> headers = new LinkedHashSet<>();

        if (jsonNode.isArray()) {
            for (JsonNode itemNode : jsonNode) {
                headers.addAll(extractObjectFields(itemNode));
            }
        }
        else {
            headers.addAll(extractObjectFields(jsonNode));
        }

        return headers;
    }

    private Set<String> extractObjectFields(JsonNode jsonNode)
        throws PayloadConversionException {
        final Set<String> headers = new LinkedHashSet<>();

        if (jsonNode.isObject()) {
            jsonNode.fieldNames().forEachRemaining(headers::add);
        }
        else {
            throw new PayloadConversionException(
                "This converter can convert only simple JSON objects and JSON" +
                    " array containing simple JSON objects");
        }

        return headers;
    }
}
