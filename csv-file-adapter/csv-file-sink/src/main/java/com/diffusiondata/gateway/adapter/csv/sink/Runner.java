package com.diffusiondata.gateway.adapter.csv.sink;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.start;

/**
 * Main Runner class.
 *
 * @author DiffusionData Ltd
 */
public class Runner {
    public static void main(String[] args) {
        final CsvFileSinkApplication csvFileSinkApplication =
            new CsvFileSinkApplication();

        start(csvFileSinkApplication);
    }
}
