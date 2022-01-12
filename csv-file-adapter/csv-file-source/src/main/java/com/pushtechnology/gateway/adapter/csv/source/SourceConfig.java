package com.pushtechnology.gateway.adapter.csv.source;

/**
 * Sample configuration required for source application.
 *
 * @author Push Technology Limited
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
