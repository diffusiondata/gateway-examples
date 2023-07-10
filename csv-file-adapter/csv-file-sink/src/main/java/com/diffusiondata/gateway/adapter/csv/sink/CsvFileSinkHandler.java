package com.diffusiondata.gateway.adapter.csv.sink;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSinkServicePropertiesBuilder;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import com.diffusiondata.gateway.framework.SinkHandler;
import com.diffusiondata.gateway.framework.TopicType;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import net.jcip.annotations.Immutable;

/**
 * CSV file sink handler to write received string update into a CSV file.
 *
 * @author DiffusionData Ltd
 */
@Immutable
final class CsvFileSinkHandler implements SinkHandler<String> {

    private final String filePath;

    CsvFileSinkHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public SinkServiceProperties getSinkServiceProperties() throws InvalidConfigurationException {
        return newSinkServicePropertiesBuilder()
            .payloadConvertorName("$JSON_to_CSV_STRING")
            .build();
    }

    @Override
    public CompletableFuture<?> update(String diffusionTopic, String value) {

        final CompletableFuture<?> updateCf =
            new CompletableFuture<>();

        final String fileName = constructFileName(diffusionTopic);

        try (FileOutputStream outputStream =
                 new FileOutputStream(createPathToWrite(fileName).toFile())) {

            outputStream.write(value.getBytes());
            updateCf.complete(null);
        }
        catch (IOException ex) {
            updateCf.completeExceptionally(ex);
        }

        return updateCf;
    }

    @Override
    public CompletableFuture<?> pause(PauseReason pauseReason) {
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason resumeReason) {
        return completedFuture(null);
    }

    private Path createPathToWrite(String fileName) throws IOException {
        final File fileToCreate = new File(fileName);

        final File parentDirectories = fileToCreate.getParentFile();

        if (!parentDirectories.exists()) {
            boolean mkdirs = parentDirectories.mkdirs();
            if (!mkdirs) {
                throw new IllegalStateException("Failed to create parent " +
                    "directories for file: " + fileName);
            }
        }

        return Files.write(
            Path.of(fileName),
            "".getBytes(),
            StandardOpenOption.CREATE);
    }

    private String constructFileName(String diffusionTopic) {
        final StringBuilder fileNameBuilder = new StringBuilder(filePath);

        if (!filePath.endsWith("/")) {
            fileNameBuilder.append("/");
        }
        fileNameBuilder.append(diffusionTopic);
        fileNameBuilder.append(".csv");

        return fileNameBuilder.toString();
    }
}
