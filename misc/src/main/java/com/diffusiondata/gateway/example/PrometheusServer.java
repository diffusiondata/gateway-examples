package com.diffusiondata.gateway.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;

import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Http server to expose Prometheus metrics.
 *
 * @author DiffusionData Ltd
 */
public final class PrometheusServer implements Runnable {
    private static final String path = "/metrics";
    private static final int port = 8085;
    private final HttpServer httpServer;
    private final PrometheusMeterRegistry meterRegistry;

    PrometheusServer(
        final PrometheusMeterRegistry meterRegistry) throws IOException {

        this.httpServer =
            HttpServer.create(new InetSocketAddress(port), 0);
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void run() {
        httpServer.createContext(path, httpExchange -> {
            final String response = meterRegistry.scrape();
            httpExchange.sendResponseHeaders(
                200,
                response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        });

        httpServer.start();
    }

    /**
     * Closes {@link #httpServer}.
     */
    public void close() {
        httpServer.stop(3);
    }
}
