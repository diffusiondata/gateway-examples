package com.diffusiondata.gateway.adapter.csv.source;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.initialize;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main Runner class.
 *
 * @author Push Technology Limited
 */
public class Runner {
    public static void main(String[] args) {
        final CsvFileSourceApplication csvFileSourceApplication =
            new CsvFileSourceApplication(new SourceConfigValidator(new ObjectMapper()));

        initialize(csvFileSourceApplication)
            .connect();
    }
}
