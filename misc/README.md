# Example Gateway Application

## Introduction

This example Gateway application is created to demonstrate creating Hybrid
service type (handlers) and Streaming source service types which handles missing
topic notifications. It is also implemented to expose JMX and Prometheus metrics.

This application supports two service types:

1. DATE_APPENDER
2. MISSING_TOPIC_HANDLER

### DATE_APPENDER

This hybrid service supports getting Diffusion JSON topic updates and appends
timestamp to the JSON data, if the data is of type JSON object. For any other
types of JSON data, they will be ignored. The updated data will then be
published to another Diffusion JSON topic. This target Diffusion topic path will
be created by appending value of `targetTopicPrefix` from service configuration
to the actual path, from where update is received. Setting `targetTopicPrefix` in configuration is optional. If this configuration paramter is not set then its default value is used which is `enhanced/`.

The schema of configuration for this service type is:

```json
 {
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "targetTopicPrefix": {
      "type": "string",
      "default": "enhanced/",
      "description": "The target Diffusion topic prefix to publish updates to. This prefix is appended to the topic path from which the update is received, creating a new topic path. The updated data is then published to the newly created topic path"
    }
  }
}
```

Below is an example of an overall configuration of a service of
type `DATE_APPENDER`:

```json
    {
  "serviceName": "dateAppender1",
  "serviceType": "DATE_APPENDER",
  "config": {
    "framework": {
      "sink": {
        "diffusionTopicSelector": "fx/EUR"
      }
    },
    "application": {
      "targetTopicPrefix": "updated/"
    }
  }
}
```

With this configuration, the service will subscribe to the `fx/EUR` topic path.
Upon receiving an update for this topic, if the update is in the form of a JSON
object, the service will add a timestamp to the update and publish it to a new
JSON topic named `updated/fx/EUR`

### MISSING_TOPIC_HANDLER

This streaming source service registers a Missing topic notification handler for
the `missingTopicSelector` that is passed in the configuration. If there is any
subscription to the configured missing topic branch, the service would create
the missing topic of type JSON and publish a dummy JSON data.

The schema of configuration for this service type is:

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "missingTopicSelector": {
      "type": "string",
      "description": "Missing topic selector to get missing topic notifications"
    }
  },
  "required": [
    "missingTopicSelector"
  ]
}
```

Below is an example of an overall configuration of a service of
type `MISSING_TOPIC_HANDLER`:

```json
    {
  "serviceName": "missingTopicHandler1",
  "serviceType": "MISSING_TOPIC_HANDLER",
  "config": {
    "application": {
      "missingTopicSelector": "fx/EUR"
    }
  }
}
```

With this configuration, the service will register for missing topic
notifications for the `fx/EUR` topic path. If any other session subscribes to
this path and the topic does not exist in server, this service will create a
JSON-type topic and publish dummy data to it.

NOTE:
> For the demonstration purposes, supplied configuration file: `src/main/resources/configuration.json` is created such that, a service of type `MISSING_TOPIC_HANDLER` will be created which will register for missing topic notification for topic path `fx/EUR`. Services of type `DATE_APPENDER` are created such that they subscribe to topic `fx/EUR`. Hence, when these services are added into the application, services `dateAppender1` and `dateAppender2` will subscribe to `fx/EUR` topic and service `missingTopicHandler` will create and update `fx/EUR` topic. As soon as the topic is created, `dateAppender1` and `dateAppender2` will send updates with date appended in them to topics `updated/fx/EUR` and `enhanced/fx/EUR` respectively. 

## Metrics
Out of the box metrics provided by Framework are exposed with JMX and Prometheus. 

Exposed Prometheus metrics can be accessed using `http://localhost:8085/metrics`.

## Running the application in IDE
To run the application via IDE, run `misc/src/main/java/com/diffusiondata/gateway/example/Runner.java` file. Configuration file can be set as VM arguments in Run configuration as follows:

> -Dgateway.config.file=misc/src/main/resources/configuration.json -Dgateway.config.use-local-services=true