package com.pushtechnology.gateway.adapter.csv.sink;

import static com.pushtechnology.gateway.framework.DiffusionGatewayFramework.initialize;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main Runner class.
 *
 * @author Push Technology Limited
 */
public class Runner {
    public static void main(String[] args) {
        final CsvFileSinkApplication csvFileSinkApplication =
            new CsvFileSinkApplication();

        initialize(csvFileSinkApplication)
            .connect();
    }
}
