package com.diffusiondata.example.adapter.human;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class HumanStreamingSourceHandler implements StreamingSourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HumanStreamingSourceHandler.class);
    private final StateHandler stateHandler;
    private final HumanGui gui;

    public HumanStreamingSourceHandler(
        Publisher publisher,
        StateHandler stateHandler,
        String greeting,
        String topicPath
    ) {
        this.stateHandler = stateHandler;
        this.gui = new HumanGui(
            greeting,
            this.stateHandler::getState,
            ev -> this.stateHandler.reportStatus(ev.getStatus(), ev.getTitle(), ev.getDescription())
        );
        this.gui.addSendEventHandler((ev) -> {
            try {
                publisher.publish(topicPath, ev.getMessage()).get(1, TimeUnit.SECONDS);
            }
            catch (PayloadConversionException | InterruptedException | ExecutionException | TimeoutException ex) {
                LOG.error("Cannot publish '{}' to {}", ev.getMessage(), topicPath, ex);
            }
        });
    }

    @Override
    public CompletableFuture<?> start() {
        LOG.info("start()");
        this.gui.setVisible(true);
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        LOG.info("pause({})", reason);
        this.gui.setSendEndabled(false);
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        LOG.info("resume({})", reason);
        this.gui.setSendEndabled(true);
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<?> stop() {
        LOG.info("stop()");

        this.gui.setVisible(false);
        return completedFuture(null);
    }




}

