package diffusion;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.SinkHandler;
import com.diffusiondata.gateway.framework.TopicProperties;

public class LocalStreamer implements SinkHandler<Object> {
    private static final Logger LOG =
        LoggerFactory.getLogger(LocalStreamer.class);

    @Override
    public Class<Object> valueType() {
        return Object.class;
    }

    @Override
    public CompletableFuture<?> update(
        String path,
        Object value,
        TopicProperties topicProperties) {

        LOG.info(
            "Received {} from {} with {} properties",
            value,
            path,
            topicProperties);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        return CompletableFuture.completedFuture(null);
    }
}
