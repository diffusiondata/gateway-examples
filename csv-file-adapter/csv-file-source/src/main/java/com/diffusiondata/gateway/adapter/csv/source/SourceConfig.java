package com.diffusiondata.gateway.adapter.csv.source;

/**
 * Sample configuration required for source application.
 *
 * @author DiffusionData Ltd
 */
public final class SourceConfig {
    private String fileName;

    private String diffusionTopicName;

    public String getFileName() {
        return fileName;
    }

    public String getDiffusionTopicName() {
        return diffusionTopicName;
    }
}
