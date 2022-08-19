package com.pushtechnology.gateway.adapter.csv.source;

import static com.pushtechnology.gateway.framework.DiffusionGatewayFramework.newSourceServicePropertiesBuilder;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.gateway.framework.PollingSourceHandler;
import com.pushtechnology.gateway.framework.Publisher;
import com.pushtechnology.gateway.framework.UpdateMode;
import com.pushtechnology.gateway.framework.exceptions.ApplicationConfigurationException;
import com.pushtechnology.gateway.framework.exceptions.InvalidConfigurationException;
import com.pushtechnology.gateway.framework.exceptions.PayloadConversionException;

/**
 * Polling source handler implementation for CSV source.
 *
 * @author Push Technology Limited
 */
final class CsvPollingSourceHandler implements PollingSourceHandler {
    private static final Logger LOG =
        LoggerFactory.getLogger(CsvPollingSourceHandler.class);
    static final int CONVERSION_ERROR_THRESHOLD = 5;

    private final Publisher publisher;
    private final String diffusionTopicName;
    private final AtomicInteger conversionErrorCount = new AtomicInteger(0);

    private final String fileName;
    private File file;

    CsvPollingSourceHandler(
        final String fileName,
        final String diffusionTopicName,
        final Publisher publisher) {

        this.diffusionTopicName = diffusionTopicName;
        this.publisher = publisher;
        this.fileName = fileName;
    }

    @Override
    public CompletableFuture<?> start() {
        final URL url = getClass().getClassLoader().getResource(fileName);

        if (url == null) {
            throw new ApplicationConfigurationException(fileName + " could not be " +
                "found");
        }
        try {
            this.file = new File(url.toURI());
        }
        catch (URISyntaxException ex) {
            throw new ApplicationConfigurationException("Failed to read file: " + fileName, ex);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> poll() {
        LOG.debug("Polled");
        final CompletableFuture<?> pollCf = new CompletableFuture<>();
        try {
            publisher
                .publish(diffusionTopicName, file)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        LOG.error(
                            "Failed to publish to topic {} from file {}",
                            diffusionTopicName,
                            fileName,
                            ex);
                        pollCf.completeExceptionally(ex);
                    }
                    else {
                        pollCf.complete(null);
                    }
                });
        }
        catch (PayloadConversionException ex) {
            LOG.error("Failed to convert content of {} to JSON", fileName, ex);
            if (conversionErrorCount.getAndIncrement() == CONVERSION_ERROR_THRESHOLD) {
                pollCf.completeExceptionally(ex);
            }
            else {
                pollCf.complete(null);
            }
        }

        return pollCf;
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        conversionErrorCount.set(0);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public SourceServiceProperties getSourceServiceProperties() throws InvalidConfigurationException {
        return
            newSourceServicePropertiesBuilder()
                .updateMode(UpdateMode.STREAMING)
                .payloadConvertorName("$CSV_to_JSON")
                .build();
    }
}
