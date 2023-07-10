package com.diffusiondata.gateway.adapter.csv.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CsvFileSinkHandler}.
 *
 * @author DiffusionData Ltd
 */
class CsvFileSinkHandlerTest {

    private static final String testFilePath = "./testPath";
    private CsvFileSinkHandler csvFileSinkHandler;

    @BeforeEach
    void setUp() {
        csvFileSinkHandler = new CsvFileSinkHandler(testFilePath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(Path.of(testFilePath))
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    @Test
    void testUpdateWithSimpleDiffusionTopic() throws IOException {
        String dataToWrite = "data";
        String diffusionTopic = "testTopic";
        String expectedFinalFileName = testFilePath + "/" + diffusionTopic +
            ".csv";

        assertFalse(new File(expectedFinalFileName).exists());

        csvFileSinkHandler.update(diffusionTopic, dataToWrite);

        assertTrue(new File(expectedFinalFileName).exists());

        String fileContent = Files.readString(Path.of(expectedFinalFileName));
        assertEquals(dataToWrite, fileContent);
    }

    @Test
    void testUpdateWithComplexDiffusionTopic() throws IOException {
        String dataToWrite = "data";
        String diffusionTopic = "folder/oneMore/testTopic";
        String expectedFinalFileName = testFilePath + "/" + diffusionTopic +
            ".csv";

        assertFalse(new File(expectedFinalFileName).exists());

        csvFileSinkHandler.update(diffusionTopic, dataToWrite);

        assertTrue(new File(expectedFinalFileName).exists());

        String fileContent = Files.readString(Path.of(expectedFinalFileName));
        assertEquals(dataToWrite, fileContent);
    }

    @Test
    void testUpdateMultipleTimes() throws IOException {
        String firstDataToWrite = "data";
        String diffusionTopic = "testTopic";
        String expectedFinalFileName = testFilePath + "/" + diffusionTopic +
            ".csv";

        assertFalse(new File(expectedFinalFileName).exists());

        csvFileSinkHandler.update(diffusionTopic, firstDataToWrite);

        assertTrue(new File(expectedFinalFileName).exists());

        String fileContent = Files.readString(Path.of(expectedFinalFileName));
        assertEquals(firstDataToWrite, fileContent);

        String lastDataToWrite = "newData";
        csvFileSinkHandler.update(diffusionTopic, lastDataToWrite);

        assertTrue(new File(expectedFinalFileName).exists());

        String finalFileContent =
            Files.readString(Path.of(expectedFinalFileName));
        assertEquals(lastDataToWrite, finalFileContent);
    }

    @Test
    void testUpdateWithCompleteFilePath() throws IOException {
        csvFileSinkHandler = new CsvFileSinkHandler(testFilePath + "/");

        String dataToWrite = "data";
        String diffusionTopic = "testTopic";
        String expectedFinalFileName = testFilePath + "/" + diffusionTopic +
            ".csv";

        assertFalse(new File(expectedFinalFileName).exists());

        csvFileSinkHandler.update(diffusionTopic, dataToWrite);

        assertTrue(new File(expectedFinalFileName).exists());

        String fileContent = Files.readString(Path.of(expectedFinalFileName));
        assertEquals(dataToWrite, fileContent);
    }
}