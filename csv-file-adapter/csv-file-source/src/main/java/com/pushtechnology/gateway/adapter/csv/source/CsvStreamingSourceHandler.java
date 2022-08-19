package com.pushtechnology.gateway.adapter.csv.source;

import static com.pushtechnology.gateway.framework.DiffusionGatewayFramework.newSourceServicePropertiesBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.gateway.framework.Publisher;
import com.pushtechnology.gateway.framework.StateHandler;
import com.pushtechnology.gateway.framework.StateHandler.Status;
import com.pushtechnology.gateway.framework.StreamingSourceHandler;
import com.pushtechnology.gateway.framework.UpdateMode;
import com.pushtechnology.gateway.framework.exceptions.ApplicationConfigurationException;
import com.pushtechnology.gateway.framework.exceptions.InvalidConfigurationException;
import com.pushtechnology.gateway.framework.exceptions.PayloadConversionException;

/**
 * Implementation of {@link StreamingSourceHandler} which listens to csv file
 * changes and publishes contents to Diffusion server.
 *
 * @author Push Technology Limited
 */
final class CsvStreamingSourceHandler implements StreamingSourceHandler {
    private static final Logger LOG =
        LoggerFactory.getLogger(CsvStreamingSourceHandler.class);
    private static final int CONVERSION_ERROR_THRESHOLD = 5;

    private final StateHandler stateHandler;
    private final Publisher publisher;
    private final String diffusionTopicName;
    private final ExecutorService executorService =
        Executors.newSingleThreadExecutor();
    private final AtomicInteger conversionErrorCount = new AtomicInteger(0);

    private final String fileName;
    private Path path;

    private Future<?> future;
    private WatchService watchService;

    CsvStreamingSourceHandler(
        final String fileName,
        final String diffusionTopicName,
        final StateHandler stateHandler,
        final Publisher publisher) {

        this.diffusionTopicName = diffusionTopicName;
        this.stateHandler = stateHandler;
        this.publisher = publisher;
        this.fileName = fileName;
    }

    @Override
    public CompletableFuture<?> start() {
        updateAndStartWatchingFile();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> stop() {
        pause();

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

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        pause();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        conversionErrorCount.set(0);

        return start();
    }

    private void update() {
        LOG.debug("Streaming update to server from {} file", fileName);
        try {
            publisher
                .publish(diffusionTopicName, path.toFile())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        LOG.error(
                            "Failed to publish to topic {} from " +
                                "file {}",
                            diffusionTopicName,
                            fileName,
                            ex);
                    }
                    else {
                        LOG.debug("Published to server from file: {}",
                            fileName);
                    }
                });
        }
        catch (PayloadConversionException ex) {
            LOG.error("Failed to convert content of {} to JSON", fileName, ex);
            if (conversionErrorCount.getAndIncrement() == CONVERSION_ERROR_THRESHOLD) {
                stateHandler.reportStatus(
                    Status.RED,
                    "Error in sourceFile",
                    "Content in " + fileName + " failed to be converted to " +
                        "JSON " + CONVERSION_ERROR_THRESHOLD + " times");
            }
        }
    }

    private void watchFile() throws IOException {
        LOG.debug("Starting to watch file");
        this.watchService = FileSystems.getDefault().newWatchService();

        try {
            WatchKey register =
                path.getParent().register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            register.reset();
        }
        catch (IOException ex) {
            LOG.error("Failed to register watch service", ex);
            stateHandler.reportStatus(
                Status.RED,
                "File read error",
                "Error occurred when trying to watch the file due to " + ex.getMessage());
        }

        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {

                    final Kind<?> kind = event.kind();

                    LOG.info("Received {} event in {} file", kind, fileName);
                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind) ||
                        StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                        update();
                    }
                    else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                        publisher.remove(diffusionTopicName);
                    }
                }
                key.reset();
            }
        }
        catch (InterruptedException ex) {
            LOG.warn("Interrupted when listening for file change", ex);
        }
    }

    private void pause() {
        try {
            if (future != null) {
                future.cancel(true);
            }
            if (watchService != null) {
                watchService.close();
            }
        }
        catch (IOException ex) {
            LOG.error("Failed to pause", ex);
        }
    }

    private void initializePath() {
        final URL url = getClass().getClassLoader().getResource(fileName);

        if (url == null) {
            throw new ApplicationConfigurationException(fileName + " could not be " +
                "found");
        }

        try {
            this.path = Paths.get(url.toURI());
        }
        catch (URISyntaxException ex) {
            throw new ApplicationConfigurationException("Failed to read file: " + fileName, ex);
        }
    }

    private void updateAndStartWatchingFile() {
        if (path == null) {
            initializePath();
        }

        update();

        future = executorService.submit(() -> {
            try {
                watchFile();
            }
            catch (IOException ex) {
                LOG.error("Failed to start watch service", ex);
            }
        });
    }
}
