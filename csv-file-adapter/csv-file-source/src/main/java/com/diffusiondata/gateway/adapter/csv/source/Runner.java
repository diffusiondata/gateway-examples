package com.diffusiondata.gateway.adapter.csv.source;

import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main Runner class.
 *
 * @author DiffusionData Ltd
 */
public class Runner {
    public static void main(String[] args) {
        final CsvFileSourceApplication csvFileSourceApplication =
            new CsvFileSourceApplication(new SourceConfigValidator(new ObjectMapper()));

        DiffusionGatewayFramework.start(csvFileSourceApplication);
    }
}
