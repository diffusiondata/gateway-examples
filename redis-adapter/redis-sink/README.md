# Redis Sink Adapter

## Introduction
This adapter can be used to get updates from Diffusion JSON topics and write the update to a Redis instance. It supports only one type of service.

### REDIS_SINK
This sink service supports getting Diffusion JSON topic updates and publishing them to a Redis instance specified in the configuration. This service will correctly function only for Diffusion topics of JSON topic type. If any other type of topic selector is used in its configuration, or the topic selector matches any non JSON topic type, when an update is received for this topic, Payload convertor exception will be thrown. This service type requires the following configuration to be declared in each defined service in the configuration file:

    "application": {
      "redisUrl": "redisUrl"
    }

Below is an example of an overall configuration of a service of type `REDIS_SINK`:

    {
      "serviceName": "dataSelectorSink",
      "serviceType": "REDIS_SINK",
      "description": "Subscribes to JSON diffusion topic and writes its content to Redis instance",
      "config": {
        "framework": {
          "diffusionTopicSelector": "?data//"
        },
        "application": {
          "redisUrl": "redis://password@localhost:6379/"
        }
      }
    }   

With this configuration, this service will subscribe to all topics that match the selectors passed in `diffusionTopicSelectors` field. For each topic path update, a Redis entry will be created with tthe topic path as the key and the content, in the form a JSON string, as the value.