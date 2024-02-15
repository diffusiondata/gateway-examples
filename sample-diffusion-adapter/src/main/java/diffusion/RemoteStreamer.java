package diffusion;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.features.Topics.UnsubscribeReason;
import com.pushtechnology.diffusion.client.features.Topics.ValueStream;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;

public class RemoteStreamer implements StreamingSourceHandler {
    private final Logger LOG =
        LoggerFactory.getLogger(RemoteStreamer.class);

    private final String url;
    private final String principal;
    private final String password;
    private final String topicSelector;
    private final String prefix;
    private final Publisher publisher;

    private Session session;

    public RemoteStreamer(String url, String principal, String password,
        String topicSelector, String prefix, Publisher publisher) {
        this.url = url;
        this.principal = principal;
        this.password = password;
        this.topicSelector = topicSelector;
        this.prefix = prefix;
        this.publisher = publisher;
    }

    @Override
    public CompletableFuture<?> start() {

        session =
            Diffusion.sessions().principal(principal).password(password).open(url);

        Topics topics = session.feature(Topics.class);

        ValueStream<Object> valueStream = new ValueStreamImpl();
        topics.addStream(topicSelector, Object.class, valueStream);
        topics.subscribe(topicSelector);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        session.close();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        return start();
    }

    class ValueStreamImpl implements ValueStream<Object> {

        @Override
        public void onValue(String topicPath,
            TopicSpecification specification, Object oldValue,
            Object newValue) {
            try {

                publisher.publish(prefix + topicPath, newValue,
                        publisher.getConfiguredTopicProperties())
                    .whenComplete((res, ex) -> {
                        if (ex == null) {
                            LOG.info("Published to {}", topicPath);
                        }
                        else {
                            LOG.error("Failed to publish from {}", topicPath);
                        }
                    });
            }
            catch (PayloadConversionException ex) {
                LOG.error("Failed to convert", ex);
            }

        }

        @Override
        public void onSubscription(String topicPath,
            TopicSpecification specification) {

        }

        @Override
        public void onUnsubscription(String topicPath,
            TopicSpecification specification, UnsubscribeReason reason) {

        }

        @Override
        public void onClose() {

        }

        @Override
        public void onError(ErrorReason errorReason) {

        }
    }
}
